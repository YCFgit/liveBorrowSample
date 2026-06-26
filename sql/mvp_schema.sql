-- 直播借样一期 MVP 数据库结构
-- 目标数据库：MySQL 8.0
-- 字符集：utf8mb4

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS biz_sequence (
  sequence_key VARCHAR(64) NOT NULL COMMENT '序列键',
  current_value BIGINT UNSIGNED NOT NULL COMMENT '当前值',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (sequence_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业务编号序列表';

CREATE TABLE IF NOT EXISTS virtual_store_config (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  virtual_store_code VARCHAR(32) NOT NULL COMMENT '虚店编码',
  virtual_store_name VARCHAR(64) NOT NULL COMMENT '虚店名称',
  enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  sort_no INT NOT NULL DEFAULT 0 COMMENT '排序号',
  remark VARCHAR(256) DEFAULT NULL COMMENT '备注',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_virtual_store_code (virtual_store_code),
  KEY idx_virtual_store_enabled_sort (enabled, sort_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='直播借样虚店配置表';

CREATE TABLE IF NOT EXISTS sample_apply (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  apply_no VARCHAR(32) NOT NULL COMMENT '申请单号',
  applicant_emp_id VARCHAR(32) NOT NULL COMMENT '申请人工号',
  applicant_name VARCHAR(64) NOT NULL COMMENT '申请人姓名',
  dept_code VARCHAR(32) DEFAULT NULL COMMENT '申请人部门编码',
  dept_name VARCHAR(64) DEFAULT NULL COMMENT '申请人部门名称',
  virtual_store_code VARCHAR(32) NOT NULL COMMENT '借样虚店编码',
  virtual_store_name VARCHAR(64) NOT NULL COMMENT '借样虚店名称',
  delivery_type VARCHAR(16) NOT NULL COMMENT 'EXPRESS/PICKUP',
  pickup_store_code VARCHAR(32) DEFAULT NULL COMMENT '自提门店编码',
  pickup_store_name VARCHAR(64) DEFAULT NULL COMMENT '自提门店名称',
  receiver_name VARCHAR(64) DEFAULT NULL COMMENT '收货人',
  receiver_mobile VARCHAR(32) DEFAULT NULL COMMENT '收货手机号',
  receiver_province VARCHAR(32) DEFAULT NULL COMMENT '收货省',
  receiver_city VARCHAR(32) DEFAULT NULL COMMENT '收货市',
  receiver_district VARCHAR(32) DEFAULT NULL COMMENT '收货区',
  receiver_address VARCHAR(256) DEFAULT NULL COMMENT '收货地址',
  audit_status VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED',
  audit_reason VARCHAR(512) DEFAULT NULL COMMENT '审核原因',
  source_channel VARCHAR(16) NOT NULL DEFAULT 'DINGTALK' COMMENT '申请来源',
  remark VARCHAR(512) DEFAULT NULL COMMENT '备注',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_at DATETIME DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_apply_no (apply_no),
  KEY idx_apply_emp_status (applicant_emp_id, audit_status),
  KEY idx_apply_virtual_store (virtual_store_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='借样申请主表';

CREATE TABLE IF NOT EXISTS sample_apply_item (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  apply_id BIGINT UNSIGNED NOT NULL COMMENT '申请单主键',
  line_no INT NOT NULL COMMENT '行号',
  spu_code VARCHAR(32) DEFAULT NULL COMMENT 'SPU 编码',
  sku_code VARCHAR(32) NOT NULL COMMENT 'SKU 编码',
  size_code VARCHAR(16) NOT NULL COMMENT '尺码',
  product_name VARCHAR(128) DEFAULT NULL COMMENT '商品名称',
  color_name VARCHAR(64) DEFAULT NULL COMMENT '颜色',
  apply_qty INT NOT NULL COMMENT '申请数量',
  approved_qty INT DEFAULT NULL COMMENT '审核通过数量',
  can_use_inventory_qty INT DEFAULT NULL COMMENT '审批时可用库存',
  source_sku_code VARCHAR(32) DEFAULT NULL COMMENT '替代 SKU',
  source_size_code VARCHAR(16) DEFAULT NULL COMMENT '替代尺码',
  is_blacklisted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否黑名单商品',
  remark VARCHAR(256) DEFAULT NULL COMMENT '备注',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_apply_line (apply_id, line_no),
  KEY idx_apply_item_sku (sku_code, size_code),
  CONSTRAINT fk_apply_item_apply FOREIGN KEY (apply_id) REFERENCES sample_apply (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='借样申请明细表';

CREATE TABLE IF NOT EXISTS sourcing_plan (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  apply_no VARCHAR(32) NOT NULL COMMENT '申请单号',
  task_no VARCHAR(32) DEFAULT NULL COMMENT '任务单号',
  plan_no VARCHAR(32) NOT NULL COMMENT '寻源方案号',
  priority_no INT NOT NULL COMMENT '优先级',
  sku_code VARCHAR(32) NOT NULL COMMENT 'SKU 编码',
  size_code VARCHAR(16) NOT NULL COMMENT '尺码',
  request_qty INT NOT NULL COMMENT '需求数量',
  source_store_code VARCHAR(32) NOT NULL COMMENT '发货门店/仓编码',
  source_store_name VARCHAR(64) NOT NULL COMMENT '发货门店/仓名称',
  source_store_type VARCHAR(16) NOT NULL COMMENT 'STORE/WAREHOUSE',
  available_qty INT NOT NULL COMMENT '可用库存',
  inventory_grade VARCHAR(8) DEFAULT NULL COMMENT '库存品相 A/B/C',
  estimated_freight DECIMAL(10,2) DEFAULT NULL COMMENT '预估物流成本',
  split_order_count INT DEFAULT NULL COMMENT '拆单数',
  selected_flag TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否选中',
  plan_payload JSON DEFAULT NULL COMMENT '寻源原始结果',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_plan_no_priority (plan_no, priority_no),
  KEY idx_plan_apply (apply_no),
  KEY idx_plan_task (task_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='寻源方案明细';

CREATE TABLE IF NOT EXISTS sample_task (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  task_no VARCHAR(32) NOT NULL COMMENT '任务单号',
  borrow_no VARCHAR(32) NOT NULL COMMENT '借样主单号',
  apply_no VARCHAR(32) NOT NULL COMMENT '申请单号',
  applicant_emp_id VARCHAR(32) NOT NULL COMMENT '申请人工号',
  applicant_name VARCHAR(64) NOT NULL COMMENT '申请人姓名',
  virtual_store_code VARCHAR(32) NOT NULL COMMENT '借样虚店编码',
  virtual_store_name VARCHAR(64) NOT NULL COMMENT '借样虚店名称',
  source_store_code VARCHAR(32) DEFAULT NULL COMMENT '发货门店/仓编码',
  source_store_name VARCHAR(64) DEFAULT NULL COMMENT '发货门店/仓名称',
  dest_store_code VARCHAR(32) DEFAULT NULL COMMENT '收货门店/仓编码',
  dest_store_name VARCHAR(64) DEFAULT NULL COMMENT '收货门店/仓名称',
  delivery_type VARCHAR(16) NOT NULL COMMENT 'EXPRESS/PICKUP',
  task_status VARCHAR(32) NOT NULL COMMENT '任务主状态',
  delivery_status VARCHAR(32) NOT NULL DEFAULT 'NONE' COMMENT '发货状态',
  pickup_status VARCHAR(32) NOT NULL DEFAULT 'NOT_APPLICABLE' COMMENT '自提状态',
  return_status VARCHAR(32) NOT NULL DEFAULT 'NONE' COMMENT '归还状态',
  exception_status VARCHAR(32) NOT NULL DEFAULT 'NONE' COMMENT '异常状态',
  current_return_batch_no VARCHAR(32) DEFAULT NULL COMMENT '当前归还批次号',
  logistics_no VARCHAR(64) DEFAULT NULL COMMENT '物流单号',
  gms_out_bill_no VARCHAR(64) DEFAULT NULL COMMENT 'GMS 借出调拨单号',
  gms_return_bill_no VARCHAR(64) DEFAULT NULL COMMENT 'GMS 归还调拨单号',
  logistics_mode TINYINT DEFAULT NULL COMMENT '1 快递 2 自提',
  overdue_days INT NOT NULL DEFAULT 0 COMMENT '逾期天数',
  borrowed_at DATETIME DEFAULT NULL COMMENT '借出开始时间',
  expected_return_at DATETIME DEFAULT NULL COMMENT '预计归还时间',
  completed_at DATETIME DEFAULT NULL COMMENT '完成时间',
  version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  remark VARCHAR(512) DEFAULT NULL COMMENT '备注',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_task_no (task_no),
  UNIQUE KEY uk_borrow_task (borrow_no, task_no),
  KEY idx_task_apply (apply_no),
  KEY idx_task_applicant_status (applicant_emp_id, task_status),
  KEY idx_task_virtual_store_status (virtual_store_code, task_status),
  KEY idx_task_return_batch (current_return_batch_no),
  KEY idx_task_gms_out_bill (gms_out_bill_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='借样任务主表';

SET @sample_task_logistics_no_sql = (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'sample_task'
        AND COLUMN_NAME = 'logistics_no'
    ),
    'SELECT 1',
    'ALTER TABLE sample_task ADD COLUMN logistics_no VARCHAR(64) DEFAULT NULL COMMENT ''物流单号'' AFTER current_return_batch_no'
  )
);
PREPARE sample_task_logistics_no_stmt FROM @sample_task_logistics_no_sql;
EXECUTE sample_task_logistics_no_stmt;
DEALLOCATE PREPARE sample_task_logistics_no_stmt;

CREATE TABLE IF NOT EXISTS sample_task_item (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  task_id BIGINT UNSIGNED NOT NULL COMMENT '任务主键',
  line_no INT NOT NULL COMMENT '行号',
  spu_code VARCHAR(32) DEFAULT NULL COMMENT 'SPU 编码',
  sku_code VARCHAR(32) NOT NULL COMMENT 'SKU 编码',
  size_code VARCHAR(16) NOT NULL COMMENT '尺码',
  product_name VARCHAR(128) DEFAULT NULL COMMENT '商品名称',
  color_name VARCHAR(64) DEFAULT NULL COMMENT '颜色',
  inventory_grade VARCHAR(8) DEFAULT NULL COMMENT '借出品相',
  apply_qty INT NOT NULL COMMENT '申请数量',
  approved_qty INT NOT NULL DEFAULT 0 COMMENT '审批数量',
  shipped_qty INT NOT NULL DEFAULT 0 COMMENT '已发数量',
  received_qty INT NOT NULL DEFAULT 0 COMMENT '已收数量',
  picked_qty INT NOT NULL DEFAULT 0 COMMENT '已提数量',
  borrowing_qty INT NOT NULL DEFAULT 0 COMMENT '借样中数量',
  returned_apply_qty INT NOT NULL DEFAULT 0 COMMENT '已发起归还数量',
  returned_received_qty INT NOT NULL DEFAULT 0 COMMENT '已收回数量',
  qc_pass_qty INT NOT NULL DEFAULT 0 COMMENT '质检通过数量',
  qc_abnormal_qty INT NOT NULL DEFAULT 0 COMMENT '质检异常数量',
  latest_return_method VARCHAR(16) DEFAULT NULL COMMENT '最新归还方式 EXPRESS/IN_PERSON',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_task_line (task_id, line_no),
  KEY idx_task_item_sku (sku_code, size_code),
  KEY idx_task_item_borrowing (task_id, borrowing_qty),
  CONSTRAINT fk_task_item_task FOREIGN KEY (task_id) REFERENCES sample_task (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='借样任务明细表';

CREATE TABLE IF NOT EXISTS return_batch (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  return_batch_no VARCHAR(32) NOT NULL COMMENT '归还批次号',
  creator_emp_id VARCHAR(32) NOT NULL COMMENT '创建人工号',
  creator_name VARCHAR(64) DEFAULT NULL COMMENT '创建人姓名',
  virtual_store_code VARCHAR(32) NOT NULL COMMENT '虚店编码',
  virtual_store_name VARCHAR(64) DEFAULT NULL COMMENT '虚店名称',
  source_type VARCHAR(16) NOT NULL COMMENT 'TASK_DETAIL/GENERAL_RETURN',
  sample_filter_type VARCHAR(16) NOT NULL DEFAULT 'ALL' COMMENT 'ALL/PICKUP_ONLY',
  return_method VARCHAR(16) NOT NULL COMMENT 'EXPRESS/IN_PERSON',
  status VARCHAR(32) NOT NULL COMMENT '批次状态',
  logistics_company_code VARCHAR(32) DEFAULT NULL COMMENT '物流公司编码',
  logistics_company_name VARCHAR(64) DEFAULT NULL COMMENT '物流公司名称',
  logistics_no VARCHAR(64) DEFAULT NULL COMMENT '物流单号',
  gms_return_bill_created TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已建 GMS 归还单',
  remark VARCHAR(512) DEFAULT NULL COMMENT '备注',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_return_batch_no (return_batch_no),
  KEY idx_return_batch_store_status (virtual_store_code, status),
  KEY idx_return_batch_logistics_no (logistics_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='归还批次主表';

CREATE TABLE IF NOT EXISTS return_batch_item (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  return_batch_id BIGINT UNSIGNED NOT NULL COMMENT '归还批次主键',
  line_no INT NOT NULL COMMENT '行号',
  sku_code VARCHAR(32) NOT NULL COMMENT 'SKU 编码',
  size_code VARCHAR(16) NOT NULL COMMENT '尺码',
  product_name VARCHAR(128) DEFAULT NULL COMMENT '商品名称',
  sample_type VARCHAR(16) NOT NULL COMMENT 'EXPRESS/PICKUP',
  source_store_name VARCHAR(64) DEFAULT NULL COMMENT '来源门店名，自提样品展示用',
  available_return_qty INT NOT NULL COMMENT '提交时可还数量',
  apply_return_qty INT NOT NULL COMMENT '本次归还数量',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_return_batch_line (return_batch_id, line_no),
  KEY idx_return_batch_item_sku (sku_code, size_code),
  CONSTRAINT fk_return_batch_item_batch FOREIGN KEY (return_batch_id) REFERENCES return_batch (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='归还批次商品汇总表';

CREATE TABLE IF NOT EXISTS return_allocation (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  return_batch_id BIGINT UNSIGNED NOT NULL COMMENT '归还批次主键',
  return_batch_item_id BIGINT UNSIGNED DEFAULT NULL COMMENT '归还批次商品主键',
  task_id BIGINT UNSIGNED NOT NULL COMMENT '任务主键',
  task_item_id BIGINT UNSIGNED NOT NULL COMMENT '任务明细主键',
  task_no VARCHAR(32) NOT NULL COMMENT '任务单号',
  sku_code VARCHAR(32) NOT NULL COMMENT 'SKU 编码',
  size_code VARCHAR(16) NOT NULL COMMENT '尺码',
  allocated_qty INT NOT NULL COMMENT '分配归还数量',
  return_method VARCHAR(16) NOT NULL COMMENT 'EXPRESS/IN_PERSON',
  allocation_seq INT NOT NULL COMMENT 'FIFO 序号',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_return_alloc_seq (return_batch_id, allocation_seq),
  KEY idx_return_alloc_task_item (task_item_id),
  KEY idx_return_alloc_task (task_id),
  CONSTRAINT fk_return_alloc_batch FOREIGN KEY (return_batch_id) REFERENCES return_batch (id),
  CONSTRAINT fk_return_alloc_task FOREIGN KEY (task_id) REFERENCES sample_task (id),
  CONSTRAINT fk_return_alloc_task_item FOREIGN KEY (task_item_id) REFERENCES sample_task_item (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='归还批次到任务明细的 FIFO 分配表';

CREATE TABLE IF NOT EXISTS gms_bill_relation (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  biz_type VARCHAR(16) NOT NULL COMMENT 'OUTBOUND/RETURN',
  biz_no VARCHAR(32) NOT NULL COMMENT '业务单号，任务单号或归还批次号',
  task_no VARCHAR(32) DEFAULT NULL COMMENT '任务单号',
  gms_bill_no VARCHAR(64) NOT NULL COMMENT 'GMS 单号',
  ref_bill_no VARCHAR(64) DEFAULT NULL COMMENT '外部关联单号',
  bill_status VARCHAR(32) DEFAULT NULL COMMENT 'GMS 单据状态',
  sync_status VARCHAR(32) NOT NULL DEFAULT 'INIT' COMMENT 'INIT/SUCCESS/FAILED',
  last_sync_at DATETIME DEFAULT NULL COMMENT '最近同步时间',
  sync_payload JSON DEFAULT NULL COMMENT '同步原始内容',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_gms_bill_no (gms_bill_no),
  KEY idx_gms_biz (biz_type, biz_no),
  KEY idx_gms_task_no (task_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='GMS 单据关系表';

CREATE TABLE IF NOT EXISTS express_record (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  biz_type VARCHAR(16) NOT NULL COMMENT 'OUTBOUND/RETURN',
  biz_no VARCHAR(32) NOT NULL COMMENT '任务单号或归还批次号',
  task_no VARCHAR(32) DEFAULT NULL COMMENT '任务单号',
  return_batch_no VARCHAR(32) DEFAULT NULL COMMENT '归还批次号',
  company_code VARCHAR(32) DEFAULT NULL COMMENT '物流公司编码',
  company_name VARCHAR(64) DEFAULT NULL COMMENT '物流公司名称',
  logistics_no VARCHAR(64) NOT NULL COMMENT '物流单号',
  logistics_status VARCHAR(32) NOT NULL DEFAULT 'INIT' COMMENT 'INIT/SHIPPED/SIGNED/FAILED',
  signed_at DATETIME DEFAULT NULL COMMENT '签收时间',
  source_type VARCHAR(16) NOT NULL DEFAULT 'MANUAL' COMMENT 'MANUAL/SYSTEM/LIXUN',
  status_payload JSON DEFAULT NULL COMMENT '物流原始信息',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_logistics_no_biz (biz_type, biz_no, logistics_no),
  KEY idx_express_logistics_no (logistics_no),
  KEY idx_express_task_no (task_no),
  KEY idx_express_return_batch_no (return_batch_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物流记录表';

CREATE TABLE IF NOT EXISTS qc_record (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  task_id BIGINT UNSIGNED NOT NULL COMMENT '任务主键',
  task_item_id BIGINT UNSIGNED NOT NULL COMMENT '任务明细主键',
  return_batch_no VARCHAR(32) DEFAULT NULL COMMENT '归还批次号',
  unique_code VARCHAR(64) NOT NULL COMMENT '唯一码',
  borrow_grade VARCHAR(8) DEFAULT NULL COMMENT '借出品相',
  return_grade VARCHAR(8) DEFAULT NULL COMMENT '归还品相',
  qc_result VARCHAR(16) NOT NULL COMMENT 'PASS/DEGRADED/PENDING',
  qc_note VARCHAR(512) DEFAULT NULL COMMENT '质检备注',
  qc_at DATETIME DEFAULT NULL COMMENT '质检时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_qc_unique_code (unique_code),
  KEY idx_qc_task_item (task_item_id),
  KEY idx_qc_return_batch (return_batch_no),
  CONSTRAINT fk_qc_task FOREIGN KEY (task_id) REFERENCES sample_task (id),
  CONSTRAINT fk_qc_task_item FOREIGN KEY (task_item_id) REFERENCES sample_task_item (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='唯一码质检记录表';

CREATE TABLE IF NOT EXISTS task_exception (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  task_no VARCHAR(32) NOT NULL COMMENT '任务单号',
  return_batch_no VARCHAR(32) DEFAULT NULL COMMENT '归还批次号',
  exception_type VARCHAR(32) NOT NULL COMMENT '异常类型',
  exception_status VARCHAR(16) NOT NULL DEFAULT 'OPEN' COMMENT 'OPEN/RESOLVED/CLOSED',
  exception_level VARCHAR(16) NOT NULL DEFAULT 'NORMAL' COMMENT 'LOW/NORMAL/HIGH',
  owner_emp_id VARCHAR(32) DEFAULT NULL COMMENT '责任人工号',
  owner_name VARCHAR(64) DEFAULT NULL COMMENT '责任人',
  title VARCHAR(128) NOT NULL COMMENT '异常标题',
  detail VARCHAR(1024) DEFAULT NULL COMMENT '异常详情',
  resolved_at DATETIME DEFAULT NULL COMMENT '解决时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_exception_task_status (task_no, exception_status),
  KEY idx_exception_owner_status (owner_emp_id, exception_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='任务异常表';

CREATE TABLE IF NOT EXISTS operation_log (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  biz_type VARCHAR(32) NOT NULL COMMENT '业务类型',
  biz_no VARCHAR(32) NOT NULL COMMENT '业务单号',
  operator_type VARCHAR(16) NOT NULL COMMENT 'USER/SYSTEM/ADMIN/JOB',
  operator_id VARCHAR(32) DEFAULT NULL COMMENT '操作人 ID',
  operator_name VARCHAR(64) DEFAULT NULL COMMENT '操作人',
  action VARCHAR(64) NOT NULL COMMENT '操作动作',
  before_json JSON DEFAULT NULL COMMENT '变更前',
  after_json JSON DEFAULT NULL COMMENT '变更后',
  extra_json JSON DEFAULT NULL COMMENT '扩展内容',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_operation_biz (biz_type, biz_no),
  KEY idx_operation_operator (operator_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='操作日志表';

CREATE TABLE IF NOT EXISTS integration_call_log (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  trace_id VARCHAR(64) NOT NULL COMMENT '链路追踪 ID',
  system_code VARCHAR(32) NOT NULL COMMENT '外部系统编码',
  interface_name VARCHAR(128) NOT NULL COMMENT '接口名称',
  biz_type VARCHAR(32) DEFAULT NULL COMMENT '业务类型',
  biz_no VARCHAR(32) DEFAULT NULL COMMENT '业务单号',
  request_no VARCHAR(64) DEFAULT NULL COMMENT '请求流水号',
  request_payload JSON DEFAULT NULL COMMENT '请求报文',
  response_payload JSON DEFAULT NULL COMMENT '响应报文',
  result_code VARCHAR(32) DEFAULT NULL COMMENT '结果码',
  result_message VARCHAR(512) DEFAULT NULL COMMENT '结果描述',
  call_status VARCHAR(16) NOT NULL COMMENT 'SUCCESS/FAILED/TIMEOUT',
  called_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '调用时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_integration_biz (biz_type, biz_no),
  KEY idx_integration_trace (trace_id),
  KEY idx_integration_system_time (system_code, called_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='外部接口调用日志';

CREATE TABLE IF NOT EXISTS idempotent_request (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  idempotent_key VARCHAR(128) NOT NULL COMMENT '幂等键',
  biz_type VARCHAR(32) NOT NULL COMMENT '业务类型',
  biz_no VARCHAR(32) DEFAULT NULL COMMENT '业务单号',
  request_hash VARCHAR(64) NOT NULL COMMENT '请求哈希',
  response_json JSON DEFAULT NULL COMMENT '历史响应',
  expired_at DATETIME DEFAULT NULL COMMENT '过期时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_idempotent_key (idempotent_key),
  KEY idx_idempotent_biz (biz_type, biz_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='幂等请求记录表';

SET FOREIGN_KEY_CHECKS = 1;
