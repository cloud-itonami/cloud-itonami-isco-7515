# cloud-itonami-isco-7515

Open Occupation Blueprint for **ISCO-08 7515**: Food and Beverage Tasters and Graders.

This repository designs a forkable OSS business for a food and beverage tasting/grading practice: a sample-intake-logging and session-scheduling coordination robot manages taster/facility records under a governor-gated actor, so a taster/grader crew keeps its own operating records instead of renting a closed quality-management SaaS.

**Maturity: `:implemented`.** `src/foodtaste/` implements the
`FoodTasteActor` as a `langgraph.graph/state-graph`
(`foodtaste.actor`) wired to a `Food and Beverage Taster/Grader
Advisor` (`foodtaste.advisor`) and an independent `FoodTasteGovernor`
(`foodtaste.governor`), following the itonami actor pattern
(ADR-2607121000): `:intake -> :advise -> :govern -> :decide -+-> :commit
(:ok?) +-> :request-approval (:escalate?, human-in-the-loop interrupt)
+-> :hold (:hard?)`. 29 tests / 64 assertions green (`clojure
-M:test`). HARD invariants (always hold, never overridable): taster provenance, facility provenance, no-actuation
(`:effect` must be `:propose`), a closed op-allowlist
(`:log-sample-record`, `:schedule-tasting-session`,
`:flag-anomaly-concern`, `:coordinate-supply-order` — nothing else may
ever be proposed), and a permanent, unconditional block on any
proposal that would perform the sensory evaluation itself (no robot
can taste), assign or finalize a quality-grade assignment, determine
or finalize a pass/fail determination, or finalize or authorize a
food-safety clearance. Always-escalate paths (human sign-off
regardless of confidence, mapping this repo's Trust Controls in
[`docs/business-model.md`](docs/business-model.md)):
`:flag-anomaly-concern` (always) and `:coordinate-supply-order` above
the registered cost threshold.

## Robotics premise

All cloud-itonami verticals are designed on the premise that a **robot performs
the physical domain work**. Here a sample-intake-logging and session-scheduling
coordination robot performs sample-intake/batch-identifier metadata logging,
tasting-session scheduling and tasting-supply coordination for a food and
beverage taster/grader crew, under an actor that proposes actions and an
independent **FoodTasteGovernor** that gates them. The governor never
dispatches hardware itself, NEVER performs the sensory evaluation itself (no
robot can taste), NEVER records or proposes a quality-grade/pass-fail
determination, and NEVER finalizes a food-safety-clearance decision; `:high`/
`:safety-critical` actions (such as a flagged sample-condition anomaly, or an
above-threshold supply order) require human sign-off. **This actor
coordinates sample-intake logging and session scheduling only — it never
performs, records or finalizes any sensory/grading/food-safety-clearance
judgment itself; that judgment always requires a human taster's own sensory
evaluation.**

## Core Contract

```text
taster roster + facility registration + anomaly-reporting policy
        |
        v
Food and Beverage Taster/Grader Advisor -> FoodTasteGovernor -> log/schedule/coordinate, or human sign-off
        |
        v
robot actions (gated) + operating records + audit ledger
```

No automated advice can dispatch a robot action the governor refuses, perform
the sensory evaluation itself, assign or finalize a quality-grade assignment,
determine or finalize a pass/fail determination, finalize or authorize a
food-safety clearance, suppress an operating record, or disclose sensitive
data without governor approval and audit evidence.

## Capability layer

Resolves via [`kotoba-lang/occupation`](https://github.com/kotoba-lang/occupation)
(ISCO-08 `7515`). Required capabilities:

- :robotics
- :identity
- :audit-ledger

See [`docs/business-model.md`](docs/business-model.md) and
[`docs/operator-guide.md`](docs/operator-guide.md).

## Reference implementation

`src/foodtaste/{store,advisor,governor,actor}.cljc` is a real
implementation of the Core Contract above, pure `.cljc` (portable
across JVM/cljs/WASM per this workspace's runtime-priority rules — no
JVM-only interop). Modeled on cloud-itonami-isco-7412's `elecmech.*`
(closest available structural reference at scaffold time — same
closed-allowlist + independently-verified-provenance +
always-escalate-concern shape; neither cloud-itonami-isco-7514 nor
cloud-itonami-isco-7511 exist yet, confirmed via `gh api` 404 before
this actor was scaffolded).

```bash
kbb -M:test   # 29 tests, 64 assertions, green
```

This is what backs this repo's `:maturity :implemented` entry in
[`kotoba-lang/occupation`](https://github.com/kotoba-lang/occupation)
(ADR-2607121000, Wave3 production/trades).

## License

AGPL-3.0-or-later.
