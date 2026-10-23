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
