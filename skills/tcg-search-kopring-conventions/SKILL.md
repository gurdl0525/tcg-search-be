---
name: tcg-search-kopring-conventions
description: Use when writing, reviewing, refactoring, documenting, or testing Kotlin Spring Boot code in the TCG Search backend. Enforces Kotlin-first Kopring style, current package conventions, Spring Security/JPA/Flyway patterns, and KDoc rules.
---

# TCG Search Kopring Conventions

## When To Use

Use this skill for work in this repository that touches Kotlin, Spring Boot,
Spring Security, JPA, Flyway, tests, API DTOs, or KDoc.

If the task mentions code convention, KDoc, Kopring, Kotlin Spring, Spring
Security, auth, persistence, or backend style in this repo, apply this skill
before editing.

## Required Reading

Before changing code, read these files in order:

1. `skills/tcg-search-kopring-conventions/SKILL.md`
2. `docs/code-conventions.md`
3. `docs/kdoc-style.md`

Then inspect nearby production and test code. The repo docs are the source of
truth when they are stricter than this skill summary.

## Core Rules

- Prefer Kotlin-native style over Java carryover patterns.
- Use primary constructor injection. Do not add field injection except in
  Spring-managed tests that need it.
- Use `data class` for request/response DTOs and `@ConfigurationProperties`;
  do not use `data class` for JPA entities.
- Keep controllers thin. Put transaction boundaries and use-case flow in
  services.
- Use Flyway migrations as the database contract. Do not rely on Hibernate
  schema generation.
- Keep native app authentication stateless: Bearer JWT access tokens,
  persisted hashed refresh tokens, no server session dependence.
- Configure CORS/CSRF/security through Spring Security `SecurityFilterChain`.
  Do not introduce deprecated `WebSecurityConfigurerAdapter` style.

## Kotlin Style

- Use 4 spaces, not tabs.
- Use expression bodies for simple single-expression functions.
- Use named arguments and trailing commas in multiline calls and constructors.
- Prefer Kotlin collections and null-safety over Java Stream/Optional patterns.
- Avoid `!!`; if framework validation makes it safe, keep the boundary narrow
  and consider a non-null DTO or explicit validation helper.
- Use scope functions only when they make ownership and control flow clearer.
  Do not hide exception or transaction behavior inside `takeIf`/Elvis/lambda
  chains.
- Omit redundant `public` modifiers.

## Package And Layering

Follow the current structure:

- `domain.<feature>.controller`
- `domain.<feature>.dto.request`
- `domain.<feature>.dto.response`
- `domain.<feature>.entity`
- `domain.<feature>.repository`
- `domain.<feature>.service`
- `global.annotation`
- `global.property`
- `global.querydsl`
- `global.security`
- `global.util`

Do not create new top-level architectural buckets unless the feature needs a
new shared boundary.

## KDoc

- Add KDoc for public boundary classes, interfaces, entities, custom
  annotations, configuration properties, and non-obvious public methods.
- Start with a concise Korean summary sentence.
- Add detail only for caller-visible behavior, policy, failure mode, or domain
  meaning.
- Use `@param`, `@return`, and `@throws` for meaning, not for repeating types.
- Preserve `@author gurdl0525` and `@since DD-MM-YYYY` on class/interface KDoc
  when adding new documented types.
- Do not add KDoc to every private helper. Prefer clear names unless the helper
  encodes a security or domain rule.
- Remove temporary comments such as `// ?` instead of documenting uncertainty.

## Verification

- For code changes, run `./gradlew test` unless the user narrowed the scope or
  the environment blocks it.
- For docs/skill-only changes, validate that the skill frontmatter exists and
  review the changed markdown files.
- If tests fail in unrelated dirty files, report the exact failing area and do
  not rewrite unrelated user changes.
