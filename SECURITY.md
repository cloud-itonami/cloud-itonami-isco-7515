# Security Policy

This project handles food and beverage tasting/grading operating workflows.
Treat vulnerabilities as potentially high impact even when the demo data is
synthetic — this domain's failure modes include foodborne-illness exposure
and consumer-safety incidents if a food-safety-clearance decision were ever
routed through an unqualified path.

## Do Not Disclose Publicly

Report privately before opening public issues for:

- credential exposure
- real taster, facility or operator data exposure
- authorization bypass
- FoodTasteGovernor bypass
- audit-ledger tampering
- over-disclosure in reports or exports
- unsafe robot action dispatch
- any path that lets a proposal reach the sensory evaluation itself, a
  quality-grade assignment, a pass/fail determination, or a
  food-safety-clearance decision

## Reporting

Use GitHub private vulnerability reporting when available for the repository.
If that is unavailable, contact the repository maintainers through the
cloud-itonami organization before publishing details.

Include:

- affected commit or version
- reproduction steps
- expected and actual behavior
- impact on taster/facility data, policy enforcement or audit
  logging
- suggested fix, if known

## Production Guidance

- Store secrets outside Git.
- Keep real taster/facility/operator data outside this repository.
- Run policy tests before deployment.
- Export and review audit logs regularly.
- Use least privilege for operators and facility accounts.
