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

## Entry 06 — 2026-09-18

| Field | Detail |
|---|---|
| Date | 18-09-2026 |
| AI tool | OpenAI-compatible assistant (opencode CLI on Windows PowerShell) |
| Task | Implement the accessory model hierarchy — `Accessory`, `Controller`, `Cable` and `Memory` — in `com.gamezone.model`, and update the hierarchy diagram |
| Prompt summary | "Implement Developer 1's accessory hierarchy per the assignment guide, with one atomic commit per class" |
| Legitimate-use category | Java/Maven guidance; code review of my own module; explanation of inheritance, polymorphism and defensive setters |
| What I did myself | Pulled the shared `feature/accessory-module` branch (created by the leader) with Veronica's `data/accessories.json` already merged; reviewed the guide and `Product` to align field names and constructor order; ran `mvn -q compile` after each class; wrote the Mermaid branch of `docs/hierarchy-diagram.md`; created and pushed the six commits stated below |
| What the AI provided | First-draft source code for the four classes and the explanations in the assignment guide (why `Accessory` is abstract, the ternary `null` guard, `String.join` in `getDescription()`, and why compatibility is stored as console ids instead of `Console` objects) |
| Output used? | Yes — used as the working basis for the accessory classes, then verified by compilation and my own review of each segment |
| Fully understood? | Yes — I can explain every class without the guide: inheritance chain, `super` chaining, the `@Override getDescription()` polymorphism, and the null-safe list handling |

### Notes / decisions

- The commits produced in this session, one per deliverable: `feat(model): add abstract Accessory class with console compatibility`, `feat(model): add Controller accessory`, `feat(model): add Cable accessory`, `feat(model): add Memory accessory`, `docs: update hierarchy diagram with accessory classes`, `docs: add AI usage entry for accessory model`.
- `data/accessories.json` was added by a teammate before my commits; I kept it and did not touch it, since persistence is another developer's responsibility.
- Named the exact fields of the subclasses (`connectionType`, `lengthInMeters`, `connectorType`, `capacityInGb`, `memoryType`) so the sales deserializer and the CSV format can rely on them.

---

## Entry 07 — 2026-09-18

| Field | Detail |
|---|---|
| Date | 18-09-2026 |
| AI tool | OpenAI-compatible assistant (opencode CLI on Windows PowerShell) |
| Task | Implement the promotion model hierarchy — `Promotion`, `PercentageDiscount`, `CategoryDiscount`, `BulkPurchaseDiscount` — and the `Product.getCategory()` discriminator, then update the hierarchy diagram |
| Prompt summary | "Proceed with Developer 1's promotion model per docs/promotion-development-guide.md, one atomic commit per class" |
| Legitimate-use category | Java/Maven guidance; code review of my own module; explanation of abstract methods, polymorphism and defensive null handling |
| What I did myself | Connected to the shared `feature/promotion-module` branch (created by the leader) and pulled the guide, the analysis scaffold and the preloaded `data/promotions.json`; read `docs/promotion-development-guide.md` (the module contract) and the class diagram to align field names and signatures; fixed a pre-existing compile error in `AccessoryService` (constructor calls used the wrong argument order against the model hierarchy); ran `mvn -q compile` after every commit; wrote the Mermaid branch of `docs/hierarchy-diagram.md`; created and pushed one atomic commit per class |
| What the AI provided | First-draft source code for the four promotion classes and the `getCategory()` additions, following the exact signatures in the leader's development guide (`calculateDiscount(List<Product>)`, shared `discountPercentage`, `minimumQuantity`), plus explanations of the inclusive `isActive` window and the null-safe discount calculations |
| Output used? | Yes — used as the working basis for the promotion classes, then verified by compilation and my own review of each segment |
| Fully understood? | Yes — I can explain the hierarchy, why `calculateDiscount` is abstract, how `isActive` checks the inclusive window, and why each concrete promotion turns the shared percentage into money differently |

### Notes / decisions

- The contract in `docs/promotion-development-guide.md` deviates from the exam wording — `calculateDiscount(Sale)` became `calculateDiscount(List<Product>)`, `discountPercentage` moved up to `Promotion`, `minimumQuantity` replaces `minQuantity`, and category filtering uses a new `Product.getCategory()` (`VIDEOGAME`/`CONSOLE`/`ACCESSORY`) instead of `instanceof`. I followed the guide as the agreed module contract and flagged the differences in this log.
- Commits produced in this session: `fix(accessory): align constructor calls with the model hierarchy`, `feat(model): add abstract Promotion class with shared discount attributes`, `feat(model): add category discriminator to the Product hierarchy`, `feat(model): add PercentageDiscount promotion`, `feat(model): add CategoryDiscount promotion`, `feat(model): add BulkPurchaseDiscount promotion`, `docs: update hierarchy diagram with promotion classes`.
- Field names the service and deserializer can rely on: `Promotion.{id,name,startDate,endDate,discountPercentage}`, `CategoryDiscount.targetCategory`, `BulkPurchaseDiscount.minimumQuantity`.

---

## Entry 08 — 2026-09-18

| Field | Detail |
|---|---|
| Date | 18-09-2026 |
| AI tool | OpenAI-compatible assistant (opencode CLI on Windows PowerShell) |
| Task | Implement the warranty model hierarchy — `Warranty`, `BasicWarranty` and `ExtendedWarranty` — in `com.gamezone.model`, and update the hierarchy diagram |
| Prompt summary | "Proceed with requirement 4 as Developer 1: the warranty hierarchy per the assignment guide, one atomic commit per class" |
| Legitimate-use category | Java/Maven guidance; code review of my own module; explanation of abstract classes, constructor-time polymorphism and date arithmetic |
| What I did myself | Connected to the shared `feature/warranty-module` branch (created by the leader from `develop`) and created my sub-branch `feature/warranty-model`; verified there was no leader warranty guide yet and that the base compiles; ran `mvn -q compile` after each class; wrote the Mermaid branch of `docs/hierarchy-diagram.md`; created and pushed the commits stated below |
| What the AI provided | First-draft source code for the three classes and explanations of the design points: why `endDate` must be computed inside the constructor via the abstract `getDurationInMonths()` (polymorphism at construction time), the null-safe inclusive range in `isActive`, and that `ExtendedWarranty`'s cost derives from `getProduct().getPrice()` |
| Output used? | Yes — used as the working basis for the warranty classes, then verified by compilation and my own review of each class |
| Fully understood? | Yes — I can explain each class without the guide: the `super(id, product, sale, startDate)` chaining, when the `@Override` methods are invoked, the `startDate.plusMonths(...)` arithmetic, and why the certificate is a user-facing string hence written in Spanish |

### Notes / decisions

- The commits produced in this session, one per deliverable: `feat(model): add abstract Warranty class with automatic expiration`, `feat(model): add BasicWarranty`, `feat(model): add ExtendedWarranty`, `docs: update hierarchy diagram with warranty classes`, `docs: add AI usage entry for warranty model`.
- The assignment states the warranty certificate must be formatted in Spanish, so its content is Spanish even though identifiers, comments and commit messages stay in English.
- Only files in the model package and the hierarchy diagram were touched; persistence, service and integration belong to the other two developers.

---

## Entry 09 — 2026-10-02

| Field | Detail |
|---|---|
| Date | 02-10-2026 |
| AI tool | Claude Code (Claude Opus 5.5, CLI on Windows) |
| Phase and branch | Phase 2 (promotions) — adjustment A1, branches `feature/accessory-category-discount` (PR #30) and `feature/accessory-category-discount-completion` |
| Goal | Publish my local A1 branch, which `git pull` rejected for having no upstream, and complete every point of A1 from the Requirement 5 statement |
| Query | "There is no tracking information for the current branch" (pasted error); "how many commits do I have unpublished, should the leader or I create this branch?"; "commit and push"; "did I finish my part?" with the Requirement 5 statement pasted; "do what is missing" |
| Response | The branch existed only locally; per `TEAM.md` each developer creates their own branch. Before pushing, the AI found the branch did not compile: it was based on an old `main` without the promotion module, `CategoryDiscount.java` was saved as UTF-16 and `PromotionService.java` had a BOM and broken accents (encoding damage from PowerShell). It rebuilt the branch from `develop` with clean UTF-8, then listed the A1 points still missing: explicit `Accessory` detection in `calculateDiscount`, the accessories option in `ConsoleMenu`, a preloaded accessory promotion and this log entry |
| Decision | Accepted rebuilding the branch from `develop` instead of pushing the broken commits (originals kept in a local backup branch). Accepted an `instanceof Accessory` check in `CategoryDiscount` so any accessory counts as `ACCESSORY` regardless of `getCategory()`. Accepted replacing the free-text category prompt with a numbered menu (videogames, consoles, accessories) so the user cannot type an invalid category. Modified the statement's `data/promotions.csv` to `data/promotions.json`, because the project persists promotions as JSON; the promotion runs 2026-09-01 to 2026-12-31, covering the work week like the other preloaded promotions. Verified with `mvn -q compile` and a scratch check that `PR-4` loads as a `CategoryDiscount` and discounts 25% from a controller but not under a VIDEOGAME promotion |
| Related commit | `82571c2` docs(model): mention ACCESSORY as a CategoryDiscount target category; `380b8d1` feat: validate ACCESSORY in PromotionService.registerCategory; `c143afb` feat: detect Accessory instances as ACCESSORY in CategoryDiscount; `8ca5cba` feat: add accessories option to the category promotion menu; `1be3081` feat: add preloaded accessory category promotion |

---

## Entry 10 — 2026-10-02

| Field | Detail |
|---|---|
| Date | 02-10-2026 |
| AI tool | Claude Code (Claude Opus 5.5, CLI on Windows) |
| Phase and branch | Phase 4 (returns) — adjustment A5, branch `fix/return-discounted-refund` created from `develop` |
| Goal | Refund returned items proportionally to the discount of the original sale, and show the breakdown in the return receipt |
| Query | The technical lead's review, pasted: "A5 (fix/return-discounted-refund) does not exist on the remote yet and blocks A7. Return only stores saleId and does not know the sale discount; to apply price × (1 − discount / subtotal) it must receive that data when the return is registered and store it. That touches ReturnRepository, so coordinate it. The PR needs problem, cause, solution and verification." |
| Response | The AI read `Return`, `ReturnService`, `ReturnRepository` and `Sale` on `develop`, and the open branches `fix/return-accessory-stock` (A4) and `fix/monthly-balance-report` (A6). It found that `ReturnRepository` serializes `Return` by reflection with Gson in both `develop` and A6, so new fields in `Return` are saved and loaded without changing the repository. It proposed storing the sale subtotal and discount in `Return` at registration time, a private helper for the proportional discount, and a per-item receipt |
| Decision | Accepted storing `saleSubtotal` and `saleDiscount` in `Return` (values at the moment of the return) instead of looking the sale up again, because the model must not depend on services or repositories. Accepted changing only the `new Return(...)` call in `ReturnService` to pass `calculateSubtotal()` and `getDiscountAmount()` of the original sale; this is the single line shared with Developer 2's A4/A6 branches, so I coordinate it with her before merging. Returns saved before this change have no such fields (Gson leaves them at 0) and are refunded at list price, which keeps old data loadable. Rewrote the receipt in Spanish because it is user-visible, matching the sale receipt. Verified with `mvn -q compile` after each commit and a scratch check: on a 2,500,000 sale with a 375,000 discount, a 2,000,000 console refunds 1,700,000 and a 300,000 controller refunds 255,000; with no discount the full price is refunded |
| Related commit | `d6befad` fix: store the original sale subtotal and discount in Return; `7ccdbd6` fix: refund returned items proportionally to the sale discount; `b38bb60` fix: show list price, proportional discount and refund per item in the return receipt |

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