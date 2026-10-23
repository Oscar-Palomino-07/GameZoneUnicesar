# GRASP Audit Report — Developer 1

* **Auditor:** Manuel Ospino (Developer 1)
* **Assigned Scope:** Product Module (`Product`, `VideoGame`, `Console`, `ProductRepository`, `ProductService`) and Extension Model Hierarchies (`Accessory`, `Promotion`, `Warranty`).
* **Branch:** `feature/grasp-audit`
* **Date:** 2026-10-22

---

## 1. Information Expert

### Principle
Assign a responsibility to the class that has the necessary information to fulfill it. The information owner should perform the calculation or state mutation directly rather than exposing its internals to external classes.

### Audit by Class

#### 1. `com.gamezone.model.Product`
* **Verdict:** `Correct`
* **Evidence:**
  * Method: `updateStock(int quantity)` (lines 72–74).
  * The abstract class `Product` encapsulates the private attribute `stock` (line 14) and directly mutates it via `this.stock = quantity`. It does not expose mutable state to outside classes.
  * Method: `getCategory()` (line 65) and `getDescription()` (line 82) are declared `abstract` so each concrete product with specific fields provides the required data.

#### 2. `com.gamezone.model.VideoGame`
* **Verdict:** `Correct`
* **Evidence:**
  * Method: `getDescription()` (lines 69–76).
  * Formats the video game description using its own internal fields (`platform`, `genre`, `ageRating`, lines 10–12) together with inherited getters (`getTitle()`, `getPrice()`, `getStock()`), keeping textual formatting within the data owner.
  * Method: `getCategory()` (lines 58–60) directly returns `"VIDEOGAME"`, matching its own type identity.

#### 3. `com.gamezone.model.Console`
* **Verdict:** `Correct`
* **Evidence:**
  * Method: `getDescription()` (lines 69–76).
  * Formats the console description using its specific attributes (`brand`, `model`, `generation`, lines 10–12) and inherited attributes, fulfilling the Information Expert principle.
  * Method: `getCategory()` (lines 58–60) directly returns `"CONSOLE"`.

#### 4. `com.gamezone.persistence.ProductRepository`
* **Verdict:** `Correct`
* **Evidence:**
  * Methods: `saveAllVideoGames(List<VideoGame>)` (lines 47–49), `loadAllVideoGames()` (lines 57–59), `saveAllConsoles(List<Console>)` (lines 67–69), `loadAllConsoles()` (lines 77–79).
  * The repository acts as the expert on storage details: file paths (`data/videogames.json`, `data/consoles.json`, lines 31–32) and Gson `TypeToken` representations (lines 34–37).

#### 5. `com.gamezone.service.ProductService`
* **Verdict:** `Violation`
* **Evidence:**
  * Method: `restoreStock(String productId, int quantity)` (lines 148–158).
  * In line 156: `product.updateStock(product.getStock() + quantity);`. The service queries the product's current stock via getter, computes the addition externally, and sets the new value. Under Information Expert, the entity `Product` should define an `increaseStock(int quantity)` or `addStock(int quantity)` method to encapsulate arithmetic on its own state.


---

## 2. Creator

### Principle
Assign class $B$ the responsibility to create an instance of class $A$ if $B$ contains, aggregates, records, closely uses, or has the initializing data for $A$.

### Audit by Class

#### 1. `com.gamezone.model.Product`, `VideoGame`, `Console`
* **Verdict:** `Not applicable`
* **Evidence:** These classes are domain entities that represent catalog items. They do not hold or instantiate other domain objects.

#### 2. `com.gamezone.persistence.ProductRepository`
* **Verdict:** `Correct`
* **Evidence:**
  * Methods: `loadAllVideoGames()` (lines 57–59) and `loadAllConsoles()` (lines 77–79) via `readList` (lines 113–124).
  * The repository instantiates product collections from JSON files because it has the raw serialized data and the generic type tokens necessary to reconstruct the object graph.

#### 3. `com.gamezone.service.ProductService`
* **Verdict:** `Correct`
* **Evidence:**
  * Methods: `registerVideoGame(...)` (line 58: `new VideoGame(...)`) and `registerConsole(...)` (line 85: `new Console(...)`).
  * `ProductService` directly aggregates and records the created instances into its internal collections (`videoGames.add(...)`, `consoles.add(...)`) and possesses the full set of input attributes passed from the user interface.

---

## 3. Controller

### Principle
Assign the responsibility of handling system events/messages to a non-UI class that represents either the overall system (facade) or a specific use case scenario.

### Audit by Class

#### 1. `com.gamezone.model.Product`, `VideoGame`, `Console`
* **Verdict:** `Not applicable`
* **Evidence:** Domain model classes do not handle system operations or user interface actions.

#### 2. `com.gamezone.persistence.ProductRepository`
* **Verdict:** `Not applicable`
* **Evidence:** Dedicated strictly to file I/O operations; does not orchestrate application use cases.

#### 3. `com.gamezone.service.ProductService`
* **Verdict:** `Correct`
* **Evidence:**
  * Methods: `registerVideoGame`, `registerConsole`, `listAllProducts`, `findById`, `updateStock`, `restoreStock` (lines 52–158).
  * `ProductService` operates as the Use Case Controller for product management. The UI class `ConsoleMenu` delegates all user inputs to `ProductService`, ensuring that terminal I/O remains isolated from business workflows.


---

## 4. Low Coupling

### Principle
Assign responsibilities to maintain low dependency between classes, minimizing the impact of change and increasing reusability.

### Audit by Class

#### 1. `com.gamezone.model.Product`, `VideoGame`, `Console`
* **Verdict:** `Correct`
* **Evidence:**
  * Package: `com.gamezone.model`.
  * The model classes have zero imports from `service`, `persistence`, `ui`, or external libraries. They depend strictly on standard Java language types, ensuring absolute independence from technological choices.

#### 2. `com.gamezone.persistence.ProductRepository`
* **Verdict:** `Correct`
* **Evidence:**
  * Imports (lines 3–20): Only couples to `VideoGame`, `Console`, Google Gson, and standard Java I/O (`java.nio.file`).
  * The repository does not know `ProductService` or `ConsoleMenu`. It encapsulates third-party JSON serialization entirely, shielding the remainder of the system from changes in the persistence library.

#### 3. `com.gamezone.service.ProductService`
* **Verdict:** `Correct`
* **Evidence:**
  * Dependencies (lines 19–21, 29–33): Receives `ProductRepository` via constructor dependency injection.
  * It does not instantiate repositories directly, nor does it couple to the UI layer. Changes in `ConsoleMenu` do not affect `ProductService`.

---

## 5. High Cohesion

### Principle
Keep responsibilities focused, understandable and closely related. Avoid bloated classes that perform heterogeneous tasks.

### Audit by Class

#### 1. `com.gamezone.model.Product`, `VideoGame`, `Console`
* **Verdict:** `Correct`
* **Evidence:**
  * Each class has a single, well-defined responsibility: modeling the attributes and polymorphic description of its concrete catalog item.

#### 2. `com.gamezone.persistence.ProductRepository`
* **Verdict:** `Correct`
* **Evidence:**
  * Focuses exclusively on file I/O operations (reading and writing JSON files). It contains no terminal formatting, user prompts or business calculation logic.

#### 3. `com.gamezone.service.ProductService`
* **Verdict:** `Violation`
* **Evidence:**
  * Methods: `registerVideoGame` (lines 54–57), `registerConsole` (lines 81–84), `validate` (lines 170–177), and `isBlank` (lines 185–187).
  * `ProductService` mixes business coordination with low-level string sanitization (`isBlank`), primitive range validation (`price < 0 || stock < 0`), and terminal exception message formatting. This dilutes the class's primary role as a use case coordinator.


---

## 6. Polymorphism

### Principle
When related behaviors vary by type (class), assign responsibility for the behavior to the types using polymorphic operations rather than conditional logic based on type.

### Audit by Class

#### 1. `com.gamezone.model.Product`, `VideoGame`, `Console`
* **Verdict:** `Correct`
* **Evidence:**
  * Declarations: `Product.getDescription()` (line 82) and `Product.getCategory()` (line 65).
  * Overrides: `VideoGame` (lines 58–60, 69–76) and `Console` (lines 58–60, 69–76).
  * External callers (such as sales reporting or UI menus) print product descriptions or filter categories through dynamic dispatch without performing `if (p instanceof VideoGame)` or `switch(type)` type-checking.

#### 2. `com.gamezone.service.ProductService`
* **Verdict:** `Violation`
* **Evidence:**
  * Lines 20–21 and 92–97:
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
  * `ProductService` stores inventory items in segregated concrete lists rather than a single polymorphic `List<Product>`. Consequently, `listAllProducts()` must manually instantiate a new list and merge both collections on every call. If a third product type is added, `ProductService` must add another list field, modify `listAllProducts()`, and duplicate registration logic, breaking open/closed extensibility at the service level.

#### 3. Extension Models (Accessories and Promotions)
* **Verdict:** `Correct`
* **Evidence:**
  * `Accessory.getDescription()` is implemented polymorphically across `Controller`, `Cable`, and `Memory`.
  * `Promotion.calculateDiscount(List<Product>)` is an abstract method in `Promotion` implemented polymorphically by `PercentageDiscount`, `CategoryDiscount`, and `BulkPurchaseDiscount`. `SaleService` evaluates active promotions through dynamic dispatch without checking concrete promotion classes.


---

## 7. Pure Fabrication

### Principle
Assign a cohesive set of responsibilities to an artificial or technical class that does not represent a problem domain concept, in order to support Low Coupling, High Cohesion, and clean separation of concerns.

### Audit by Class

#### 1. `com.gamezone.model.Product`, `VideoGame`, `Console`
* **Verdict:** `Not applicable`
* **Evidence:** These classes directly mirror real-world entities in the video game store domain, not artificial software constructs.

#### 2. `com.gamezone.persistence.ProductRepository`
* **Verdict:** `Correct`
* **Evidence:**
  * Package: `com.gamezone.persistence` (lines 28–125).
  * A "repository" is not a physical concept in a game store, but an artificial technical abstraction invented to encapsulate file I/O, directory creation and Gson JSON serialization. Without `ProductRepository`, `Product` would be forced to handle file reading and writing, violating High Cohesion and Information Expert.

#### 3. `com.gamezone.service.ProductService`
* **Verdict:** `Correct`
* **Evidence:**
  * Package: `com.gamezone.service` (lines 17–196).
  * Application services are Pure Fabrications designed to coordinate workflows, enforce business policies, and orchestrate persistence across multiple domain objects without attaching procedural workflow logic to entities.

---

## 8. Indirection

### Principle
Assign responsibility to an intermediate object to mediate between other components or services, preventing direct coupling between them.

### Audit by Class

#### 1. `com.gamezone.model.Product`, `VideoGame`, `Console`
* **Verdict:** `Not applicable`
* **Evidence:** Domain model entities do not act as intermediaries between separate subsystems.

#### 2. `com.gamezone.persistence.ProductRepository`
* **Verdict:** `Correct`
* **Evidence:**
  * Methods: `saveAllVideoGames`, `loadAllVideoGames`, `saveAllConsoles`, `loadAllConsoles` (lines 47–79).
  * Serves as an indirection layer between in-memory product models and physical disk storage (`data/videogames.json` and `data/consoles.json`).

#### 3. `com.gamezone.service.ProductService`
* **Verdict:** `Correct`
* **Evidence:**
  * Operates as an indirection boundary between `ConsoleMenu` (UI layer) and `ProductRepository` (persistence layer). The UI never accesses repositories or files directly; it only communicates with `ProductService`. If storage mechanisms change, the UI layer remains completely insulated.


---

## 9. Protected Variations

### Principle
Identify points of predicted variation or instability; assign responsibilities to create a stable interface around them, protecting the rest of the system from ripple effects.

### Audit by Class

#### 1. `com.gamezone.model.Product`
* **Verdict:** `Correct`
* **Evidence:**
  * Lines 11–14: All state fields (`id`, `title`, `price`, `stock`) are strictly encapsulated with `private` visibility.
  * Abstract methods `getDescription()` and `getCategory()` provide a stable polymorphic interface, shielding services and menus from the addition of new catalog item types.

#### 2. `com.gamezone.persistence.ProductRepository`
* **Verdict:** `Correct`
* **Evidence:**
  * File paths (`data/videogames.json`, `data/consoles.json`) and JSON serialization mechanics are private implementation details. If the storage format changes (e.g. from JSON to XML or a relational database), the public methods `saveAllVideoGames`, `loadAllVideoGames`, etc. remain stable, preventing modifications in `ProductService`.

#### 3. `com.gamezone.service.ProductService`
* **Verdict:** `Violation`
* **Evidence:**
  * Methods: `registerVideoGame` (lines 52–60) and `registerConsole` (lines 79–87).
  * Instead of receiving a polymorphic `Product` or using a factory pattern, `ProductService` hardcodes explicit methods and concrete parameters for each product type. Adding a new product category (e.g. board games or digital subscriptions) forces modification of `ProductService`'s public interface, violating Protected Variations and the Open/Closed Principle.

#### 4. Extension Models (Promotions and Warranties)
* **Verdict:** `Correct`
* **Evidence:**
  * `Promotion`: Declares abstract `calculateDiscount(List<Product>)`, protecting `SaleService` from needing changes when new promotional schemes (e.g. 2-for-1 campaigns) are introduced.
  * `Warranty`: Declares abstract duration and cost calculations, protecting sales and returns workflows from changes when new warranty tiers are added.

---

## Summary of Identified Violations (Developer 1 Scope)

| Violation ID | Class / Method | Pattern Violated | Description |
|---|---|---|---|
| **V-01** | `ProductService.restoreStock(String, int)` (line 156) | **Information Expert** | The service performs arithmetic on the product's stock externally (`product.getStock() + quantity`) instead of calling an encapsulated `increaseStock(quantity)` method on `Product`. |
| **V-02** | `ProductService` (`validate`, `isBlank`, lines 170–187) | **High Cohesion** | Low-level string validation and error formatting are embedded inline within the service, mixing validation concerns with workflow coordination. |
| **V-03** | `ProductService.listAllProducts()` (lines 20–21, 92–97) | **Polymorphism** | Inventory is segregated into concrete `List<VideoGame>` and `List<Console>` collections, forcing repetitive collection merging instead of managing a unified `List<Product>`. |
| **V-04** | `ProductService.registerVideoGame` / `registerConsole` (lines 52–87) | **Protected Variations** | Specific registration methods are tightly bound to concrete product subtypes instead of using a polymorphic contract or factory abstraction. |
