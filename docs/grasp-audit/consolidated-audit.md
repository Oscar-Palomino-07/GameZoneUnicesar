# Consolidated GRASP Audit — GameZone Unicesar

* **Consolidator:** Oscar Palomino (Technical Lead)
* **Branch:** `feature/grasp-audit`
* **Date:** 2026-10-23
* **Inputs:** `docs/grasp-audit/leader-audit.md`, `docs/grasp-audit/developer1-audit.md` and `docs/grasp-audit/developer2-audit.md`.
* **Reference for Phase 2:** this document. The team corrects exactly the violations listed in section 5.

---

## 1. Audited Commit

The three reports audit the same state of the code: commit **`8b0be4b`**, the final state of Requirement 6 (head of `feature/exception-handling`). The Technical Lead's and Developer 2's reports state that commit in their header. Developer 1's report does not state it, so its line numbers were checked against the same commit (section 2).

The source files of `8b0be4b` are the ones that `develop` holds once the pull request of Requirement 6 is merged: that pull request only adds the Requirement 6 work of the Technical Lead (`ConsoleMenu`, `Main`, the non-regression script and documents). The classes of the other team members were already in `develop` before it.

---

## 2. Validation of the Evidence

### Technical Lead (`leader-audit.md`)
The author checked every line number against `8b0be4b` before publishing it. The metrics quoted for `ConsoleMenu` (923 lines, 48 methods, seven services, nine model imports) were counted on the file.

### Developer 1 (`developer1-audit.md`): returned once, accepted after correction
The first version was returned to its author on 2026-10-22 for four reasons, and the corrected version was merged:
* Its line numbers belonged to the code before Requirement 6 (for example, the methods `validate` and `isBlank` of `ProductService` no longer existed), and one violation (V-02 of the first version) described code that Requirement 6 had already corrected.
* Its violation V-01 was about `ProductService.restoreStock`, which the responsibility table assigns to the Technical Lead.
* It did not audit `Return`, `Sale.canBeReturned`, the hierarchies of accessories, promotions and warranties, nor the packages `exceptions` and `validation`.
* The violation table lacked fields required by the requirement, and the solution of one violation required changing `ConsoleMenu` without saying so.

The corrected report was verified on `8b0be4b`: `ProductService` lines 24–25, 59–68, 90–99 and 104–109, and `CategoryDiscount` lines 76–81 match what the report quotes, and `Accessory.getCategory()` returns `"ACCESSORY"`, which the correction of its V-02 relies on.

### Developer 2 (`developer2-audit.md`): accepted without returning
Twenty references to the four services of the report were checked on `8b0be4b`: `ReturnService` lines 28, 102–103, 124–126, 306, 357–358 and 374–376; `WarrantyService` lines 89–93, 168, 208, 288, 291–297 and 325–327; `AccessoryService` lines 29–31, 86–88, 149–153, 246–248 and 268–270; and `PromotionService` lines 28–30, 107–109 and 209–211. All of them match the code. The report also shows that `ReturnService.personService` (line 28) is assigned in the constructor (line 53) and is not used by any other method, which is the basis of its V-02.

### Classification
No finding was reclassified. In two cases the agreed solution differs from the proposed one, because the proposed one would have changed the behavior of the system or introduced a new violation (V-09 and V-11, section 5).

---

## 3. Coverage Table

The system has 43 classes. Each one was audited by the team member who is responsible for it according to the responsibility table of the requirement. No class was left without audit, and no method was audited twice: when a class has code of more than one member, the methods are split as shown in the last column.

| Package | Class | Audited by | Notes |
|---|---|---|---|
| `com.gamezone` | `Main` | Technical Lead | |
| `exceptions` | `GameZoneException`, `ResourceNotFoundException`, `BusinessRuleException`, `InvalidDataException`, `PersistenceException` | Developer 1 | Requirement 6 |
| `validation` | `ProductValidator`, `PersonValidator`, `SaleValidator` | Developer 1 | Requirement 6 |
| `model` | `Product`, `VideoGame`, `Console` | Developer 1 | |
| `model` | `Person`, `Customer`, `Seller` | Developer 2 | |
| `model` | `Sale` | Technical Lead | `canBeReturned()` is Developer 1's (Requirement 3) |
| `model` | `Accessory`, `Controller`, `Cable`, `Memory` | Developer 1 | |
| `model` | `Promotion`, `PercentageDiscount`, `CategoryDiscount`, `BulkPurchaseDiscount` | Developer 1 | |
| `model` | `Return` | Developer 1 | |
| `model` | `Warranty`, `BasicWarranty`, `ExtendedWarranty` | Developer 1 | |
| `persistence` | `ProductRepository` | Developer 1 | The translation of `IOException` into `PersistenceException` is Developer 2's (Requirement 6) |
| `persistence` | `PersonRepository` | Developer 2 | |
| `persistence` | `SaleRepository` | Technical Lead | The translation of `IOException` is Developer 2's (Requirement 6) |
| `persistence` | `AccessoryRepository`, `PromotionRepository`, `ReturnRepository`, `WarrantyRepository` | Developer 2 | |
| `service` | `ProductService` | Developer 1 | `restoreStock` is the Technical Lead's (Requirement 3); the validator calls are Developer 2's (Requirement 6) |
| `service` | `PersonService` | Developer 2 | |
| `service` | `SaleService` | Technical Lead | The validator calls and the two `BusinessRuleException` are Developer 2's (Requirement 6) |
| `service` | `AccessoryService`, `PromotionService`, `ReturnService`, `WarrantyService` | Developer 2 | `ReturnService` includes the monthly balance |
| `ui` | `ConsoleMenu` | Technical Lead | Includes the extensions of every module and the differentiated handling of exceptions |

**Result:** 43 of 43 classes audited by their responsible. The cross-references of Developer 2's report to the Technical Lead's findings ("see leader V-XX") are references, not second audits.

---

## 4. Team Verdict for Each Pattern

The verdict of the team for a pattern is `Violation` when at least one of the three reports registers one, and `Correct` when the three reports find it correctly applied or not applicable.

| Pattern | Technical Lead | Developer 1 | Developer 2 | **Team verdict** | Definitive violations |
|---|---|---|---|---|---|
| Information Expert | Violation | Correct | Violation | **Violation** | V-01, V-06, V-07, V-12 |
| Creator | Correct | Correct | Correct | **Correct** | — |
| Controller | Violation | Correct | Correct | **Violation** | V-04 |
| Low Coupling | Violation | Correct | Violation | **Violation** | V-05, V-13 (also V-02) |
| High Cohesion | Violation | Correct | Violation | **Violation** | V-05, V-14 |
| Polymorphism | Violation | Violation | Violation | **Violation** | V-03, V-09, V-10, V-15, V-16 (also V-02 and V-04) |
| Pure Fabrication | Violation | Correct | Violation | **Violation** | V-08, V-18, V-19 (also V-05 and V-14) |
| Indirection | Violation | Correct | Correct | **Violation** | V-02 |
| Protected Variations | Violation | Violation | Violation | **Violation** | V-03, V-05, V-11, V-17 (also V-02, V-15 and V-16) |

When a fragment breaks more than one pattern, the violation is registered once, under the pattern that guides its correction, and the other patterns are named in the explanation. Creator is the only pattern that the three reports find correct.
