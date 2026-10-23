# GRASP Analysis — GameZone Unicesar

> Answers to the five orienting questions of the GRASP audit (exam Requerimiento 7).
> Each question is answered by the team member assigned to it. The answers refer to the
> individual reports in `docs/grasp-audit/` and, when it exists, to the consolidated audit.

## Assignment of the questions

| Question | Topic | Assigned to |
|---|---|---|
| Q1 | Criteria for a violation, a correct application or "not applicable", and the minimum evidence | Oscar Palomino (Technical Lead) |
| Q2 | Boundary findings between the code of two team members | Oscar Palomino (Technical Lead) |
| Q3 | Fragments that break more than one pattern | Manuel Ospino (Developer 1) |
| Q4 | Strategy to preserve the external behavior | Veronica Padilla (Developer 2) |
| Q5 | Order of the corrections and coordination in the shared branch | Oscar Palomino (Technical Lead) |

Each assigned member writes the answer in the section of the question, in English and in their
own words. The Technical Lead answers Q2 and Q5 after the consolidated audit is merged, because
both depend on the final list of violations.

---

## Q1: What criteria did the team use to decide whether a fragment of code is a violation of a GRASP pattern, whether the pattern is correctly applied, or whether it does not apply? What minimum evidence must accompany each verdict to be accepted in the consolidation?

**Assigned to:** Oscar Palomino (Technical Lead).

For each pattern we started from the question that the pattern answers (for example, for Information Expert, "who has the information needed for this responsibility?") and applied it to each class and method of the code under our responsibility. The verdict depends on the answer:

- **Violation.** The responsibility exists in the code, it is placed in a class that does not own the information or the role that the pattern asks for, and that placement has a concrete cost: duplicated knowledge, a rule that lives in two places, or a change that is foreseeable for this business (new product types, new modules, a different storage) that would force editing several classes. A style preference with no such cost is not a violation. For example, the service that computes `item.getStock() - 1` outside `Product` was recorded as a violation of Information Expert because the same arithmetic is repeated in three classes.
- **Correct.** The responsibility is in the class that the pattern designates, and we can point at the class and the method where it happens. For example, `SaleService.registerSale` creates the `Sale` because it records the sales and has all the data to build it (Creator).
- **Not applicable.** The class has no point where the pattern could be applied, and we say why. For example, Creator does not apply to `Sale` because it creates no domain object, only a date.

We also decided not to force violations. A conditional that chooses a concrete product class while reading a JSON file (`SaleRepository.ProductDeserializer`) was judged correct for Polymorphism, because that is the only place where the type of the object has to be chosen. Creator was fully correct in the code of the Technical Lead, and the report says so.

The minimum evidence that a verdict needs is:

1. The **class and the method** involved, with the **exact lines** of the audited commit, which is stated at the top of each report.
2. For a violation, the **fragment of code** or the exact statement that shows it, an **explanation** of why it breaks the question of the pattern, and the **concrete consequence** today or under a foreseeable change.
3. For a violation, a **proposed solution** that names the pattern that would be applied and says whether it requires changing code of another team member.
4. For a correct verdict, the **place where the pattern is applied**; for a not applicable verdict, the **reason** why there is no application point.

Before consolidating, every line number and every quoted statement is checked against the code, and a finding without that evidence, or with the wrong classification, is returned to its author to be fixed in their own report.

---

## Q2: Some violations appear on the border between the code of two team members, for example when a class of one member depends in an inappropriate way on a class of another. What findings of this kind did the team identify? How were they registered in the consolidation and who was assigned their correction? Present at least one concrete case or, if there was none, explain how we verified that none existed.

**Assigned to:** Oscar Palomino (Technical Lead).

*Pending: to be written after the consolidated audit is merged.*

---

## Q3: Some patterns are closely related. Did the team find fragments of code that break more than one pattern at the same time? How were they registered without duplicating findings, and how was it decided which pattern guides the correction?

**Assigned to:** Manuel Ospino (Developer 1).

Yes, our team found several code fragments that simultaneously broke more than one GRASP pattern. This happens naturally because GRASP patterns are tightly interconnected: evaluative principles like **Low Coupling** and **High Cohesion** describe the overall architectural health of the system, whereas assignative patterns like **Information Expert**, **Polymorphism**, **Pure Fabrication**, **Indirection**, and **Protected Variations** are the concrete mechanisms through which low coupling and high cohesion are achieved. Consequently, failing to apply an assignative pattern almost always produces collateral violations of the evaluative patterns as well.

### 1. Concrete Examples of Overlapping Violations Identified by the Team

The individual audits from Developer 1 and the Technical Lead identified several clear cases of multi-pattern violations:

* **Monolithic User Interface (`ConsoleMenu`, Violation V-05 in Technical Lead Scope):**  
  `ConsoleMenu` (923 lines and 48 methods) simultaneously broke four patterns:
  * **High Cohesion:** It concentrated the main dispatch loop, eight domain submenus (`productMenu` through `promotionMenu`), low-level terminal I/O reading and parsing (`readText`, `readDate`, `parseIds`), error handling (`runGuarded`), and formatting for multiple entities (`printSale`, `printReturn`, `printWarranties`, `printPromotions`).
  * **Low Coupling:** It depended directly on all seven system services and imported nine domain model entities.
  * **Pure Fabrication:** Cross-cutting UI responsibilities (input parsing and error reporting) lacked dedicated technical helper abstractions.
  * **Protected Variations:** The top-level menu was coupled to a hardcoded `switch` statement over module numbers; introducing any new store module forces modifications to `ConsoleMenu.start()`.

* **Dual-Inventory Handling in Sales and Returns (`SaleService` & `ReturnService`, Violation V-02 in Technical Lead Scope):**  
  In `SaleService.resolveItem` / `discountStock` (and identically in `ReturnService.resolveItem` / `restoreStock`), the workflow queried both `ProductService` and `AccessoryService` and checked `item instanceof Accessory`:
  * **Indirection:** No mediating component existed between the transaction use cases and the two discrete inventory stores.
  * **Polymorphism:** Stock deduction and lookup were decided via explicit conditional branching on concrete types (`instanceof Accessory`) rather than through uniform polymorphic operations.
  * **Low Coupling:** Both `SaleService` and `ReturnService` were coupled directly to two separate inventory services and to the concrete `Accessory` subclass.

* **Hardcoded Warranty Eligibility Checks (`SaleService`, Violation V-03 in Technical Lead Scope):**  
  In `SaleService.validateExtendedWarrantyRequest` (line 254) and `assignWarranties` (line 277), warranty eligibility was checked with `resolveItem(requestedId) instanceof Console`:
  * **Polymorphism:** The system used runtime type reflection instead of delegating the query polymorphically to the product hierarchy.
  * **Protected Variations:** The rest of the sales and warranty subsystems remained exposed to instability whenever new warrantable product types are added to the store in the future.

* **Product Catalog Registration (`ProductService`, Violation V-03 in Developer 1 Scope):**  
  In `ProductService.java` (lines 59–68 and 90–99), `registerVideoGame` and `registerConsole` exposed concrete, subtype-specific parameter lists:
  * **Protected Variations:** The public service interface is unstable; introducing any new catalog item type (e.g., board games or digital subscriptions) forces adding new methods to `ProductService` and altering caller invocations in `ConsoleMenu`.
  * **Polymorphism:** Catalog ingestion lacked a polymorphic abstraction (such as receiving a `Product` entity or using a factory method).

* **External Stock Arithmetic (Violation V-01 in Technical Lead Scope):**  
  `ProductService.restoreStock` (line 160), `SaleService.discountStock` (line 312), and `AccessoryService.restoreStock` (line 241) fetched current stock, performed external arithmetic (`item.getStock() - 1` or `product.getStock() + quantity`), and called `updateStock(...)`:
  * **Information Expert:** Stock mutation arithmetic was executed outside the class that encapsulates the data (`Product`).
  * **Low Coupling:** Encapsulation leakage forced three independent caller classes to duplicate identical stock arithmetic rules.

### 2. How Overlapping Findings Were Registered Without Duplicating Findings

To maintain clarity, traceability, and avoid artificial inflation of violation counts, the team followed three consolidation rules:

1. **Single Canonical Violation ID:** Every distinct problematic code fragment was assigned exactly one violation identifier (e.g., `V-01`, `V-02`, `V-03`, `V-05`). It was never logged as multiple separate line items in the violations tables.
2. **Primary Pattern Heading with Cross-References:** The violation was cataloged under the primary pattern that represents the root cause of the flaw. Secondary and collateral patterns were explicitly noted in parentheses in the table's "Pattern" column—for example:  
   * `Indirection (also Polymorphism and Low Coupling)` for `V-02`.  
   * `Polymorphism (also Protected Variations)` for `V-03`.  
   * `High Cohesion (also Pure Fabrication, Low Coupling and Protected Variations)` for `V-05`.  
   In the individual audit text for the secondary patterns, we recorded an explicit cross-reference (e.g., *"See V-02"* or *"registered once, under V-04, whose correction relies on V-03"*), explaining the relationship without duplicating the finding.
3. **Distinct Violations for Distinct Classes:** When two findings shared a similar conceptual cause but resided in different classes and modules, they were maintained as separate violations. For example, Developer 1's `V-02` (`CategoryDiscount` using `instanceof Accessory`) and Technical Lead's `V-03` (`SaleService` using `instanceof Console`) both violate Polymorphism via `instanceof`, but they affect entirely separate files and require independent commits by different developers.

### 3. How the Guiding Pattern for Correction Was Decided

When a fragment violates multiple patterns, deciding which pattern guides the refactoring was based on two criteria:

1. **Root Cause Analysis (Causal Priority):**  
   We asked: *Which pattern addresses the fundamental structural deficiency that, when resolved, naturally eliminates the secondary violations?*
   * For `ConsoleMenu` (V-05), the root problem is excessive, multi-domain responsibility. Guiding the refactoring by **High Cohesion** (extracting `ConsoleInput` and `ConsoleErrorReporter` fabrications and splitting submenus into independent modules) naturally eliminates the Low Coupling, Pure Fabrication, and Protected Variations violations.
   * For dual-inventory access (V-02), the fundamental missing piece is an intermediate collaborator. Guiding the refactoring by **Indirection** (introducing `InventoryService`) directly removes the coupling to multiple services and eliminates the `instanceof` branching at caller sites.
   * For warranty eligibility (V-03), the root issue is behavioral dispatch based on type. Guiding the refactoring by **Polymorphism** (adding `supportsWarranty()` to `Product` and overriding it in `Console`) directly provides the stable interface required by **Protected Variations**.

2. **Prioritizing Operational Mechanisms over Evaluative Yardsticks:**  
   Larman distinguishes between evaluative principles (**Low Coupling**, **High Cohesion**) that measure design quality, and operational/assignative patterns (**Information Expert**, **Polymorphism**, **Pure Fabrication**, **Indirection**, **Protected Variations**) that specify *how* to allocate responsibilities. Whenever a defect involved both an evaluative goal and an operational pattern, the **operational pattern** was chosen to guide the correction, as it prescribes the exact structural mechanism (such as creating an intermediate service, introducing a polymorphic method, or moving arithmetic into an entity) to implement.

---

## Q4: The correction must preserve the external behavior of the system. What strategy does the team follow to guarantee that all the menu operations keep working the same? What evidence is shown at the end to prove that nothing was broken?

**Assigned to:** Veronica Padilla (Developer 2).

*Pending.*

---

## Q5: In what order are the violations corrected and why? When the correction of a violation involves code of more than one team member, how is the work coordinated to avoid conflicts in the shared branch?

**Assigned to:** Oscar Palomino (Technical Lead).

*Pending: to be written after the consolidated audit is merged.*
