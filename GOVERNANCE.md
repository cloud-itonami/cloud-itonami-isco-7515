# Governance

`cloud-itonami-isco-7515` is an OSS open-occupation blueprint. Governance
covers both code and the operator model.

## Maintainers

Maintainers may merge changes that preserve these invariants:

- the Advisor cannot directly dispatch robot actions or disclose records.
- FoodTasteGovernor remains independent of the advisor.
- hard policy violations cannot be overridden by human approval.
- performing the sensory evaluation itself, assigning or finalizing a
  quality-grade assignment, determining or finalizing a pass/fail
  determination, and finalizing or authorizing a food-safety
  clearance, stay permanently outside this actor's op-allowlist.
- every commit, hold and approval path is auditable.
- real taster/facility/operator data stays outside Git.

## Decision Records

Architecture decisions live in `docs/adr/`. Changes to the trust model,
storage contract, public business model, operator certification, license, or
the closed op-allowlist should add or update an ADR.

## Operator Governance

Anyone may fork and operate independently. itonami.cloud certification is a
separate trust mark and should require security, audit, support and data-flow
review.

Certified operators can lose certification for:

- bypassing policy checks
- mishandling taster/facility/operator data
- misrepresenting certification status
- failing to respond to security incidents
- hiding material changes to customer-facing operation
- attempting to route the sensory evaluation itself, a quality-grade
  assignment, a pass/fail determination, or a food-safety-clearance
  decision, through this actor
