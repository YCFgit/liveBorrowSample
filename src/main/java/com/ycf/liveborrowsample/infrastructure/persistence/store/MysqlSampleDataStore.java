package com.ycf.liveborrowsample.infrastructure.persistence.store;

import com.ycf.liveborrowsample.application.port.SampleDataStore;
import com.ycf.liveborrowsample.domain.enums.AuditStatus;
import com.ycf.liveborrowsample.domain.enums.BatchStatus;
import com.ycf.liveborrowsample.domain.enums.DeliveryStatus;
import com.ycf.liveborrowsample.domain.enums.DeliveryType;
import com.ycf.liveborrowsample.domain.enums.ErrorCode;
import com.ycf.liveborrowsample.domain.enums.ExceptionStatus;
import com.ycf.liveborrowsample.domain.enums.PickupStatus;
import com.ycf.liveborrowsample.domain.enums.ReturnMethod;
import com.ycf.liveborrowsample.domain.enums.ReturnStatus;
import com.ycf.liveborrowsample.domain.enums.SampleFilterType;
import com.ycf.liveborrowsample.domain.enums.SourceType;
import com.ycf.liveborrowsample.domain.enums.TaskStatus;
import com.ycf.liveborrowsample.domain.exception.BusinessException;
import com.ycf.liveborrowsample.domain.model.BorrowApplication;
import com.ycf.liveborrowsample.domain.model.BorrowApplicationItem;
import com.ycf.liveborrowsample.domain.model.ReturnAllocation;
import com.ycf.liveborrowsample.domain.model.ReturnBatch;
import com.ycf.liveborrowsample.domain.model.ReturnBatchItem;
import com.ycf.liveborrowsample.domain.model.SampleTask;
import com.ycf.liveborrowsample.domain.model.SampleTaskItem;
import com.ycf.liveborrowsample.infrastructure.persistence.entity.BorrowApplicationEntity;
import com.ycf.liveborrowsample.infrastructure.persistence.entity.BorrowApplicationItemEntity;
import com.ycf.liveborrowsample.infrastructure.persistence.entity.ReturnAllocationEntity;
import com.ycf.liveborrowsample.infrastructure.persistence.entity.ReturnBatchEntity;
import com.ycf.liveborrowsample.infrastructure.persistence.entity.ReturnBatchItemEntity;
import com.ycf.liveborrowsample.infrastructure.persistence.entity.SampleTaskEntity;
import com.ycf.liveborrowsample.infrastructure.persistence.entity.SampleTaskItemEntity;
import com.ycf.liveborrowsample.infrastructure.persistence.mapper.LiveBorrowSampleMapper;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.LongSupplier;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Profile("!memory")
@Component
public class MysqlSampleDataStore implements SampleDataStore {

    private static final DateTimeFormatter DATE_TOKEN = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final LiveBorrowSampleMapper mapper;
    private final JdbcTemplate jdbcTemplate;
    private final Map<String, String> virtualStores = new LinkedHashMap<>();

    public MysqlSampleDataStore(LiveBorrowSampleMapper mapper, JdbcTemplate jdbcTemplate) {
        this.mapper = mapper;
        this.jdbcTemplate = jdbcTemplate;
        virtualStores.put("VS0001", "直播虚店一号");
        virtualStores.put("VS0002", "直播虚店二号");
    }

    @Override
    public synchronized String nextApplyNo() {
        return buildDailyNo("BA", 4, "APPLY_NO:" + todayToken());
    }

    @Override
    public synchronized String nextBorrowNo() {
        return buildDailyNo("BR", 4, "BORROW_NO:" + todayToken());
    }

    @Override
    public synchronized String nextTaskNo() {
        return buildDailyNo("BT", 4, "TASK_NO:" + todayToken());
    }

    @Override
    public synchronized String nextReturnBatchNo() {
        return buildDailyNo("RT", 6, "RETURN_BATCH_NO:" + todayToken());
    }

    @Override
    public synchronized Long nextTaskId() {
        return nextSequenceValue("TASK_ID", () -> maxId("sample_task") + 1);
    }

    @Override
    public synchronized Long nextTaskItemId() {
        return nextSequenceValue("TASK_ITEM_ID", () -> maxId("sample_task_item") + 1);
    }

    @Override
    public void saveApplication(BorrowApplication application) {
        BorrowApplicationEntity entity = toEntity(application);
        Long applyId = mapper.findApplyIdByApplyNo(application.getApplyNo());
        if (applyId == null) {
            mapper.insertApply(entity);
            applyId = entity.getId();
        } else {
            entity.setId(applyId);
            mapper.updateApply(entity);
        }

        List<BorrowApplicationItemEntity> itemEntities = new ArrayList<>();
        for (BorrowApplicationItem item : application.getItems()) {
            BorrowApplicationItemEntity itemEntity = new BorrowApplicationItemEntity();
            itemEntity.setApplyId(applyId);
            itemEntity.setLineNo(item.getLineNo());
            itemEntity.setSkuCode(item.getSkuCode());
            itemEntity.setSizeCode(item.getSizeCode());
            itemEntity.setProductName(item.getProductName());
            itemEntity.setApplyQty(item.getApplyQty());
            itemEntity.setApprovedQty(item.getApprovedQty());
            itemEntities.add(itemEntity);
        }
        if (!itemEntities.isEmpty()) {
            mapper.upsertApplyItems(itemEntities);
        }
    }

    @Override
    public Optional<BorrowApplication> findApplicationByApplyNo(String applyNo) {
        BorrowApplicationEntity entity = mapper.findApplyByApplyNo(applyNo);
        if (entity == null) {
            return Optional.empty();
        }
        List<BorrowApplicationItem> items = mapper.listApplyItems(entity.getId()).stream()
            .sorted(Comparator.comparing(BorrowApplicationItemEntity::getLineNo))
            .map(this::toDomain)
            .toList();
        return Optional.of(new BorrowApplication(
            entity.getApplyNo(),
            entity.getApplicantEmpId(),
            entity.getApplicantName(),
            entity.getVirtualStoreCode(),
            entity.getVirtualStoreName(),
            DeliveryType.valueOf(entity.getDeliveryType()),
            entity.getPickupStoreCode(),
            entity.getPickupStoreName(),
            entity.getReceiverName(),
            entity.getReceiverMobile(),
            entity.getReceiverProvince(),
            entity.getReceiverCity(),
            entity.getReceiverDistrict(),
            entity.getReceiverAddress(),
            AuditStatus.valueOf(entity.getAuditStatus()),
            entity.getSourceChannel(),
            entity.getRemark(),
            items
        ));
    }

    @Override
    public List<SampleTask> listTasks() {
        return loadTasks(mapper.listTasks());
    }

    @Override
    public Optional<SampleTask> findTask(String taskNo) {
        SampleTaskEntity entity = mapper.findTaskByTaskNo(taskNo);
        if (entity == null) {
            return Optional.empty();
        }
        return Optional.of(loadTasks(List.of(entity)).get(0));
    }

    @Override
    public void saveTask(SampleTask task) {
        SampleTaskEntity entity = toEntity(task);
        Long taskId = mapper.findTaskIdByTaskNo(task.getTaskNo());
        if (taskId == null) {
            mapper.insertTask(entity);
            taskId = entity.getId();
        } else {
            entity.setId(taskId);
            mapper.updateTask(entity);
        }

        List<SampleTaskItemEntity> itemEntities = new ArrayList<>();
        int lineNo = 1;
        for (SampleTaskItem item : task.getItems()) {
            SampleTaskItemEntity itemEntity = new SampleTaskItemEntity();
            itemEntity.setId(item.getId());
            itemEntity.setTaskId(taskId);
            itemEntity.setLineNo(lineNo++);
            itemEntity.setSkuCode(item.getSkuCode());
            itemEntity.setSizeCode(item.getSizeCode());
            itemEntity.setProductName(item.getProductName());
            itemEntity.setInventoryGrade(item.getInventoryGrade());
            itemEntity.setApplyQty(item.getApprovedQty());
            itemEntity.setApprovedQty(item.getApprovedQty());
            itemEntity.setShippedQty(item.getShippedQty());
            itemEntity.setReceivedQty(item.getReceivedQty());
            itemEntity.setPickedQty(item.getPickedQty());
            itemEntity.setBorrowingQty(item.getBorrowingQty());
            itemEntity.setReturnedApplyQty(item.getReturnedApplyQty());
            itemEntity.setReturnedReceivedQty(item.getReturnedReceivedQty());
            itemEntities.add(itemEntity);
        }
        if (!itemEntities.isEmpty()) {
            mapper.upsertTaskItems(itemEntities);
        }
    }

    @Override
    public List<SampleTask> listTasksByVirtualStore(String virtualStoreCode) {
        return loadTasks(mapper.listTasksByVirtualStore(virtualStoreCode));
    }

    @Override
    public void saveBatch(ReturnBatch returnBatch) {
        ReturnBatchEntity entity = toEntity(returnBatch);
        ReturnBatchEntity existing = mapper.findReturnBatchByNo(returnBatch.getReturnBatchNo());
        if (existing == null) {
            mapper.insertReturnBatch(entity);
            replaceReturnBatchChildren(entity.getId(), returnBatch);
            return;
        }

        entity.setId(existing.getId());
        mapper.updateReturnBatch(entity);
        replaceReturnBatchChildren(existing.getId(), returnBatch);
    }

    @Override
    public Optional<ReturnBatch> findBatch(String returnBatchNo) {
        ReturnBatchEntity entity = mapper.findReturnBatchByNo(returnBatchNo);
        if (entity == null) {
            return Optional.empty();
        }
        List<ReturnBatchItem> items = mapper.listReturnBatchItems(entity.getId()).stream()
            .sorted(Comparator.comparing(ReturnBatchItemEntity::getLineNo))
            .map(this::toDomain)
            .toList();
        List<ReturnAllocation> allocations = mapper.listReturnAllocations(entity.getId()).stream()
            .sorted(Comparator.comparing(ReturnAllocationEntity::getAllocationSeq))
            .map(this::toDomain)
            .toList();
        return Optional.of(new ReturnBatch(
            entity.getReturnBatchNo(),
            entity.getVirtualStoreCode(),
            entity.getVirtualStoreName(),
            SourceType.valueOf(entity.getSourceType()),
            SampleFilterType.valueOf(entity.getSampleFilterType()),
            ReturnMethod.valueOf(entity.getReturnMethod()),
            BatchStatus.valueOf(entity.getStatus()),
            entity.getLogisticsCompanyName(),
            entity.getLogisticsNo(),
            items,
            allocations
        ));
    }

    @Override
    public Map<String, String> getVirtualStores() {
        return Map.copyOf(loadVirtualStores());
    }

    @Override
    public String getVirtualStoreName(String code) {
        return loadVirtualStores().getOrDefault(code, code);
    }

    private Map<String, String> loadVirtualStores() {
        Map<String, String> stores = new LinkedHashMap<>();
        try {
            for (Map<String, String> row : mapper.listVirtualStores()) {
                String code = row.get("virtual_store_code");
                String name = row.get("virtual_store_name");
                if (code != null && name != null) {
                    stores.put(code, name);
                }
            }
        } catch (RuntimeException ignored) {
            // Keep older local schemas usable until sql/mvp_schema.sql is reapplied.
        }
        if (stores.isEmpty()) {
            stores.putAll(virtualStores);
        }
        return stores;
    }

    private List<SampleTask> loadTasks(List<SampleTaskEntity> taskEntities) {
        if (taskEntities.isEmpty()) {
            return List.of();
        }

        List<Long> taskIds = taskEntities.stream()
            .map(SampleTaskEntity::getId)
            .toList();
        Map<Long, List<SampleTaskItem>> itemMap = new LinkedHashMap<>();
        for (SampleTaskItemEntity itemEntity : mapper.listTaskItemsByTaskIds(taskIds)) {
            itemMap.computeIfAbsent(itemEntity.getTaskId(), key -> new ArrayList<>())
                .add(toDomain(itemEntity));
        }

        return taskEntities.stream()
            .map(taskEntity -> new SampleTask(
                taskEntity.getId(),
                taskEntity.getTaskNo(),
                taskEntity.getBorrowNo(),
                taskEntity.getApplyNo(),
                taskEntity.getApplicantEmpId(),
                taskEntity.getApplicantName(),
                taskEntity.getVirtualStoreCode(),
                taskEntity.getVirtualStoreName(),
                taskEntity.getSourceStoreName(),
                DeliveryType.valueOf(taskEntity.getDeliveryType()),
                TaskStatus.valueOf(taskEntity.getTaskStatus()),
                DeliveryStatus.valueOf(taskEntity.getDeliveryStatus()),
                PickupStatus.valueOf(taskEntity.getPickupStatus()),
                ReturnStatus.valueOf(taskEntity.getReturnStatus()),
                ExceptionStatus.valueOf(taskEntity.getExceptionStatus()),
                taskEntity.getCurrentReturnBatchNo(),
                taskEntity.getLogisticsNo(),
                taskEntity.getBorrowedAt(),
                taskEntity.getExpectedReturnAt(),
                itemMap.getOrDefault(taskEntity.getId(), List.of())
            ))
            .toList();
    }

    private BorrowApplicationEntity toEntity(BorrowApplication application) {
        BorrowApplicationEntity entity = new BorrowApplicationEntity();
        entity.setApplyNo(application.getApplyNo());
        entity.setApplicantEmpId(application.getApplicantEmpId());
        entity.setApplicantName(application.getApplicantName());
        entity.setVirtualStoreCode(application.getVirtualStoreCode());
        entity.setVirtualStoreName(application.getVirtualStoreName());
        entity.setDeliveryType(application.getDeliveryType().name());
        entity.setPickupStoreCode(application.getPickupStoreCode());
        entity.setPickupStoreName(application.getPickupStoreName());
        entity.setReceiverName(application.getReceiverName());
        entity.setReceiverMobile(application.getReceiverMobile());
        entity.setReceiverProvince(application.getReceiverProvince());
        entity.setReceiverCity(application.getReceiverCity());
        entity.setReceiverDistrict(application.getReceiverDistrict());
        entity.setReceiverAddress(application.getReceiverAddress());
        entity.setAuditStatus(application.getAuditStatus().name());
        entity.setSourceChannel(application.getSourceChannel());
        entity.setRemark(application.getRemark());
        return entity;
    }

    private SampleTaskEntity toEntity(SampleTask task) {
        SampleTaskEntity entity = new SampleTaskEntity();
        entity.setId(task.getId());
        entity.setTaskNo(task.getTaskNo());
        entity.setBorrowNo(task.getBorrowNo());
        entity.setApplyNo(task.getApplyNo());
        entity.setApplicantEmpId(task.getApplicantEmpId());
        entity.setApplicantName(task.getApplicantName());
        entity.setVirtualStoreCode(task.getVirtualStoreCode());
        entity.setVirtualStoreName(task.getVirtualStoreName());
        entity.setSourceStoreName(task.getSourceStoreName());
        entity.setDeliveryType(task.getDeliveryType().name());
        entity.setTaskStatus(task.getTaskStatus().name());
        entity.setDeliveryStatus(task.getDeliveryStatus().name());
        entity.setPickupStatus(task.getPickupStatus().name());
        entity.setReturnStatus(task.getReturnStatus().name());
        entity.setExceptionStatus(task.getExceptionStatus().name());
        entity.setCurrentReturnBatchNo(task.getCurrentReturnBatchNo());
        entity.setLogisticsNo(task.getLogisticsNo());
        entity.setBorrowedAt(task.getBorrowedAt());
        entity.setExpectedReturnAt(task.getExpectedReturnAt());
        return entity;
    }

    private void replaceReturnBatchChildren(Long batchId, ReturnBatch returnBatch) {
        mapper.deleteReturnAllocations(batchId);
        mapper.deleteReturnBatchItems(batchId);

        List<ReturnBatchItemEntity> batchItemEntities = new ArrayList<>();
        int lineNo = 1;
        for (ReturnBatchItem item : returnBatch.getItems()) {
            ReturnBatchItemEntity itemEntity = new ReturnBatchItemEntity();
            itemEntity.setReturnBatchId(batchId);
            itemEntity.setLineNo(lineNo++);
            itemEntity.setSkuCode(item.getSkuCode());
            itemEntity.setSizeCode(item.getSizeCode());
            itemEntity.setProductName(item.getProductName());
            itemEntity.setSampleType(item.getSampleType());
            itemEntity.setSourceStoreName(item.getSourceStoreName());
            itemEntity.setAvailableReturnQty(item.getAvailableReturnQty());
            itemEntity.setApplyReturnQty(item.getApplyReturnQty());
            batchItemEntities.add(itemEntity);
        }
        if (!batchItemEntities.isEmpty()) {
            mapper.insertReturnBatchItems(batchItemEntities);
        }

        List<ReturnAllocationEntity> allocationEntities = new ArrayList<>();
        for (ReturnAllocation allocation : returnBatch.getAllocations()) {
            ReturnAllocationEntity allocationEntity = new ReturnAllocationEntity();
            allocationEntity.setReturnBatchId(batchId);
            allocationEntity.setTaskId(resolveTaskId(allocation.getTaskNo()));
            allocationEntity.setTaskItemId(allocation.getTaskItemId());
            allocationEntity.setTaskNo(allocation.getTaskNo());
            allocationEntity.setSkuCode(allocation.getSkuCode());
            allocationEntity.setSizeCode(allocation.getSizeCode());
            allocationEntity.setAllocatedQty(allocation.getAllocatedQty());
            allocationEntity.setReturnMethod(allocation.getReturnMethod().name());
            allocationEntity.setAllocationSeq(allocation.getAllocationSeq());
            allocationEntities.add(allocationEntity);
        }
        if (!allocationEntities.isEmpty()) {
            mapper.insertReturnAllocations(allocationEntities);
        }
    }

    private ReturnBatchEntity toEntity(ReturnBatch batch) {
        ReturnBatchEntity entity = new ReturnBatchEntity();
        entity.setReturnBatchNo(batch.getReturnBatchNo());
        entity.setCreatorEmpId("E-DEMO");
        entity.setCreatorName("演示用户");
        entity.setVirtualStoreCode(batch.getVirtualStoreCode());
        entity.setVirtualStoreName(batch.getVirtualStoreName());
        entity.setSourceType(batch.getSourceType().name());
        entity.setSampleFilterType(batch.getSampleFilterType().name());
        entity.setReturnMethod(batch.getReturnMethod().name());
        entity.setStatus(batch.getStatus().name());
        entity.setLogisticsCompanyName(batch.getLogisticsCompanyName());
        entity.setLogisticsNo(batch.getLogisticsNo());
        entity.setRemark(null);
        return entity;
    }

    private BorrowApplicationItem toDomain(BorrowApplicationItemEntity entity) {
        return new BorrowApplicationItem(
            entity.getLineNo(),
            entity.getSkuCode(),
            entity.getSizeCode(),
            entity.getProductName(),
            entity.getApplyQty(),
            entity.getApprovedQty()
        );
    }

    private SampleTaskItem toDomain(SampleTaskItemEntity entity) {
        return new SampleTaskItem(
            entity.getId(),
            entity.getSkuCode(),
            entity.getSizeCode(),
            entity.getProductName(),
            entity.getInventoryGrade(),
            entity.getApprovedQty(),
            entity.getShippedQty(),
            entity.getReceivedQty(),
            entity.getPickedQty(),
            entity.getBorrowingQty(),
            entity.getReturnedApplyQty(),
            entity.getReturnedReceivedQty()
        );
    }

    private ReturnBatchItem toDomain(ReturnBatchItemEntity entity) {
        return new ReturnBatchItem(
            entity.getSkuCode(),
            entity.getSizeCode(),
            entity.getProductName(),
            entity.getSampleType(),
            entity.getAvailableReturnQty(),
            entity.getApplyReturnQty(),
            entity.getSourceStoreName()
        );
    }

    private ReturnAllocation toDomain(ReturnAllocationEntity entity) {
        return new ReturnAllocation(
            entity.getTaskNo(),
            entity.getTaskItemId(),
            entity.getSkuCode(),
            entity.getSizeCode(),
            entity.getAllocatedQty(),
            ReturnMethod.valueOf(entity.getReturnMethod()),
            entity.getAllocationSeq()
        );
    }

    private Long resolveTaskId(String taskNo) {
        Long taskId = mapper.findTaskIdByTaskNo(taskNo);
        if (taskId == null) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "关联任务不存在: " + taskNo);
        }
        return taskId;
    }

    private String buildDailyNo(String prefix, int digits, String sequenceKey) {
        long sequence = nextSequenceValue(sequenceKey, () -> 1L);
        return prefix + todayToken() + String.format("%0" + digits + "d", sequence);
    }

    private String todayToken() {
        return LocalDate.now().format(DATE_TOKEN);
    }

    private Long maxId(String tableName) {
        Long value = jdbcTemplate.queryForObject("SELECT COALESCE(MAX(id), 0) FROM " + tableName, Long.class);
        return value == null ? 0L : value;
    }

    private Long nextSequenceValue(String sequenceKey, LongSupplier initialValueSupplier) {
        Long currentValue = jdbcTemplate.query(
            "SELECT current_value FROM biz_sequence WHERE sequence_key = ?",
            rs -> rs.next() ? rs.getLong(1) : null,
            sequenceKey
        );
        if (currentValue == null) {
            long initialValue = initialValueSupplier.getAsLong();
            try {
                jdbcTemplate.update(
                    "INSERT INTO biz_sequence (sequence_key, current_value) VALUES (?, ?)",
                    sequenceKey,
                    initialValue
                );
                return initialValue;
            } catch (DuplicateKeyException ignored) {
                currentValue = jdbcTemplate.query(
                    "SELECT current_value FROM biz_sequence WHERE sequence_key = ?",
                    rs -> rs.next() ? rs.getLong(1) : null,
                    sequenceKey
                );
            }
        }

        long nextValue = currentValue + 1;
        jdbcTemplate.update(
            "UPDATE biz_sequence SET current_value = ? WHERE sequence_key = ?",
            nextValue,
            sequenceKey
        );
        return nextValue;
    }
}
