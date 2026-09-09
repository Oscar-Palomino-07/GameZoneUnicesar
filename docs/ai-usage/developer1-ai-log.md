# Developer 1 — AI Usage Log

**Name:** Manuel Ospino
**Role:** Developer 1 (Product Module)
**Branch:** `feature/product-module`

This log records every AI-assistant interaction during the project, in accordance
with the assignment's AI policy. Only legitimate uses are allowed (conceptual
doubts, error explanations, code review, Java/Maven guidance, English identifier
suggestions, and Git command help). The AI was never asked to design the system,
answer the `analysis.md` questions, or write complete classes to copy and paste.

## Entry 01 — 2026-09-08

| Field | Detail |
|---|---|
| AI tool | OpenAI-compatible assistant (opencode CLI on Windows PowerShell) |
| Task | Set up the local work environment and inspect the repository state |
| Prompt summary | "Prepare my product module as Developer 1" / "Clone the repo, configure git identity, create my feature branch" |
| Legitimate-use category | Git command help; Maven project structure exploration; local environment verification |
| What I did myself | Ran the `git clone`, `git config`, `git checkout -b` commands; verified tool versions with `java -version` and `mvn -version` |
| What the AI provided | Read-only status checks and explanations: remote branch list, `develop` branch contents, `.gitignore` and `pom.xml` inspection, package structure walkthrough |
| Output used? | Yes — used to confirm `develop` has no `docs/`/`TEAM.md` yet and to plan the wait-for-design step |
| Fully understood? | Yes — I can explain each command and why the feature branch is created from `develop` |

### Notes / decisions

- Proposed to the team a JSON-based persistence format using Gson, with one file
  per module under `data/` (e.g. `data/products.json`). Final decision pending
  team validation and leader coordination on `pom.xml`.
- Confirmed the assignment requirement that `data/` stays versioned (the
  `.gitignore` does not exclude it).
- Flagged to the leader that `feature/maven-project-setup` was merged but not
  deleted from the remote.

---

<div style="page-break-after: always;"></div>

## Entry 02 — 2026-09-08

| Field | Detail |
|---|---|
| Date | 08-09-2026 |
| AI tool | OpenAI-compatible assistant (opencode CLI on Windows PowerShell) |
| Task | Implement the product module classes following the team's approved design |
| Prompt summary | "Proceed with Phase 3: implement Product, VideoGame, Console, ProductRepository, ProductService, commit + push per class" |
| Legitimate-use category | Java/Maven guidance; code review of my own module; error explanation |
| What I did myself | Synced `develop` and merged it into `feature/product-module`; reviewed `docs/analysis.md` and `docs/class-diagram.md` to extract the agreed signatures; compiled with `mvn -q compile`; ran a persistence round-trip smoke test; created one atomic commit per class and pushed each immediately |
| What the AI provided | First-draft code aligned strictly to the signatures in `docs/class-diagram.md` (fields, methods, multiplicities), JavaDoc templates, and a smoke test to verify save/load/stock; also caught an extra `ProductLine` type I had introduced and removed it to honor the "no unnecessary classes" rule |
| Output used? | Yes — used as the working basis for my PR, then verified with compile + smoke test |
| Fully understood? | Yes — I can explain each class: inheritance, `abstract getDescription()`, `@Override`, the repository's text-file format (tab-separated, 7 fields), and the service validations and auto-save |

### Notes / decisions

- Persistence went with **plain text files** (`data/videogames.txt`, `data/consoles.txt`),
  as already decided by the team in `docs/analysis.md` Q9. My earlier JSON/Gson
  proposal was not adopted; logged here for transparency.
- `Product.updateStock(int)` follows the class diagram (sets the new quantity).
  The sales module will call `ProductService.updateStock(productId, quantity)`
  to reduce inventory; semantics must be synchronized with the Technical Lead.
- Two leftover Git issues reported to the leader: `feature/maven-project-setup`
  still exists on the remote after being merged, and several docs feature
  branches are pending cleanup.

---

## Entry 03 — 2026-09-08

| Field | Detail |
|---|---|
| Date | 08-09-2026 |
| AI tool | OpenAI-compatible assistant (opencode CLI on Windows PowerShell) |
| Task | Align `ProductRepository` persistence with the leader's requirement to use JSON + Gson from the start |
| Prompt summary | "Confirm you will use JSON with Gson from the start for ProductRepository; review diagram consistency" |
| Legitimate-use category | Consultation on a specific Java library implementation (Gson serialization/deserialization); code review; error explanation |
| What I did myself | Chose Gson 2.14.0 after checking Maven Central; kept the repository's four public methods exactly as in `docs/class-diagram.md`; re-ran `mvn -q compile` and the round-trip smoke test (two video games + one console: register → reload → findById → updateStock → reload); deleted temporary files |
| What the AI provided | Confirmed how to serialize/deserialize `List<T>` with Gson (`TypeToken`), pretty-printing, and how to make the load methods return an empty list when the file is missing or malformed |
| Output used? | Yes — used to rewrite `ProductRepository` to `data/videogames.json` and `data/consoles.json` |
| Fully understood? | Yes — I can explain why Gson needs the `TypeToken` for generic lists and why a missing file must load an empty list |
| Decision | Deviation from `docs/analysis.md` Q9 wording ("text files"): JSON is a text-based format, so the class diagram stays valid; I recommended the team update Q9 to mention JSON explicitly for coherence |

---

## Entry 04 — 2026-09-09

| Field | Detail |
|---|---|
| Date | 09-09-2026 |
| AI tool | OpenAI-compatible assistant (opencode CLI on Windows PowerShell) |
| Task | Sync the local repository after the person module was merged, verify the full build, and clean up leftover placeholders |
| Prompt summary | "Check if a new pull is needed" / "I need two more atomic commits" |
| Legitimate-use category | Git command help; repository state inspection; error explanation |
| What I did myself | Confirmed remote `develop` had advanced (`cf4cb84` → `0b6029d`, PR #12 person module); fast-forwarded `develop` and `feature/product-module`; ran `mvn -q compile` (passed); reviewed what the pull brought in before merging |
| What the AI provided | Walked me through the git fast-forward/pull sequence, explained the merge conflict resolution that Veronica had done on `.gitignore`/`pom.xml`, and suggested safe cleanup commits (removing `.gitkeep` placeholders, logging this session) |
| Output used? | Yes — used to sync and to prepare the two atomic commits |
| Fully understood? | Yes — I can explain fast-forward vs merge and why the `.gitkeep` placeholders are no longer needed in packages that already contain classes |

---

## Entry 05 — 2026-09-09

| Field | Detail |
|---|---|
| Date | 09-09-2026 |
| AI tool | OpenAI-compatible assistant (opencode CLI on Windows PowerShell) |
| Task | Align `ProductService` error handling with the other services, as requested by the Technical Lead |
| Prompt summary | "Make ProductService throw IllegalArgumentException like PersonService and SaleService so ConsoleMenu can know when registration or stock update fails" |
| Legitimate-use category | Code review of my own module; conceptual doubt on error handling patterns; error explanation |
| What I did myself | Compared `PersonService.registerCustomer` with my `ProductService`; planned validation ordering (blank fields, duplicate id, negative price/stock); rewrote `validate()` to throw instead of printing to `System.err`; applied the same exception-based reporting to `updateStock`; ran `mvn -q compile` and a smoke test asserting the exact exception messages for 7 invalid scenarios; updated the JavaDoc `@throws` clauses; pushed commit `7471200` immediately |
| What the AI provided | Explained the value of unchecked exceptions for the future `ConsoleMenu` (the caller catches and displays the message), and suggested keeping the exception messages consistent with `PersonService` (e.g. "A product with id X already exists.") |
| Output used? | Yes — used the resulting behavior in `ProductService`; already committed and pushed |
| Fully understood? | Yes — I can defend why services throw and the UI catches, and why signatures did not change. I still need the leader to approve this change in the PR |

---

## Future entries

(Template to keep filling throughout the project.)

| Field | Detail |
|---|---|
| Date | _dd-mm-yyyy_ |
| AI tool | _name_ |
| Task | _what I asked for_ |
| Prompt summary | _short description of the request_ |
| Legitimate-use category | _one of the allowed uses from the policy_ |
| What I did myself | _the action I performed with my own understanding_ |
| What the AI provided | _result used_ |
| Output used? | _yes/no and where_ |
| Fully understood? | _yes/no — I can defend this in the oral defense_ |

## Compliance checklist

- [ ] No AI-generated design (diagrams, hierarchy, layer structure) was used.
- [ ] No AI answers to the `analysis.md` guiding questions.
- [ ] No complete classes were copied and pasted without understanding.
- [ ] No project documentation was produced by AI without my own intervention.