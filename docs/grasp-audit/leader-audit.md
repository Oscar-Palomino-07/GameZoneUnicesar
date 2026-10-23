# GRASP Audit Report — Technical Lead

* **Auditor:** Oscar Palomino (Technical Lead)
* **Branch:** `feature/grasp-audit`
* **Date:** 2026-10-22
* **Audited code state:** `develop` plus Requirement 6 (head of `feature/exception-handling`, commit `8b0be4b`). Every line number in this report refers to that state. `ConsoleMenu` and `Main` differ from the current `develop` because of Requirement 6, which is still pending its merge; the rest of the audited code is identical.
* **Target reviewers:** Manuel Ospino (Developer 1) / Veronica Padilla (Developer 2)

---

## 1. Audited Scope & Method Inventory

In accordance with the responsibility table of the requirement, the Technical Lead audits the code that he developed in each requirement. The methods that other team members added to these classes are audited by their authors and are listed at the end of this section.

### Module 1: Sales, user interface and startup (Taller 1)
* **`com.gamezone.model.Sale`:** constructor, `getId()`, `getDate()`, `getCustomer()`, `getSeller()`, `getProducts()`, `calculateSubtotal()`, `calculateTotal()`, `generateReceipt()`, `formatAmount(double)`.
* **`com.gamezone.persistence.SaleRepository`:** `saveAll(List<Sale>)`, `loadAll()`, `writeList(...)`, `readList(...)`, inner classes `ProductDeserializer` and `LocalDateAdapter`.
* **`com.gamezone.service.SaleService`:** constructor, both `registerSale(...)` overloads, `nextSaleId()`, `viewAllSales()`, `viewSalesByCustomer(String)`, `viewSalesBySeller(String)`, `save()`, `findById(String)`.
* **`com.gamezone.ui.ConsoleMenu`:** `start()`, `productMenu()`, `personMenu()`, `saleMenu()`, `registerVideoGame()`, `registerConsole()`, `listAllProducts()`, `registerCustomer()`, `listCustomers()`, `listSellers()`, `registerSale()`, `viewAllSales()`, `viewSalesByCustomer()`, `viewSalesBySeller()`, `viewSaleDetail()`, `printSale(Sale)`, `parseIds(String)`, `readText(String)`, `readInt(String)`, `readDouble(String)`.
* **`com.gamezone.Main`:** `main(String[])` and `createMenu()`.

### Module 2: Accessories (Requirement 1)
* **`SaleService`:** `resolveAndValidateItems(List<String>)` (item resolution only), `resolveItem(String)`, `discountStock(Product)`.
* **`ConsoleMenu`:** `accessoryMenu()`, `registerController()`, `registerCable()`, `registerMemory()`, `listAllAccessories()`, `listAccessoriesByType()`, `listAccessoriesCompatibleWith()`, and the accessory identifiers read in `registerSale()` (lines 303–304).

### Module 3: Promotions (Requirement 2)
* **`SaleService`:** `applyBestPromotion(Sale)`.
* **`Sale`:** `getAppliedPromotionName()`, `setAppliedPromotionName(String)`, `getDiscountAmount()`, `setDiscountAmount(double)`, the discount part of `calculateTotal()` and the breakdown of `generateReceipt()`.
* **`ConsoleMenu`:** `promotionMenu()`, `registerPercentagePromotion()`, `registerCategoryPromotion()`, `readPromotionCategory()`, `registerBulkPurchasePromotion()`, `printPromotions(...)`, `readDate(String)`, and the promotion text of `printSale(Sale)`.

### Module 4: Returns (Requirement 3)
* **`com.gamezone.service.ProductService`:** `restoreStock(String, int)` (lines 156–162).
* **`ConsoleMenu`:** `returnMenu()`, `registerReturn()`, `viewAllReturns()`, `viewReturnsByCustomer()`, `viewReturnsBySale()`, `printReturn(Return)`, `reportsMenu()`, `monthlyBalance()`.

### Module 5: Warranties (Requirement 4)
* **`SaleService`:** `validateExtendedWarrantyRequest(List<String>, List<String>)` (the rule; the typed exceptions of its two `throw` statements are Developer 2's), `assignWarranties(Sale, Set<String>)`.
* **`Sale`:** `getWarrantyExtraCost()`, `setWarrantyExtraCost(double)`.
* **`ConsoleMenu`:** `askExtendedWarranties(List<String>)`, `warrantyMenu()`, `viewWarrantyByProduct()`, `viewWarrantiesExpiringSoon()`, `printWarranties(...)`.

### Module 6: Exceptions and validations (Requirement 6)
* **`ConsoleMenu`:** `runGuarded(Runnable)` (lines 864–878) and its fifteen call sites.
* **`Main`:** the `try/catch` of `PersistenceException` in `main(String[])` (lines 43–49).

### Code of other team members in these classes (not audited here)
* **`Sale.canBeReturned()`** (lines 199–204): Developer 1, Requirement 3.
* **`SaleService`:** the calls to `SaleValidator`, `PersonValidator` and `ProductValidator` and the typed exceptions (lines 151–160, 207–208, 251–255): Developer 2, Requirement 6.
* **`SaleRepository`:** the translation of `IOException` into `PersistenceException` (lines 104–106 and 128–130): Developer 2, Requirement 6.

### Method
Every verdict is based on the code of the audited state. The metrics quoted (lines, methods, dependencies) were counted on the files. A pattern that has no application point is reported as `Not applicable`, and a pattern that is correctly applied is reported as `Correct` with the place where it is applied.

---

## 2. Pattern 1: Information Expert

### Principle
Assign a responsibility to the class that has the information needed to fulfill it. State changes and calculations must live in the class that encapsulates the attributes involved.

### Audit by Class
* **`Sale`:** `Correct`
  * `calculateSubtotal()` (lines 142–148) and `calculateTotal()` (lines 157–159) are computed from the sale's own `products`, `discountAmount` and `warrantyExtraCost`.
  * `generateReceipt()` (lines 169–193) builds the receipt from the sale's own fields and from the data of its customer, seller and products.
* **`SaleRepository`:** `Correct`
  * It is the expert on where and how sales are stored: the file path (line 50), the `TypeToken` (lines 52–53) and the Gson configuration (lines 61–66).
* **`SaleService`:** `Violation`
  * `discountStock(Product)` (lines 311–318): line 312, `int newStock = item.getStock() - 1;`, reads the stock of the item, subtracts outside the item and writes the result back. See **V-01**.
  * `applyBestPromotion(Sale)` (lines 226–231): the service takes the products out of the sale (line 229, `sale.getProducts()`), computes the discount and writes the name and the amount through two setters (lines 228–229). The sale owns all that information. See **V-07**.
  * `nextSaleId()` (lines 377–389) and `viewSalesByCustomer`/`viewSalesBySeller` (lines 334–358) use the `sales` list that the service holds, so they are `Correct`.
* **`ConsoleMenu`:** `Violation`
  * `printSale(Sale)` (lines 381–392): lines 388–390 repeat the condition `getAppliedPromotionName() != null && getDiscountAmount() > 0`, which is the same rule that `Sale.generateReceipt()` applies at line 184. The knowledge of when a sale has a discount belongs to `Sale`. See **V-06**.
* **`ProductService.restoreStock(String, int)`:** `Violation`
  * Line 160, `product.updateStock(product.getStock() + quantity);`, performs the addition outside `Product`. See **V-01**.
* **`Main`:** `Correct`
  * `createMenu()` (lines 58–86) is the only place that knows every repository and every service, so it is the one that wires them.

---

## 3. Pattern 2: Creator

### Principle
Assign class B the responsibility of creating instances of class A when B contains or records A, closely uses A, or has the data to initialize A.

### Audit by Class
* **`Sale`:** `Not applicable`
  * The class receives its customer, seller and products in the constructor (lines 42–48). The only object it creates is the `LocalDate` of the sale, a value type.
* **`SaleRepository`:** `Correct`
  * `ProductDeserializer.deserialize(...)` (lines 143–159) creates the concrete product subtype because it is the class that holds the serialized data. `LocalDateAdapter.deserialize(...)` (lines 174–176) creates the dates for the same reason.
* **`SaleService`:** `Correct`
  * `registerSale(...)` creates the sale at line 164, `new Sale(nextSaleId(), customer, seller, products)`, and records it at line 181 (`sales.add(sale)`). It holds the sales list and has every datum needed to initialize the sale. The warranties are created by `WarrantyService` (lines 278 and 280), which is the class that records them.
* **`ConsoleMenu`:** `Correct`
  * It creates only its `Scanner` (line 49) and the lists of identifiers that it reads from the user (`parseIds`, lines 880–888). It creates no domain object.
* **`Main`:** `Correct`
  * `createMenu()` (lines 58–86) creates the repositories, the services and the menu, because it is the composition root and owns the dependency order described in its JavaDoc.
* **`ProductService.restoreStock(String, int)`:** `Not applicable`
  * The method creates no object.

---

## 4. Pattern 3: Controller

### Principle
Assign the responsibility of receiving and coordinating the system events to a non-UI class that represents the system or a use case. The user interface only captures input and shows output.

### Audit by Class
* **`ConsoleMenu`:** `Violation`
  * Most operations only read input and delegate. For example `registerSale()` (lines 298–311) reads the data and calls `saleService.registerSale(...)` (line 307).
  * `askExtendedWarranties(List<String>)` (lines 321–334) does not delegate: it resolves each item through `productService.findById(...)` (line 324) and decides with `item instanceof Console` (line 325) which items are offered an extended warranty. That is a business rule, and `SaleService.validateExtendedWarrantyRequest(...)` enforces the same rule at line 254. See **V-04**.
* **`SaleService`:** `Correct`
  * `registerSale(...)` (lines 147–184) is the use-case controller of the sale registration: it coordinates the validators, the item resolution, the promotion, the warranties, the stock update and the persistence in a fixed order documented in its JavaDoc (lines 103–120). The user interface does not contain any of those steps.
* **`Sale`, `SaleRepository`:** `Not applicable`
  * A domain entity and a file-storage class do not receive system events.
* **`Main`:** `Not applicable`
  * It starts the application and wires the objects. Its only error handling (lines 44–48) prints the persistence message when the data files cannot be loaded.
* **`ProductService.restoreStock(String, int)`:** `Not applicable`
  * It is a step that `ReturnService` invokes inside the return use case, not a point where the system events arrive.

---

## 5. Pattern 4: Low Coupling

### Principle
Assign responsibilities so that the dependencies between classes stay low, which reduces the impact of changes and increases reuse.

### Audit by Class
* **`ConsoleMenu`:** `Violation`
  * It depends on the seven services of the system (fields in lines 42–48 and constructor in lines 62–64) and on nine classes of the model (imports in lines 7–15: `Accessory`, `Console`, `Customer`, `Product`, `Promotion`, `Return`, `Sale`, `Seller` and `Warranty`).
  * A change in the API of any service, or in any of those entities, can force a change in this class. See **V-05**.
* **`SaleService`:** `Violation`
  * It receives six collaborators (fields in lines 41–46). `ProductService` and `AccessoryService` are used only to resolve an item (`resolveItem`, lines 297–303) and to change its stock (`discountStock`, lines 311–318), and the service is also tied to the concrete classes `Console` (lines 254 and 277) and `Accessory` (line 313). See **V-02** and **V-03**.
* **`Sale`:** `Correct`
  * It depends only on `Customer`, `Seller` and `Product` of the model and on standard Java types (imports in lines 3–6).
* **`SaleRepository`:** `Correct`
  * It knows only the model classes it serializes and Gson (imports in lines 3–33). It does not know the services or the user interface.
* **`Main`:** `Correct`
  * It depends on every class by design, because it is the composition root. No other class depends on `Main`.
* **`ProductService.restoreStock(String, int)`:** `Correct`
  * It depends on `ProductValidator` and on `Product`, as any method of its class does.

---

## 6. Pattern 5: High Cohesion

### Principle
Keep the responsibilities of a class focused and closely related. Avoid classes that mix tasks of different kinds.

### Audit by Class
* **`ConsoleMenu`:** `Violation`
  * It has 923 lines and 48 methods. It mixes: the main menu and eight submenus (`start()` line 77, `productMenu()` 126, `personMenu()` 154, `saleMenu()` 182, `returnMenu()` 398, `reportsMenu()` 489, `accessoryMenu()` 523, `warrantyMenu()` 647, `promotionMenu()` 717); the reading and parsing of the user input (`readDate` 842, `parseIds` 880, `readText` 890, `readInt` 899, `readDouble` 912); the translation of errors into messages (`runGuarded` 864–878); and the formatting of four kinds of entities (`printSale` 381, `printReturn` 481, `printWarranties` 697, `printPromotions` 821).
  * Every module of the system has code in this single class. See **V-05**.
* **`SaleService`:** `Correct`
  * All its methods belong to the sale use case: `registerSale` and the steps it is made of (`resolveAndValidateItems`, `applyBestPromotion`, `validateExtendedWarrantyRequest`, `assignWarranties`, `discountStock`), the queries over the sales (`viewAllSales`, `viewSalesByCustomer`, `viewSalesBySeller`, `findById`) and their persistence (`save`, `nextSaleId`).
* **`Sale`:** `Correct`
  * It holds the data of a sale and the calculations and the receipt derived from that data.
* **`SaleRepository`:** `Correct`
  * It only reads and writes the sales file. The duplicated helper classes it contains are reported under Pure Fabrication (**V-08**).
* **`Main`:** `Correct`
  * It has one task: to create the objects and start the menu (lines 43–86).
* **`ProductService.restoreStock(String, int)`:** `Correct`
  * It restores the stock of a product and persists the change, which is one task.

---

## 7. Pattern 6: Polymorphism

### Principle
When behavior varies by type, assign the behavior to the types through polymorphic operations instead of conditionals on the type.

### Audit by Class
* **`SaleService`:** `Violation`
  * `validateExtendedWarrantyRequest(...)` (line 254, `resolveItem(requestedId) instanceof Console`) and `assignWarranties(...)` (line 277, `product instanceof Console`) decide which items have warranties by asking for the concrete class. See **V-03**.
  * `discountStock(Product)` (line 313, `item instanceof Accessory`) chooses which inventory service to call by the concrete class. See **V-02**.
  * `applyBestPromotion(...)` (line 229) calls `promotion.calculateDiscount(...)`, an abstract method that each promotion implements, without checking the promotion type. That part is `Correct`.
* **`ConsoleMenu`:** `Violation`
  * `askExtendedWarranties(...)` (line 325, `item instanceof Console`) repeats the type check of the service. It is registered once, under **V-04**, whose correction relies on the polymorphic method proposed in **V-03**.
* **`SaleRepository`:** `Correct`
  * `ProductDeserializer.deserialize(...)` (lines 143–159) chooses the subtype from the attributes present in the JSON. That branching happens where the object is created from stored data, which is the one place where the type must be chosen. After the object is created, the rest of the system works with `Product` polymorphically.
* **`Sale`:** `Correct`
  * No method of the class inspects the type of a product: `calculateSubtotal()` (lines 142–148) and `generateReceipt()` (lines 179–182) only call `getPrice()`, `getTitle()` and `getId()`.
* **`Main`, `ProductService.restoreStock(String, int)`:** `Not applicable`
  * Neither contains behavior that varies by type.

---

## 8. Pattern 7: Pure Fabrication

### Principle
Assign a cohesive set of responsibilities to an artificial class that does not represent a domain concept, to keep high cohesion and low coupling in the domain classes.

### Audit by Class
* **`SaleRepository`:** `Violation`
  * It has two private helper classes: `ProductDeserializer` (lines 139–160) and `LocalDateAdapter` (lines 166–177). The same code is repeated in `ReturnRepository` (`ProductDeserializer` line 128, `LocalDateAdapter` line 155) and `LocalDateAdapter` also in `PromotionRepository` (line 149). The shared technical task of converting dates and rebuilding products from JSON never received its own class. See **V-08**.
* **`ConsoleMenu`:** `Violation`
  * The reading and validation of input (`readText`, `readInt`, `readDouble`, `readDate`, `parseIds`) and the translation of errors into messages (`runGuarded`) are technical tasks that are not part of any menu flow and have no class of their own. See **V-05**.
* **`SaleService`:** `Correct`
  * An application service is an artificial class: it does not exist in a video game store, and it concentrates the orchestration of the sale (lines 147–184) so that `Sale` stays a simple entity.
* **`Main`:** `Correct`
  * It is a technical class with a single job, the dependency wiring, which no domain class should do.
* **`Sale`:** `Not applicable`
  * It represents a real concept of the domain.
* **`ProductService.restoreStock(String, int)`:** `Correct`
  * It lives in a service, which is the fabrication that holds the stock operations and their persistence, so `Product` does not need to know about files.

---

## 9. Pattern 8: Indirection

### Principle
Assign responsibility to an intermediate object that mediates between two components, so they are not coupled directly.

### Audit by Class
* **`SaleService`:** `Violation`
  * Nothing mediates between the sale use case and the two inventories (products and accessories). `SaleService` talks to both `ProductService` and `AccessoryService` and decides by type which one to use: `resolveItem` (lines 297–303) and `discountStock` (lines 311–318). `ReturnService` repeats the same code (`resolveItem` line 184, `restoreStock` lines 199–205). See **V-02**.
* **`ConsoleMenu`:** `Correct`
  * The user interface never touches a repository or a file: its imports (lines 16–22) are only services, so the services are the mediators between the interface and the persistence layer.
* **`SaleRepository`:** `Correct`
  * It mediates between the `Sale` objects and the JSON file. The services only know `saveAll(...)` and `loadAll()` (lines 74–86).
* **`Main`:** `Correct`
  * The wiring of the objects goes through one place, so no class creates its own collaborators.
* **`Sale`, `ProductService.restoreStock(String, int)`:** `Not applicable`
  * Neither of them mediates between two components.

---

## 10. Pattern 9: Protected Variations

### Principle
Identify the points of predicted variation or instability and create a stable interface around them, so the rest of the system is protected from the changes.

### Audit by Class
* **`SaleService`:** `Violation`
  * The business foresees new kinds of products, and a new kind that has a warranty would require editing `validateExtendedWarrantyRequest` (line 254) and `assignWarranties` (line 277) because they test for `Console`. See **V-03**. The same happens with a new inventory for `discountStock` (line 313). See **V-02**.
  * The promotions are protected: `applyBestPromotion` (lines 226–231) depends on the abstract `Promotion.calculateDiscount(...)`, so a new type of promotion does not change the service. That part is `Correct`.
* **`ConsoleMenu`:** `Violation`
  * Adding a module to the system forces editing `start()`: the options are printed in lines 80–89 and dispatched by the `switch` in lines 91–121. See **V-05**.
* **`SaleRepository`:** `Correct`
  * The file path (line 50), the Gson configuration and the helper classes are private. If the storage format changed, only this class would change, because the rest of the system uses `saveAll(...)` and `loadAll()` (lines 74–86).
* **`Sale`:** `Correct`
  * All its state is private (lines 24–31), and the rest of the system accesses it through methods.
* **`Main`:** `Correct`
  * A change in the way a service is built affects only the wiring in `createMenu()`.
* **`ProductService.restoreStock(String, int)`:** `Correct`
  * `ReturnService` depends on the operation and not on how the stock is stored.

---

## 11. Summary of Verdicts (Technical Lead Scope)

| Pattern | Verdict | Violations |
|---|---|---|
| Information Expert | `Violation` | V-01, V-06, V-07 |
| Creator | `Correct` | — |
| Controller | `Violation` | V-04 |
| Low Coupling | `Violation` | V-02, V-05 |
| High Cohesion | `Violation` | V-05 |
| Polymorphism | `Violation` | V-03 (also V-02 and V-04) |
| Pure Fabrication | `Violation` | V-08 (also V-05) |
| Indirection | `Violation` | V-02 |
| Protected Variations | `Violation` | V-03, V-05 (also V-02) |

When a fragment breaks more than one pattern, it is registered once, under the pattern that guides its correction, and the other patterns are named in the explanation.

---

## 12. Consolidated Violations Table (Technical Lead Scope)

| Violation ID | Pattern | Module | Evidence (Class, Method, Lines) | Explanation | Consequence | Proposed Solution | Code of another team member |
|---|---|---|---|---|---|---|---|
| **V-01** | **Information Expert** | Returns (Req. 3), Sales and Accessories (integration) | `ProductService.restoreStock` — line 160, `product.updateStock(product.getStock() + quantity);`. `SaleService.discountStock` — line 312, `int newStock = item.getStock() - 1;`. The same operation appears in `AccessoryService.restoreStock` — line 241. | The stock belongs to `Product`, but the services read it, do the arithmetic outside the entity and write the result back with `updateStock(...)`. | The rule of how the stock grows or shrinks is repeated in three places. Any change (for example, reserved stock) must be applied to every caller. | Add `increaseStock(int)` and `decreaseStock(int)` to `Product` and make the three callers delegate to them. The services keep the validation and the persistence they have today. | **Yes.** `Product` (Developer 1) receives the two methods. `AccessoryService.restoreStock` (Developer 2) must call `increaseStock`. |
| **V-02** | **Indirection** (also Polymorphism and Low Coupling) | Accessories (integration), Sales, Returns | `SaleService.resolveItem` (lines 297–303) and `discountStock` (lines 311–318, `instanceof Accessory` at line 313). Duplicated in `ReturnService.resolveItem` (line 184) and `ReturnService.restoreStock` (lines 199–205, `instanceof Accessory` at line 200). Fields `productService` and `accessoryService` in lines 43–44. | There is no intermediate object between the use cases and the two inventories. Each use case looks the item up in both services and chooses by type which one must change the stock. | Adding a new category of sellable items requires editing `SaleService` and `ReturnService`. Both classes are coupled to two services and to the concrete class `Accessory`. | Create an `InventoryService` in the service layer with `findItem(String)`, `decreaseStock(String, int)` and `restoreStock(String, int)`, which delegates to `ProductService` or `AccessoryService` according to where the item is found. `SaleService` and `ReturnService` use only it, and `Main` wires it. | **Yes.** `ReturnService` (Developer 2) must replace its own `resolveItem` and `restoreStock`. `AccessoryService` (Developer 2) may need a method to decrease the stock. |
| **V-03** | **Polymorphism** (also Protected Variations) | Warranties (Req. 4), Sales, Returns | `SaleService.validateExtendedWarrantyRequest` — line 254, `instanceof Console`. `SaleService.assignWarranties` — line 277, `instanceof Console`. The same decision is repeated in `WarrantyService` (line 288) and `ReturnService` (line 162). | Which items have a warranty is decided by asking for the concrete class instead of asking the item. | A new product type with a warranty requires finding and editing four places. Forgetting one gives a wrong behavior without any error. | Add `supportsWarranty()` to `Product`, returning `false`, and override it in `Console` to return `true`. The four callers use `item.supportsWarranty()`. | **Yes.** `Product` and `Console` (Developer 1) receive the method. `WarrantyService` and `ReturnService` (Developer 2) must use it. |
| **V-04** | **Controller** (also Polymorphism) | Warranties (Req. 4), user interface | `ConsoleMenu.askExtendedWarranties` — lines 321–334. Line 324, `productService.findById(itemId)`, and line 325, `item instanceof Console`. The same rule is enforced in `SaleService.validateExtendedWarrantyRequest` (line 254). | The user interface decides which items are offered an extended warranty. That is a business rule, and the menu must only capture the answer. | The rule lives in two classes, which can disagree. The menu has to know the domain classes `Product` and `Console`. | Make `SaleService` provide the identifiers of the items that can receive an extended warranty (using `supportsWarranty()` from V-03). `ConsoleMenu` only asks the user about each one. | **No.** Both classes belong to the Technical Lead. It depends on V-03. |
| **V-05** | **High Cohesion** (also Pure Fabrication, Low Coupling and Protected Variations) | User interface, all modules | `ConsoleMenu` — the whole class: 923 lines and 48 methods. Eight submenus (`productMenu` at line 126 to `promotionMenu` at line 717), input helpers (`readDate` at line 842 and `parseIds`, `readText`, `readInt`, `readDouble` at lines 880–923), error handling `runGuarded` (lines 864–878) and four `print*` methods. Seven services (lines 42–48) and nine model imports (lines 7–15). `start()` `switch` in lines 91–121. | One class has the responsibilities of eight modules and of several technical tasks. | Any change in any module edits this class. The three developers collide in the same file. Adding a module requires editing `start()`. No menu can be understood or tested apart. | Extract `ConsoleInput` (input and parsing) and `ConsoleErrorReporter` (`runGuarded`) as Pure Fabrications. Create one menu class per module, each with only its own services, behind a common `Menu` interface. `ConsoleMenu` keeps only the main loop. | **No.** `ConsoleMenu` belongs to the Technical Lead. It must be coordinated with Developer 1's V-03, which changes the calls to the product registration. |
| **V-06** | **Information Expert** | Promotions (Req. 2), user interface | `ConsoleMenu.printSale` — lines 388–390, the condition `getAppliedPromotionName() != null && getDiscountAmount() > 0`. The same rule is in `Sale.generateReceipt` — line 184. | `Sale` knows when it has a discount, but the menu repeats the rule instead of asking the sale. | If the rule changes, the receipt and the sales list can show different information. | Add `hasDiscount()` to `Sale` and use it in `generateReceipt` and in `printSale`. | **No.** |
| **V-07** | **Information Expert** | Promotions (Req. 2) | `SaleService.applyBestPromotion` — lines 226–231. Line 229, `sale.getProducts()` and `promotion.calculateDiscount(...)`, and lines 228–229, the two setters `setAppliedPromotionName` and `setDiscountAmount`. | The service takes data from the sale, computes with it and writes the result back in two separate calls. The sale owns the products, the name and the discount. | The two values can be left inconsistent (a name without discount or the opposite), and the setters are public for any class. | Add `applyPromotion(Promotion)` to `Sale`, which sets the name and the discount together. The two setters become unnecessary. | **No.** |
| **V-08** | **Pure Fabrication** | Sales, Returns, Promotions (persistence) | `SaleRepository.ProductDeserializer` (lines 139–160) and `SaleRepository.LocalDateAdapter` (lines 166–177). Copies in `ReturnRepository` (lines 128 and 155) and `PromotionRepository` (line 149). | The technical task of converting dates and rebuilding products from JSON is repeated in three repositories instead of having one class. | A change in the date format or a new product type must be repeated in each copy. | Extract package-private `LocalDateAdapter` and `ProductDeserializer` classes in the persistence package and use them from the three repositories. | **Yes.** `ReturnRepository` and `PromotionRepository` (Developer 2). |

---

## 13. Notes for the Consolidation

### Boundary findings (code of more than one team member)
* **V-01:** `Product` (Developer 1), `AccessoryService` (Developer 2), `ProductService.restoreStock` and `SaleService` (Technical Lead).
* **V-02:** `ReturnService` and `AccessoryService` (Developer 2), `SaleService` and `Main` (Technical Lead).
* **V-03:** `Product` and `Console` (Developer 1), `WarrantyService` and `ReturnService` (Developer 2), `SaleService` (Technical Lead).
* **V-08:** `ReturnRepository` and `PromotionRepository` (Developer 2), `SaleRepository` (Technical Lead).

### Relations with other reports
* **Developer 1, V-03** (`ProductService.registerVideoGame`/`registerConsole` as a polymorphic registration): the correction changes the calls in `ConsoleMenu`. It must be applied after the split of V-05, or on the product menu that V-05 creates.
* **Developer 1, V-02** (`CategoryDiscount` uses `instanceof Accessory`): it has the same cause as V-03 of this report, but it affects a different class, so it stays as a separate violation.
* **Overlaps inside this report:** V-01 and V-02 both touch `SaleService.discountStock`, and V-03 and V-04 both touch the `instanceof Console` check. They are kept apart because each one needs a different change.

### Suggested correction order
1. V-01 and V-03 first, because they add methods to `Product` and `Console` that the other corrections use.
2. V-02, after V-01, because `InventoryService` uses the new stock methods.
3. V-04, after V-03.
4. V-06, V-07 and V-08, which are independent.
5. V-05 last, because it moves code that the other corrections change.

### Functional defects noticed (outside the scope of this requirement)
They are not corrected here, because correcting them changes what the user sees.
* **Blank identifier in the returns queries:** `ConsoleMenu.viewReturnsByCustomer` (lines 457–467) and `viewReturnsBySale` (lines 469–479) have no error handling, so an empty identifier ends the program with a stack trace. It already happened before Requirement 6.
* **Price zero:** `ProductValidator.validateProductData` (line 38, `price <= 0`) rejects a price of zero. Before Requirement 6 only negative prices were rejected. It is code of Developer 1.
* **Memory and file out of sync on a write failure:** `SaleService.registerSale` adds the sale to the in-memory list (line 181) and then saves (line 182); if saving fails, the sale and the stock decrease stay in memory but not in the file. `ProductService` has the same order when registering a product.
