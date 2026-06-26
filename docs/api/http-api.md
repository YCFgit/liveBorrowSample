# 直播借样 MVP 接口定义

## 1. 说明

本文档定义一期 MVP 的核心接口，供钉钉 H5、管理后台和后端服务联调使用。

- 接口前缀：`/api/v1`
- 鉴权方式：钉钉免登换取业务侧 token，后续通过 `Authorization: Bearer <token>` 调用
- 返回结构统一：

```json
{
  "code": "0",
  "message": "success",
  "requestId": "20260624xxxx",
  "data": {}
}
```

- 分页结构统一：

```json
{
  "list": [],
  "pageNo": 1,
  "pageSize": 20,
  "total": 120
}
```

## 2. 枚举约定

### `deliveryType`

- `EXPRESS`
- `PICKUP`

### `returnMethod`

- `EXPRESS`
- `IN_PERSON`

### `sampleFilterType`

- `ALL`
- `PICKUP_ONLY`

## 3. 借样端接口

## 3.1 发起借样申请

- `POST /api/v1/borrow/applications`

请求体：

```json
{
  "virtualStoreCode": "VS0001",
  "deliveryType": "EXPRESS",
  "pickupStoreCode": null,
  "receiver": {
    "name": "张三",
    "mobile": "13800000000",
    "province": "上海市",
    "city": "上海市",
    "district": "闵行区",
    "address": "xx路xx号"
  },
  "remark": "618 直播借样",
  "items": [
    {
      "skuCode": "SKU001",
      "sizeCode": "M",
      "applyQty": 2
    }
  ]
}
```

校验规则：

- `virtualStoreCode` 必填，且当前用户有权限
- `deliveryType=PICKUP` 时 `pickupStoreCode` 必填
- `items` 至少一条
- 每条 `applyQty > 0`
- 自动校验借样资格、逾期、借样上限、黑名单、库存可用性

返回体：

```json
{
  "applyNo": "BA202606240001",
  "auditStatus": "APPROVED",
  "borrowNo": "BR202606240001",
  "taskNos": [
    "BT202606240001"
  ]
}
```

## 3.2 查询我的借样任务列表

- `GET /api/v1/borrow/tasks?pageNo=1&pageSize=20&status=BORROWING`

返回字段：

- `taskNo`
- `borrowNo`
- `virtualStoreCode`
- `deliveryType`
- `taskStatus`
- `deliveryStatus`
- `pickupStatus`
- `returnStatus`
- `expectedReturnAt`
- `itemSummary`

## 3.3 查询借样任务详情

- `GET /api/v1/borrow/tasks/{taskNo}`

返回字段：

- 任务基础信息
- 收发货信息
- 明细列表
- GMS 单号
- 当前归还批次号
- 可执行动作列表
- 操作日志摘要

## 3.4 确认已收货

- `POST /api/v1/borrow/tasks/{taskNo}/receive-confirm`

请求体：

```json
{
  "logisticsNo": "SF123456789",
  "remark": "已收到"
}
```

约束：

- 仅快递任务可调用
- 任务必须处于 `IN_TRANSIT` 或 `SIGNED`
- 幂等键建议：`taskNo + logisticsNo + action`

## 3.5 确认已自提

- `POST /api/v1/borrow/tasks/{taskNo}/pickup-confirm`

请求体：

```json
{
  "items": [
    {
      "taskItemId": 1001,
      "confirmQty": 1
    }
  ],
  "remark": "已在门店领取"
}
```

约束：

- 仅 `deliveryType=PICKUP`
- `confirmQty <= approvedQty - pickedQty`
- 全部确认后任务进入 `BORROWING`
- 部分确认后任务保持 `WAIT_PICKUP`

## 4. 通用归还接口

## 4.1 查询当前用户可用虚店

- `GET /api/v1/returns/virtual-stores`

返回体：

```json
[
  {
    "virtualStoreCode": "VS0001",
    "virtualStoreName": "直播间虚店A"
  }
]
```

## 4.2 查询通用归还聚合清单

- `GET /api/v1/returns/aggregations?virtualStoreCode=VS0001&sampleFilterType=ALL`

返回体：

```json
{
  "virtualStoreCode": "VS0001",
  "sampleFilterType": "ALL",
  "rows": [
    {
      "skuCode": "SKU001",
      "sizeCode": "M",
      "productName": "连衣裙",
      "sampleType": "EXPRESS",
      "availableReturnQty": 3,
      "sourceStoreName": null,
      "taskRefs": [
        {
          "taskNo": "BT202606240001",
          "taskItemId": 1001,
          "borrowedAt": "2026-06-24 10:00:00",
          "remainingQty": 2
        }
      ]
    }
  ]
}
```

说明：

- 页面只展示聚合结果
- `taskRefs` 仅供调试或详情展开，不直接暴露给普通用户也可以

## 4.3 发起归还批次

- `POST /api/v1/returns/batches`

请求体：

```json
{
  "virtualStoreCode": "VS0001",
  "sourceType": "GENERAL_RETURN",
  "sampleFilterType": "PICKUP_ONLY",
  "returnMethod": "IN_PERSON",
  "remark": "本周直播还样",
  "items": [
    {
      "skuCode": "SKU001",
      "sizeCode": "M",
      "applyReturnQty": 2
    }
  ]
}
```

后端处理：

1. 校验虚店权限
2. 校验聚合结果仍然足够
3. 执行 FIFO 分配
4. 创建 `return_batch`
5. 创建 `return_batch_item`
6. 创建 `return_allocation`
7. 更新任务明细 `returned_apply_qty`
8. 生成还样调拨任务

返回体：

```json
{
  "returnBatchNo": "RT20260624000001",
  "status": "PENDING",
  "returnMethod": "IN_PERSON",
  "taskSummaries": [
    {
      "taskNo": "BT202606240001",
      "allocatedQty": 2
    }
  ]
}
```

## 4.4 查询归还批次详情

- `GET /api/v1/returns/batches/{returnBatchNo}`

返回字段：

- 批次基础信息
- 批次明细
- 任务分配明细
- 物流信息
- 批次状态
- 可执行动作

## 4.5 填写归还物流单号

- `POST /api/v1/returns/batches/{returnBatchNo}/logistics`

请求体：

```json
{
  "companyCode": "SF",
  "companyName": "顺丰",
  "logisticsNo": "SF123456789"
}
```

约束：

- 仅 `returnMethod=EXPRESS`
- 批次状态必须是 `PENDING`
- 同一批次只允许一个有效物流单号
- 成功后更新批次状态为 `LOGISTICS_FILLED`

## 4.6 自行归还后门店确认收货

- `POST /api/v1/returns/batches/{returnBatchNo}/store-receive-confirm`

请求体：

```json
{
  "operatorEmpId": "90001",
  "remark": "门店已收货"
}
```

说明：

- 一期可由后台或演示入口触发
- 成功后批次和关联任务进入 `QUALITY_CHECKING`

## 5. 管理后台接口

## 5.1 查询任务列表

- `GET /api/v1/admin/tasks?pageNo=1&pageSize=20&taskStatus=ABNORMAL_PENDING`

过滤条件建议：

- `taskNo`
- `borrowNo`
- `virtualStoreCode`
- `applicantEmpId`
- `taskStatus`
- `deliveryType`
- `returnStatus`
- `exceptionStatus`
- 时间范围

## 5.2 查询异常池

- `GET /api/v1/admin/exceptions?pageNo=1&pageSize=20&exceptionStatus=OPEN`

## 5.3 手工重试 GMS 同步

- `POST /api/v1/admin/tasks/{taskNo}/retry-gms`

请求体：

```json
{
  "bizType": "RETURN",
  "reason": "接口恢复后重试"
}
```

## 5.4 手工关闭异常

- `POST /api/v1/admin/exceptions/{id}/close`

## 6. 内部集成接口约束

## 6.1 GMS 适配器

详细接口契约见 [GMS integration API](../integration/gms-api.md)。

建议在服务内抽象如下接口：

- `createBorrowTransfer(BorrowTransferCommand command)`
- `createReturnTransfer(ReturnTransferCommand command)`
- `queryBillStatus(String gmsBillNo)`
- `fillLogisticsNo(GmsLogisticsCommand command)`
- `queryAvailableInventory(InventoryQuery query)`

要求：

- 每次请求都带业务流水号 `requestNo`
- 原始请求/响应必须落 `integration_call_log`
- 失败必须可重试

## 6.2 物流适配器

- `queryExpressStatus(String logisticsNo)`

要求：

- 支持丽讯查询
- 支持人工补录后继续追踪

## 6.3 钉钉适配器

- `sendCard(DingTalkCardCommand command)`
- `sendNotice(DingTalkNoticeCommand command)`
- `queryUser(String unionId)`

## 7. 错误码建议

### 业务错误码

- `BORROW_001` 无借样权限
- `BORROW_002` 存在逾期借样
- `BORROW_003` 已超过借样上限
- `BORROW_004` 商品命中黑名单
- `BORROW_005` 库存不足
- `RETURN_001` 可还数量不足
- `RETURN_002` 归还方式非法
- `RETURN_003` 当前批次不可填写物流
- `TASK_001` 当前状态不可执行此操作
- `TASK_002` 并发更新失败，请重试
- `GMS_001` GMS 建单失败
- `GMS_002` GMS 回填物流失败

## 8. 联调顺序建议

1. 借样申请 + 审核
2. GMS 借出建单
3. 任务查询
4. 自提确认 / 收货确认
5. 通用归还聚合
6. 归还批次创建 + FIFO
7. 物流补录
8. GMS 归还建单
9. 质检销账
