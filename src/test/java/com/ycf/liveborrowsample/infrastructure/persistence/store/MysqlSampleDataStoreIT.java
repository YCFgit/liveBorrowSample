package com.ycf.liveborrowsample.infrastructure.persistence.store;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ycf.liveborrowsample.domain.enums.AuditStatus;
import com.ycf.liveborrowsample.domain.enums.BatchStatus;
import com.ycf.liveborrowsample.domain.enums.DeliveryStatus;
import com.ycf.liveborrowsample.domain.enums.DeliveryType;
import com.ycf.liveborrowsample.domain.enums.ExceptionStatus;
import com.ycf.liveborrowsample.domain.enums.PickupStatus;
import com.ycf.liveborrowsample.domain.enums.ReturnMethod;
import com.ycf.liveborrowsample.domain.enums.ReturnStatus;
import com.ycf.liveborrowsample.domain.enums.SampleFilterType;
import com.ycf.liveborrowsample.domain.enums.SourceType;
import com.ycf.liveborrowsample.domain.enums.TaskStatus;
import com.ycf.liveborrowsample.domain.model.BorrowApplication;
import com.ycf.liveborrowsample.domain.model.BorrowApplicationItem;
import com.ycf.liveborrowsample.domain.model.ReturnAllocation;
import com.ycf.liveborrowsample.domain.model.ReturnBatch;
import com.ycf.liveborrowsample.domain.model.ReturnBatchItem;
import com.ycf.liveborrowsample.domain.model.SampleTask;
import com.ycf.liveborrowsample.domain.model.SampleTaskItem;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
class MysqlSampleDataStoreIT {

    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("live_borrow_sample_it")
        .withUsername("it_user")
        .withPassword("it_password");

    @DynamicPropertySource
    static void registerDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.datasource.driver-class-name", MYSQL::getDriverClassName);
        registry.add("spring.profiles.active", () -> "local");
    }

    @Autowired
    private MysqlSampleDataStore store;

    @Test
    void shouldPersistAndLoadApplicationItems() {
        BorrowApplication application = new BorrowApplication(
            "BA209901010001",
            "E-IT",
            "集成测试用户",
            "VS0001",
            "直播虚店一号",
            DeliveryType.EXPRESS,
            null,
            null,
            "集成测试用户",
            "13800000000",
            "上海市",
            "上海市",
            "徐汇区",
            "漕溪北路 100 号",
            AuditStatus.APPROVED,
            "TEST",
            "integration test",
            List.of(new BorrowApplicationItem(1, "SKU-IT-001", "M", "集成测试商品", 2, 2))
        );

        store.saveApplication(application);

        BorrowApplication loaded = store.findApplicationByApplyNo("BA209901010001").orElseThrow();
        assertEquals("集成测试用户", loaded.getApplicantName());
        assertEquals(1, loaded.getItems().size());
        assertEquals("SKU-IT-001", loaded.getItems().get(0).getSkuCode());
        assertEquals(2, loaded.getItems().get(0).getApprovedQty());
    }

    @Test
    void shouldLoadVirtualStoresFromMigrationSeedData() {
        assertTrue(store.getVirtualStores().containsKey("VS0001"));
        assertEquals("直播虚店一号", store.getVirtualStoreName("VS0001"));
    }

    @Test
    void shouldPersistReturnBatchAllocationsAndTaskReturnState() {
        SampleTask task = new SampleTask(
            store.nextTaskId(),
            "BT209901010001",
            "BR209901010001",
            "BA209901010099",
            "E-IT",
            "集成测试用户",
            "VS0001",
            "直播虚店一号",
            "上海门店",
            DeliveryType.EXPRESS,
            TaskStatus.BORROWING,
            DeliveryStatus.RECEIVED,
            PickupStatus.NOT_APPLICABLE,
            ReturnStatus.NONE,
            ExceptionStatus.NONE,
            LocalDateTime.of(2099, 1, 1, 10, 0),
            LocalDateTime.of(2099, 1, 8, 10, 0),
            List.of(new SampleTaskItem(
                store.nextTaskItemId(),
                "SKU-IT-RETURN",
                "L",
                "集成测试归还商品",
                "A",
                3,
                3,
                3,
                0,
                3
            ))
        );

        task.attachReturnBatch("RT209901010001", ReturnStatus.PENDING);
        task.getItems().get(0).applyReturn(2);
        store.saveTask(task);

        ReturnBatch batch = new ReturnBatch(
            "RT209901010001",
            "VS0001",
            "直播虚店一号",
            SourceType.GENERAL_RETURN,
            SampleFilterType.ALL,
            ReturnMethod.EXPRESS,
            BatchStatus.PENDING,
            List.of(new ReturnBatchItem(
                "SKU-IT-RETURN",
                "L",
                "集成测试归还商品",
                DeliveryType.EXPRESS.name(),
                3,
                2,
                null
            )),
            List.of(new ReturnAllocation(
                "BT209901010001",
                task.getItems().get(0).getId(),
                "SKU-IT-RETURN",
                "L",
                2,
                ReturnMethod.EXPRESS,
                1
            ))
        );

        store.saveBatch(batch);

        ReturnBatch loadedBatch = store.findBatch("RT209901010001").orElseThrow();
        assertEquals(BatchStatus.PENDING, loadedBatch.getStatus());
        assertEquals(ReturnMethod.EXPRESS, loadedBatch.getReturnMethod());
        assertEquals(1, loadedBatch.getItems().size());
        assertEquals(2, loadedBatch.getItems().get(0).getApplyReturnQty());
        assertEquals(1, loadedBatch.getAllocations().size());
        assertEquals("BT209901010001", loadedBatch.getAllocations().get(0).getTaskNo());
        assertEquals(2, loadedBatch.getAllocations().get(0).getAllocatedQty());

        SampleTask loadedTask = store.findTask("BT209901010001").orElseThrow();
        assertEquals(TaskStatus.RETURNING, loadedTask.getTaskStatus());
        assertEquals(ReturnStatus.PENDING, loadedTask.getReturnStatus());
        assertEquals("RT209901010001", loadedTask.getCurrentReturnBatchNo());
        assertEquals(2, loadedTask.getItems().get(0).getReturnedApplyQty());
        assertEquals(1, loadedTask.getItems().get(0).getRemainingReturnQty());
    }
}
