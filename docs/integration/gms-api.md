# GMS Integration API

This document records the GMS integration boundary used by Live Borrow Sample.
It is the project-side contract, not a replacement for the upstream GMS Apifox/Dubbo documentation.

## Source Documents

Upstream GMS documentation is currently referenced from the product plan:

- GMS Apifox space: <https://s.apifox.cn/893875f2-97ab-4d09-9973-a34bdba85544?pwd=mEUfIuYx>
- gms-api(DUBBO) 调配货库存调整通用接口
- gms-api(DUBBO) 召回业务自动化接口
- gms-api(DUBBO) 退货通知单接口

Before production integration, copy the confirmed method names, request fields,
response fields, enum values, and error codes from the upstream documents into this file.

## Current Project Contract

Code location:

- Port: `src/main/java/com/ycf/liveborrowsample/integration/gms/GmsClient.java`
- Mock adapter: `src/main/java/com/ycf/liveborrowsample/infrastructure/gms/MockGmsClient.java`
- Contract test: `src/test/java/com/ycf/liveborrowsample/infrastructure/gms/MockGmsClientTest.java`

The application must depend on `GmsClient`, not on a concrete HTTP/Dubbo SDK.
Production integration should add a separate adapter such as `HttpGmsClient` or `DubboGmsClient`.

## Required Capabilities

| Capability | Project method | Current status | Production note |
| --- | --- | --- | --- |
| 可用库存查询 | `queryAvailableInventory` | Mock only | Confirm SKU/size, warehouse/store, usable quantity and grade fields. |
| 借出调拨单创建 | `createTransferOrder(BORROW_OUT)` | Contract + mock | Confirm transfer type `41`, `refBillNo`, receiver info and logistics mode. |
| 归还调拨单创建 | `createTransferOrder(RETURN_IN)` | Contract + mock | Confirm how return order links to borrow task or borrow GMS bill. |
| 调拨单状态查询 | `getTransferOrder` | Contract + mock | Map upstream status to local task/return states. |
| 物流单号回填 | `fillLogistics` | Contract + mock | Upstream capability is still marked as pending confirmation in the product plan. |

## Transfer Order Types

The project uses two high-level transfer order types:

| Type | Meaning | Typical business number |
| --- | --- | --- |
| `BORROW_OUT` | 借出调拨单 | `taskNo`, for example `BT202606260001` |
| `RETURN_IN` | 归还调拨单 | `returnBatchNo`, for example `RT202606260001` |

Both are expected to map to the GMS sample transfer bill type currently described as `41`.
The exact upstream enum name and value must be confirmed before implementing the production adapter.

## Create Transfer Order Command

Project-side command:

```java
CreateTransferOrderCommand(
    TransferOrderType type,
    String bizNo,
    String fromOrgCode,
    String toOrgCode,
    List<Item> items
)
```

Item fields:

| Field | Meaning |
| --- | --- |
| `skuCode` | 商品编码 |
| `sizeCode` | 尺码 |
| `quantity` | 调拨数量 |

Production adapter must enrich this command with upstream-specific fields where required,
for example receiver name/mobile/address, `logisticsMode`, operator, remark and request number.
If those fields are mandatory upstream, extend this command deliberately and update tests.

## Transfer Order Response

Project-side response:

```java
GmsTransferOrder(
    String gmsOrderNo,
    TransferOrderType type,
    String bizNo,
    String fromOrgCode,
    String toOrgCode,
    String status,
    String logisticsCompanyName,
    String logisticsNo,
    List<CreateTransferOrderCommand.Item> items
)
```

Minimum production mapping:

| Project field | Upstream source |
| --- | --- |
| `gmsOrderNo` | GMS 调拨单号 |
| `bizNo` | External business number / `refBillNo` |
| `status` | GMS bill status, mapped to local states |
| `logisticsCompanyName` | GMS or logistics platform carrier name |
| `logisticsNo` | GMS or logistics platform tracking number |

## Local State Mapping

Initial mapping target:

| GMS event/status | Local effect |
| --- | --- |
| Borrow order created | task can move from transfer creating to wait ship or wait pickup |
| Borrow order shipped | task can move to `IN_TRANSIT` |
| Borrow order received/closed | task can move to `BORROWING` after user confirmation or system sync |
| Return order created | return batch remains pending logistics or store receipt |
| Return logistics filled | return batch moves to logistics filled |
| Return order received | return batch can move to warehouse received / quality checking |

Exact status codes must be copied from upstream GMS documentation before production.

## Idempotency And Audit

Every production GMS call must carry a business request number.
Recommended request number format:

```text
<bizNo>:<operation>:<attempt-or-date-token>
```

Required persistence:

- Save raw request and response to `integration_call_log`.
- Save GMS bill relation to `gms_bill_relation`.
- Repeated calls for the same business operation must not create duplicate GMS bills.
- Failed calls must be retryable from an admin operation or async compensation job.

## Open Questions

- What is the exact upstream method for sample transfer order creation?
- Is `logisticsMode=2` the confirmed value for self-pickup in every relevant GMS method?
- Which upstream field should hold the borrow task number or return batch number: `refBillNo`, another external bill number, or both?
- What is the confirmed interface for logistics number fill-back?
- Which status codes are returned by `queryNtBillStatus`, and which statuses are terminal?
- Does GMS support idempotency natively, or must this service enforce it completely?
