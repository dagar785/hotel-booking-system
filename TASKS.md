# Candidate Tasks

Build: `./gradlew build` (JDK 17+, Android SDK 35). Run the app: `./gradlew :app:installDebug`.
Read `README.md` first. The build is green at the start. **It must be green when you finish**, so read `docs/TEST_GATE.md` early.

Test cards (type into the booking form):

| Card | Behaviour |
|---|---|
| 4242 4242 4242 4242 | succeeds |
| 4000 0000 0000 0002 | declined |
| 4000 0000 0000 3220 | needs 3-D Secure confirmation |
| 4000 0000 0000 0341 | network error on 1st attempt, OK on retry with the *same* idempotency key |
| 4000 0000 0000 0259 | charge succeeds server-side but the client sees a timeout |

Talk through your decisions. Tasks are ordered; skip around if you are stuck.

## T1 – Dependency injection (15 min)
Paying twice with the "timeout" card can double charge even though the gateway is idempotent. Find out why using the DI graph, fix it, and prove it with a test. Then explain `@Binds` vs `@Provides`, and how you would swap the fake gateway for a real one in release builds only.

## T2 – Search correctness (15 min)
Type "paris" quickly. Results sometimes show the wrong list. Fix it in `SearchViewModel` (debounce + cancel stale requests). Keep the MVI shape (state, intent, effect, reducer). Add tests with virtual time.

## T3 – 3-D Secure (25 min)
Card `…3220` returns `RequiresAuth`. Extend `PaymentState` and `PayForBookingUseCase` / `BookingViewModel` / `BookingActivity` so the user can confirm and the payment completes via `confirmAuth`.

## T4 – Unknown outcome and process death (25 min)
Card `…0259`: the user sees "network problem", taps Pay again, and is charged twice (new idempotency key per attempt). Make retries safe (stable key, survives process death via `SavedStateHandle`) and resolve uncertain results with `status()` before charging again.

## T5 – Animation and performance (15 min)
Hotel detail: the price animation recomposes the whole screen each frame. Fix it (defer the read). Then add an animated transition when switching rooms, and explain how you would verify jank (Layout Inspector recomposition counts, macrobenchmark).

## T6 – Review (10 min, optional)
Review `BookingViewModel` and `PayForBookingUseCase` as if they were a teammate's PR. List the risks you see.

## Rules
- Do not edit `test-gate.yml` thresholds or `build-logic`. Waivers are allowed, with honest reasons.
- Prefer fakes over mocks. Tests must be deterministic (no real delays).
