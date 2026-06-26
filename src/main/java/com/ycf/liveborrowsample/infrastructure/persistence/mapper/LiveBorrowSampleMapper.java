package com.ycf.liveborrowsample.infrastructure.persistence.mapper;

import com.ycf.liveborrowsample.infrastructure.persistence.entity.BorrowApplicationEntity;
import com.ycf.liveborrowsample.infrastructure.persistence.entity.BorrowApplicationItemEntity;
import com.ycf.liveborrowsample.infrastructure.persistence.entity.ReturnAllocationEntity;
import com.ycf.liveborrowsample.infrastructure.persistence.entity.ReturnBatchEntity;
import com.ycf.liveborrowsample.infrastructure.persistence.entity.ReturnBatchItemEntity;
import com.ycf.liveborrowsample.infrastructure.persistence.entity.SampleTaskEntity;
import com.ycf.liveborrowsample.infrastructure.persistence.entity.SampleTaskItemEntity;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface LiveBorrowSampleMapper {

    @Select("SELECT id FROM sample_apply WHERE apply_no = #{applyNo} LIMIT 1")
    Long findApplyIdByApplyNo(String applyNo);

    @Select("""
        SELECT
            id,
            apply_no,
            applicant_emp_id,
            applicant_name,
            virtual_store_code,
            virtual_store_name,
            delivery_type,
            pickup_store_code,
            pickup_store_name,
            receiver_name,
            receiver_mobile,
            receiver_province,
            receiver_city,
            receiver_district,
            receiver_address,
            audit_status,
            source_channel,
            remark
        FROM sample_apply
        WHERE apply_no = #{applyNo}
        LIMIT 1
        """)
    BorrowApplicationEntity findApplyByApplyNo(String applyNo);

    @Insert("""
        INSERT INTO sample_apply (
            apply_no,
            applicant_emp_id,
            applicant_name,
            virtual_store_code,
            virtual_store_name,
            delivery_type,
            pickup_store_code,
            pickup_store_name,
            receiver_name,
            receiver_mobile,
            receiver_province,
            receiver_city,
            receiver_district,
            receiver_address,
            audit_status,
            source_channel,
            remark
        ) VALUES (
            #{applyNo},
            #{applicantEmpId},
            #{applicantName},
            #{virtualStoreCode},
            #{virtualStoreName},
            #{deliveryType},
            #{pickupStoreCode},
            #{pickupStoreName},
            #{receiverName},
            #{receiverMobile},
            #{receiverProvince},
            #{receiverCity},
            #{receiverDistrict},
            #{receiverAddress},
            #{auditStatus},
            #{sourceChannel},
            #{remark}
        )
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertApply(BorrowApplicationEntity entity);

    @Update("""
        UPDATE sample_apply
        SET applicant_emp_id = #{applicantEmpId},
            applicant_name = #{applicantName},
            virtual_store_code = #{virtualStoreCode},
            virtual_store_name = #{virtualStoreName},
            delivery_type = #{deliveryType},
            pickup_store_code = #{pickupStoreCode},
            pickup_store_name = #{pickupStoreName},
            receiver_name = #{receiverName},
            receiver_mobile = #{receiverMobile},
            receiver_province = #{receiverProvince},
            receiver_city = #{receiverCity},
            receiver_district = #{receiverDistrict},
            receiver_address = #{receiverAddress},
            audit_status = #{auditStatus},
            source_channel = #{sourceChannel},
            remark = #{remark}
        WHERE id = #{id}
        """)
    int updateApply(BorrowApplicationEntity entity);

    @Insert({
        "<script>",
        "INSERT INTO sample_apply_item (",
        "    apply_id, line_no, sku_code, size_code, product_name, apply_qty, approved_qty",
        ") VALUES",
        "<foreach collection='items' item='item' separator=','>",
        "(",
        "    #{item.applyId}, #{item.lineNo}, #{item.skuCode}, #{item.sizeCode},",
        "    #{item.productName}, #{item.applyQty}, #{item.approvedQty}",
        ")",
        "</foreach>",
        "ON DUPLICATE KEY UPDATE",
        "    sku_code = VALUES(sku_code),",
        "    size_code = VALUES(size_code),",
        "    product_name = VALUES(product_name),",
        "    apply_qty = VALUES(apply_qty),",
        "    approved_qty = VALUES(approved_qty)",
        "</script>"
    })
    int upsertApplyItems(@Param("items") List<BorrowApplicationItemEntity> items);

    @Select("""
        SELECT
            apply_id,
            line_no,
            sku_code,
            size_code,
            product_name,
            apply_qty,
            approved_qty
        FROM sample_apply_item
        WHERE apply_id = #{applyId}
        ORDER BY line_no
        """)
    List<BorrowApplicationItemEntity> listApplyItems(Long applyId);

    @Select("""
        SELECT
            id,
            task_no,
            borrow_no,
            apply_no,
            applicant_emp_id,
            applicant_name,
            virtual_store_code,
            virtual_store_name,
            source_store_name,
            delivery_type,
            task_status,
            delivery_status,
            pickup_status,
            return_status,
            exception_status,
            current_return_batch_no,
            logistics_no,
            borrowed_at,
            expected_return_at
        FROM sample_task
        WHERE task_no = #{taskNo}
        LIMIT 1
        """)
    SampleTaskEntity findTaskByTaskNo(String taskNo);

    @Select("""
        SELECT
            id,
            task_no,
            borrow_no,
            apply_no,
            applicant_emp_id,
            applicant_name,
            virtual_store_code,
            virtual_store_name,
            source_store_name,
            delivery_type,
            task_status,
            delivery_status,
            pickup_status,
            return_status,
            exception_status,
            current_return_batch_no,
            logistics_no,
            borrowed_at,
            expected_return_at
        FROM sample_task
        ORDER BY borrowed_at DESC, id DESC
        """)
    List<SampleTaskEntity> listTasks();

    @Select("""
        SELECT
            id,
            task_no,
            borrow_no,
            apply_no,
            applicant_emp_id,
            applicant_name,
            virtual_store_code,
            virtual_store_name,
            source_store_name,
            delivery_type,
            task_status,
            delivery_status,
            pickup_status,
            return_status,
            exception_status,
            current_return_batch_no,
            logistics_no,
            borrowed_at,
            expected_return_at
        FROM sample_task
        WHERE virtual_store_code = #{virtualStoreCode}
        ORDER BY borrowed_at DESC, id DESC
        """)
    List<SampleTaskEntity> listTasksByVirtualStore(String virtualStoreCode);

    @Insert("""
        INSERT INTO sample_task (
            id,
            task_no,
            borrow_no,
            apply_no,
            applicant_emp_id,
            applicant_name,
            virtual_store_code,
            virtual_store_name,
            source_store_name,
            delivery_type,
            task_status,
            delivery_status,
            pickup_status,
            return_status,
            exception_status,
            current_return_batch_no,
            logistics_no,
            logistics_mode,
            borrowed_at,
            expected_return_at
        ) VALUES (
            #{id},
            #{taskNo},
            #{borrowNo},
            #{applyNo},
            #{applicantEmpId},
            #{applicantName},
            #{virtualStoreCode},
            #{virtualStoreName},
            #{sourceStoreName},
            #{deliveryType},
            #{taskStatus},
            #{deliveryStatus},
            #{pickupStatus},
            #{returnStatus},
            #{exceptionStatus},
            #{currentReturnBatchNo},
            #{logisticsNo},
            CASE WHEN #{deliveryType} = 'PICKUP' THEN 2 ELSE 1 END,
            #{borrowedAt},
            #{expectedReturnAt}
        )
        """)
    int insertTask(SampleTaskEntity entity);

    @Update("""
        UPDATE sample_task
        SET borrow_no = #{borrowNo},
            apply_no = #{applyNo},
            applicant_emp_id = #{applicantEmpId},
            applicant_name = #{applicantName},
            virtual_store_code = #{virtualStoreCode},
            virtual_store_name = #{virtualStoreName},
            source_store_name = #{sourceStoreName},
            delivery_type = #{deliveryType},
            task_status = #{taskStatus},
            delivery_status = #{deliveryStatus},
            pickup_status = #{pickupStatus},
            return_status = #{returnStatus},
            exception_status = #{exceptionStatus},
            current_return_batch_no = #{currentReturnBatchNo},
            logistics_no = #{logisticsNo},
            logistics_mode = CASE WHEN #{deliveryType} = 'PICKUP' THEN 2 ELSE 1 END,
            borrowed_at = #{borrowedAt},
            expected_return_at = #{expectedReturnAt}
        WHERE id = #{id}
        """)
    int updateTask(SampleTaskEntity entity);

    @Select({
        "<script>",
        "SELECT",
        "    id, task_id, line_no, sku_code, size_code, product_name, inventory_grade,",
        "    apply_qty, approved_qty, shipped_qty, received_qty, picked_qty, borrowing_qty,",
        "    returned_apply_qty, returned_received_qty",
        "FROM sample_task_item",
        "WHERE task_id IN",
        "<foreach collection='taskIds' item='taskId' open='(' separator=',' close=')'>",
        "#{taskId}",
        "</foreach>",
        "ORDER BY task_id, line_no",
        "</script>"
    })
    List<SampleTaskItemEntity> listTaskItemsByTaskIds(@Param("taskIds") List<Long> taskIds);

    @Insert({
        "<script>",
        "INSERT INTO sample_task_item (",
        "    id, task_id, line_no, sku_code, size_code, product_name, inventory_grade,",
        "    apply_qty, approved_qty, shipped_qty, received_qty, picked_qty, borrowing_qty,",
        "    returned_apply_qty, returned_received_qty",
        ") VALUES",
        "<foreach collection='items' item='item' separator=','>",
        "(",
        "    #{item.id}, #{item.taskId}, #{item.lineNo}, #{item.skuCode}, #{item.sizeCode},",
        "    #{item.productName}, #{item.inventoryGrade}, #{item.applyQty}, #{item.approvedQty}, #{item.shippedQty},",
        "    #{item.receivedQty}, #{item.pickedQty}, #{item.borrowingQty},",
        "    #{item.returnedApplyQty}, #{item.returnedReceivedQty}",
        ")",
        "</foreach>",
        "ON DUPLICATE KEY UPDATE",
        "    sku_code = VALUES(sku_code),",
        "    size_code = VALUES(size_code),",
        "    product_name = VALUES(product_name),",
        "    inventory_grade = VALUES(inventory_grade),",
        "    apply_qty = VALUES(apply_qty),",
        "    approved_qty = VALUES(approved_qty),",
        "    shipped_qty = VALUES(shipped_qty),",
        "    received_qty = VALUES(received_qty),",
        "    picked_qty = VALUES(picked_qty),",
        "    borrowing_qty = VALUES(borrowing_qty),",
        "    returned_apply_qty = VALUES(returned_apply_qty),",
        "    returned_received_qty = VALUES(returned_received_qty)",
        "</script>"
    })
    int upsertTaskItems(@Param("items") List<SampleTaskItemEntity> items);

    @Select("""
        SELECT
            id,
            return_batch_no,
            creator_emp_id,
            creator_name,
            virtual_store_code,
            virtual_store_name,
            source_type,
            sample_filter_type,
            return_method,
            status,
            logistics_company_name,
            logistics_no,
            remark
        FROM return_batch
        WHERE return_batch_no = #{returnBatchNo}
        LIMIT 1
        """)
    ReturnBatchEntity findReturnBatchByNo(String returnBatchNo);

    @Insert("""
        INSERT INTO return_batch (
            return_batch_no,
            creator_emp_id,
            creator_name,
            virtual_store_code,
            virtual_store_name,
            source_type,
            sample_filter_type,
            return_method,
            status,
            logistics_company_name,
            logistics_no,
            remark
        ) VALUES (
            #{returnBatchNo},
            #{creatorEmpId},
            #{creatorName},
            #{virtualStoreCode},
            #{virtualStoreName},
            #{sourceType},
            #{sampleFilterType},
            #{returnMethod},
            #{status},
            #{logisticsCompanyName},
            #{logisticsNo},
            #{remark}
        )
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertReturnBatch(ReturnBatchEntity entity);

    @Update("""
        UPDATE return_batch
        SET creator_emp_id = #{creatorEmpId},
            creator_name = #{creatorName},
            virtual_store_code = #{virtualStoreCode},
            virtual_store_name = #{virtualStoreName},
            source_type = #{sourceType},
            sample_filter_type = #{sampleFilterType},
            return_method = #{returnMethod},
            status = #{status},
            logistics_company_name = #{logisticsCompanyName},
            logistics_no = #{logisticsNo},
            remark = #{remark}
        WHERE id = #{id}
        """)
    int updateReturnBatch(ReturnBatchEntity entity);

    @Delete("DELETE FROM return_batch_item WHERE return_batch_id = #{returnBatchId}")
    int deleteReturnBatchItems(Long returnBatchId);

    @Select("""
        SELECT
            return_batch_id,
            line_no,
            sku_code,
            size_code,
            product_name,
            sample_type,
            source_store_name,
            available_return_qty,
            apply_return_qty
        FROM return_batch_item
        WHERE return_batch_id = #{returnBatchId}
        ORDER BY line_no
        """)
    List<ReturnBatchItemEntity> listReturnBatchItems(Long returnBatchId);

    @Insert({
        "<script>",
        "INSERT INTO return_batch_item (",
        "    return_batch_id, line_no, sku_code, size_code, product_name, sample_type,",
        "    source_store_name, available_return_qty, apply_return_qty",
        ") VALUES",
        "<foreach collection='items' item='item' separator=','>",
        "(",
        "    #{item.returnBatchId}, #{item.lineNo}, #{item.skuCode}, #{item.sizeCode},",
        "    #{item.productName}, #{item.sampleType}, #{item.sourceStoreName},",
        "    #{item.availableReturnQty}, #{item.applyReturnQty}",
        ")",
        "</foreach>",
        "</script>"
    })
    int insertReturnBatchItems(@Param("items") List<ReturnBatchItemEntity> items);

    @Delete("DELETE FROM return_allocation WHERE return_batch_id = #{returnBatchId}")
    int deleteReturnAllocations(Long returnBatchId);

    @Select("""
        SELECT
            return_batch_id,
            task_id,
            task_item_id,
            task_no,
            sku_code,
            size_code,
            allocated_qty,
            return_method,
            allocation_seq
        FROM return_allocation
        WHERE return_batch_id = #{returnBatchId}
        ORDER BY allocation_seq
        """)
    List<ReturnAllocationEntity> listReturnAllocations(Long returnBatchId);

    @Insert({
        "<script>",
        "INSERT INTO return_allocation (",
        "    return_batch_id, task_id, task_item_id, task_no, sku_code, size_code,",
        "    allocated_qty, return_method, allocation_seq",
        ") VALUES",
        "<foreach collection='items' item='item' separator=','>",
        "(",
        "    #{item.returnBatchId}, #{item.taskId}, #{item.taskItemId}, #{item.taskNo},",
        "    #{item.skuCode}, #{item.sizeCode}, #{item.allocatedQty}, #{item.returnMethod},",
        "    #{item.allocationSeq}",
        ")",
        "</foreach>",
        "</script>"
    })
    int insertReturnAllocations(@Param("items") List<ReturnAllocationEntity> items);

    @Select("SELECT id FROM sample_task WHERE task_no = #{taskNo} LIMIT 1")
    Long findTaskIdByTaskNo(String taskNo);

    @Select("""
        SELECT virtual_store_code, virtual_store_name
        FROM virtual_store_config
        WHERE enabled = 1
        ORDER BY sort_no, virtual_store_code
        """)
    List<java.util.Map<String, String>> listVirtualStores();
}
