# Contributing

## Development Flow

1. Create a feature branch.
2. Keep changes focused and small.
3. Run tests before opening a pull request.
4. Update docs and examples when behavior changes.

## Local Checks

```bash
make test
make package
```

## Commit Style

Use conventional commit prefixes when practical:

- `feat:` user-facing feature
- `fix:` bug fix
- `docs:` documentation only
- `test:` tests only
- `refactor:` behavior-preserving code change
- `chore:` build, scripts, maintenance

## Pull Request Checklist

- Tests pass locally.
- New behavior has tests or a clear reason why not.
- README/docs/examples are updated.
- No secrets or local machine files are committed.
