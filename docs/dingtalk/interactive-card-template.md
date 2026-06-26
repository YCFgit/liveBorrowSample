# 钉钉互动卡片模板设计 - 可选通知入口

本文档基于 [legacy-h5-demo-v1.5.html](../../examples/legacy-h5-demo-v1.5.html) 提炼一张适合钉钉卡片发送的业务通知卡片，而不是把整份 H5 原样搬进钉钉卡片。

互动卡片不是一期核心流程依赖。当前一期正式业务承载面是 H5 页面：

- `/dingtalk/borrow-assistant.html`

互动卡片只作为可选的“提醒 + 摘要 + 跳转入口”，用于把用户带回 H5 任务详情或归还页面。

## 1. 设计目标

用于在以下场景向借样人发送一张单聊卡片：

- 待收货提醒
- 借样中提醒
- 逾期待还提醒
- 待填写归还物流提醒

卡片只承载“摘要 + 跳转”。完整借样申请、通用归还、FIFO 明细确认、物流补录等流程继续在 H5 页面里处理。

## 2. 参考自 H5 的核心字段

从 H5 原型中抽取的高价值字段：

- `taskNo`：借样任务号
- `statusText`：任务状态
- `virtualShopName`：借入虚店
- `goodsSummary`：商品摘要，例如 `A001/38 x1, A002/39 x2`
- `receiverName`：收货人
- `receiverPhone`：手机号
- `receiverAddress`：收货地址
- `pickupStoreName`：自提门店
- `dueDate`：应归还日期
- `logisticsNo`：正向物流单号
- `returnLogisticsNo`：归还物流单号
- `actionText`：按钮文案
- `detailUrl`：跳转链接

## 3. 推荐卡片模板变量

建议在钉钉卡片平台把模板变量定义成下面这组，尽量贴近业务含义，不要过度缩写：

```text
title
statusText
taskNo
virtualShopName
goodsSummary
receiverInfo
pickupStoreName
dueDate
logisticsInfo
actionText
detailUrl
```

变量含义：

- `title`：卡片标题，例如“直播借样待处理”
- `statusText`：如“待收货”“借样中”“逾期待还”“待填写物流”
- `taskNo`：借样任务号
- `virtualShopName`：借样归属虚店
- `goodsSummary`：商品摘要
- `receiverInfo`：快递场景可展示“张三 138xxxx 上海市...”
- `pickupStoreName`：自提场景展示门店名
- `dueDate`：应归还日期
- `logisticsInfo`：如“顺丰 SF123456”
- `actionText`：按钮文案
- `detailUrl`：点击跳转到 H5 详情页

## 4. 卡片布局建议

### 4.1 头部区

- 标题：`{{title}}`
- 状态标签：`{{statusText}}`

### 4.2 信息区

- 任务号：`任务号：{{taskNo}}`
- 虚店：`虚店：{{virtualShopName}}`
- 商品：`商品：{{goodsSummary}}`
- 收货信息：`{{receiverInfo}}`
- 自提门店：`{{pickupStoreName}}`
- 应归还日：`{{dueDate}}`
- 物流信息：`{{logisticsInfo}}`

### 4.3 操作区

- 主按钮文案：`{{actionText}}`
- 跳转地址：`{{detailUrl}}`

## 5. 推荐拆成 4 个业务态

### 5.1 待收货卡片

- `title`：直播借样待收货
- `statusText`：待收货
- `actionText`：查看详情

### 5.2 借样中卡片

- `title`：直播借样进行中
- `statusText`：借样中
- `actionText`：查看任务

### 5.3 逾期待还卡片

- `title`：直播借样已逾期
- `statusText`：逾期待还
- `actionText`：立即还样

### 5.4 待填写物流卡片

- `title`：请填写归还物流
- `statusText`：待填写物流
- `actionText`：填写物流

## 6. 推荐发送 payload

下面这份 payload 适合作为当前联调的默认结构：

```json
{
  "outTrackId": "card-debug-20260625-001",
  "cardTemplateId": "2fec46db-af53-4730-b9c4-64a23fd2351e.schema",
  "openSpaceId": "dtv1.card//IM_ROBOT.当前用户userId",
  "cardData": {
    "cardParamMap": {
      "title": "直播借样待处理",
      "statusText": "待收货",
      "taskNo": "BT202606250001",
      "virtualShopName": "抖音直播间01",
      "goodsSummary": "A001/38 x1, A002/39 x2",
      "receiverInfo": "张三 13800001111 上海市徐汇区XX路1号",
      "pickupStoreName": "",
      "dueDate": "2026-06-30",
      "logisticsInfo": "顺丰 SF1234567890",
      "actionText": "查看详情",
      "detailUrl": "https://your-domain/dingtalk/borrow-assistant.html?corpId=$CORPID$"
    }
  },
  "imRobotOpenSpaceModel": {
    "supportForward": true
  },
  "imRobotOpenDeliverModel": {
    "spaceType": "IM_ROBOT"
  }
}
```

## 7. 实施建议

- 不要试图把整张任务列表页直接做成卡片。
- 生产上线可先不启用互动卡片，只保留 H5 业务页和待办/普通通知。
- 如果需要卡片，先做 1 张“借样任务提醒卡片”，打通模板、变量、发送链路。
- 后续再衍生“逾期待还卡片”“待填写物流卡片”等业务态。
- 若一个模板无法兼容所有状态，可拆成 2 到 4 张模板。

## 8. 当前项目状态

当前项目已完成：

- 卡片发送接口：`POST /api/v1/dingtalk/cards/create-and-deliver`
- H5 联调页中的“互动卡片测试”
- 联调 payload 示例

当前仍需你在钉钉卡片平台确认：

- 是否真的需要在生产启用互动卡片
- 生产卡片模板 ID
- 该模板的真实变量名是否与本文推荐字段一致
- 如不一致，需要把 `cardParamMap` 的 key 改成模板实际变量名
