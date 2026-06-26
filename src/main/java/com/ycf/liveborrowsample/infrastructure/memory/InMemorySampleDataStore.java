package com.ycf.liveborrowsample.infrastructure.memory;

import com.ycf.liveborrowsample.application.port.SampleDataStore;
import com.ycf.liveborrowsample.domain.enums.BatchStatus;
import com.ycf.liveborrowsample.domain.enums.DeliveryStatus;
import com.ycf.liveborrowsample.domain.enums.DeliveryType;
import com.ycf.liveborrowsample.domain.enums.ExceptionStatus;
import com.ycf.liveborrowsample.domain.enums.PickupStatus;
import com.ycf.liveborrowsample.domain.enums.ReturnStatus;
import com.ycf.liveborrowsample.domain.enums.TaskStatus;
import com.ycf.liveborrowsample.domain.model.BorrowApplication;
import com.ycf.liveborrowsample.domain.model.ReturnBatch;
import com.ycf.liveborrowsample.domain.model.SampleTask;
import com.ycf.liveborrowsample.domain.model.SampleTaskItem;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("memory")
@Component
public class InMemorySampleDataStore implements SampleDataStore {

    private static final DateTimeFormatter DATE_TOKEN = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final Map<String, SampleTask> tasks = new ConcurrentHashMap<>();
    private final Map<String, BorrowApplication> applications = new ConcurrentHashMap<>();
    private final Map<String, ReturnBatch> batches = new ConcurrentHashMap<>();
    private final Map<String, String> virtualStores = new LinkedHashMap<>();
    private final AtomicLong taskIdGenerator = new AtomicLong(1000L);
    private final AtomicLong taskItemIdGenerator = new AtomicLong(10000L);
    private final AtomicLong applySeq = new AtomicLong(1L);
    private final AtomicLong borrowSeq = new AtomicLong(1L);
    private final AtomicLong taskSeq = new AtomicLong(1L);
    private final AtomicLong batchSeq = new AtomicLong(1L);

    public InMemorySampleDataStore() {
        virtualStores.put("VS0001", "直播虚店一号");
        virtualStores.put("VS0002", "直播虚店二号");
        seed();
    }

    @Override
    public synchronized String nextApplyNo() {
        return "BA" + LocalDateTime.now().format(DATE_TOKEN) + String.format("%04d", applySeq.getAndIncrement());
    }

    @Override
    public synchronized String nextBorrowNo() {
        return "BR" + LocalDateTime.now().format(DATE_TOKEN) + String.format("%04d", borrowSeq.getAndIncrement());
    }

    @Override
    public synchronized String nextTaskNo() {
        return "BT" + LocalDateTime.now().format(DATE_TOKEN) + String.format("%04d", taskSeq.getAndIncrement());
    }

    @Override
    public synchronized String nextReturnBatchNo() {
        return "RT" + LocalDateTime.now().format(DATE_TOKEN) + String.format("%06d", batchSeq.getAndIncrement());
    }

    @Override
    public synchronized Long nextTaskId() {
        return taskIdGenerator.getAndIncrement();
    }

    @Override
    public synchronized Long nextTaskItemId() {
        return taskItemIdGenerator.getAndIncrement();
    }

    @Override
    public void saveApplication(BorrowApplication application) {
        applications.put(application.getApplyNo(), application);
    }

    @Override
    public Optional<BorrowApplication> findApplicationByApplyNo(String applyNo) {
        return Optional.ofNullable(applications.get(applyNo));
    }

    @Override
    public List<SampleTask> listTasks() {
        return tasks.values().stream()
            .sorted(Comparator.comparing(SampleTask::getBorrowedAt).reversed())
            .toList();
    }

    @Override
    public Optional<SampleTask> findTask(String taskNo) {
        return Optional.ofNullable(tasks.get(taskNo));
    }

    @Override
    public synchronized void saveTask(SampleTask task) {
        tasks.put(task.getTaskNo(), task);
    }

    @Override
    public List<SampleTask> listTasksByVirtualStore(String virtualStoreCode) {
        return tasks.values().stream()
            .filter(task -> task.getVirtualStoreCode().equals(virtualStoreCode))
            .toList();
    }

    @Override
    public synchronized void saveBatch(ReturnBatch returnBatch) {
        batches.put(returnBatch.getReturnBatchNo(), returnBatch);
    }

    @Override
    public Optional<ReturnBatch> findBatch(String returnBatchNo) {
        return Optional.ofNullable(batches.get(returnBatchNo));
    }

    @Override
    public Map<String, String> getVirtualStores() {
        return virtualStores;
    }

    @Override
    public String getVirtualStoreName(String code) {
        return virtualStores.getOrDefault(code, code);
    }

    private void seed() {
        List<SampleTask> seedTasks = new ArrayList<>();

        seedTasks.add(new SampleTask(
            nextTaskId(), "BT202606240001", "BR202606240001", "BA202606240001", "E10001", "张三",
            "VS0001", "直播虚店一号", "徐汇门店", DeliveryType.EXPRESS,
            TaskStatus.BORROWING, DeliveryStatus.RECEIVED, PickupStatus.NOT_APPLICABLE, ReturnStatus.NONE,
            ExceptionStatus.NONE, LocalDateTime.of(2026, 6, 20, 10, 0), LocalDateTime.of(2026, 7, 1, 10, 0),
            List.of(new SampleTaskItem(nextTaskItemId(), "SKU001", "M", "基础连衣裙", "A", 2, 2, 2, 0, 2))
        ));
        applications.put("BA202606240001", new BorrowApplication(
            "BA202606240001", "E10001", "张三", "VS0001", "直播虚店一号",
            DeliveryType.EXPRESS, null, null, "张三", "13800001111",
            "上海市", "上海市", "徐汇区", "漕溪北路 100 号 1602",
            com.ycf.liveborrowsample.domain.enums.AuditStatus.APPROVED, "DINGTALK", "直播排品测试",
            List.of()
        ));
        seedTasks.add(new SampleTask(
            nextTaskId(), "BT202606240002", "BR202606240002", "BA202606240002", "E10001", "张三",
            "VS0001", "直播虚店一号", "徐汇门店", DeliveryType.EXPRESS,
            TaskStatus.BORROWING, DeliveryStatus.RECEIVED, PickupStatus.NOT_APPLICABLE, ReturnStatus.NONE,
            ExceptionStatus.NONE, LocalDateTime.of(2026, 6, 22, 12, 0), LocalDateTime.of(2026, 7, 2, 12, 0),
            List.of(new SampleTaskItem(nextTaskItemId(), "SKU001", "M", "基础连衣裙", "A", 1, 1, 1, 0, 1))
        ));
        applications.put("BA202606240002", new BorrowApplication(
            "BA202606240002", "E10001", "张三", "VS0001", "直播虚店一号",
            DeliveryType.EXPRESS, null, null, "张三", "13800001111",
            "上海市", "上海市", "徐汇区", "肇嘉浜路 889 号",
            com.ycf.liveborrowsample.domain.enums.AuditStatus.APPROVED, "DINGTALK", "追加借样",
            List.of()
        ));
        seedTasks.add(new SampleTask(
            nextTaskId(), "BT202606240003", "BR202606240003", "BA202606240003", "E10002", "李四",
            "VS0002", "直播虚店二号", "浦东门店", DeliveryType.PICKUP,
            TaskStatus.WAIT_PICKUP, DeliveryStatus.NONE, PickupStatus.WAIT_PICKUP, ReturnStatus.NONE,
            ExceptionStatus.NONE, LocalDateTime.of(2026, 6, 23, 15, 0), LocalDateTime.of(2026, 7, 3, 15, 0),
            List.of(new SampleTaskItem(nextTaskItemId(), "SKU002", "L", "卫衣外套", "A", 2, 0, 0, 0, 0))
        ));
        applications.put("BA202606240003", new BorrowApplication(
            "BA202606240003", "E10002", "李四", "VS0002", "直播虚店二号",
            DeliveryType.PICKUP, "PUDONG-01", "浦东门店", null, null,
            null, null, null, null,
            com.ycf.liveborrowsample.domain.enums.AuditStatus.APPROVED, "DINGTALK", "自提借样",
            List.of()
        ));
        seedTasks.add(new SampleTask(
            nextTaskId(), "BT202606240004", "BR202606240004", "BA202606240004", "E10003", "王五",
            "VS0001", "直播虚店一号", "长宁门店", DeliveryType.EXPRESS,
            TaskStatus.IN_TRANSIT, DeliveryStatus.SHIPPED, PickupStatus.NOT_APPLICABLE, ReturnStatus.NONE,
            ExceptionStatus.NONE, LocalDateTime.of(2026, 6, 24, 9, 0), LocalDateTime.of(2026, 7, 4, 9, 0),
            List.of(new SampleTaskItem(nextTaskItemId(), "SKU003", "S", "基础衬衫", "A", 1, 1, 1, 0, 0))
        ));
        applications.put("BA202606240004", new BorrowApplication(
            "BA202606240004", "E10003", "王五", "VS0001", "直播虚店一号",
            DeliveryType.EXPRESS, null, null, "王五", "13900002222",
            "上海市", "上海市", "长宁区", "延安西路 1200 号",
            com.ycf.liveborrowsample.domain.enums.AuditStatus.APPROVED, "DINGTALK", "运输中任务",
            List.of()
        ));

        seedTasks.forEach(task -> tasks.put(task.getTaskNo(), task));
    }
}
