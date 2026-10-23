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
