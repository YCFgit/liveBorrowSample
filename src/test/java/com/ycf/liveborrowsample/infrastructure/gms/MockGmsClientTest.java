package com.ycf.liveborrowsample.infrastructure.gms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.ycf.liveborrowsample.integration.gms.CreateTransferOrderCommand;
import com.ycf.liveborrowsample.integration.gms.GmsClient;
import com.ycf.liveborrowsample.integration.gms.GmsTransferOrder;
import com.ycf.liveborrowsample.integration.gms.TransferOrderType;
import java.util.List;
import org.junit.jupiter.api.Test;

class MockGmsClientTest {

    private final MockGmsClient client = new MockGmsClient();

    @Test
    void shouldQueryAvailableInventory() {
        List<GmsClient.AvailableInventory> inventories = client.queryAvailableInventory("SKU001", "M");

        assertFalse(inventories.isEmpty());
        assertEquals("SKU001", inventories.get(0).skuCode());
        assertEquals("M", inventories.get(0).sizeCode());
    }

    @Test
    void shouldCreateBorrowAndReturnTransferOrders() {
        GmsTransferOrder borrowOrder = client.createTransferOrder(new CreateTransferOrderCommand(
            TransferOrderType.BORROW_OUT,
            "BT202606260001",
            "VS0001",
            "STORE001",
            List.of(new CreateTransferOrderCommand.Item("SKU001", "M", 2))
        ));

        GmsTransferOrder returnOrder = client.createTransferOrder(new CreateTransferOrderCommand(
            TransferOrderType.RETURN_IN,
            "RT202606260001",
            "STORE001",
            "VS0001",
            List.of(new CreateTransferOrderCommand.Item("SKU001", "M", 1))
        ));

        assertEquals("BT202606260001", borrowOrder.bizNo());
        assertEquals(TransferOrderType.BORROW_OUT, borrowOrder.type());
        assertEquals("CREATED", borrowOrder.status());
        assertEquals("RT202606260001", returnOrder.bizNo());
        assertEquals(TransferOrderType.RETURN_IN, returnOrder.type());
    }

    @Test
    void shouldFillLogisticsAndLoadLatestOrderStatus() {
        GmsTransferOrder order = client.createTransferOrder(new CreateTransferOrderCommand(
            TransferOrderType.RETURN_IN,
            "RT202606260002",
            "STORE001",
            "VS0001",
            List.of(new CreateTransferOrderCommand.Item("SKU001", "M", 1))
        ));

        client.fillLogistics(order.gmsOrderNo(), "顺丰速运", "SF1234567890");

        GmsTransferOrder loaded = client.getTransferOrder(order.gmsOrderNo()).orElseThrow();
        assertEquals("LOGISTICS_FILLED", loaded.status());
        assertEquals("顺丰速运", loaded.logisticsCompanyName());
        assertEquals("SF1234567890", loaded.logisticsNo());
    }
}
