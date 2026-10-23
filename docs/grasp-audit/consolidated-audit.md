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

---

## 5. Definitive List of Violations

There are 19 violations. The identifiers V-01 to V-08 are those of the Technical Lead's report, V-09 to V-11 are those of Developer 1's report (V-01 to V-03 there), and V-12 to V-19 are those of Developer 2's report (V-01 to V-08 there). Each one lists the part of the correction that belongs to each team member. A class or abstraction that a correction creates is created by the responsible of the violation that originates it. Sections 6 and 7 give the order in which the parts must be applied.

### V-01 — Information Expert
* **Origin:** `leader-audit` V-01. Developer 2's report registers the `AccessoryService` line as a cross-reference.
* **Evidence:** `ProductService.restoreStock` line 160, `product.updateStock(product.getStock() + quantity);`. `SaleService.discountStock` line 312, `int newStock = item.getStock() - 1;`. `AccessoryService.restoreStock` line 241, `accessory.updateStock(accessory.getStock() + quantity);`.
* **Explanation:** The stock belongs to `Product`, but three services read it, do the arithmetic outside the entity and write it back.
* **Consequence:** The rule of how the stock grows or shrinks is repeated in three places; any change (for example, reserved stock) has to be applied to each of them.
* **Agreed solution:** Add `increaseStock(int)` and `decreaseStock(int)` to `Product`. The three callers delegate to them and keep the validation and the persistence they have today.
* **Responsible for the correction:** Developer 1 (`Product`), Technical Lead (`ProductService.restoreStock`, `SaleService.discountStock`) and Developer 2 (`AccessoryService.restoreStock`). Developer 1 goes first.

### V-02 — Indirection (also Polymorphism and Low Coupling)
* **Origin:** `leader-audit` V-02. Developer 2's report registers its part as a cross-reference.
* **Evidence:** `SaleService.resolveItem` lines 297–303 and `discountStock` lines 311–318 (`instanceof Accessory` at line 313). The same code in `ReturnService.resolveItem` lines 184–190 and `restoreStock` lines 199–205 (`instanceof Accessory` at line 200). Fields `productService` and `accessoryService` in `SaleService` (lines 43–44) and in `ReturnService` (lines 29–30).
* **Explanation:** Nothing mediates between the use cases and the two inventories, so each use case looks the item up in both services and decides by type which one changes the stock.
* **Consequence:** A new category of sellable items requires editing `SaleService` and `ReturnService`, and both stay coupled to two services and to the concrete class `Accessory`.
* **Agreed solution:** Create `InventoryService` in the service layer with `findItem(String)`, `decreaseStock(String, int)` and `restoreStock(String, int)`. It finds the item in the product service first and in the accessory service afterwards, so it needs no type check, and it uses the stock methods of V-01. `SaleService` and `ReturnService` use only it, and `Main` wires it.
* **Responsible for the correction:** Technical Lead creates `InventoryService` and changes `SaleService` and `Main`. Developer 2 changes `ReturnService` and, if the inventory needs it, `AccessoryService`.

### V-03 — Polymorphism (also Protected Variations)
* **Origin:** `leader-audit` V-03. Developer 2's report registers its part as a cross-reference.
* **Evidence:** `SaleService.validateExtendedWarrantyRequest` line 254 and `assignWarranties` line 277 (`instanceof Console`). The same decision in `WarrantyService.validateWarrantyRequest` line 288 and in `ReturnService.registerReturn` line 162.
* **Explanation:** Which items have a warranty is decided by asking for the concrete class instead of asking the item.
* **Consequence:** A new product type with a warranty requires finding and editing four places, and forgetting one gives a wrong behavior without any error.
* **Agreed solution:** Add `supportsWarranty()` to `Product` (returns `false`) and override it in `Console` (returns `true`). The four callers use it.
* **Responsible for the correction:** Developer 1 (`Product`, `Console`), Technical Lead (`SaleService`) and Developer 2 (`WarrantyService`, `ReturnService`). Developer 1 goes first.

### V-04 — Controller (also Polymorphism)
* **Origin:** `leader-audit` V-04.
* **Evidence:** `ConsoleMenu.askExtendedWarranties` lines 321–334: line 324 `productService.findById(itemId)` and line 325 `item instanceof Console`. The same rule is enforced by `SaleService.validateExtendedWarrantyRequest` (line 254).
* **Explanation:** The user interface decides which items are offered an extended warranty, which is a business rule.
* **Consequence:** The rule lives in two classes that can disagree, and the menu has to know the domain classes `Product` and `Console`.
* **Agreed solution:** `SaleService` provides the identifiers of the items that can receive an extended warranty, using `supportsWarranty()` from V-03. `ConsoleMenu` only asks the user about each one, in the same order and with the same text as today.
* **Responsible for the correction:** Technical Lead. It must be applied after V-03.

### V-05 — High Cohesion (also Pure Fabrication, Low Coupling and Protected Variations)
* **Origin:** `leader-audit` V-05.
* **Evidence:** `ConsoleMenu`, the whole class: 923 lines and 48 methods; eight submenus (`productMenu` line 126 to `promotionMenu` line 717); input helpers (`readDate` line 842, `parseIds`, `readText`, `readInt` and `readDouble` lines 880–923); `runGuarded` (lines 864–878); four `print*` methods; seven services (lines 42–48); nine model imports (lines 7–15); the `switch` of `start()` in lines 91–121.
* **Explanation:** One class holds the responsibilities of eight modules and of several technical tasks.
* **Consequence:** Any change in any module edits this file, the three developers collide in it, adding a module requires editing `start()`, and no menu can be understood or tested apart.
* **Agreed solution:** Extract `ConsoleInput` (reading and parsing the input) and `ConsoleErrorReporter` (the content of `runGuarded`). Create one menu class per module, each with only its own services, behind a common `Menu` interface; `ConsoleMenu` keeps only the main loop. Prompts, messages and order of the options stay identical.
* **Responsible for the correction:** Technical Lead. It is applied last, because it moves the code that V-04, V-11 and V-14 change.

### V-06 — Information Expert
* **Origin:** `leader-audit` V-06.
* **Evidence:** `ConsoleMenu.printSale` lines 388–390 repeat the condition `getAppliedPromotionName() != null && getDiscountAmount() > 0` that `Sale.generateReceipt` uses at line 184.
* **Explanation:** `Sale` knows when it has a discount, but the menu repeats the rule instead of asking the sale.
* **Consequence:** If the rule changes, the receipt and the list of sales can show different information.
* **Agreed solution:** Add `hasDiscount()` to `Sale` and use it in `generateReceipt` and in `printSale`.
* **Responsible for the correction:** Technical Lead.

### V-07 — Information Expert
* **Origin:** `leader-audit` V-07.
* **Evidence:** `SaleService.applyBestPromotion` lines 226–231: line 229 takes `sale.getProducts()` and calls `promotion.calculateDiscount(...)`, and lines 228–229 write the result with `setAppliedPromotionName` and `setDiscountAmount`.
* **Explanation:** The service takes data from the sale, computes with it and writes the result back in two calls, although the sale owns the products, the name and the discount.
* **Consequence:** The two values can be left inconsistent, and the setters are public for any class.
* **Agreed solution:** Add `applyPromotion(Promotion)` to `Sale`, which sets the name and the discount together. The two setters stop being used and are removed.
* **Responsible for the correction:** Technical Lead.

### V-08 — Pure Fabrication
* **Origin:** `leader-audit` V-08. Developer 2's report registers its part as a cross-reference.
* **Evidence:** `SaleRepository.ProductDeserializer` lines 139–160 and `LocalDateAdapter` lines 166–177, with copies in `ReturnRepository` (lines 128 and 155) and `LocalDateAdapter` in `PromotionRepository` (line 149).
* **Explanation:** The technical task of converting dates and rebuilding products from JSON is repeated in three repositories.
* **Consequence:** A change in the date format or a new product type has to be repeated in each copy.
* **Agreed solution:** Extract package-private `LocalDateAdapter` and `ProductDeserializer` classes in the persistence package and use them from the three repositories. The files keep exactly the same content.
* **Responsible for the correction:** Technical Lead creates the classes and changes `SaleRepository`. Developer 2 changes `ReturnRepository` and `PromotionRepository`. It must be applied before V-18.

### V-09 — Polymorphism
* **Origin:** `developer1-audit` V-01.
* **Evidence:** `ProductService` lines 24–25 (`List<VideoGame> videoGames`, `List<Console> consoles`) and `listAllProducts()` lines 104–109.
* **Explanation:** The inventory is kept in two concrete lists and merged on every call, instead of in one polymorphic `List<Product>`.
* **Consequence:** A new product subtype adds another list and another change in `listAllProducts()`.
* **Agreed solution:** `ProductService` keeps one `List<Product>`. `ProductRepository` gets `saveAllProducts(List<Product>)` and `loadAllProducts()`, which keep the two files (`videogames.json` and `consoles.json`) with the same content and order; separating the products by type is done inside the repository, which is the one place where the type must be chosen. **Behavior condition:** today the listing shows all the video games first and then all the consoles, even when they were registered interleaved. The listing must keep that order, and Developer 1 demonstrates it with the scenario `01-products` of the non-regression check. If the order cannot be kept without bringing back a type check in the service, the violation moves to the out-of-scope section with that justification.
* **Responsible for the correction:** Developer 1.

### V-10 — Polymorphism
* **Origin:** `developer1-audit` V-02.
* **Evidence:** `CategoryDiscount.belongsToTargetCategory` lines 76–81, `product instanceof Accessory` at line 77.
* **Explanation:** The check asks for the concrete class even though `Accessory.getCategory()` already returns `"ACCESSORY"` (line 91).
* **Consequence:** The class stays coupled to `Accessory` and the branch is redundant.
* **Agreed solution:** Remove the `instanceof` branch and compare `targetCategory` with `product.getCategory()` for every product. The result is identical because of the value that `Accessory.getCategory()` returns.
* **Responsible for the correction:** Developer 1.
