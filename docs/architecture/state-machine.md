# 直播借样状态机与实现约束

## 1. 设计原则

- 主状态和子状态分离，避免一个大枚举覆盖所有语义。
- 所有状态变更只能通过 domain service 完成，禁止 controller/repository 直接改状态。
- 每次状态流转必须写 `operation_log`。
- 外部系统同步失败不直接丢弃，进入异常或重试状态。

## 2. 申请单状态 `audit_status`

- `PENDING`
- `APPROVED`
- `REJECTED`

流转：

- `PENDING -> APPROVED`
- `PENDING -> REJECTED`

说明：

- 申请单通过后生成或更新借样任务
- 被拒绝的申请单不可直接恢复，只能重新发起

## 3. 任务主状态 `task_status`

- `CREATED`
- `AUDITING`
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

## 4. 子状态

### `delivery_status`

- `NONE`
- `WAITING`
- `SHIPPED`
- `SIGNED`
- `RECEIVED`

### `pickup_status`

- `NOT_APPLICABLE`
- `WAIT_PICKUP`
- `PART_PICKED`
- `PICKED`

### `return_status`

- `NONE`
- `PENDING`
- `LOGISTICS_FILLED`
- `STORE_PENDING`
- `WAREHOUSE_RECEIVED`
- `QUALITY_CHECKING`
- `COMPLETED`

### `exception_status`

- `NONE`
- `RECEIVE_DIFF`
- `RETURN_DIFF`
- `QUALITY_DOWNGRADED`
- `GMS_SYNC_FAILED`
- `LOGISTICS_TIMEOUT`

## 5. 主流程状态流转

## 5.1 快递借样

1. `CREATED`
2. `AUDITING`
3. `SOURCING`
4. `TRANSFER_CREATING`
5. `WAIT_SHIP`
6. `IN_TRANSIT`
7. `BORROWING`
8. `RETURNING`
9. `QUALITY_CHECKING`
10. `COMPLETED`

对应子状态演进：

- `delivery_status`: `NONE -> WAITING -> SHIPPED -> SIGNED -> RECEIVED`
- `pickup_status`: 固定 `NOT_APPLICABLE`
- `return_status`: `NONE -> PENDING -> LOGISTICS_FILLED -> WAREHOUSE_RECEIVED -> QUALITY_CHECKING -> COMPLETED`

## 5.2 自提借样

1. `CREATED`
2. `AUDITING`
3. `SOURCING`
4. `TRANSFER_CREATING`
5. `WAIT_PICKUP`
6. `BORROWING`
7. `RETURNING`
8. `QUALITY_CHECKING`
9. `COMPLETED`

对应子状态演进：

- `pickup_status`: `WAIT_PICKUP -> PART_PICKED -> PICKED`
- `return_status`:
  - 快递归还：`NONE -> PENDING -> LOGISTICS_FILLED -> WAREHOUSE_RECEIVED -> QUALITY_CHECKING -> COMPLETED`
  - 到店归还：`NONE -> STORE_PENDING -> QUALITY_CHECKING -> COMPLETED`

## 6. 关键流转触发器

| 触发动作 | 前置条件 | 状态变化 |
| --- | --- | --- |
| 提交借样申请 | 参数校验通过 | `CREATED -> AUDITING` |
| 审核通过 | 审核规则通过 | `AUDITING -> SOURCING` |
| 寻源成功 | 选定库存点 | `SOURCING -> TRANSFER_CREATING` |
| GMS 借出单创建成功 | 拿到 `gms_out_bill_no` | 快递：`TRANSFER_CREATING -> WAIT_SHIP`；自提：`TRANSFER_CREATING -> WAIT_PICKUP` |
| GMS 发货 | 查询到已发货 | `WAIT_SHIP -> IN_TRANSIT` |
| 用户确认收货 | 快递签收或收到通知 | `IN_TRANSIT -> BORROWING` |
| 用户确认已自提 | 提货数量全部完成 | `WAIT_PICKUP -> BORROWING` |
| 发起归还 | 归还批次创建成功 | `BORROWING -> RETURNING` |
| 填写物流单号 | 快递归还 | `return_status: PENDING -> LOGISTICS_FILLED` |
| 门店确认收货 | 到店归还 | `return_status: STORE_PENDING -> QUALITY_CHECKING` |
| 仓库签收 | 快递归还收回 | `return_status: LOGISTICS_FILLED -> WAREHOUSE_RECEIVED -> QUALITY_CHECKING` |
| 质检通过 | 品相一致 | `QUALITY_CHECKING -> COMPLETED` |
| 品相降级 | A 降 B/C | `QUALITY_CHECKING -> ABNORMAL_PENDING` |

## 7. 数量字段约束

对 `sample_task_item` 执行以下不变量：

- `0 <= shipped_qty <= approved_qty`
- `0 <= received_qty <= shipped_qty`
- `0 <= picked_qty <= approved_qty`
- `0 <= borrowing_qty <= max(received_qty, picked_qty)`
- `0 <= returned_apply_qty <= borrowing_qty + returned_received_qty`
- `0 <= returned_received_qty <= returned_apply_qty`
- `qc_pass_qty + qc_abnormal_qty <= returned_received_qty`

说明：

- 快递场景 `borrowing_qty` 主要来源于 `received_qty`
- 自提场景 `borrowing_qty` 主要来源于 `picked_qty`
- 发起归还后先增加 `returned_apply_qty`
- 仓库收回后再增加 `returned_received_qty`

## 8. 并发控制约束

以下动作必须事务化：

- 借样申请提交
- 审核通过并建任务
- FIFO 归还分配
- 自提确认
- 收货确认
- 归还物流填写

以下动作必须使用乐观锁或行锁：

- 更新 `sample_task.version`
- 更新 `sample_task_item.returned_apply_qty`
- 更新 `sample_task_item.picked_qty`

推荐策略：

- 归还分配使用 `select ... for update`
- 外部接口调用放在事务提交后，通过事件驱动发起

## 9. 失败与补偿规则

## 9.1 GMS 建借出单失败

- 任务保留在 `TRANSFER_CREATING`
- `exception_status = GMS_SYNC_FAILED`
- 异步重试
- 超过阈值后进入异常池

## 9.2 GMS 归还建单失败

- 批次仍保留
- 不回滚用户提交的批次号
- 标记异常并允许后台重试

原因：

- 用户已经看到归还批次号，回滚会导致前端与操作事实不一致

## 9.3 物流回填失败

- `return_status` 暂不回退
- 写异常日志
- 继续重试 GMS 回填

## 9.4 唯一码结果延迟

- 任务停留在 `QUALITY_CHECKING`
- 定时轮询 7 天
- 超时后进入异常池人工处理

## 10. 实现约束

## 10.1 代码结构建议

- `application`: 用例编排
- `domain`: 聚合、实体、状态流转规则
- `infrastructure`: DB、外部接口适配器
- `interfaces`: Controller、DTO、VO

## 10.2 聚合边界建议

- `BorrowTaskAggregate`
- `ReturnBatchAggregate`
- `TaskItemAggregate`

不要把所有逻辑都堆进 `SampleTaskService`。

## 10.3 事件建议

- `BorrowTaskCreatedEvent`
- `BorrowTaskShippedEvent`
- `PickupConfirmedEvent`
- `ReturnBatchCreatedEvent`
- `ReturnLogisticsFilledEvent`
- `WarehouseReceivedEvent`
- `QualityCheckedEvent`

## 10.4 禁止事项

- 禁止前端直接控制任务状态值
- 禁止通过 SQL 脚本绕过状态校验修改线上状态
- 禁止在单接口里同时处理“建单 + 回调 + 通知 + 对账”四类动作

## 11. 建议的开发顺序

1. 先实现枚举、状态机和聚合根
2. 再实现借样申请与任务建模
3. 然后实现 GMS 适配层
4. 再实现通用归还和 FIFO
5. 最后补异步任务、质检和后台
