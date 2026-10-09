# Quoter roadmap

The [issue tracker](https://github.com/Damercy/Quoter/issues) is the public ledger
for requests, implementation checklists and progress. This page summarizes
direction; issue milestones show the current plan. Release dates and version
numbers are assigned when scope is ready, not promised here.

| Horizon | Focus | Tracking |
| --- | --- | --- |
| Next release | Home-screen quote widgets | [#10](https://github.com/Damercy/Quoter/issues/10) |
| Later | AppFunctions for quote access through compatible assistants | [#11](https://github.com/Damercy/Quoter/issues/11) |
| Later exploration | Optional animated glass/gradient backgrounds or UI refinements | [#12](https://github.com/Damercy/Quoter/issues/12) |

## Request ledger

[Request a feature](https://github.com/Damercy/Quoter/issues/new?template=feature-request.yml),
[report a bug](https://github.com/Damercy/Quoter/issues/new?template=bug-report.yml),
or add context and a thumbs-up reaction to an existing request.
Anyone with a GitHub account can participate; maintainers set release priorities.
See [CONTRIBUTING.md](CONTRIBUTING.md) for updates and pull requests.

## Planning stages

Browse the live [milestones](https://github.com/Damercy/Quoter/milestones),
[open requests](https://github.com/Damercy/Quoter/issues?q=is%3Aissue+is%3Aopen+label%3Aenhancement)
and [completed work](https://github.com/Damercy/Quoter/issues?q=is%3Aissue+is%3Aclosed).

- **Needs triage:** new reports carry `needs-triage` until reviewed.
- **Backlog Candidates:** proposals awaiting a decision or more evidence.
- **Backlog:** accepted direction with no assigned release.
- **On Deck:** accepted work being considered for an upcoming release.
- **Next release — Widgets:** the current release focus; tracked with scoped
  plan issues and completion checklists.

Use `bug` or `enhancement` to identify the request, `plan-item` for a release
tracking issue, and area labels for widgets, AppFunctions or appearance. Remove
`needs-triage` after review. A milestone describes planning, not implementation
status: linked PRs and issue checklists show progress. An accepted proposal can
return to the backlog if scope or evidence changes. Explain duplicate/declined
requests before closing them; preserve history.

## Completing a plan item

Agree scope and unresolved product choices on the issue before implementation.
Link focused PRs and verification evidence. Check accessibility, themes, reduced
motion, offline/data behavior and relevant device layouts. Document deferred
hardware checks, update release notes, and close the issue when agreed scope is
delivered. Close the release milestone when all included work is accounted for.
Code completion and Play submission do not prove store publication.

This process follows OpenAI's
[structured feature requests](https://github.com/openai/codex/blob/main/.github/ISSUE_TEMPLATE/5-feature-request.yml)
and VS Code's [milestone planning](https://github.com/microsoft/vscode/wiki/Development-Process)
and [triage stages](https://github.com/microsoft/vscode/wiki/Issues-Triaging),
adapted to Quoter's release scope.
