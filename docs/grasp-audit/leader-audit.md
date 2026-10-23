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
