# GRASP Audit Report — Developer 2

* **Auditor:** Veronica Padilla (Developer 2)
* **Branch:** `feature/grasp-audit`
* **Date:** 2026-10-23
* **Audited code state:** commit `8b0be4b`, the same state audited by the Technical Lead. Every line number in this report refers to that commit. All the classes audited here are identical in the current `develop`.
* **Target reviewers:** Manuel Ospino (Developer 1) / Oscar Palomino (Technical Lead)

---

## 1. Audited Scope & Method Inventory

In accordance with the responsibility table of the requirement, Developer 2 audits the repository and the service of every module, the person module of Taller 1, and the use of exceptions and validators in the services and repositories (Requirement 6).

### Module 1: People (Taller 1)
* **`com.gamezone.model.Person` (abstract):** constructor, `getId()`/`setId()`, `getFirstName()`/`setFirstName()`, `getLastName()`/`setLastName()`, `getPhone()`/`setPhone()`.
* **`com.gamezone.model.Customer`:** constructor, `getEmail()`/`setEmail()`.
* **`com.gamezone.model.Seller`:** constructor, `getEmployeeCode()`/`setEmployeeCode()`, `getShift()`/`setShift()`.
* **`com.gamezone.persistence.PersonRepository`:** `saveAllCustomers(List<Customer>)`, `loadAllCustomers()`, `saveAllSellers(List<Seller>)`, `loadAllSellers()`, `writeList(...)`, `readList(...)`.
* **`com.gamezone.service.PersonService`:** constructor, `registerCustomer(...)`, `listAllCustomers()`, `listAllSellers()`, `findCustomerById(String)`, `findSellerById(String)`, `save()`.

### Module 2: Accessories (Requirement 1, adjustment A4)
* **`com.gamezone.persistence.AccessoryRepository`:** constructor, `saveAll(List<Accessory>)`, `loadAll()`, `writeList(...)`, `readList(...)`, inner class `AccessoryDeserializer`.
* **`com.gamezone.service.AccessoryService`:** constructor, `registerController(...)`, `registerCable(...)`, `registerMemory(...)`, `listAllAccessories()`, `listAccessoriesByType(String)`, `findAccessoriesCompatibleWith(String)`, `findById(String)`, `updateStock(String, int)`, `restoreStock(String, int)` (A4), and the private helpers `validateCommonFields`, `normalizeConnectionType`, `matchesType`, `cleanConsoleIds`, `nextAccessoryId`, `isBlank`, `save`.

### Module 3: Promotions (Requirement 2)
* **`com.gamezone.persistence.PromotionRepository`:** constructor, `saveAll(List<Promotion>)`, `loadAll()`, `writeList(...)`, `readList(...)`, inner classes `PromotionDeserializer` and `LocalDateAdapter`.
* **`com.gamezone.service.PromotionService`:** constructor, `registerPercentage(...)`, `registerCategory(...)`, `registerBulkPurchase(...)`, `listAllPromotions()`, `listActivePromotions(LocalDate)`, `findById(String)`, `bestPromotionFor(List<Product>)`, and the private helpers `validateCommonFields`, `validatePercentage`, `normalizeCategory`, `nextPromotionId`, `save`.

### Module 4: Returns (Requirement 3, adjustments A4, A6 and A7)
* **`com.gamezone.persistence.ReturnRepository`:** constructor, `saveAll(List<Return>)`, `loadAll()`, `writeList(...)`, `readList(...)`, inner classes `ProductDeserializer` and `LocalDateAdapter`.
* **`com.gamezone.service.ReturnService`:** constructor, `registerReturn(String, List<String>)`, `resolveItem(String)`, `restoreStock(Product, int)`, `countAlreadyReturnedByProduct(Sale)`, `viewAllReturns()`, `viewReturnsByCustomer(String)`, `viewReturnsBySale(String)`, the monthly balance (`calculateMonthlySales(int, int)`, `calculateMonthlyReturns(int, int)`, `generateMonthlyBalance(int, int)`, `validatePeriod`, `isInPeriod`), `save()`, `nextReturnId()`.
* The commit history shows that both classes were first created by Developer 1, but the responsibility table assigns them to Developer 2, who implemented A4, A6 and A7 in them. They are audited here in full.

### Module 5: Warranties (Requirement 4, adjustments A2 and A7)
* **`com.gamezone.persistence.WarrantyRepository`:** `saveAll(List<WarrantyRecord>)`, `loadAll()`, `isExtended(String)`, `basicType()`, `extendedType()`, inner class `WarrantyRecord`.
* **`com.gamezone.service.WarrantyService`:** constructor, `rebuildWarranties()` (A2), `findSale(...)`, `assignBasicWarranty(...)`, `assignExtendedWarranty(...)`, `findWarrantyByProduct(String, String)`, `cancelWarranties(String, String)` (A7), `listAllWarranties()`, `listActiveWarranties()`, `listWarrantiesExpiringSoon(int)`, `validateWarrantyRequest(...)`, `nextWarrantyId()`, `save()`.

### Module 6: Exceptions and validations (Requirement 6)
* **`ProductService`:** the validator calls in `registerVideoGame` (lines 61–65), `registerConsole` (lines 92–96), `updateStock` (lines 138–139) and `restoreStock` (lines 157 and 159).
* **`PersonService`:** the validator calls in `registerCustomer` (lines 63–64).
* **`SaleService`:** the validator calls in `registerSale` (lines 151–160) and `resolveAndValidateItems` (lines 207–208), and the two `BusinessRuleException` of `validateExtendedWarrantyRequest` (lines 251 and 255).
* **`ProductRepository`, `PersonRepository`, `SaleRepository`:** the translation of `IOException` and `JsonSyntaxException` into `PersistenceException` (lines 98 and 122, 98 and 122, 105 and 129).

### Code of other team members in these classes (not audited here)
* **`ProductService`:** the rest of the class is Developer 1's, and the body of `restoreStock` is the Technical Lead's (Requirement 3).
* **`SaleService`, `SaleRepository`:** everything except the lines listed in Module 6 belongs to the Technical Lead.
* The model classes used by these services (`Accessory` and its subclasses, `Promotion` and its subclasses, `Return`, `Warranty` and its subclasses, `Sale`) belong to Developer 1 and to the Technical Lead.

### Method
Each class was read against the question of every pattern. The line numbers and the quoted statements were checked on commit `8b0be4b`. A finding that the Technical Lead already registered in `leader-audit.md` is not registered again: it is cross-referenced as "see leader V-XX", and the part of the correction that belongs to Developer 2 is stated in section 13.

---

## 2. Pattern 1: Information Expert

### Principle
Assign a responsibility to the class that has the information needed to fulfill it.

### Audit by Class
* **`Person`, `Customer`, `Seller`:** `Correct`
  * Each class holds and exposes only its own data (`Person` lines 15–18, `Customer` line 14, `Seller` lines 12–13). No other class computes anything with that data.
* **`PersonService`:** `Correct`
  * `findCustomerById` (lines 94–98) and `findSellerById` (lines 107–111) search the lists that the service itself keeps (lines 29–30).
* **`AccessoryService`:** `Correct`, with one cross-reference
  * `findAccessoriesCompatibleWith` (lines 170–182) asks each accessory `isCompatibleWith(...)` (line 177) instead of reading its list of consoles.
  * `restoreStock` (line 241, `accessory.updateStock(accessory.getStock() + quantity);`) performs the stock arithmetic outside the entity. It is already registered by the Technical Lead. See **leader V-01**.
* **`PromotionService`:** `Correct`
  * `listActivePromotions` (lines 133–142) asks each promotion `isActive(date)` (line 137), and `bestPromotionFor` (lines 170–186) asks each promotion `calculateDiscount(products)` (line 178).
* **`WarrantyService`:** `Violation`
  * `listActiveWarranties` (lines 244–254) correctly asks each warranty `isActive(today)` (line 249).
  * `validateWarrantyRequest` (lines 291–297) walks through `sale.getProducts()` to find out whether the product belongs to the sale. That information belongs to `Sale`. See **V-01**.
* **`ReturnService`:** `Violation`
  * `registerReturn` (lines 124–126) counts the purchased units of a product with a stream over `originalSale.getProducts()`. The sale owns its product list and should answer that question. See **V-01**.
  * `calculateMonthlySales` (line 308) and `calculateMonthlyReturns` (line 329) correctly ask `sale.calculateTotal()` and `returnObj.calculateRefundAmount()`.
* **Repositories (`PersonRepository`, `AccessoryRepository`, `PromotionRepository`, `ReturnRepository`, `WarrantyRepository`):** `Correct`
  * Each one is the expert on its own file: the path and the Gson type are private constants (for example `PersonRepository` lines 30–37, `WarrantyRepository` lines 38–44).

---

## 3. Pattern 2: Creator

### Principle
Assign class B the responsibility of creating instances of class A when B contains or records A, or has the data to initialize A.

### Audit by Class
* **`PersonService`:** `Correct`
  * `registerCustomer` creates the customer (line 65, `new Customer(...)`) and records it in the `customers` list that it keeps (line 29).
* **`AccessoryService`:** `Correct`
  * `registerController` (line 63), `registerCable` (line 92) and `registerMemory` (line 120) create the accessories and add them to the list that the service records (line 35). The service also generates the identifier (`nextAccessoryId`, lines 296–309), which it can do because it holds every accessory.
* **`PromotionService`:** `Correct`
  * The three `register*` methods create the promotions (lines 60, 84 and 110) and record them (lines 62, 86 and 112).
* **`ReturnService`:** `Correct`
  * `registerReturn` creates the `Return` (line 167) with data that only it has gathered (the validated products, the original sale and the warranty refund) and records it (line 170).
* **`WarrantyService`:** `Correct`
  * `assignBasicWarranty` (line 127), `assignExtendedWarranty` (line 147) and `rebuildWarranties` (lines 90 and 92) create the warranties, and the service records them (line 43).
* **Repositories:** `Correct`
  * The deserializers (`AccessoryRepository` lines 125–142, `PromotionRepository` lines 130–143, `ReturnRepository` lines 128–149) create the concrete objects because they are the classes that hold the serialized data. `WarrantyService.save()` creates the `WarrantyRecord` objects (line 328) because it is the only class that knows both the warranty and the record format.
* **`Person`, `Customer`, `Seller`:** `Not applicable`
  * They create no objects.

---

## 4. Pattern 3: Controller

### Principle
Assign the responsibility of receiving and coordinating a system event to a non-UI class that represents a use case.

### Audit by Class
* **`PersonService`, `AccessoryService`, `PromotionService`, `ReturnService`, `WarrantyService`:** `Correct`
  * Each service is the use-case controller of its module: the menu only reads the data and calls one method. For example, `ReturnService.registerReturn` (lines 80–174) coordinates the validations, the stock restoration, the warranty cancellation and the persistence of a return, and `PromotionService.registerCategory` (lines 79–89) validates, creates and saves a promotion. None of these services reads input from the console or prints to it.
* **Model classes and repositories:** `Not applicable`
  * Entities and file-storage classes do not receive system events.

---

## 5. Pattern 4: Low Coupling

### Principle
Assign responsibilities so that the dependencies between classes stay low.

### Audit by Class
* **`ReturnService`:** `Violation`
  * It receives six collaborators (fields in lines 27–32 and constructor in lines 49–58). One of them, `PersonService` (field in line 28, assigned in line 53), is never used by any method: the customer and the seller are taken from the original sale (lines 102–103). See **V-02**.
  * The dependencies on `ProductService` and `AccessoryService` (lines 29–30), used only to resolve an item and restore its stock, are already registered by the Technical Lead. See **leader V-02**.
* **`WarrantyService`:** `Correct`
  * It depends on `WarrantyRepository`, `SaleRepository` and `ProductService` (lines 39–41). The dependency on `SaleRepository` instead of `SaleService` is the deliberate solution of adjustment A2: it breaks the cycle `SaleService → WarrantyService → SaleService`, and it is documented in the class JavaDoc.
* **`PersonService`, `AccessoryService`, `PromotionService`:** `Correct`
  * Each one depends only on its own repository and on its model classes (`PersonService` lines 3–8, `AccessoryService` lines 3–7, `PromotionService` lines 3–8).
* **Repositories:** `Correct`
  * They depend only on the model classes they serialize and on Gson. None of them knows a service or the user interface. `WarrantyRepository` does not even depend on the model, because it stores only identifiers (`WarrantyRecord`, lines 131–190).
* **`Person`, `Customer`, `Seller`:** `Correct`
  * They have no dependencies outside the class hierarchy.

---

## 6. Pattern 5: High Cohesion

### Principle
Keep the responsibilities of a class focused and closely related.

### Audit by Class
* **`ReturnService`:** `Violation`
  * Besides the return use case (`registerReturn` and the queries, lines 80–291), the class computes the monthly report of the whole store (lines 303–376). `calculateMonthlySales` (lines 303–312) iterates the sales through `saleService.viewAllSales()` (line 306) and has nothing to do with returns. See **V-03**.
* **`AccessoryService`, `PromotionService`:** `Violation`
  * Besides registering and querying, each service contains all the validation rules of its data: `AccessoryService` lines 86–91, 114–119, 246–256 and 259–265, and `PromotionService` lines 107–109 and 189–216. The system has a validation layer (`com.gamezone.validation`), but these modules do not use it. See **V-08**.
* **`PersonService`, `WarrantyService`:** `Correct`
  * `PersonService` only registers, lists and finds people and delegates the validation to `PersonValidator` (lines 63–64). `WarrantyService` only manages the life cycle of the warranties: creation, rebuilding, queries and cancellation.
* **Repositories:** `Correct`
  * Each one reads and writes one file. The repetition of the same read/write code in all of them is reported under Pure Fabrication (**V-07**).
* **`Person`, `Customer`, `Seller`:** `Correct`
  * They hold the data of a person and nothing else.

---

## 7. Pattern 6: Polymorphism

### Principle
When behavior varies by type, assign it to the types through polymorphic operations instead of conditionals on the type.

### Audit by Class
* **`AccessoryService`:** `Violation`
  * `matchesType(Accessory, String)` (lines 268–279) is a `switch` over the strings `CONTROLLER`, `CABLE` and `MEMORY` that answers with `instanceof Controller`, `instanceof Cable` and `instanceof Memory`. The same three strings are checked again in `listAccessoriesByType` (lines 149–153). See **V-04**.
* **`WarrantyService`:** `Violation`
  * `findWarrantyByProduct` (line 168), `cancelWarranties` (line 208) and `save()` (line 325) ask `instanceof ExtendedWarranty`, and `rebuildWarranties` (lines 89–93) chooses the subclass with an `if` on a type string. The hierarchy already has polymorphic methods (`getDurationInMonths()`, `getAdditionalCost()`, `getWarrantyType()` in `Warranty`), but the service does not use them. See **V-05**.
  * `validateWarrantyRequest` (line 288, `instanceof Console`) is already registered by the Technical Lead. See **leader V-03**.
* **`ReturnService`:** `Violation` (registered by the Technical Lead)
  * `registerReturn` (line 162, `instanceof Console`) and `restoreStock` (line 200, `instanceof Accessory`). See **leader V-03** and **leader V-02**.
* **`PromotionService`:** `Correct`
  * `bestPromotionFor` (line 178) calls the abstract `calculateDiscount(...)` and never asks the type of the promotion.
* **Deserializers (`AccessoryRepository` lines 134–140, `PromotionRepository` lines 135–141, `ReturnRepository` lines 135–147):** `Correct`
  * Choosing the subclass from the attributes of the JSON happens at the only point where the type must be chosen, which is when the object is rebuilt from the file.
* **`PersonService`, `Person`, `Customer`, `Seller`:** `Not applicable`
  * No behavior varies by the type of person: customers and sellers only differ in their data.

---

## 8. Pattern 7: Pure Fabrication

### Principle
Assign a cohesive set of responsibilities to an artificial class that does not represent a domain concept.

### Audit by Class
* **Repositories (`PersonRepository`, `AccessoryRepository`, `PromotionRepository`, `ReturnRepository`, `WarrantyRepository`):** `Violation`
  * The repositories themselves are correct fabrications, but the technical task they share, writing a list to a JSON file and reading it back, is copied in each of them: `PersonRepository` lines 90–124, `AccessoryRepository` lines 85–119, `PromotionRepository` lines 91–124, `ReturnRepository` lines 87–120 and `WarrantyRepository` lines 62–94. The same code also exists in `ProductRepository` (Developer 1) and `SaleRepository` (Technical Lead). See **V-07**.
  * The duplicated `LocalDateAdapter` and `ProductDeserializer` of `PromotionRepository` and `ReturnRepository` are already registered by the Technical Lead. See **leader V-08**.
* **`AccessoryService`, `PromotionService`:** `Violation`
  * Their validation rules have no class of their own, as described under High Cohesion. See **V-08**.
* **`WarrantyRepository.WarrantyRecord`:** `Correct`
  * It is an artificial record (lines 131–190) that keeps the warranty file free of `Sale` and `Product` objects. This is the solution of adjustment A2.
* **Use of the validators in `ProductService`, `PersonService` and `SaleService` (Requirement 6):** `Correct`
  * The validators are fabrications that keep the rules out of the services: for example `ProductValidator.validateProductData` (`ProductService` lines 61 and 92), `PersonValidator.validatePersonData` (`PersonService` line 63) and `SaleValidator.validateSaleData` (`SaleService` line 160).
* **`Person`, `Customer`, `Seller`:** `Not applicable`
  * They represent real concepts of the domain.

---

## 9. Pattern 8: Indirection

### Principle
Assign the responsibility to an intermediate object that mediates between two components, so that they are not coupled directly.

### Audit by Class
* **`PersonService`, `AccessoryService`, `PromotionService`, `ReturnService`, `WarrantyService`:** `Correct`
  * Each service mediates between the user interface and the file of its module, so `ConsoleMenu` never touches a repository. For example, `PersonService.save()` (lines 120–123) is the only path to `PersonRepository`.
  * The missing intermediary between `ReturnService` and the two inventories (`resolveItem`, lines 184–190, and `restoreStock`, lines 199–205) is already registered by the Technical Lead. See **leader V-02**.
* **`WarrantyService`:** `Correct`
  * Since adjustment A2 it is the intermediary that rebuilds the references of the stored warranties (`rebuildWarranties`, lines 72–96), so `WarrantyRepository` does not depend on any other module.
* **Repositories:** `Correct`
  * They mediate between the objects and the JSON files. The services only know `saveAll(...)` and `loadAll()`.
* **`Person`, `Customer`, `Seller`:** `Not applicable`
  * They do not mediate between components.

---

## 10. Pattern 9: Protected Variations

### Principle
Identify the points of predicted variation and create a stable interface around them.

### Audit by Class
* **`PromotionService`:** `Violation`
  * The valid categories of a category promotion are three string constants in the service (lines 28–30), checked in `normalizeCategory` (lines 209–216). The same strings are the values returned by `getCategory()` in `VideoGame`, `Console` and `Accessory`. A new kind of product, which the business already foresees, requires editing the service as well as the product hierarchy, and nothing warns if they disagree. See **V-06**.
* **`AccessoryService`:** `Violation`
  * A new type of accessory requires editing the constants (lines 29–31), the check in `listAccessoriesByType` (lines 149–153) and the `switch` of `matchesType` (lines 268–279). See **V-04**.
* **`WarrantyService`:** `Violation`
  * A new type of warranty requires editing the four type checks listed under Polymorphism. See **V-05**.
* **Use of the exceptions in the repositories of Requirement 6:** `Correct`
  * `ProductRepository`, `PersonRepository` and `SaleRepository` translate the I/O errors into `PersistenceException` (for example `PersonRepository` lines 98 and 122), so the menu does not depend on `IOException` or on Gson.
* **`PersonService`:** `Correct`
  * The format of the files is hidden behind `PersonRepository` (lines 47–79). `findCustomerById` and `findSellerById` return `Optional`, so the callers do not depend on how a missing person is represented.
* **`Person`, `Customer`, `Seller`:** `Correct`
  * Their state is private and accessed only through methods.
