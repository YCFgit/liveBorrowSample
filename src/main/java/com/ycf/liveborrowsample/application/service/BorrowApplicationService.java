package com.ycf.liveborrowsample.application.service;

import com.ycf.liveborrowsample.application.port.SampleDataStore;
import com.ycf.liveborrowsample.domain.enums.DeliveryStatus;
import com.ycf.liveborrowsample.domain.enums.DeliveryType;
import com.ycf.liveborrowsample.domain.enums.ErrorCode;
import com.ycf.liveborrowsample.domain.enums.ExceptionStatus;
import com.ycf.liveborrowsample.domain.enums.PickupStatus;
import com.ycf.liveborrowsample.domain.enums.ReturnStatus;
import com.ycf.liveborrowsample.domain.enums.TaskAction;
import com.ycf.liveborrowsample.domain.enums.TaskStatus;
import com.ycf.liveborrowsample.domain.enums.AuditStatus;
import com.ycf.liveborrowsample.domain.exception.BusinessException;
import com.ycf.liveborrowsample.domain.model.BorrowApplication;
import com.ycf.liveborrowsample.domain.model.BorrowApplicationItem;
import com.ycf.liveborrowsample.domain.model.SampleTask;
import com.ycf.liveborrowsample.domain.model.SampleTaskItem;
import com.ycf.liveborrowsample.domain.service.TaskStateMachine;
import com.ycf.liveborrowsample.interfaces.http.request.BorrowApplicationCreateRequest;
import com.ycf.liveborrowsample.interfaces.http.request.BorrowItemRequest;
import com.ycf.liveborrowsample.interfaces.http.request.PickupConfirmRequest;
import com.ycf.liveborrowsample.interfaces.http.request.ReceiveConfirmRequest;
import com.ycf.liveborrowsample.interfaces.http.response.BorrowApplicationCreateResponse;
import com.ycf.liveborrowsample.interfaces.http.response.BorrowTaskDetailResponse;
import com.ycf.liveborrowsample.interfaces.http.response.BorrowTaskItemResponse;
import com.ycf.liveborrowsample.interfaces.http.response.BorrowTaskListItemResponse;
import com.ycf.liveborrowsample.interfaces.http.response.OperationResultResponse;
import com.ycf.liveborrowsample.interfaces.http.response.PageResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BorrowApplicationService {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SampleDataStore dataStore;
    private final TaskStateMachine taskStateMachine;

    public BorrowApplicationService(SampleDataStore dataStore, TaskStateMachine taskStateMachine) {
        this.dataStore = dataStore;
        this.taskStateMachine = taskStateMachine;
    }

    @Transactional
    public BorrowApplicationCreateResponse create(BorrowApplicationCreateRequest request) {
        validateCreateRequest(request);

        String applyNo = dataStore.nextApplyNo();
        String borrowNo = dataStore.nextBorrowNo();
        String taskNo = dataStore.nextTaskNo();

        List<SampleTaskItem> items = new ArrayList<>();
        List<BorrowApplicationItem> applicationItems = new ArrayList<>();
        int lineNo = 1;
        for (BorrowItemRequest item : request.items()) {
            items.add(new SampleTaskItem(
                dataStore.nextTaskItemId(),
                item.skuCode(),
                item.sizeCode(),
                item.skuCode(),
                "A",
                item.applyQty(),
                request.deliveryType() == DeliveryType.EXPRESS ? item.applyQty() : 0,
                request.deliveryType() == DeliveryType.EXPRESS ? item.applyQty() : 0,
                0,
                0
            ));
            applicationItems.add(new BorrowApplicationItem(
                lineNo++,
                item.skuCode(),
                item.sizeCode(),
                item.skuCode(),
                item.applyQty(),
                item.applyQty()
            ));
        }

        BorrowApplication application = new BorrowApplication(
            applyNo,
            "E-DEMO",
            "演示用户",
            request.virtualStoreCode(),
            dataStore.getVirtualStoreName(request.virtualStoreCode()),
            request.deliveryType(),
            request.pickupStoreCode(),
            request.pickupStoreCode(),
            request.receiver() == null ? null : request.receiver().name(),
            request.receiver() == null ? null : request.receiver().mobile(),
            request.receiver() == null ? null : request.receiver().province(),
            request.receiver() == null ? null : request.receiver().city(),
            request.receiver() == null ? null : request.receiver().district(),
            request.receiver() == null ? null : request.receiver().address(),
            AuditStatus.APPROVED,
            "DINGTALK",
            request.remark(),
            applicationItems
        );
        dataStore.saveApplication(application);

        TaskStatus taskStatus = request.deliveryType() == DeliveryType.PICKUP ? TaskStatus.WAIT_PICKUP : TaskStatus.WAIT_SHIP;
        DeliveryStatus deliveryStatus = request.deliveryType() == DeliveryType.PICKUP ? DeliveryStatus.NONE : DeliveryStatus.WAITING;
        PickupStatus pickupStatus = request.deliveryType() == DeliveryType.PICKUP ? PickupStatus.WAIT_PICKUP : PickupStatus.NOT_APPLICABLE;

        SampleTask task = new SampleTask(
            dataStore.nextTaskId(),
            taskNo,
            borrowNo,
            applyNo,
            "E-DEMO",
            "演示用户",
            request.virtualStoreCode(),
            dataStore.getVirtualStoreName(request.virtualStoreCode()),
            request.deliveryType() == DeliveryType.PICKUP ? request.pickupStoreCode() : "直播仓",
            request.deliveryType(),
            taskStatus,
            deliveryStatus,
            pickupStatus,
            ReturnStatus.NONE,
            ExceptionStatus.NONE,
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(7),
            items
        );
        dataStore.saveTask(task);
        return new BorrowApplicationCreateResponse(applyNo, "APPROVED", borrowNo, List.of(taskNo));
    }

    private void validateCreateRequest(BorrowApplicationCreateRequest request) {
        if (request.deliveryType() == DeliveryType.EXPRESS) {
            if (request.receiver() == null
                || isBlank(request.receiver().name())
                || isBlank(request.receiver().mobile())
                || isBlank(request.receiver().province())
                || isBlank(request.receiver().city())
                || isBlank(request.receiver().district())
                || isBlank(request.receiver().address())) {
                throw new BusinessException(ErrorCode.PARAM_INVALID, "快递借样必须填写完整收货信息");
            }
            return;
        }
        if (isBlank(request.pickupStoreCode())) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "自提借样必须填写自提门店");
        }
    }

    public PageResponse<BorrowTaskListItemResponse> queryTasks(int pageNo, int pageSize, String status) {
        List<BorrowTaskListItemResponse> all = dataStore.listTasks().stream()
            .filter(task -> status == null || task.getTaskStatus().name().equalsIgnoreCase(status))
            .map(this::toListItem)
            .toList();
        return paginate(all, pageNo, pageSize);
    }

    public BorrowTaskDetailResponse getTaskDetail(String taskNo) {
        SampleTask task = findTask(taskNo);
        BorrowApplication application = dataStore.findApplicationByApplyNo(task.getApplyNo()).orElse(null);
        return toDetail(task, application);
    }

    @Transactional
    public OperationResultResponse confirmReceive(String taskNo, ReceiveConfirmRequest request) {
        SampleTask task = findTask(taskNo);
        if (!taskStateMachine.canExecute(task.getTaskStatus(), TaskAction.CONFIRM_RECEIVE)) {
            throw new BusinessException(ErrorCode.TASK_ACTION_ILLEGAL);
        }
        task.confirmReceive(request.logisticsNo());
        dataStore.saveTask(task);
        return toOperationResult(task, "收货确认完成");
    }

    @Transactional
    public OperationResultResponse confirmPickup(String taskNo, PickupConfirmRequest request) {
        SampleTask task = findTask(taskNo);
        if (!taskStateMachine.canExecute(task.getTaskStatus(), TaskAction.CONFIRM_PICKUP)) {
            throw new BusinessException(ErrorCode.TASK_ACTION_ILLEGAL);
        }

        for (var itemRequest : request.items()) {
            SampleTaskItem item = task.getItems().stream()
                .filter(it -> it.getId().equals(itemRequest.taskItemId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.PARAM_INVALID, "taskItemId 不存在"));
            int remain = item.getApprovedQty() - item.getPickedQty();
            if (itemRequest.confirmQty() > remain) {
                throw new BusinessException(ErrorCode.PARAM_INVALID, "确认数量超过可提数量");
            }
            item.confirmPickup(itemRequest.confirmQty());
        }
        boolean fullyPicked = task.getItems().stream().allMatch(item -> item.getPickedQty() >= item.getApprovedQty());
        task.confirmPickup(fullyPicked);
        dataStore.saveTask(task);
        return toOperationResult(task, fullyPicked ? "自提确认完成" : "部分自提已确认");
    }

    private SampleTask findTask(String taskNo) {
        return dataStore.findTask(taskNo)
            .orElseThrow(() -> new BusinessException(ErrorCode.PARAM_INVALID, "任务不存在"));
    }

    private BorrowTaskListItemResponse toListItem(SampleTask task) {
        String itemSummary = task.getItems().stream()
            .map(item -> item.getProductName() + "(" + item.getSizeCode() + ")x" + item.getApprovedQty())
            .findFirst()
            .orElse("-");
        return new BorrowTaskListItemResponse(
            task.getTaskNo(),
            task.getBorrowNo(),
            task.getVirtualStoreCode(),
            task.getVirtualStoreName(),
            task.getSourceStoreName(),
            task.getDeliveryType().name(),
            task.getTaskStatus().name(),
            task.getDeliveryStatus().name(),
            task.getPickupStatus().name(),
            task.getReturnStatus().name(),
            format(task.getExpectedReturnAt()),
            task.getLogisticsNo(),
            itemSummary
        );
    }

    private BorrowTaskDetailResponse toDetail(SampleTask task, BorrowApplication application) {
        List<String> actions = new ArrayList<>();
        if (taskStateMachine.canExecute(task.getTaskStatus(), TaskAction.CONFIRM_RECEIVE)) {
            actions.add(TaskAction.CONFIRM_RECEIVE.getCode());
        }
        if (taskStateMachine.canExecute(task.getTaskStatus(), TaskAction.CONFIRM_PICKUP)) {
            actions.add(TaskAction.CONFIRM_PICKUP.getCode());
        }
        if (taskStateMachine.canExecute(task.getTaskStatus(), TaskAction.CREATE_RETURN_BATCH)) {
            actions.add(TaskAction.CREATE_RETURN_BATCH.getCode());
        }

        List<BorrowTaskItemResponse> items = task.getItems().stream()
            .map(item -> new BorrowTaskItemResponse(
                item.getId(),
                item.getSkuCode(),
                item.getSizeCode(),
                item.getProductName(),
                item.getApprovedQty(),
                item.getShippedQty(),
                item.getReceivedQty(),
                item.getPickedQty(),
                item.getBorrowingQty(),
                item.getReturnedApplyQty()
            ))
            .toList();

        return new BorrowTaskDetailResponse(
            task.getTaskNo(),
            task.getBorrowNo(),
            task.getApplyNo(),
            task.getApplicantEmpId(),
            task.getApplicantName(),
            task.getVirtualStoreCode(),
            task.getVirtualStoreName(),
            task.getSourceStoreName(),
            application == null ? null : application.getPickupStoreCode(),
            application == null ? null : application.getPickupStoreName(),
            application == null ? null : application.getReceiverName(),
            application == null ? null : application.getReceiverMobile(),
            application == null ? null : formatAddress(application),
            application == null ? null : application.getRemark(),
            task.getDeliveryType().name(),
            task.getTaskStatus().name(),
            task.getDeliveryStatus().name(),
            task.getPickupStatus().name(),
            task.getReturnStatus().name(),
            task.getCurrentReturnBatchNo(),
            task.getLogisticsNo(),
            format(task.getBorrowedAt()),
            format(task.getExpectedReturnAt()),
            items,
            actions
        );
    }

    private String formatAddress(BorrowApplication application) {
        return String.join("",
            valueOrEmpty(application.getReceiverProvince()),
            valueOrEmpty(application.getReceiverCity()),
            valueOrEmpty(application.getReceiverDistrict()),
            valueOrEmpty(application.getReceiverAddress())
        ).trim();
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private OperationResultResponse toOperationResult(SampleTask task, String message) {
        return new OperationResultResponse(
            task.getTaskNo(),
            task.getTaskStatus().name(),
            task.getDeliveryStatus().name(),
            task.getPickupStatus().name(),
            task.getReturnStatus().name(),
            message
        );
    }

    private String format(LocalDateTime time) {
        return time == null ? null : time.format(DATE_TIME);
    }

    private <T> PageResponse<T> paginate(List<T> all, int pageNo, int pageSize) {
        int safePageNo = Math.max(pageNo, 1);
        int safePageSize = Math.max(pageSize, 1);
        int from = Math.min((safePageNo - 1) * safePageSize, all.size());
        int to = Math.min(from + safePageSize, all.size());
        return new PageResponse<>(all.subList(from, to), safePageNo, safePageSize, all.size());
    }
}
