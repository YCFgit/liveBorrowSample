# 直播借样系统完整解决方案

## 1. 建设目标

基于《直播借样一期MVP方案 V1.2》，建设一套以钉钉助手为统一入口、以业务中台服务为核心编排层、以 GMS 为库存与调拨执行系统的数据闭环平台，覆盖以下能力：

- 借样申请、审核、自动寻源、GMS 借出建单
- 门店发货、自提核销、快递收货确认、差异留痕
- 单任务归还、通用批量归还、FIFO 自动分配、归还批次管理
- 物流单号补录、GMS 状态同步、质检状态同步
- 唯一码品相巡检、自动销账、异常待处理
- 钉钉消息提醒、任务中心、操作留痕、审计追溯、基础报表

系统目标不是替代 GMS，而是补齐“钉钉交互层 + 业务编排层 + 业务追踪层 + 数据分析层”。

## 2. 总体架构

建议采用四层架构：

### 2.1 交互层

- 钉钉助手机器人
- 钉钉 H5 / 微应用页面
- 管理后台（运营、货控、客服、仓库、管理员）

### 2.2 业务应用层

- 借样申请服务
- 审核与规则引擎
- 库存寻源服务
- 借出调拨编排服务
- 收货与自提核销服务
- 归还编排服务
- FIFO 分配服务
- 质检销账服务
- 通知中心
- 报表服务

### 2.3 集成层

- GMS 适配器
- 丽讯物流适配器
- 钉钉开放平台适配器
- 用户/组织/门店主数据适配器
- 唯一码流水查询适配器

### 2.4 数据层

- MySQL：交易主库
- Redis：缓存、分布式锁、幂等键、短时会话
- MQ：异步事件总线
- 对象存储：质检图片、附件、操作凭证
- OLAP/BI：运营报表与审计分析

## 3. 推荐技术栈

### 3.1 后端

- `Java 17 + Spring Boot 3`
- `MyBatis Plus` 或 `JPA`
- `MySQL 8.0`
- `Redis`
- `RocketMQ` 或 `Kafka`
- `XXL-Job` / `Quartz` 负责定时轮询任务
- `OpenFeign` 或内部 HTTP / Dubbo SDK 对接 GMS、钉钉和物流

选择 Java 的原因：

- 适合企业内中后台、接口编排、复杂状态机和异步任务
- 与 Dubbo / GMS 类企业系统对接更自然
- 便于做稳定性治理、审计与权限体系

### 3.2 前端

- 钉钉 H5：`React + Vite + Ant Design Mobile`
- 管理后台：`React + Ant Design Pro`

### 3.3 DevOps

- 容器化部署：Docker + K8s
- 环境：dev / test / uat / prod
- 日志：ELK / Loki
- 监控：Prometheus + Grafana
- 链路追踪：SkyWalking / OpenTelemetry

## 4. 核心产品形态

## 4.1 钉钉助手

钉钉助手承担四类职责：

- 对话入口：用户 `@借样助手`
- 可选通知卡片：任务摘要、状态提醒、跳转到 H5 详情页
- 消息通知：审核通过、发货、签收提醒、待处理提醒、销账完成
- 快捷查询：输入借样单号/归还批次号查询进度

建议采用：

- H5 页面负责一期正式业务闭环
- 机器人消息、互动卡片、待办负责“提醒 + 跳转”
- H5 页面负责“复杂表单 + 列表 + 详情 + 批量操作”

原因是：

- 钉钉卡片适合短链路交互
- 借样申请、通用归还、FIFO 明细确认不适合完全在卡片内完成

## 4.2 管理后台

为货控、运营、仓库、管理员提供：

- 借样任务查询
- GMS 对账
- 异常任务处理
- 规则配置
- 黑名单维护
- 借样额度配置
- 任务重试与补偿
- 报表看板

## 5. 业务域拆分

建议按领域拆成以下模块：

### 5.1 用户与主数据域

- 员工
- 组织
- 虚店
- 实体门店
- 仓库
- 商品与 SKU
- 黑名单规则

### 5.2 借样域

- 借样申请单
- 借样任务单
- 借样明细
- 审核结果
- 寻源方案
- 借出调拨关系

### 5.3 履约域

- 发货记录
- 物流记录
- 收货确认
- 自提记录

### 5.4 归还域

- 归还批次
- 归还分配
- 归还明细
- 归还方式
- 归还物流
- 门店收货

### 5.5 质检与销账域

- 唯一码档案
- 质检结果
- 品相变化
- 销账结果
- 异常待处理

### 5.6 审计与通知域

- 操作日志
- 事件日志
- 钉钉消息记录
- 外部接口调用日志

## 6. 核心流程设计

## 6.1 借样申请与审核

流程：

1. 用户在钉钉点击“发起借样”
2. 选择虚店、商品、数量、物流方式、收货信息
3. 后端做同步校验
4. 通过后生成借样申请单 `sample_apply`
5. 规则引擎执行审核
6. 审核通过后生成借样任务 `sample_task`
7. 调用寻源服务
8. 调用 GMS 建借出调拨单
9. 回写任务状态并通知用户

审核规则建议拆三层：

- 基础资格：是否有虚店、是否离职、是否在白名单组织
- 风险约束：是否有逾期、是否超过额度、是否命中黑名单
- 履约约束：是否有可用库存、是否允许降级尺码替代

规则引擎不要硬编码在 Controller。建议：

- 规则配置表 + 规则执行器
- 支持按组织、虚店、直播间等级配置阈值

## 6.2 自动寻源与 GMS 建单

寻源流程：

1. 获取申请明细 SKU + 尺码 + 数量
2. 调用 GMS 可用库存接口，拉取候选库存点
3. 根据规则排序：
   - 物流成本最低
   - 拆单数最少
   - B 品库存优先
   - 距离/区域优先
4. 生成寻源方案
5. 以借样单号作为外部单号调用 GMS 建样品调拨单
6. 保存 GMS 单号映射

建议注意：

- 寻源结果必须持久化，不能只保留最终门店
- 后续审计需要知道“为什么分配到这个门店”

## 6.3 发货、自提、收货

### 快递场景

1. 轮询 GMS 调拨单状态
2. 获取发货状态和快递单号
3. 推送“已发货”通知
4. 丽讯查询签收状态
5. 用户在钉钉确认收货
6. 回写 GMS 收货完成

### 自提场景

1. 建单时设置 `logisticsMode=2`
2. 任务进入 `WAIT_PICKUP`
3. 用户到店后在钉钉点击“确认已自提”
4. 支持部分提货
5. 累计 `picked_qty`
6. 全部提完后任务进入 `BORROWING`

### 差异反馈

一期可只做轻量能力：

- 收货异常备注
- 上传图片
- 通知货控处理
- 不阻塞主流程，但生成异常单

## 6.4 归还流程

归还必须拆成“归还入口”和“归还执行”两个概念。

### 单任务归还

- 从任务详情发起
- 只作用于当前任务
- 系统生成单独 `return_batch`

### 通用归还

- 用户先选择虚店
- 后端聚合该虚店下全部“可归还数量”
- 页面以 `virtual_store + sku + size` 维度展示
- 用户填写归还数量
- 后端按 FIFO 把数量拆分到具体任务
- 生成一个 `return_batch`
- 一个批次可关联多个任务

### 快递归还

1. 发起归还
2. 创建归还批次
3. 为涉及任务生成还样调拨单
4. 状态为 `PENDING_LOGISTICS`
5. 用户填写物流单号
6. 系统批量回写同批次任务
7. 调用 GMS 回填物流
8. 轮询仓库签收

### 自行归还到店

1. 发起归还
2. 创建归还批次
3. 状态变为 `STORE_PENDING`
4. 门店确认收货
5. 转入质检

## 6.5 质检与销账

1. 还样单收货后进入 `QUALITY_CHECKING`
2. 定时任务轮询唯一码流水
3. 比对借出品相与归还品相
4. 若品相一致，自动销账完成
5. 若降级，转 `ABNORMAL_PENDING`
6. 发送通知给责任人

建议：

- 不要把“质检中”和“已销账”只挂在任务总状态上
- 应在任务明细或唯一码层保留结果，否则部分归还场景无法解释

## 7. 状态机设计

建议将状态拆成“任务主状态 + 子状态”，不要做一个超大枚举。

### 7.1 任务主状态 `task_status`

- `CREATED`
- `AUDITING`
- `REJECTED`
- `SOURCING`
- `TRANSFER_CREATING`
- `WAIT_SHIP`
- `WAIT_PICKUP`
- `IN_TRANSIT`
- `BORROWING`
- `RETURNING`
- `QUALITY_CHECKING`
- `COMPLETED`
- `ABNORMAL_PENDING`
- `CLOSED`

### 7.2 发货状态 `delivery_status`

- `NONE`
- `WAITING`
- `SHIPPED`
- `SIGNED`
- `RECEIVED`

### 7.3 自提状态 `pickup_status`

- `NOT_APPLICABLE`
- `WAIT_PICKUP`
- `PART_PICKED`
- `PICKED`

### 7.4 归还状态 `return_status`

- `NONE`
- `PENDING`
- `LOGISTICS_FILLED`
- `STORE_PENDING`
- `WAREHOUSE_RECEIVED`
- `QUALITY_CHECKING`
- `COMPLETED`

### 7.5 异常状态 `exception_status`

- `NONE`
- `RECEIVE_DIFF`
- `RETURN_DIFF`
- `QUALITY_DOWNGRADED`
- `GMS_SYNC_FAILED`
- `LOGISTICS_TIMEOUT`

## 8. 数据模型设计

文档里提出在任务表增加 `returnBatchNo`、`returnStatus`、`returnedQty`、`returnLogisticsNo`、`returnMethod`。这些字段可以保留，但不能作为最终核心模型。原因是：

- 一个任务可能发生多次部分归还
- 一个归还批次可关联多个任务
- 一个任务后续可能存在多个物流动作和多次状态推进

因此建议采用“主表 + 关系表 + 事件表”。

## 8.1 核心表

### `sample_apply`

借样申请主表

- `id`
- `apply_no`
- `applicant_emp_id`
- `virtual_store_code`
- `delivery_type`
- `pickup_store_code`
- `status`
- `remark`
- `created_at`

### `sample_apply_item`

- `id`
- `apply_id`
- `sku_code`
- `size_code`
- `apply_qty`
- `approved_qty`
- `source_sku_code`
- `source_size_code`

### `sample_task`

借样任务主表，一次借样申请可拆成多个任务

- `id`
- `task_no`
- `apply_id`
- `borrow_no`
- `applicant_emp_id`
- `virtual_store_code`
- `source_store_code`
- `dest_store_code`
- `delivery_type`
- `task_status`
- `delivery_status`
- `pickup_status`
- `return_status`
- `gms_out_bill_no`
- `gms_return_bill_no`
- `current_return_batch_no`
- `borrowed_at`
- `completed_at`
- `version`

### `sample_task_item`

- `id`
- `task_id`
- `sku_code`
- `size_code`
- `apply_qty`
- `shipped_qty`
- `received_qty`
- `picked_qty`
- `borrowing_qty`
- `returned_apply_qty`
- `returned_received_qty`
- `qc_pass_qty`
- `qc_abnormal_qty`

### `return_batch`

归还批次主表

- `id`
- `return_batch_no`
- `virtual_store_code`
- `creator_emp_id`
- `source_type`：`task_detail` / `general_return`
- `sample_filter_type`：`all` / `pickup_only`
- `return_method`
- `status`
- `logistics_company`
- `logistics_no`
- `created_at`

### `return_batch_item`

记录用户在聚合页面勾选的商品维度

- `id`
- `return_batch_id`
- `sku_code`
- `size_code`
- `apply_return_qty`

### `return_allocation`

FIFO 拆分结果，解决“批次到任务”的分配问题

- `id`
- `return_batch_id`
- `task_id`
- `task_item_id`
- `sku_code`
- `size_code`
- `allocated_qty`
- `return_method`
- `allocation_seq`

### `gms_bill_relation`

- `id`
- `biz_type`：`OUTBOUND` / `RETURN`
- `biz_no`
- `task_no`
- `gms_bill_no`
- `sync_status`
- `last_sync_at`

### `express_record`

- `id`
- `biz_type`
- `biz_no`
- `company_code`
- `logistics_no`
- `logistics_status`
- `signed_at`

### `qc_record`

- `id`
- `task_id`
- `task_item_id`
- `unique_code`
- `borrow_grade`
- `return_grade`
- `qc_result`
- `qc_at`

### `operation_log`

- `id`
- `biz_type`
- `biz_no`
- `operator_type`
- `operator_id`
- `action`
- `before_json`
- `after_json`
- `created_at`

## 8.2 索引建议

- `sample_task.uk_task_no`
- `sample_task.idx_applicant_status`
- `sample_task.idx_virtual_store_status`
- `sample_task.idx_borrow_no`
- `return_batch.uk_return_batch_no`
- `return_allocation.idx_task_item`
- `gms_bill_relation.idx_gms_bill_no`
- `express_record.idx_logistics_no`

## 9. 接口设计

建议按“内部业务接口”和“外部适配接口”分层。

## 9.1 钉钉前台接口

### 借样

- `POST /api/borrow/applications`
- `GET /api/borrow/applications/{applyNo}`
- `GET /api/borrow/tasks`
- `GET /api/borrow/tasks/{taskNo}`
- `POST /api/borrow/tasks/{taskNo}/confirm-receive`
- `POST /api/borrow/tasks/{taskNo}/confirm-pickup`

### 通用归还

- `GET /api/returns/virtual-stores`
- `GET /api/returns/aggregations?virtualStoreCode=&sampleType=`
- `POST /api/returns/batches`
- `GET /api/returns/batches/{returnBatchNo}`
- `POST /api/returns/batches/{returnBatchNo}/logistics`
- `POST /api/returns/tasks/{taskNo}/confirm-inperson-return`

### 查询

- `GET /api/my/tasks`
- `GET /api/my/notifications`
- `GET /api/common/enums`

## 9.2 管理后台接口

- `GET /admin/tasks`
- `GET /admin/tasks/{taskNo}`
- `POST /admin/tasks/{taskNo}/retry-gms`
- `POST /admin/tasks/{taskNo}/close-exception`
- `GET /admin/return-batches`
- `GET /admin/exceptions`
- `POST /admin/rules/borrow-limit`
- `POST /admin/rules/blacklist`

## 9.3 外部适配接口

封装到 adapter 层，业务层只依赖内部接口：

- `GmsInventoryAdapter.queryAvailableInventory`
- `GmsTransferAdapter.createBorrowTransfer`
- `GmsTransferAdapter.createReturnTransfer`
- `GmsTransferAdapter.queryBillStatus`
- `GmsTransferAdapter.fillLogisticsNo`
- `LogisticsAdapter.queryExpressStatus`
- `DingTalkAdapter.sendCard`
- `DingTalkAdapter.getUserInfo`
- `UniqueCodeAdapter.queryGradeFlow`

## 9.4 幂等与签名

必须做：

- 钉钉回调验签
- 外部接口请求流水号
- 关键写接口幂等键
- MQ 消费幂等
- 定时任务分布式锁

## 10. 关键算法设计

## 10.1 通用归还聚合

SQL / 视图逻辑按以下维度聚合：

- `virtual_store_code`
- `sku_code`
- `size_code`
- `sample_type`

可还数量计算：

`borrowing_qty - returned_apply_qty`

仅统计：

- 任务状态为 `BORROWING` 或 `RETURNING`
- 仍有剩余可还数量的明细

## 10.2 FIFO 分配

步骤：

1. 查出用户选择商品对应的所有候选任务明细
2. 按 `borrowed_at asc, task_no asc, task_item_id asc` 排序
3. 逐行扣减直到满足本次归还量
4. 写入 `return_allocation`
5. 更新任务明细的 `returned_apply_qty`

必须放在事务内，并对 `sample_task_item` 做行锁或版本号控制，防止并发归还超扣。

## 10.3 额度校验

统计口径建议：

- 当前借样中数量
- 待归还数量
- 待质检数量

不只统计“已借出未归还”，否则在归还途中会出现额度异常波动。

## 11. 异步任务与事件流

建议定义以下事件：

- `BorrowApplicationApproved`
- `BorrowTaskCreated`
- `BorrowTransferCreated`
- `BorrowTaskShipped`
- `BorrowTaskSigned`
- `BorrowTaskReceived`
- `PickupConfirmed`
- `ReturnBatchCreated`
- `ReturnLogisticsFilled`
- `ReturnWarehouseReceived`
- `QualityCheckCompleted`
- `TaskCompleted`
- `TaskExceptionRaised`

消费者任务：

- GMS 状态同步任务
- 物流状态同步任务
- 唯一码巡检任务
- 钉钉通知任务
- 失败补偿任务

## 12. 稳定性与补偿设计

这是该系统成败关键，必须从第一版就做。

### 12.1 关键补偿点

- GMS 建单成功但本地落库失败
- 本地状态已更新但钉钉通知失败
- 物流单号已填本地但 GMS 回写失败
- 仓库已收货但唯一码查询延迟
- MQ 重复消费

### 12.2 方案

- 所有外部调用都记 `integration_call_log`
- 建单接口采用“请求流水号 + 结果对账”
- GMS 调用失败进入 `RETRY_PENDING`
- 提供后台人工重试
- 每天做任务/GMS 对账任务

## 13. 权限与安全

### 13.1 权限模型

- 借样人：只能看自己有权限虚店的任务
- 货控：可看所辖组织/门店任务
- 仓库：可看待收货、待质检任务
- 管理员：全量查看、规则配置、补偿操作

### 13.2 安全要求

- 钉钉用户身份与内部员工主数据映射
- 敏感操作审计留痕
- 图片/附件访问权限控制
- 接口限流和防重放

## 14. MVP 范围建议

一期不建议一次做满所有理想能力。建议 MVP 只落以下闭环：

### 14.1 必做

- 钉钉发起借样
- 借样审核与资格校验
- 自动寻源
- GMS 借出建单
- 发货状态同步
- 自提确认
- 任务详情查询
- 单任务归还
- 通用归还
- FIFO 分配
- 快递物流补录
- GMS 还样建单
- GMS 状态同步
- 简化质检状态同步
- 销账完成通知

### 14.2 可延后

- 复杂差异反馈流转
- 自动呼叫回程快递
- 门店 POS 正式对接
- 赔付流程
- 智能推荐替代尺码
- BI 高级分析

## 15. 分阶段实施计划

## 第 0 阶段：前置准备，1 周

- 明确 GMS 接口契约
- 确认物流单号回填能力
- 整理钉钉企业内部应用权限
- 梳理员工、虚店、门店主数据来源

产出：

- 接口契约文档
- 枚举字典
- 主数据映射表

## 第 1 阶段：借样闭环，2 周

- 借样申请 H5
- 审核规则引擎
- 寻源服务
- GMS 借出建单
- 任务列表/详情
- 发货同步
- 自提确认

## 第 2 阶段：归还闭环，2 周

- 单任务归还
- 通用归还页面
- FIFO 分配
- 归还批次
- 物流补录
- GMS 还样建单
- 仓库收货同步

## 第 3 阶段：质检销账与后台运营，2 周

- 唯一码巡检
- 自动销账
- 异常任务池
- 管理后台
- 对账与补偿任务

## 第 4 阶段：压测与上线，1 周

- 联调
- UAT
- 灰度
- 上线回收与监控

总周期建议：`6-8 周`

## 16. 团队配置建议

- 产品经理 1
- 后端工程师 2
- 前端工程师 1
- 测试工程师 1
- 兼任运维/架构 0.5
- 业务接口人：货控、仓库、直播运营各 1

## 17. 测试策略

### 17.1 功能测试

- 借样快递流程
- 借样自提流程
- 单任务归还
- 通用归还
- 部分提货
- 部分归还
- 归还批次复用

### 17.2 异常测试

- GMS 超时
- GMS 重复回调
- 物流无单号
- 用户重复提交
- FIFO 并发归还
- 唯一码结果延迟

### 17.3 回归测试重点

- 自提与快递混合路径
- 一个批次关联多个任务
- 同一任务多次部分归还
- 任务状态与 GMS 状态一致性

## 18. 上线与运维

上线建议：

- 先灰度到 1-2 个直播间和少量虚店
- 保留人工兜底后台
- 每日输出对账清单
- 关键指标日报化

核心监控指标：

- 借样申请成功率
- GMS 建单成功率
- 发货同步延迟
- 归还批次生成成功率
- 物流回填成功率
- 自动销账成功率
- 异常任务积压量

## 19. 关键风险与规避建议

### 风险 1：任务表字段不够支撑通用归还

规避：

- 保留文档中的兼容字段
- 但正式实现必须增加 `return_batch` 和 `return_allocation`

### 风险 2：GMS 回填物流能力不稳定

规避：

- 接口前置打样
- 本地保存物流单并做重试队列
- 后台提供人工重推

### 风险 3：状态机混乱导致数据不一致

规避：

- 任务主状态和子状态分离
- 所有状态流转统一走 domain service
- 禁止各接口随意直接改库

### 风险 4：并发归还超扣

规避：

- FIFO 分配事务化
- 行锁或乐观锁
- 归还幂等键

### 风险 5：通知依赖外部平台不稳定

规避：

- 通知异步化
- 消息失败重试
- 任务详情页永远是最终事实来源

## 20. 最终建议

这套系统最合理的落地方式，不是“做一个简单机器人”，而是：

- 以钉钉助手作为统一入口
- 以一个独立的借样业务服务作为编排核心
- 以 MySQL + Redis + MQ 作为基础设施
- 以 GMS、物流、唯一码系统作为外部能力源
- 以“归还批次 + FIFO 分配 + 状态机 + 异步补偿”为系统设计核心

如果按这个方案实施，一期就可以形成真正可运营、可审计、可扩展的闭环，而不是只做一个表单收集工具。
