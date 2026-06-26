# 直播借样实现架构图与落地方案

## 1. 目标与范围

本文档给出直播借样一期 MVP 的可落地实现架构，覆盖以下范围：

- 钉钉助手入口
- Spring Boot 3 / Java 17 后端服务
- MyBatis + MySQL 数据持久化
- 借样申请、任务流转、通用归还、物流补录核心闭环
- 本地开发到生产部署的迁移边界

当前代码实现已按本文档的核心链路落地，并完成本地 MySQL 烟测验证。

## 2. 总体系统架构图

```mermaid
flowchart TB
    A[钉钉群 / 钉钉工作台] --> B[钉钉助手机器人]
    B --> C[钉钉 H5 表单页]
    B --> D[消息卡片 / 待办提醒]
    C --> E[直播借样后端 API]
    D --> E

    subgraph APP[直播借样应用服务]
        E --> F[借样域服务]
        E --> G[归还域服务]
        E --> H[管理域服务]
        F --> I[状态机 / FIFO 分配 / 规则校验]
        G --> I
        H --> I
    end

    subgraph DATA[数据与集成]
        I --> J[(MySQL 8.0)]
        I --> K[(Redis, 二期建议)]
        I --> L[任务调度 / MQ, 二期建议]
        I --> M[GMS 接口]
        I --> N[物流接口]
        I --> O[唯一码 / 质检系统]
        I --> P[组织与权限中心]
    end

    J --> Q[运营后台 / BI 报表]
```

## 3. 后端分层实现图

```mermaid
flowchart LR
    A[interfaces/http] --> B[application/service]
    B --> C[domain/model]
    B --> D[domain/service]
    B --> E[application/store.SampleDataStore]
    E --> F[infrastructure/persistence/MySQL]
    F --> G[MyBatis Mapper]
    G --> H[(MySQL)]

    D --> D1[TaskStateMachine]
    D --> D2[FifoReturnAllocationService]

    F --> F1[BorrowApplicationEntity]
    F --> F2[SampleTaskEntity]
    F --> F3[ReturnBatchEntity]
```

## 4. 核心数据架构图

```mermaid
erDiagram
    sample_apply ||--o{ sample_apply_item : contains
    sample_apply ||--o{ sample_task : generates
    sample_task ||--o{ sample_task_item : contains
    return_batch ||--o{ return_batch_item : aggregates
    return_batch ||--o{ return_allocation : allocates
    sample_task ||--o{ return_allocation : matched_by
    sample_task_item ||--o{ return_allocation : matched_item
    sample_task ||--o{ qc_record : checked_by
    sample_task ||--o{ task_exception : may_raise
    sample_task ||--o{ gms_bill_relation : syncs
    return_batch ||--o{ express_record : sends
    sample_task ||--o{ operation_log : audited
    return_batch ||--o{ operation_log : audited

    sample_apply {
        bigint id PK
        varchar apply_no UK
        varchar applicant_emp_id
        varchar virtual_store_code
        varchar delivery_type
        varchar audit_status
    }

    sample_task {
        bigint id PK
        varchar task_no UK
        varchar borrow_no
        varchar apply_no
        varchar task_status
        varchar return_status
        varchar current_return_batch_no
        varchar logistics_no
    }

    sample_task_item {
        bigint id PK
        bigint task_id FK
        varchar sku_code
        varchar size_code
        int apply_qty
        int borrowing_qty
        int returned_apply_qty
    }

    return_batch {
        bigint id PK
        varchar return_batch_no UK
        varchar virtual_store_code
        varchar return_method
        varchar status
        varchar logistics_no
    }

    return_allocation {
        bigint id PK
        bigint return_batch_id FK
        bigint task_id FK
        bigint task_item_id FK
        int allocated_qty
        int allocation_seq
    }
```

## 5. 借样主流程时序图

```mermaid
sequenceDiagram
    participant U as 借样人
    participant DT as 钉钉助手/H5
    participant API as Live Borrow Sample API
    participant RULE as 审核/状态机/规则
    participant DB as MySQL
    participant GMS as GMS

    U->>DT: 发起借样申请
    DT->>API: POST /api/v1/borrow/applications
    API->>RULE: 校验虚店、物流方式、商品数量
    RULE->>DB: 生成 applyNo / borrowNo / taskNo
    API->>DB: 保存 sample_apply / sample_apply_item
    API->>DB: 保存 sample_task / sample_task_item
    API-->>DT: 返回申请号、借样号、任务号
    API->>GMS: 二期接入调拨建单
    GMS-->>API: 返回调拨单号/状态
    API->>DB: 回写任务状态与物流信息
    API-->>U: 卡片通知 / 待办提醒
```

## 6. 通用归还主流程时序图

```mermaid
sequenceDiagram
    participant U as 借样人
    participant DT as 钉钉H5
    participant API as Live Borrow Sample API
    participant FIFO as FIFO归还分配服务
    participant DB as MySQL
    participant GMS as GMS/仓配

    U->>DT: 选择虚店并发起还样
    DT->>API: GET /api/v1/returns/aggregations
    API->>DB: 查询借样中任务与可还数量
    API-->>DT: 返回按 SKU+尺码 聚合结果

    U->>DT: 提交归还批次
    DT->>API: POST /api/v1/returns/batches
    API->>FIFO: 按 borrowedAt 先进先出分配
    FIFO-->>API: 返回 allocation 结果
    API->>DB: 保存 return_batch / return_batch_item / return_allocation
    API->>DB: 更新 sample_task.return_status 与 returned_apply_qty
    API-->>DT: 返回 returnBatchNo

    U->>DT: 补录物流单号
    DT->>API: POST /api/v1/returns/batches/{no}/logistics
    API->>DB: 更新 return_batch.status / logistics_no
    API->>DB: 更新 sample_task.return_status / logistics_no
    API->>GMS: 二期接入归还单回写
    API-->>U: 返回批次详情
```

## 7. 生产部署架构图

```mermaid
flowchart TB
    subgraph Client[访问层]
        A1[钉钉机器人]
        A2[钉钉 H5]
        A3[运营后台]
    end

    subgraph Gateway[接入层]
        B1[Nginx / API Gateway]
    end

    subgraph AppCluster[应用层]
        C1[borrow-sample-api-01]
        C2[borrow-sample-api-02]
        C3[XXL-Job / Scheduler]
    end

    subgraph Middleware[中间件层]
        D1[(MySQL 主库)]
        D2[(MySQL 只读库)]
        D3[(Redis)]
        D4[(MQ)]
    end

    subgraph External[外部系统]
        E1[GMS]
        E2[物流平台]
        E3[钉钉开放平台]
        E4[主数据/组织权限]
        E5[唯一码/质检]
    end

    A1 --> B1
    A2 --> B1
    A3 --> B1
    B1 --> C1
    B1 --> C2
    C1 --> D1
    C1 --> D2
    C1 --> D3
    C1 --> D4
    C2 --> D1
    C2 --> D2
    C2 --> D3
    C2 --> D4
    C3 --> D4
    C1 --> E1
    C1 --> E2
    C1 --> E3
    C1 --> E4
    C1 --> E5
    C2 --> E1
    C2 --> E2
```

## 8. 模块职责拆分

| 模块 | 责任 | 当前状态 |
| --- | --- | --- |
| `interfaces/http` | REST API、参数校验、统一返回体 | 已落地 |
| `application/service` | 用例编排、事务边界、响应组装 | 已落地 |
| `domain/model` | 任务、归还批次、申请单等领域状态 | 已落地 |
| `domain/service` | 状态机、FIFO 分配规则 | 已落地 |
| `application/store` | 存储抽象，隔离业务与持久化 | 已落地 |
| `infrastructure/persistence` | MyBatis Mapper、Entity、MySQL 读写 | 已落地 |
| `integration/gms` | 调拨建单、状态回写 | 待接入 |
| `integration/logistics` | 物流单查询、签收回执 | 待接入 |
| `integration/dingtalk` | 用户身份、卡片回调、待办通知 | 待接入 |
| `job/async` | 超时扫描、补偿、重试、回调消费 | 二期建议 |

## 9. 当前实现与生产版差异

### 已完成

- Spring Boot 3 / Java 17 项目骨架
- MyBatis + MySQL 持久化
- 本地 `local` profile 数据源配置
- 幂等可重复执行的 schema / seed 脚本
- 借样创建、任务查询、聚合归还、归还批次创建、物流补录
- 本地真实烟测

### 待补齐

- 钉钉机器人事件回调
- 钉钉 H5 鉴权与用户态透传
- GMS 建单、收货、回填物流、调拨状态同步
- 物流平台回调与定时对账
- 操作日志、幂等表、集成调用日志的正式写入
- 统一认证、权限模型、审计追踪
- Redis 缓存、消息队列、定时补偿任务

## 10. 生产落地建议

### 配置切换

- 本地阶段继续使用 `SPRING_PROFILES_ACTIVE=local`
- 生产环境切 `prod` profile，不改代码，只改环境变量
- 数据源、钉钉密钥、GMS 凭证、物流密钥全部走环境变量或配置中心

### 数据库建议

- 当前 `biz_sequence` 可支撑单库单应用编号生成
- 生产若多实例部署，建议升级为：
  - Redis 原子序列
  - Leaf / segment-id
  - 或数据库号段预分配服务

### 事务与一致性建议

- 申请落库、任务落库、归还批次落库保持单库本地事务
- 调 GMS / 物流接口改为 outbox + MQ 异步补偿
- 关键接口增加幂等键和操作日志

### 部署建议

- API 服务至少双实例
- MySQL 主从分离
- Redis 做缓存、幂等、短期状态机锁
- MQ 承担外部回调、补偿、通知
- 接口网关做鉴权、限流、签名校验

## 11. 本地验证结果

本地已完成以下验证：

- `mvn test` 通过
- 本地 MySQL 权限修复完成，`ycf / ycf012!` 可直接连接
- `sql/mvp_schema.sql` 可执行
- `sql/seed_data.sql` 可重复执行
- 应用已用 `local` profile 成功启动
- 已验证接口：
  - `GET /api/v1/health`
  - `GET /api/v1/borrow/tasks`
  - `GET /api/v1/returns/virtual-stores`
  - `POST /api/v1/borrow/applications`
  - `POST /api/v1/returns/batches`
  - `POST /api/v1/returns/batches/{returnBatchNo}/logistics`

## 12. 结论

当前仓库已经从“方案文档 + 骨架”推进到“本地 MySQL 可运行的 MVP 后端”。  
下一步如果继续推进到可上线版本，优先级应为：

1. 接入钉钉身份与回调
2. 接入 GMS 和物流平台
3. 补齐异步补偿、日志审计、权限与告警
4. 完成运营后台与生产部署流水线
