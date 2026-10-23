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
