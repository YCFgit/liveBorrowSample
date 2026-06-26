package com.ycf.liveborrowsample.application.service;

import com.ycf.liveborrowsample.application.port.SampleDataStore;
import com.ycf.liveborrowsample.domain.enums.BatchStatus;
import com.ycf.liveborrowsample.domain.enums.DeliveryType;
import com.ycf.liveborrowsample.domain.enums.ErrorCode;
import com.ycf.liveborrowsample.domain.enums.ReturnMethod;
import com.ycf.liveborrowsample.domain.enums.ReturnStatus;
import com.ycf.liveborrowsample.domain.enums.SampleFilterType;
import com.ycf.liveborrowsample.domain.enums.TaskAction;
import com.ycf.liveborrowsample.domain.enums.TaskStatus;
import com.ycf.liveborrowsample.domain.exception.BusinessException;
import com.ycf.liveborrowsample.domain.model.ReturnAllocation;
import com.ycf.liveborrowsample.domain.model.ReturnBatch;
import com.ycf.liveborrowsample.domain.model.ReturnBatchItem;
import com.ycf.liveborrowsample.domain.model.SampleTask;
import com.ycf.liveborrowsample.domain.model.SampleTaskItem;
import com.ycf.liveborrowsample.domain.service.FifoReturnAllocationService;
import com.ycf.liveborrowsample.domain.service.TaskStateMachine;
import com.ycf.liveborrowsample.interfaces.http.request.ReturnBatchCreateRequest;
import com.ycf.liveborrowsample.interfaces.http.request.ReturnBatchItemRequest;
import com.ycf.liveborrowsample.interfaces.http.request.ReturnLogisticsRequest;
import com.ycf.liveborrowsample.interfaces.http.response.ReturnAggregationResponse;
import com.ycf.liveborrowsample.interfaces.http.response.ReturnAggregationRowResponse;
import com.ycf.liveborrowsample.interfaces.http.response.ReturnAllocationResponse;
import com.ycf.liveborrowsample.interfaces.http.response.ReturnBatchCreateResponse;
import com.ycf.liveborrowsample.interfaces.http.response.ReturnBatchDetailResponse;
import com.ycf.liveborrowsample.interfaces.http.response.ReturnBatchItemResponse;
import com.ycf.liveborrowsample.interfaces.http.response.ReturnTaskSummaryResponse;
import com.ycf.liveborrowsample.interfaces.http.response.TaskRefResponse;
import com.ycf.liveborrowsample.interfaces.http.response.VirtualStoreResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReturnApplicationService {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SampleDataStore dataStore;
    private final FifoReturnAllocationService fifoReturnAllocationService;
    private final TaskStateMachine taskStateMachine;

    public ReturnApplicationService(
        SampleDataStore dataStore,
        FifoReturnAllocationService fifoReturnAllocationService,
        TaskStateMachine taskStateMachine
    ) {
        this.dataStore = dataStore;
        this.fifoReturnAllocationService = fifoReturnAllocationService;
        this.taskStateMachine = taskStateMachine;
    }

    public List<VirtualStoreResponse> queryVirtualStores() {
        return dataStore.getVirtualStores().entrySet().stream()
            .map(entry -> new VirtualStoreResponse(entry.getKey(), entry.getValue()))
            .toList();
    }

    public ReturnAggregationResponse queryAggregations(String virtualStoreCode, SampleFilterType sampleFilterType) {
        Map<String, AggregationBucket> bucketMap = new LinkedHashMap<>();
        for (SampleTask task : eligibleTasks(virtualStoreCode, sampleFilterType)) {
            for (SampleTaskItem item : task.getItems()) {
                int remaining = item.getRemainingReturnQty();
                if (remaining <= 0) {
                    continue;
                }
                String bucketKey = item.getSkuCode() + "|" + item.getSizeCode() + "|" + task.getDeliveryType().name();
                AggregationBucket bucket = bucketMap.computeIfAbsent(bucketKey, key ->
                    new AggregationBucket(
                        item.getSkuCode(),
                        item.getSizeCode(),
                        item.getProductName(),
                        task.getDeliveryType().name(),
                        task.getDeliveryType() == DeliveryType.PICKUP ? task.getSourceStoreName() : null
                    )
                );
                bucket.availableReturnQty += remaining;
                bucket.taskRefs.add(new TaskRefResponse(task.getTaskNo(), item.getId(), format(task.getBorrowedAt()), remaining));
            }
        }
        List<ReturnAggregationRowResponse> rows = bucketMap.values().stream()
            .map(bucket -> new ReturnAggregationRowResponse(
                bucket.skuCode,
                bucket.sizeCode,
                bucket.productName,
                bucket.sampleType,
                bucket.availableReturnQty,
                bucket.sourceStoreName,
                bucket.taskRefs.stream()
                    .sorted(Comparator.comparing(TaskRefResponse::borrowedAt).thenComparing(TaskRefResponse::taskNo))
                    .toList()
            ))
            .toList();
        return new ReturnAggregationResponse(virtualStoreCode, sampleFilterType.name(), rows);
    }

    @Transactional
    public synchronized ReturnBatchCreateResponse createBatch(ReturnBatchCreateRequest request) {
        String batchNo = dataStore.nextReturnBatchNo();
        List<ReturnBatchItem> batchItems = new ArrayList<>();
        List<ReturnAllocation> allocations = new ArrayList<>();
        Map<String, Integer> taskQtyMap = new LinkedHashMap<>();

        int seq = 1;
        for (ReturnBatchItemRequest itemRequest : request.items()) {
            List<FifoReturnAllocationService.ReturnCandidate> candidates = buildCandidates(request.virtualStoreCode(), request.sampleFilterType(), itemRequest);
            List<FifoReturnAllocationService.FifoAllocationResult> results =
                fifoReturnAllocationService.allocate(itemRequest.applyReturnQty(), candidates);

            FifoReturnAllocationService.FifoAllocationResult firstResult = results.get(0);

            batchItems.add(new ReturnBatchItem(
                itemRequest.skuCode(),
                itemRequest.sizeCode(),
                firstResult.candidate().productName(),
                firstResult.candidate().sampleType(),
                candidates.stream().mapToInt(FifoReturnAllocationService.ReturnCandidate::remainingQty).sum(),
                itemRequest.applyReturnQty(),
                firstResult.candidate().sourceStoreName()
            ));

            for (FifoReturnAllocationService.FifoAllocationResult result : results) {
                allocations.add(new ReturnAllocation(
                    result.candidate().taskNo(),
                    result.candidate().taskItemId(),
                    result.candidate().skuCode(),
                    result.candidate().sizeCode(),
                    result.allocatedQty(),
                    request.returnMethod(),
                    seq++
                ));
                taskQtyMap.merge(result.candidate().taskNo(), result.allocatedQty(), Integer::sum);

                SampleTask task = mustFindTask(result.candidate().taskNo());
                if (!taskStateMachine.canExecute(task.getTaskStatus(), TaskAction.CREATE_RETURN_BATCH)) {
                    throw new BusinessException(ErrorCode.TASK_ACTION_ILLEGAL, "任务不可发起归还：" + task.getTaskNo());
                }
                SampleTaskItem taskItem = task.getItems().stream()
                    .filter(item -> item.getId().equals(result.candidate().taskItemId()))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException(ErrorCode.PARAM_INVALID, "任务明细不存在"));
                taskItem.applyReturn(result.allocatedQty());
                ReturnStatus newReturnStatus = request.returnMethod() == ReturnMethod.EXPRESS ? ReturnStatus.PENDING : ReturnStatus.STORE_PENDING;
                task.attachReturnBatch(batchNo, newReturnStatus);
                dataStore.saveTask(task);
            }
        }

        BatchStatus batchStatus = request.returnMethod() == ReturnMethod.EXPRESS ? BatchStatus.PENDING : BatchStatus.STORE_PENDING;
        ReturnBatch batch = new ReturnBatch(
            batchNo,
            request.virtualStoreCode(),
            dataStore.getVirtualStoreName(request.virtualStoreCode()),
            request.sourceType(),
            request.sampleFilterType(),
            request.returnMethod(),
            batchStatus,
            batchItems,
            allocations
        );
        dataStore.saveBatch(batch);

        List<ReturnTaskSummaryResponse> taskSummaries = taskQtyMap.entrySet().stream()
            .map(entry -> new ReturnTaskSummaryResponse(entry.getKey(), entry.getValue()))
            .toList();
        return new ReturnBatchCreateResponse(batchNo, batchStatus.name(), request.returnMethod().name(), taskSummaries);
    }

    public ReturnBatchDetailResponse getBatchDetail(String returnBatchNo) {
        ReturnBatch batch = dataStore.findBatch(returnBatchNo)
            .orElseThrow(() -> new BusinessException(ErrorCode.PARAM_INVALID, "归还批次不存在"));
        return new ReturnBatchDetailResponse(
            batch.getReturnBatchNo(),
            batch.getVirtualStoreCode(),
            batch.getVirtualStoreName(),
            batch.getSourceType().name(),
            batch.getSampleFilterType().name(),
            batch.getReturnMethod().name(),
            batch.getStatus().name(),
            batch.getLogisticsCompanyName(),
            batch.getLogisticsNo(),
            batch.getItems().stream()
                .map(item -> new ReturnBatchItemResponse(
                    item.getSkuCode(),
                    item.getSizeCode(),
                    item.getProductName(),
                    item.getSampleType(),
                    item.getAvailableReturnQty(),
                    item.getApplyReturnQty(),
                    item.getSourceStoreName()
                ))
                .toList(),
            batch.getAllocations().stream()
                .map(item -> new ReturnAllocationResponse(
                    item.getTaskNo(),
                    item.getTaskItemId(),
                    item.getSkuCode(),
                    item.getSizeCode(),
                    item.getAllocatedQty(),
                    item.getReturnMethod().name(),
                    item.getAllocationSeq()
                ))
                .toList()
        );
    }

    @Transactional
    public ReturnBatchDetailResponse fillLogistics(String returnBatchNo, ReturnLogisticsRequest request) {
        ReturnBatch batch = dataStore.findBatch(returnBatchNo)
            .orElseThrow(() -> new BusinessException(ErrorCode.PARAM_INVALID, "归还批次不存在"));
        if (batch.getReturnMethod() != ReturnMethod.EXPRESS || batch.getStatus() != BatchStatus.PENDING) {
            throw new BusinessException(ErrorCode.RETURN_BATCH_STATUS_ILLEGAL);
        }
        batch.fillLogistics(request.companyName(), request.logisticsNo());
        batch.getAllocations().forEach(allocation -> {
            SampleTask task = mustFindTask(allocation.getTaskNo());
            task.fillReturnLogistics(request.logisticsNo());
            dataStore.saveTask(task);
        });
        dataStore.saveBatch(batch);
        return getBatchDetail(returnBatchNo);
    }

    @Transactional
    public ReturnBatchDetailResponse completeBatch(String returnBatchNo) {
        ReturnBatch batch = dataStore.findBatch(returnBatchNo)
            .orElseThrow(() -> new BusinessException(ErrorCode.PARAM_INVALID, "归还批次不存在"));
        if (batch.getStatus() != BatchStatus.LOGISTICS_FILLED && batch.getStatus() != BatchStatus.STORE_PENDING) {
            throw new BusinessException(ErrorCode.RETURN_BATCH_STATUS_ILLEGAL);
        }
        batch.complete();
        batch.getAllocations().forEach(allocation -> {
            SampleTask task = mustFindTask(allocation.getTaskNo());
            task.completeReturn();
            dataStore.saveTask(task);
        });
        dataStore.saveBatch(batch);
        return getBatchDetail(returnBatchNo);
    }

    private List<SampleTask> eligibleTasks(String virtualStoreCode, SampleFilterType sampleFilterType) {
        return dataStore.listTasksByVirtualStore(virtualStoreCode).stream()
            .filter(task -> task.getTaskStatus() == TaskStatus.BORROWING || task.getTaskStatus() == TaskStatus.RETURNING)
            .filter(task -> sampleFilterType != SampleFilterType.PICKUP_ONLY || task.getDeliveryType() == DeliveryType.PICKUP)
            .toList();
    }

    private List<FifoReturnAllocationService.ReturnCandidate> buildCandidates(
        String virtualStoreCode,
        SampleFilterType sampleFilterType,
        ReturnBatchItemRequest itemRequest
    ) {
        List<FifoReturnAllocationService.ReturnCandidate> candidates = new ArrayList<>();
        for (SampleTask task : eligibleTasks(virtualStoreCode, sampleFilterType)) {
            for (SampleTaskItem item : task.getItems()) {
                if (!item.getSkuCode().equals(itemRequest.skuCode()) || !item.getSizeCode().equals(itemRequest.sizeCode())) {
                    continue;
                }
                int remaining = item.getRemainingReturnQty();
                if (remaining <= 0) {
                    continue;
                }
                candidates.add(new FifoReturnAllocationService.ReturnCandidate(
                    task.getTaskNo(),
                    item.getId(),
                    item.getSkuCode(),
                    item.getSizeCode(),
                    item.getProductName(),
                    task.getDeliveryType().name(),
                    task.getDeliveryType() == DeliveryType.PICKUP ? task.getSourceStoreName() : null,
                    remaining,
                    task.getBorrowedAt()
                ));
            }
        }
        if (candidates.isEmpty()) {
            throw new BusinessException(ErrorCode.RETURN_NOT_ENOUGH);
        }
        return candidates;
    }

    private SampleTask mustFindTask(String taskNo) {
        return dataStore.findTask(taskNo)
            .orElseThrow(() -> new BusinessException(ErrorCode.PARAM_INVALID, "任务不存在"));
    }

    private String format(LocalDateTime value) {
        return value == null ? null : value.format(DATE_TIME);
    }

    private static class AggregationBucket {
        private final String skuCode;
        private final String sizeCode;
        private final String productName;
        private final String sampleType;
        private final String sourceStoreName;
        private int availableReturnQty;
        private final List<TaskRefResponse> taskRefs = new ArrayList<>();

        private AggregationBucket(String skuCode, String sizeCode, String productName, String sampleType, String sourceStoreName) {
            this.skuCode = skuCode;
            this.sizeCode = sizeCode;
            this.productName = productName;
            this.sampleType = sampleType;
            this.sourceStoreName = sourceStoreName;
        }
    }
}
