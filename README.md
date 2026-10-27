# Hotel Booking System

An Android SDE-2 interview codebase: a small hotel booking app that tests dependency injection, animation, payments, architecture and testing discipline.

```
app/                  Hilt root, Compose navigation, launches the XML booking screen
core/common           Money, dispatchers, Result helper
core/data             Hotel models + fake in-memory repository
core/testing          Test fixtures (MainDispatcherRule, fakes)
payment-sdk           Fake third-party gateway (idempotency, 3DS, flaky/timeout cards)
feature/search        Compose + Clean MVI
feature/hoteldetail   Compose + Clean MVI (animations)
feature/booking       XML + MVVM (MotionLayout stepper, Compose summary card embedded in XML)
build-logic/          Convention plugins + the test gate
```

- Candidates: read `TASKS.md`, then `docs/TEST_GATE.md`.
- Build & test: `./gradlew build`. First build installs git hooks (`core.hooksPath=.githooks`).
- The seed commit is tagged `baseline`; the test gate only judges code changed since then.
