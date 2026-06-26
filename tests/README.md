# Tests

Java tests live under `src/test/java` because this project uses Maven conventions.

Current test coverage:

- Domain state machine
- FIFO return allocation
- Borrow application validation and in-memory persistence
- DingTalk callback crypto
- DingTalk configuration validation
- MySQL persistence integration coverage for application items, virtual-store seed data,
  return batches, FIFO allocations, and task return state

Docker-backed MySQL integration tests use Testcontainers and are named `*IT`.
They are intentionally excluded from the default unit test command.
Docker must be running before executing them.

```bash
make integration-test
```

Future test assets can be added here:

- `tests/e2e/` for browser-driven H5 tests
- `tests/fixtures/` for shared JSON fixtures
- `tests/load/` for lightweight API load scenarios
