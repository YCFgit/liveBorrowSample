# Project Structure

```text
.
├── .github/workflows/        # GitHub Actions quality gates
├── api/                      # Machine-readable API contracts
├── docs/                     # Product, architecture, API, database, DingTalk docs
├── examples/                 # Runnable examples and legacy prototypes
├── scripts/                  # Local automation scripts
├── sql/                      # MySQL setup, schema, seed data
├── src/main/java/            # Application source
├── src/main/resources/       # Runtime config and static H5 pages
├── src/test/java/            # Java unit tests
└── tests/                    # Test strategy and future non-Java test assets
```

## Java Package Boundaries

| Package | Responsibility |
| --- | --- |
| `domain` | Business enums, models, exceptions, and pure domain services |
| `application` | Use-case services and application ports |
| `application.port` | Interfaces required by application services |
| `infrastructure.persistence` | MyBatis mapper, entities, and MySQL-backed adapters |
| `infrastructure.memory` | In-memory adapter for demos and tests |
| `infrastructure.dingtalk` | DingTalk OpenAPI and callback crypto integration |
| `interfaces.http` | REST controllers, request DTOs, response DTOs, exception mapping |
| `config` | Spring configuration properties |

## Naming Rules

- Directories use lowercase kebab-case outside Java packages.
- Java packages use lowercase dot notation.
- Documentation files use English kebab-case filenames for stable links.
- SQL files use descriptive snake_case names.
- Scripts use imperative kebab-case names.
