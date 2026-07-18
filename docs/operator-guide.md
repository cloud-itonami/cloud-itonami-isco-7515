# Operator Guide

## First Deployment

1. Define the operator's facility coverage and taster intake process.
2. Define consent and purpose categories for taster/facility records.
3. Run synthetic operating cases (sample-intake log entry, tasting-
   session scheduling, supply coordination, anomaly-concern flagging).
4. Enable human-reviewed sign-off for `:high`/`:safety-critical`
   actions (all flagged anomaly concerns, above-threshold supply
   orders).
5. Measure operating outcomes and audit coverage.

## Minimum Production Controls

- consent and disclosure log
- anomaly-escalation path (visible defect, off-odor reported by intake
  staff)
- provenance for all operating records (taster and facility both
  independently registered, taster record including certification
  status)
- human review for high-risk cases
- audit export for all gated actions
- a hard, unconditional block on any attempt to route the sensory
  evaluation itself, a quality-grade assignment, a pass/fail
  determination, or a food-safety-clearance decision, through this
  actor — those decisions stay a human taster's exclusive,
  irreplaceable sensory judgment end to end

## Certification

Certified operators must prove that the governor gates every
safety-critical robot action, that anomaly-concern risks escalate to
humans, and that no deployment configuration can route the sensory
evaluation itself, a quality-grade assignment, a pass/fail
determination, or a food-safety-clearance decision through this actor.
