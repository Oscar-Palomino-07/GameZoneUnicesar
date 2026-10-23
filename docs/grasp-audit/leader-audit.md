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
