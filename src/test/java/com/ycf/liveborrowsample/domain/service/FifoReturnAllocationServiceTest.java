package com.ycf.liveborrowsample.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.ycf.liveborrowsample.domain.exception.BusinessException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class FifoReturnAllocationServiceTest {

    private final FifoReturnAllocationService service = new FifoReturnAllocationService();

    @Test
    void shouldAllocateByBorrowTimeAscending() {
        List<FifoReturnAllocationService.ReturnCandidate> candidates = List.of(
            new FifoReturnAllocationService.ReturnCandidate(
                "BT2", 2L, "SKU001", "M", "基础连衣裙", "EXPRESS", null, 2, LocalDateTime.of(2026, 6, 22, 10, 0)
            ),
            new FifoReturnAllocationService.ReturnCandidate(
                "BT1", 1L, "SKU001", "M", "基础连衣裙", "EXPRESS", null, 2, LocalDateTime.of(2026, 6, 20, 10, 0)
            )
        );

        List<FifoReturnAllocationService.FifoAllocationResult> results = service.allocate(3, candidates);

        assertEquals(2, results.size());
        assertEquals("BT1", results.get(0).candidate().taskNo());
        assertEquals(2, results.get(0).allocatedQty());
        assertEquals("BT2", results.get(1).candidate().taskNo());
        assertEquals(1, results.get(1).allocatedQty());
    }

    @Test
    void shouldThrowWhenQuantityNotEnough() {
        List<FifoReturnAllocationService.ReturnCandidate> candidates = List.of(
            new FifoReturnAllocationService.ReturnCandidate(
                "BT1", 1L, "SKU001", "M", "基础连衣裙", "EXPRESS", null, 1, LocalDateTime.of(2026, 6, 20, 10, 0)
            )
        );

        assertThrows(BusinessException.class, () -> service.allocate(2, candidates));
    }
}
