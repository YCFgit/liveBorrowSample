# Security Policy

## Supported Versions

This project is currently an MVP sample. Security fixes target the latest `main` branch.

## Reporting a Vulnerability

Do not open public issues for sensitive vulnerabilities.

Please report privately to the repository maintainers with:

- Affected endpoint or component
- Reproduction steps
- Expected and actual behavior
- Suggested mitigation, if known

## Secret Handling

Never commit DingTalk credentials, database passwords, tokens, callback AES keys, or production URLs. Use environment variables or your deployment platform's secret manager.
