# GRASP Audit Report — Developer 1

* **Auditor:** Manuel Ospino (Developer 1)
* **Branch:** `feature/grasp-audit`
* **Date:** 2026-10-22
* **Target Reviewer:** Veronica Padilla (Developer 2) / Oscar Palomino (Technical Lead)

---

## 1. Audited Scope & Method Inventory

In accordance with the team distribution and integration specifications, Developer 1 is responsible for auditing the following classes and methods:

### Module 1: Base Product Module
* **`com.gamezone.model.Product` (abstract):**
  * `getId()`, `getTitle()`, `getPrice()`, `getStock()`
  * `updateStock(int quantity)`
  * `getCategory()` (abstract)
  * `getDescription()` (abstract)
* **`com.gamezone.model.VideoGame`:**
  * Constructor `VideoGame(...)`
  * `getPlatform()`, `getGenre()`, `getAgeRating()`
  * `getCategory()` (`@Override`)
  * `getDescription()` (`@Override`)
* **`com.gamezone.model.Console`:**
  * Constructor `Console(...)`
  * `getBrand()`, `getModel()`, `getGeneration()`
  * `getCategory()` (`@Override`)
  * `getDescription()` (`@Override`)
* **`com.gamezone.persistence.ProductRepository`:**
  * `saveAllVideoGames(List<VideoGame>)`, `loadAllVideoGames()`
  * `saveAllConsoles(List<Console>)`, `loadAllConsoles()`
  * `writeList(String, List<?>, Type)` (private helper)
  * `readList(String, Type)` (private helper)
* **`com.gamezone.service.ProductService` (171 lines — post-Req. 6, excluding `restoreStock`):**
  * `ProductService(ProductRepository)` (constructor)
  * `registerVideoGame(String, String, double, int, String, String, String)`
  * `registerConsole(String, String, double, int, String, String, String)`
  * `listAllProducts()`
  * `findById(String)`
  * `updateStock(String, int)`
  * `saveAll()` (private helper)

### Module 2: Returns & Sale Returnability (Requirement 3 & 5)
* **`com.gamezone.model.Return`:**
  * Constructor `Return(...)`
  * `getId()`, `getSaleId()`, `getDate()`, `getCustomer()`, `getSeller()`, `getProducts()`
  * `getSaleSubtotal()`, `getSaleDiscount()`, `getWarrantyRefund()`, `setWarrantyRefund(double)`
  * `calculateRefundAmount()`
  * `calculateProportionalDiscount(Product)` (private helper)
  * `generateReturnReceipt()`
* **`com.gamezone.model.Sale`:**
  * `canBeReturned()` (evaluated exclusively; remainder of `Sale` belongs to Technical Lead)

### Module 3: Accessories Hierarchy (Requirement 1)
* **`com.gamezone.model.Accessory` (abstract):**
  * `getCompatibleConsoleIds()`, `setCompatibleConsoleIds(List<String>)`
  * `addCompatibleConsole(String)`, `removeCompatibleConsole(String)`
  * `isCompatibleWith(String)`
  * `getCategory()` (`@Override`, returns `"ACCESSORY"`)
  * `getDescription()` (abstract / shared base)
* **`com.gamezone.model.Controller`:**
  * `getConnectionType()`
  * `getDescription()` (`@Override`)
* **`com.gamezone.model.Cable`:**
  * `getLengthInMeters()`, `getConnectorType()`
  * `getDescription()` (`@Override`)
* **`com.gamezone.model.Memory`:**
  * `getCapacityInGb()`, `getMemoryType()`
  * `getDescription()` (`@Override`)

### Module 4: Promotions Hierarchy (Requirement 2)
* **`com.gamezone.model.Promotion` (abstract):**
  * `getId()`, `getName()`, `getStartDate()`, `getEndDate()`, `getDiscountPercentage()`
  * `isActive(LocalDate)`
  * `calculateDiscount(List<Product>)` (abstract)
* **`com.gamezone.model.PercentageDiscount`:**
  * `calculateDiscount(List<Product>)` (`@Override`)
* **`com.gamezone.model.CategoryDiscount`:**
  * `getTargetCategory()`, `setTargetCategory(String)`
  * `calculateDiscount(List<Product>)` (`@Override`)
  * `belongsToTargetCategory(Product)` (private helper)
* **`com.gamezone.model.BulkPurchaseDiscount`:**
  * `getMinimumQuantity()`
  * `calculateDiscount(List<Product>)` (`@Override`)

### Module 5: Warranties Hierarchy (Requirement 4)
* **`com.gamezone.model.Warranty` (abstract):**
  * `getId()`, `getProduct()`, `getSale()`, `getStartDate()`, `getEndDate()`
  * `getDurationInMonths()` (abstract), `getWarrantyType()` (abstract), `getAdditionalCost()` (abstract)
  * `isActive(LocalDate)`
  * `generateWarrantyCertificate()`
* **`com.gamezone.model.BasicWarranty`:**
  * `getDurationInMonths()` (returns 6), `getWarrantyType()` (returns `"Básica"`), `getAdditionalCost()` (returns 0.0)
* **`com.gamezone.model.ExtendedWarranty`:**
  * `getDurationInMonths()` (returns 12), `getWarrantyType()` (returns `"Extendida"`), `getAdditionalCost()` (returns 10% of console price)

### Module 6: Custom Exceptions & Validation Layer (Requirement 6)
* **`com.gamezone.exceptions.*`:**
  * `GameZoneException` (abstract root), `getErrorCode()`
  * `ResourceNotFoundException`, `BusinessRuleException`, `InvalidDataException`, `PersistenceException`
* **`com.gamezone.validation.*`:**
  * `ProductValidator`: `validateProductData`, `validateStockAvailability`, `validateProductExists`, `validateUniqueProduct`, `validateRequiredField`, `validateNonNegativeStock`
  * `PersonValidator`: `validatePersonData`, `validatePersonExists`, `validateUniquePerson`
  * `SaleValidator`: `validateSaleData`, `validateSaleExists`, `validateProductIdsNotEmpty`


---

## 2. Pattern 1: Information Expert

### Principle
Assign a responsibility to the class that has the necessary information to fulfill it. State mutations and domain calculations must reside in the class that encapsulates the corresponding attributes.

### Audit by Module and Class

#### Module 1: Base Product
* **`Product`:** `Correct`
  * `updateStock(int quantity)` (lines 72–74): Directly updates the encapsulated `stock` attribute (line 14).
  * `getCategory()` (line 65) and `getDescription()` (line 82): Declared `abstract`, delegating specialized textual composition to the subclasses possessing the specific attributes.
* **`VideoGame`:** `Correct`
  * `getDescription()` (lines 69–76): Formats title, price, stock, platform, genre, and age rating using internal fields and inherited getters.
  * `getCategory()` (lines 58–60): Returns `"VIDEOGAME"`.
* **`Console`:** `Correct`
  * `getDescription()` (lines 69–76): Formats title, price, stock, brand, model, and generation using its own state.
  * `getCategory()` (lines 58–60): Returns `"CONSOLE"`.
* **`ProductRepository`:** `Correct`
  * Lines 30–37, 47–79: Acts as the technical expert on file locations (`data/videogames.json`, `data/consoles.json`) and Gson `TypeToken` specifications.
* **`ProductService` (excluding `restoreStock`):** `Correct`
  * Method: `updateStock(String productId, int quantity)` (lines 136–142).
  * With `restoreStock` excluded (audited by Technical Lead per exam specification), `ProductService` queries the product, executes pre-condition checks via `ProductValidator`, and delegates stock mutation directly to `product.updateStock(quantity)` (line 140). It does not perform external arithmetic on the entity's state.

#### Module 2: Returns & Sale Returnability
* **`Return`:** `Correct`
  * `calculateRefundAmount()` (lines 111–117): Computes total refund using its own `products`, `saleSubtotal`, `saleDiscount`, and `warrantyRefund` fields.
  * `calculateProportionalDiscount(Product)` (lines 120–125): Encapsulates proportional discount arithmetic within the return record.
  * `generateReturnReceipt()` (lines 135–155): Composes the formatted receipt from its own fields.
* **`Sale.canBeReturned()`:** `Correct`
  * Lines 199–204: Uses `this.date` to evaluate the 30-day return eligibility window.

#### Module 3: Accessories Hierarchy
* **`Accessory`:** `Correct`
  * `isCompatibleWith(String consoleId)` (lines 82–84): Queries its own encapsulated `compatibleConsoleIds` collection.
* **`Controller`, `Cable`, `Memory`:** `Correct`
  * Each concrete subclass's `getDescription()` builds its specification using its specialized fields (`connectionType`, `lengthInMeters`, `capacityInGb`).

#### Module 4: Promotions Hierarchy
* **`Promotion`:** `Correct`
  * `isActive(LocalDate date)`: Checks whether `date` falls inside its own `startDate` and `endDate` range.
* **`PercentageDiscount`, `CategoryDiscount`, `BulkPurchaseDiscount`:** `Correct`
  * Each concrete discount computes its monetary deduction from the product list and its own discount percentage / category / minimum quantity fields.

#### Module 5: Warranties Hierarchy
* **`Warranty`:** `Correct`
  * `isActive(LocalDate date)` (lines 108–113): Checks validity against its own `startDate` and calculated `endDate`.
  * `generateWarrantyCertificate()` (lines 122–137): Generates Spanish certificate from its internal attributes.
* **`BasicWarranty`, `ExtendedWarranty`:** `Correct`
  * `ExtendedWarranty.getAdditionalCost()` computes 10% from `getProduct().getPrice()`, accessing data via valid association.

#### Module 6: Custom Exceptions & Validation Layer
* **`GameZoneException` & Subclasses:** `Correct`
  * Encapsulates `errorCode` and generates user-facing messages in Spanish.
* **`ProductValidator`, `PersonValidator`, `SaleValidator`:** `Correct`
  * Utility validators that inspect input arguments without storing state.

---

## 3. Pattern 2: Creator

### Principle
Assign class $B$ the responsibility to create an instance of class $A$ if $B$ contains, aggregates, records, or has the initializing data for $A$.

### Audit by Module and Class
* **`ProductService`:** `Correct`
  * Methods: `registerVideoGame(...)` (line 66) and `registerConsole(...)` (line 97).
  * `ProductService` records and stores instances in `videoGames` and `consoles`, and possesses the complete initialization data supplied by caller requests.
* **`ProductRepository`:** `Correct`
  * Methods: `readList(...)` (lines 113–124) deserializes and creates `VideoGame` and `Console` instances from JSON data.
* **Model Entities (`Product`, `Accessory`, `Promotion`, `Warranty`, `Return`):** `Not applicable`
  * Domain entities represent data records; they do not manufacture separate lifecycle entities.
* **Exceptions & Validators:** `Not applicable`
  * Exceptions are instantiated by callers when rules fail; validators are static utility classes with private constructors.

---

## 4. Pattern 3: Controller

### Principle
Assign responsibility for receiving and orchestrating system events to a non-UI class representing either the overall system (facade) or a use-case workflow.

### Audit by Module and Class
* **`ProductService`:** `Correct`
  * Coordinates product catalog operations: `registerVideoGame` (lines 59–68), `registerConsole` (lines 90–99), `listAllProducts` (lines 104–109), `findById` (lines 118–125), and `updateStock` (lines 136–142).
  * Serves as the Use Case Controller between UI (`ConsoleMenu`) and persistence (`ProductRepository`), ensuring `ConsoleMenu` performs no domain logic.
* **All Other Scoped Classes (Domain Models, Repositories, Exceptions, Validators):** `Not applicable`
  * Domain models represent entities; repositories handle storage; validators enforce data constraints. None coordinate user system events.
