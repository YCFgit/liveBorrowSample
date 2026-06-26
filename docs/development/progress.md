# 进度记录

## 2026-06-24

- 读取 `docs/product/mvp-plan-v1.2.md`。
- 提取核心业务流程、V1.1/V1.2 增量功能、GMS 对接边界与接口缺口。
- 建立本次分析的规划文件，准备输出完整系统方案。
- 新增 `docs/architecture/solution.md`，覆盖架构、数据模型、接口、状态机、异步任务、MVP 范围、实施计划、测试与风险。
- 新增 `sql/mvp_schema.sql`，定义一期核心业务表、日志表、幂等表和索引。
- 新增 `docs/api/http-api.md`，定义钉钉 H5、管理后台和集成接口。
- 新增 `docs/architecture/state-machine.md`，明确任务主状态、子状态、并发控制和补偿规则。
- 新增 `docs/product/requirements-and-acceptance.md`，补充产品目标、角色、验收标准和上线策略。
- 新增 `docs/database/data-architecture.md`，补充主题域、ER 图、编号规则、事件模型和治理要求。
- 新增 `pom.xml`、`src/`、`api/openapi.yaml`、`README.md`，搭建 Java 后端骨架与 OpenAPI 草案。
- 新增统一错误码、全局异常处理、枚举字典接口和基础状态机测试。
- 环境已补齐 Maven，并可在 Maven 下使用高版本 Java 执行构建。
- 新增 `borrow`、`return`、`admin` 控制器及请求/响应 DTO。
- 新增内存版业务服务、任务/归还批次领域模型、FIFO 归还分配服务。
- 发现并修复 Java 17 兼容性问题：将 `List.getFirst()` 改为 `get(0)`。
- 执行 `mvn test` 成功，当前共有 4 个单元测试通过。
- 新增 `application-local.yml` 与 `application-prod.yml`，将数据源切为分环境配置。
- 将本地默认 MySQL 账号改为 `ycf / ycf012!`，并保留环境变量覆盖能力。
- 验证本地 MySQL 时仍返回 `Access denied for user 'ycf'@'localhost'`。
- 新增 `sql/local_mysql_setup.sql`，用于本地创建数据库、账号和授权。
