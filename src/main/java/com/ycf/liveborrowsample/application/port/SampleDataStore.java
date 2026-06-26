package com.ycf.liveborrowsample.application.port;

import com.ycf.liveborrowsample.domain.model.BorrowApplication;
import com.ycf.liveborrowsample.domain.model.ReturnBatch;
import com.ycf.liveborrowsample.domain.model.SampleTask;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface SampleDataStore {

    String nextApplyNo();

    String nextBorrowNo();

    String nextTaskNo();

    String nextReturnBatchNo();

    Long nextTaskId();

    Long nextTaskItemId();

    void saveApplication(BorrowApplication application);

    Optional<BorrowApplication> findApplicationByApplyNo(String applyNo);

    List<SampleTask> listTasks();

    Optional<SampleTask> findTask(String taskNo);

    void saveTask(SampleTask task);

    List<SampleTask> listTasksByVirtualStore(String virtualStoreCode);

    void saveBatch(ReturnBatch returnBatch);

    Optional<ReturnBatch> findBatch(String returnBatchNo);

    Map<String, String> getVirtualStores();

    String getVirtualStoreName(String code);
}
