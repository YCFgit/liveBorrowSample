package com.ycf.liveborrowsample.domain.service;

import com.ycf.liveborrowsample.domain.enums.ErrorCode;
import com.ycf.liveborrowsample.domain.exception.BusinessException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class FifoReturnAllocationService {

    public List<FifoAllocationResult> allocate(int requestQty, List<ReturnCandidate> candidates) {
        int available = candidates.stream().mapToInt(ReturnCandidate::remainingQty).sum();
        if (available < requestQty) {
            throw new BusinessException(ErrorCode.RETURN_NOT_ENOUGH);
        }

        List<ReturnCandidate> sortedCandidates = candidates.stream()
            .sorted(Comparator.comparing(ReturnCandidate::borrowedAt)
                .thenComparing(ReturnCandidate::taskNo)
                .thenComparing(ReturnCandidate::taskItemId))
            .toList();

        List<FifoAllocationResult> results = new ArrayList<>();
        int remain = requestQty;
        int seq = 1;
        for (ReturnCandidate candidate : sortedCandidates) {
            if (remain <= 0) {
                break;
            }
            int allocated = Math.min(remain, candidate.remainingQty());
            if (allocated <= 0) {
                continue;
            }
            results.add(new FifoAllocationResult(candidate, allocated, seq++));
            remain -= allocated;
        }
        return results;
    }

    public record ReturnCandidate(
        String taskNo,
        Long taskItemId,
        String skuCode,
        String sizeCode,
        String productName,
        String sampleType,
        String sourceStoreName,
        int remainingQty,
        LocalDateTime borrowedAt
    ) {
    }

    public record FifoAllocationResult(
        ReturnCandidate candidate,
        int allocatedQty,
        int allocationSeq
    ) {
    }
}
