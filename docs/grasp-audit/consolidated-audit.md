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
* Its line numbers belonged to the code before Requirement 6 (for example, the methods `validate` and `isBlank` of `ProductService` no longer existed), and one violation, V-02, described code that Requirement 6 had already corrected.
* Its violation V-01 was about `ProductService.restoreStock`, which the responsibility table assigns to the Technical Lead.
* It did not audit `Return`, `Sale.canBeReturned`, the hierarchies of accessories, promotions and warranties, nor the packages `exceptions` and `validation`.
* The violation table lacked fields required by the requirement, and the solution of one violation required changing `ConsoleMenu` without saying so.

The corrected report was verified on `8b0be4b`: `ProductService` lines 24–25, 59–68, 90–99 and 104–109, and `CategoryDiscount` lines 76–81 match what the report quotes, and `Accessory.getCategory()` returns `"ACCESSORY"`, which the correction of its V-02 relies on.

### Developer 2 (`developer2-audit.md`): accepted without returning
More than thirty references were checked on `8b0be4b`, among them `ReturnService` lines 28, 102–103, 124–126, 306, 357–358 and 374–376; `WarrantyService` lines 89–93, 168, 208, 288, 291–297 and 325–327; `AccessoryService` lines 29–31, 86–88, 149–153, 246–248 and 268–270; and `PromotionService` lines 28–30, 107–109 and 209–211. All of them match the code. The report also shows that `ReturnService.personService` (line 28) is assigned in the constructor (line 53) and is not used by any other method, which is the basis of its V-02.

### Classification
No finding was reclassified. In two cases the agreed solution differs from the proposed one, because the proposed one would have changed the behavior of the system or introduced a new violation (V-09 and V-11, section 5).
