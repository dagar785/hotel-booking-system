# The Test Gate

`./gradlew build` **fails** if production code you changed has no tests and no justified waiver.
If your build failed, this page tells you where the rule lives and how to proceed.

## Where is it?
| What | Where |
|---|---|
| Gradle tasks + failure messages | `build-logic/src/main/kotlin/com/hbs/buildlogic/gate/TestGatePlugin.kt` |
| Pure rules (tested) | `build-logic/.../gate/GateLogic.kt`, tests in `build-logic/src/test` |
| Thresholds, excluded files, base ref | `test-gate.yml` |
| Waivers | `test-waivers.yml` |
| Git hooks | `.githooks/pre-commit` (fast check), `.githooks/pre-push` (full check). Installed automatically by `./gradlew installGitHooks` on the first build. |
| Wiring into `build` | root `build.gradle.kts` applies `hbs.test-gate`; `check` depends on `testGate` |

## What does it check?
Only code changed since the `baseline` git tag (`git tag` to see it) is judged. Uncommitted and untracked files count.

1. **`testGateExistence`** (fast): every changed `src/main/**/*.kt` file needs `<Name>Test.kt` (or `<Name>Tests.kt`) in the same module's `src/test` or `src/androidTest`, **or** a waiver. Files excluded in `test-gate.yml` (DI modules, DTOs, Compose screens, Activities) are not gated.
2. **`testGateCoverage`** (slow): at least `minChangedLineCoverage`% (default 80) of *changed executable lines* must be executed by unit tests (Jacoco). Waived files are skipped.

Run just the gate: `./gradlew testGate` (fast half only: `./gradlew testGateExistence`).

## I can't write a test. What now?
Add a waiver to `test-waivers.yml`:
```yaml
- path: feature/booking/src/main/kotlin/com/hbs/feature/booking/domain/Foo.kt
  category: TIME_BOXED     # GENERATED | TRIVIAL_WRAPPER | UI_COVERED_ELSEWHERE | TIME_BOXED
  reason: "Ran out of time; retry logic is covered manually via card 4000000000000341."
```
Rules: path must exist, category must be one of the four, reason >= 30 characters and not a placeholder ("todo", "n/a"…).
`TIME_BOXED` waivers are called out in the build output so the interviewer can discuss them. A waiver is a decision you defend, not a bypass.

## Why can't I just `git commit --no-verify`?
You can skip the hook, but the same gate runs inside `./gradlew build` (and CI), so the build still fails.

## Gotchas
- "cannot find base ref": run `git fetch --tags`; the seed commit must be tagged `baseline`.
- A module with no `src/test` has no coverage data: create at least one test there.
