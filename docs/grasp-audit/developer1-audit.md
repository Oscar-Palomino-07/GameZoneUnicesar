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


---

## 5. Pattern 4: Low Coupling

### Principle
Assign responsibilities so that coupling between classes remains low, reducing the impact of downstream modifications and maximizing component reuse.

### Audit by Module and Class
* **Model Layer (`Product`, `Accessory`, `Promotion`, `Warranty`, `Return` hierarchies):** `Correct`
  * Complete absence of dependencies toward `service`, `persistence`, `ui`, or external frameworks. All domain classes rely exclusively on core Java types (`java.time`, `java.util`), establishing clean architectural layer boundaries.
* **`ProductRepository`:** `Correct`
  * Lines 3–21: Confines third-party library dependency (`com.google.gson`) and file system I/O (`java.nio.file`) entirely inside the repository package. It does not import or know `ProductService` or `ConsoleMenu`.
* **`ProductService`:** `Correct`
  * Lines 2–10, 28–37: Receives `ProductRepository` via constructor dependency injection. Interacts only with model classes, custom exceptions, and static validators. It does not couple to user interface components.
* **`com.gamezone.validation.*`:** `Correct`
  * Validators are decoupled from persistence and services; they perform in-memory inspections on arguments and throw typed exceptions without external side effects.
* **`com.gamezone.exceptions.*`:** `Correct`
  * Standalone unchecked exception hierarchy without circular dependencies.

---

## 6. Pattern 5: High Cohesion

### Principle
Keep class responsibilities focused, understandable and closely related. A class must not mix unrelated domain, technical, or formatting concerns.

### Audit by Module and Class
* **Model Classes (`Product`, `VideoGame`, `Console`, `Accessory`, `Promotion`, `Warranty`, `Return`):** `Correct`
  * Each domain class is strongly focused on representing its entity state and computing domain-specific results (`calculateDiscount`, `calculateRefundAmount`, `getDescription`).
* **`ProductRepository`:** `Correct`
  * Lines 28–125: Dedicated exclusively to persistence mechanisms (serialization, file reading, stream closing). It contains no pricing algorithms, stock rules, or UI presentation logic.
* **`ProductService`:** `Correct`
  * Note on Requerimiento 6 refactoring: Prior to Requirement 6, `ProductService` mixed use-case coordination with low-level string blank-checking and range validation (`validate` and `isBlank`). Following the extraction of these checks to `ProductValidator`, `ProductService` exhibits High Cohesion, concentrating solely on orchestrating catalog workflows and synchronizing persistence.
* **Validation Layer (`ProductValidator`, `PersonValidator`, `SaleValidator`):** `Correct`
  * High cohesion around input and business rule validation, keeping services clean and preventing duplicated validation logic.


---

## 7. Pattern 6: Polymorphism

### Principle
When related behaviors vary by type, assign responsibility for the behavior to the types using polymorphic operations rather than conditional logic based on explicit type checking.

### Audit by Module and Class
* **`Product.getDescription()` & `Product.getCategory()`:** `Correct`
  * Abstract declarations in `Product` (lines 65, 82) are overridden across `VideoGame`, `Console`, and `Accessory`. External consumers retrieve descriptions and category tags through dynamic dispatch without conditional `switch` or `instanceof` cascades.
* **`Accessory` Hierarchy (`Controller`, `Cable`, `Memory`):** `Correct`
  * Each concrete accessory overrides `getDescription()` to format its particular hardware specifications polymorphically.
* **`Promotion` Hierarchy (`PercentageDiscount`, `CategoryDiscount`, `BulkPurchaseDiscount`):** `Correct`
  * Abstract method `calculateDiscount(List<Product>)` is implemented polymorphically by each concrete discount strategy. `SaleService` evaluates active promotions via dynamic dispatch.
* **`CategoryDiscount.belongsToTargetCategory(Product)`:** `Violation`
  * Evidence (lines 76–81 in `CategoryDiscount.java`):
    ```java
    private boolean belongsToTargetCategory(Product product) {
        if (product instanceof Accessory) {
            return ACCESSORY_CATEGORY.equalsIgnoreCase(targetCategory);
        }
        return targetCategory.equalsIgnoreCase(product.getCategory());
    }
    ```
  * Explanation: Explicitly checks `product instanceof Accessory` even though `Accessory` already polymorphically overrides `getCategory()` to return `"ACCESSORY"` (line 90 of `Accessory.java`). Using `instanceof` when a polymorphic method exists violates pure polymorphic dispatch and couples `CategoryDiscount` directly to the `Accessory` subclass.
* **`Warranty` Hierarchy (`BasicWarranty`, `ExtendedWarranty`):** `Correct`
  * `getDurationInMonths()`, `getWarrantyType()`, and `getAdditionalCost()` are abstract methods implemented by each warranty type, allowing `Warranty.generateWarrantyCertificate()` to format certificates polymorphically.
* **`ProductService.listAllProducts()`:** `Violation`
  * Evidence (lines 24–25, 104–109 in `ProductService.java`):
    ```java
    private final List<VideoGame> videoGames = new ArrayList<>();
    private final List<Console> consoles = new ArrayList<>();
    ...
    public List<Product> listAllProducts() {
        List<Product> products = new ArrayList<>();
        products.addAll(videoGames);
        products.addAll(consoles);
        return products;
    }
    ```
  * Explanation: Instead of managing inventory polymorphically in a single `List<Product>` collection, `ProductService` segregates items into concrete subtype lists (`videoGames` and `consoles`). Consequently, `listAllProducts()` must manually instantiate a new list and merge both collections on every call, breaking polymorphic collection reuse.

---

## 8. Pattern 7: Pure Fabrication

### Principle
Assign a cohesive set of responsibilities to an artificial or technical class that does not represent a problem domain concept, in order to support Low Coupling, High Cohesion, and clean separation of concerns.

### Audit by Module and Class
* **`ProductRepository`:** `Correct`
  * Package: `com.gamezone.persistence` (lines 28–125).
  * A "repository" does not exist in the physical video game store; it is an artificial technical construct invented to encapsulate file I/O, directory creation and Gson JSON serialization. Without `ProductRepository`, `Product` would be forced to handle file reading and writing, violating High Cohesion.
* **Validation Layer (`ProductValidator`, `PersonValidator`, `SaleValidator`):** `Correct`
  * Package: `com.gamezone.validation`.
  * Dedicated validation classes are Pure Fabrications created to isolate data constraint checks and error triggering, preventing domain entities and services from being bloated with validation procedures.
* **Exceptions Layer (`com.gamezone.exceptions.*`):** `Correct`
  * Custom exception classes are technical fabrications that translate domain and persistence errors into a structured error-code hierarchy.
* **Model Entities (`Product`, `Accessory`, `Promotion`, `Warranty`, `Return`):** `Not applicable`
  * These classes represent concrete real-world concepts in the store domain, not artificial technical constructs.
