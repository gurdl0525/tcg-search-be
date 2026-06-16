# Repository Instructions

This repository uses a project-local Codex skill for Kotlin Spring Boot work.

## Required Startup Reading

Before making code, test, migration, API, security, or documentation changes,
read these files in order:

1. `skills/tcg-search-kopring-conventions/SKILL.md`
2. `docs/code-conventions.md`
3. `docs/kdoc-style.md`

Apply the project-local skill even when a global skill with a similar name
exists. The project-local files are the source of truth for this repository.

## Default Engineering Direction

- Write Kotlin-first Kopring code. Prefer Kotlin language features and Spring's
  Kotlin support over Java-style patterns.
- Preserve the current `domain.<feature>` and `global.*` package structure.
- Keep controllers thin, services transactional, repositories persistence-only,
  and Flyway migrations as the durable database contract.
- Add useful KDoc for public boundaries and domain/security policy, not for
  obvious syntax or every private helper.
- Run `./gradlew test` for code changes when the environment allows it.
