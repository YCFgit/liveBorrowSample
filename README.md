# Live Borrow Sample

直播借样一期 MVP：一个面向钉钉 H5 的借样、收货、自提、归还和 FIFO 归还分配示例系统。

## Features

- Spring Boot 3 + Java 17 后端
- MyBatis + MySQL 8 持久化
- 钉钉 H5 免登、JSAPI 签名、回调解密和通知接口骨架
- 借样申请、任务列表、任务详情、收货确认、自提确认
- 通用归还聚合、归还批次、FIFO 分配、物流回填
- 移动端 H5 业务页和钉钉联调页
- OpenAPI 草案、数据库脚本、示例请求、Docker 和 CI 配置

## Quick Start

### Prerequisites

- JDK 17+
- Maven 3.9+
- MySQL 8.0+

### Run With In-Memory Data

```bash
make run-memory
```

Open:

- Business H5: <http://localhost:8080/dingtalk/borrow-assistant.html>
- DingTalk debug page: <http://localhost:8080/dingtalk/index.html>
- Health: <http://localhost:8080/api/v1/health>

### Run With Local MySQL

```bash
mysql -uroot -p < sql/local_mysql_setup.sql
mysql -uycf -pycf012\! live_borrow_sample < sql/mvp_schema.sql
mysql -uycf -pycf012\! live_borrow_sample < sql/seed_data.sql
make run
```

Default local database settings:

| Env | Default |
| --- | --- |
| `DB_HOST` | `localhost` |
| `DB_PORT` | `3306` |
| `DB_NAME` | `live_borrow_sample` |
| `DB_USERNAME` | `ycf` |
| `DB_PASSWORD` | `ycf012!` |

## DingTalk Configuration

All DingTalk secrets must be provided through environment variables:

```bash
export DINGTALK_APP_ID=...
export DINGTALK_AGENT_ID=...
export DINGTALK_CLIENT_ID=...
export DINGTALK_CLIENT_SECRET=...
export DINGTALK_ROBOT_CODE=...
export DINGTALK_CORP_ID=...
export DINGTALK_CALLBACK_URL=...
export DINGTALK_CALLBACK_TOKEN=...
export DINGTALK_CALLBACK_AES_KEY=...
export DINGTALK_CALLBACK_OWNER_KEY=$DINGTALK_CLIENT_ID
```

## Common Commands

```bash
make test          # run unit tests
make integration-test  # run Docker-backed MySQL integration tests
make package       # build jar
make run           # run with local profile and MySQL
make run-memory    # run with memory profile
make smoke         # smoke test local HTTP endpoints
```

## Project Layout

```text
.
├── api/                 # OpenAPI contract
├── docs/                # Product, architecture, database, DingTalk, development docs
├── examples/            # HTTP examples and legacy H5 prototypes
├── scripts/             # Local automation scripts
├── sql/                 # MySQL schema and seed scripts
├── src/main/java/       # Spring Boot application source
├── src/main/resources/  # Spring config and static H5 pages
├── src/test/java/       # Unit tests
└── tests/               # Test strategy and future integration/e2e test assets
```

See [docs/project-structure.md](docs/project-structure.md) for the full directory responsibility map.

## API

- OpenAPI: [api/openapi.yaml](api/openapi.yaml)
- HTTP examples: [examples/http/live-borrow-sample.http](examples/http/live-borrow-sample.http)

Core endpoints:

- `GET /api/v1/health`
- `POST /api/v1/borrow/applications`
- `GET /api/v1/borrow/tasks`
- `GET /api/v1/borrow/tasks/{taskNo}`
- `POST /api/v1/borrow/tasks/{taskNo}/receive-confirm`
- `POST /api/v1/borrow/tasks/{taskNo}/pickup-confirm`
- `GET /api/v1/returns/virtual-stores`
- `GET /api/v1/returns/aggregations`
- `POST /api/v1/returns/batches`
- `POST /api/v1/returns/batches/{returnBatchNo}/logistics`

## Docker

```bash
docker compose up --build
```

The compose file starts MySQL and the application. For production deployments, replace demo credentials and inject secrets through your platform secret manager.

## Documentation

- [Documentation index](docs/README.md)
- [Architecture](docs/architecture/solution.md)
- [State machine](docs/architecture/state-machine.md)
- [Database architecture](docs/database/data-architecture.md)
- [DingTalk integration](docs/dingtalk/h5-auth-and-callback.md)

## Current Scope

Included:

- MVP borrow and return flow
- MySQL-backed persistence
- DingTalk integration skeleton
- Static H5 business page
- Unit tests and CI workflow

Not yet included:

- Real GMS adapter
- Production MQ/job compensation
- Production-grade cache and distributed locking
- Full browser E2E test suite

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).
