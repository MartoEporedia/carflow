<!--
SYNC IMPACT REPORT
==================
Version change: NEW → 1.0.0 (initial ratification)
Modified principles: none (first version)
Added sections: Core Principles (I–V), Technology Stack & Constraints,
                Development Workflow, Governance
Removed sections: none
Templates reviewed:
  ✅ .specify/templates/plan-template.md — Constitution Check section is
     generic; existing gates placeholder aligns with defined principles
  ✅ .specify/templates/spec-template.md — no constitution references;
     FR/SC structure compatible with principles
  ✅ .specify/templates/tasks-template.md — phase/story structure compatible;
     no outdated principle references
  ⚠ .specify/templates/commands/ — directory not found; no command files to update
Deferred TODOs: none
-->

# CarFlow Constitution

## Core Principles

### I. Offline-First

All core functionality — expense input, storage, retrieval, and statistics — MUST
work without internet connectivity. Room database is the canonical source of truth.
Cloud sync is an optional enhancement and MUST NEVER be a hard dependency for any
core user flow. Features that require network access MUST degrade gracefully.

**Rationale**: User privacy and reliability. Car expenses are recorded on the go,
often in areas with poor connectivity. The app must always be usable.

### II. NLP-Powered Input (NON-NEGOTIABLE)

The expense parser MUST remain the primary input mechanism for the app.
Text input MUST be processed through the NLP pipeline before producing a
structured `ParsedExpense`. The parser MUST support both Italian and English
keywords. Manual structured forms are a secondary fallback, not the default.

**Rationale**: The NLP parser is the core differentiator of CarFlow. Bypassing
it for convenience features erodes the product's value proposition.

### III. Test-First

The `shared:parser` module MUST maintain ≥95% unit test coverage at all times.
New parser features MUST follow the Red-Green-Refactor cycle: tests written →
reviewed and confirmed failing → implementation begins. No parser code MUST be
merged without a corresponding passing test suite.

**Rationale**: The parser is pure logic with no Android runtime dependency,
making it fully testable. Regressions in parsing directly corrupt user data.

### IV. Multi-Module Clean Architecture

The NLP parser MUST remain a standalone Kotlin JVM library (`shared:parser`),
independently buildable and testable without the Android runtime. The `app`
module MUST consume only the public API of `shared:parser` — no direct access
to internal pipeline classes. Any new shared functionality MUST be evaluated as
a candidate for a dedicated module before being placed in `app`.

**Rationale**: Modularity ensures the parser can be tested on the JVM, reused,
and evolved without Android build cycles.

### V. Reactive Architecture

All data flows MUST use Kotlin Coroutines and Flow end-to-end. The MVVM +
Repository pattern MUST be enforced: ViewModels MUST NOT reference Room DAOs
directly. Hilt MUST be the sole mechanism for dependency injection in
production code. No manual service locator or singleton patterns are permitted.

**Rationale**: Coroutines + Flow provide structured concurrency and
lifecycle-safe reactive streams; Hilt guarantees compile-time DI validation.

## Technology Stack & Constraints

- **Language**: Kotlin 1.9.22 targeting Java 17 bytecode — no mixing with Java source files
- **UI**: Jetpack Compose + Material3 BOM 2024.01.00 — XML layouts are prohibited
- **Database**: Room 2.6.1 — direct SQLite queries outside Room are prohibited
- **Minimum API**: 26 (Android 8.0) — no API calls that require higher than the
  declared minSdk without explicit runtime guards
- **DI**: Hilt 2.50 — manual dependency wiring in production code is prohibited
- **Build**: Gradle with Kotlin DSL (`.kts`) — Groovy build scripts MUST NOT be
  introduced
- **Serialization**: Kotlinx Serialization 1.6.2 — Gson and Moshi are prohibited

## Development Workflow

- Every pull request MUST reference a feature spec under `specs/` or a bug report.
- New parser features require a `ParsedExpense`-level contract test before
  implementation begins (see Principle III).
- Breaking changes to the `shared:parser` public API MUST be explicitly called out
  in the PR description and MUST increment the module version.
- UI screens MUST be implemented as `@Composable` functions with at least one
  `@Preview` annotation covering the default state.
- Commit messages MUST be conventionally scoped (e.g., `feat(parser):`,
  `fix(ui):`, `refactor(data):`).
- All PRs and code reviews MUST verify compliance with the Core Principles above.
  Violations require a documented justification in the plan's Complexity Tracking
  table.

## Governance

This constitution supersedes all other development practices and informal
conventions. Any amendment requires:

1. A documented rationale linked to a concrete project need or post-incident
   finding.
2. A version bump to this file following semantic versioning:
   - **MAJOR**: backward-incompatible removal or redefinition of a principle
   - **MINOR**: new principle or section added, or material guidance expanded
   - **PATCH**: clarification, wording, or typo fix
3. A migration plan when existing code would violate the new or amended principle.
4. Review and explicit approval before merging.

Runtime development guidance (shell commands, build notes, active plan context)
lives in `CLAUDE.md`, not in this constitution.

**Version**: 1.0.0 | **Ratified**: 2026-04-28 | **Last Amended**: 2026-04-28
