package com.ycf.liveborrowsample.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ycf.liveborrowsample.infrastructure.memory.InMemorySampleDataStore;
import com.ycf.liveborrowsample.domain.enums.DeliveryType;
import com.ycf.liveborrowsample.domain.exception.BusinessException;
import com.ycf.liveborrowsample.domain.service.TaskStateMachine;
import com.ycf.liveborrowsample.interfaces.http.request.BorrowApplicationCreateRequest;
import com.ycf.liveborrowsample.interfaces.http.request.BorrowItemRequest;
import com.ycf.liveborrowsample.interfaces.http.request.ReceiverRequest;
import java.util.List;
import org.junit.jupiter.api.Test;

class BorrowApplicationServiceTest {

    private final InMemorySampleDataStore dataStore = new InMemorySampleDataStore();
    private final BorrowApplicationService service = new BorrowApplicationService(dataStore, new TaskStateMachine());

    @Test
    void shouldPersistApplicationItemsWhenCreatingBorrowApplication() {
        var response = service.create(new BorrowApplicationCreateRequest(
            "VS0001",
            DeliveryType.EXPRESS,
            null,
            "单元测试借样",
            new ReceiverRequest("张三", "13800000000", "上海市", "上海市", "徐汇区", "漕溪北路 100 号"),
            List.of(new BorrowItemRequest("SKU-UT-001", "M", 2))
        ));

        var application = dataStore.findApplicationByApplyNo(response.applyNo()).orElseThrow();

        assertEquals(1, application.getItems().size());
        assertEquals("SKU-UT-001", application.getItems().get(0).getSkuCode());
        assertEquals(2, application.getItems().get(0).getApprovedQty());
    }

    @Test
    void shouldRejectExpressBorrowWithoutReceiver() {
        var request = new BorrowApplicationCreateRequest(
            "VS0001",
            DeliveryType.EXPRESS,
            null,
            null,
            null,
            List.of(new BorrowItemRequest("SKU-UT-002", "L", 1))
        );

        BusinessException exception = assertThrows(BusinessException.class, () -> service.create(request));

        assertTrue(exception.getMessage().contains("收货信息"));
    }

    @Test
    void shouldRejectPickupBorrowWithoutPickupStore() {
        var request = new BorrowApplicationCreateRequest(
            "VS0001",
            DeliveryType.PICKUP,
            null,
            null,
            null,
            List.of(new BorrowItemRequest("SKU-UT-003", "S", 1))
        );

        BusinessException exception = assertThrows(BusinessException.class, () -> service.create(request));

        assertTrue(exception.getMessage().contains("自提门店"));
    }
}
