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

- Persistence went with **JSON files** (`data/videogames.json`, `data/consoles.json`)
  using Gson, as required by the Technical Lead. This updates the earlier note:
  although `docs/analysis.md` Q9 initially said "text files", JSON is a text-based
  format, so the class diagram stays valid; the recommended update to Q9 was
  applied for coherence.
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
| AI tool | OpenAI-compatible assistant (opencode CLI) |
| Task | Implement exception-based error handling requested by the Technical Lead |
| Prompt summary | "Change `ProductService` so it also throws exceptions, consistent with the other two services." |
| Legitimate-use category | Error explanation; code review of my own module |
| What I did myself | Replaced the silent `isValid()` (boolean + `System.err` prints) with a `validate()` method that throws; updated the JavaDoc with `@throws` clauses; compiled with `mvn -q compile`; ran a 7-case smoke test asserting the exact error messages; pushed commit `7471200` |
| What the AI provided | Explained how `PersonService` and `SaleService` already throw `IllegalArgumentException` and how to make `ProductService` consistent without changing the class diagram signatures |
| Reason | The Technical Lead reported an inconsistency: `PersonService` and `SaleService` throw `IllegalArgumentException`, while `ProductService` only printed to `System.err` and returned without notifying the caller. The future `ConsoleMenu` must know whether a registration or a stock update failed; error messages had to match the wording used in `PersonService`; the diagram signatures must not change |
| Solution obtained and decision taken | `registerVideoGame`/`registerConsole` now throw `IllegalArgumentException` for blank fields, duplicate id, and negative price/stock; `updateStock` throws for unknown id and negative quantity |
| Output used? | Yes — used to align `ProductService` with the other two services |
| Fully understood? | Yes — I can explain each validation and why throwing is required so the UI can catch and display the error |

---

## Entry 05 — 2026-09-09

| Field | Detail |
|---|---|
| Date | 09-09-2026 |
| AI tool | OpenAI-compatible assistant (opencode CLI) |
| Task | Repository sync, cleanup, and reaching the 12-commit minimum |
| Prompt summary | "Revisa si hay que hacer pull de nuevo" / "Me faltan dos commits, ¿los puedes hacer?" |
| Legitimate-use category | Git command help; repository maintenance |
| What I did myself | Checked the remote state with `git fetch` and `git log origin/develop`; fast-forwarded `develop` and `feature/product-module` after the person module merge; verified the nested clone held no unique work before deleting it; created two atomic commits: `chore: remove gitkeep placeholders from filled packages` and `docs: log AI usage for development sync and cleanup` |
| What the AI provided | Confirmed whether a pull was needed, spotted the stray nested clone inside the project, and explained how to reach the workshop's minimum commit count with atomic commits |
| Reason | `develop` advanced after PR #11 (product module) and PR #12 (person module); a nested clone appeared inside the project; I was two commits short of the minimum |
| Solution obtained and decision taken | Fast-forwarded `develop` and `feature/product-module` (person module merged cleanly), deleted the nested clone after verifying it held no unique work, and added the two atomic commits |
| Output used? | Yes |
| Fully understood? | Yes — I can explain the fast-forward, why the nested clone was safe to remove, and what each commit represents |

---

## Compliance checklist

- [ ] No AI-generated design (diagrams, hierarchy, layer structure) was used.
- [ ] No AI answers to the `analysis.md` guiding questions.
- [ ] No complete classes were copied and pasted without understanding.
- [ ] No project documentation was produced by AI without my own intervention.