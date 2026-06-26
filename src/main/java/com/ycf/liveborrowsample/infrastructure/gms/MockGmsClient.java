package com.ycf.liveborrowsample.infrastructure.gms;

import com.ycf.liveborrowsample.integration.gms.CreateTransferOrderCommand;
import com.ycf.liveborrowsample.integration.gms.GmsClient;
import com.ycf.liveborrowsample.integration.gms.GmsTransferOrder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("!prod")
@Component
public class MockGmsClient implements GmsClient {

    private final AtomicLong orderSequence = new AtomicLong(1);
    private final Map<String, GmsTransferOrder> orders = new LinkedHashMap<>();

    @Override
    public List<AvailableInventory> queryAvailableInventory(String skuCode, String sizeCode) {
        return List.of(
            new AvailableInventory("STORE001", "上海南京东路店", skuCode, sizeCode, 12, "A"),
            new AvailableInventory("STORE002", "杭州湖滨店", skuCode, sizeCode, 5, "B")
        );
    }

    @Override
    public GmsTransferOrder createTransferOrder(CreateTransferOrderCommand command) {
        String gmsOrderNo = "GMS" + String.format("%08d", orderSequence.getAndIncrement());
        GmsTransferOrder order = new GmsTransferOrder(
            gmsOrderNo,
            command.type(),
            command.bizNo(),
            command.fromOrgCode(),
            command.toOrgCode(),
            "CREATED",
            null,
            null,
            List.copyOf(command.items())
        );
        orders.put(gmsOrderNo, order);
        return order;
    }

    @Override
    public Optional<GmsTransferOrder> getTransferOrder(String gmsOrderNo) {
        return Optional.ofNullable(orders.get(gmsOrderNo));
    }

    @Override
    public GmsTransferOrder fillLogistics(String gmsOrderNo, String companyName, String logisticsNo) {
        GmsTransferOrder existing = getTransferOrder(gmsOrderNo)
            .orElseThrow(() -> new IllegalArgumentException("GMS transfer order not found: " + gmsOrderNo));
        GmsTransferOrder updated = new GmsTransferOrder(
            existing.gmsOrderNo(),
            existing.type(),
            existing.bizNo(),
            existing.fromOrgCode(),
            existing.toOrgCode(),
            "LOGISTICS_FILLED",
            companyName,
            logisticsNo,
            new ArrayList<>(existing.items())
        );
        orders.put(gmsOrderNo, updated);
        return updated;
    }
}
