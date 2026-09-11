# Contributing

`cloud-itonami-isco-7515` accepts contributions to the OSS actor, policy tests,
documentation, examples and open occupation blueprint.

## Development

```bash
kbb -M:test
```

Keep changes small and include tests for policy, audit, store or disclosure
behavior.

## Rules

- Do not commit real taster, facility or operator data, credentials
  or operating documents.
- Keep production writes and disclosures behind FoodTasteGovernor.
- Treat this occupation's workflows as high-risk: add tests for permission,
  scope-exclusion, anomaly-escalation and audit logging.
- Never widen the closed op-allowlist to include a sensory-evaluation-
  performance op, a quality-grade-assignment op, a pass/fail-
  determination op, or a food-safety-clearance op, without a
  dedicated ADR and explicit human review.
- Document any new business-model or operator assumption in `docs/`.

## Pull Requests

PRs should describe:

- what behavior changed
- which policy invariant is affected
- how it was tested
- whether operator or certification docs need updates
