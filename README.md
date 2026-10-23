# GameZone Unicesar

Console-based inventory and sales management system for GameZone Unicesar, a video game store in the university district of Valledupar. Developed in Java with Maven and organized in four layers: model, persistence, service and user interface.

The system integrates the base store (products, people and sales) with four extensions that work together in the same sale: accessories, promotions, warranties and returns.

## Features

**Products and people**

- Register video games and consoles, and list all the products available in the inventory.
- Register customers and list the registered customers and sellers (three sellers are preloaded).

**Accessories**

- Register accessories (controllers, cables and memories) with the consoles they are compatible with.
- List all the accessories, filter them by type and consult the accessories compatible with a specific console.

**Sales**

- Register a sale that can include video games, consoles and accessories. The sale is processed in a fixed order: validation of the items and their stock, subtotal, best promotion, warranties, final total, inventory update and persistence. A rejected sale leaves no trace in the inventory.
- The receipt shows the subtotal, the discount with the name of the promotion, the cost of the extended warranties and the final total. The detail of any sale can be consulted later.
- Consult the complete sales history, the purchases of a specific customer and the sales attended by a specific seller.

**Promotions**

- Register percentage, category (video games, consoles or accessories) and bulk purchase promotions with a validity period.
- When a sale is registered, the active promotion that grants the largest discount is applied automatically (only one per sale). The discount is calculated only over the prices of the items.
- List all the promotions or only the ones that are active today.

**Warranties**

- Every console included in a sale automatically receives a 6-month basic warranty at no cost. When registering the sale, the seller can also add a 12-month extended warranty to each console; its cost (10% of the console price) is added to the total of the sale.
- Consult the warranty of a product within a sale, list all the warranties, list the ones that are active today and list the ones that expire within a given number of days.

**Returns and reports**

- Register full or partial returns within 30 days of the sale. The stock of the returned products and accessories is restored, and a rejected return does not change the inventory.
- The refund is proportional to the discount of the original sale, and the return receipt shows the list price, the proportional discount and the refunded amount of every item.
- When a console is returned, its warranties are cancelled and the cost of the extended warranty is refunded.
- Consult the returns of a customer or of a sale.
- Monthly balance: total sales (with discounts and extended warranties), total returns and net balance, which can be negative.

**Validation and error handling**

- A cross-cutting layer of custom exceptions replaces the generic `IllegalArgumentException` and `IOException` in the product, person and sale modules. Every exception extends `GameZoneException` and belongs to one of four categories: resource not found, business rule violated, invalid data and persistence failure.
- The business rules and data checks live in dedicated validators (`ProductValidator`, `PersonValidator`, `SaleValidator`) that the services call before changing anything.
- The menu tells the user what kind of problem happened, with a different message for each category, and never shows a technical message or a stack trace.

All the information is stored in JSON files and loaded automatically at startup.

## Requirements

- Java 17 or later
- Maven 3.x

## Build

```bash
mvn clean package
```

## Run

```bash
java -jar target/gamezone-unicesar.jar
```

On the first run the application is ready to use: it loads the preloaded data of the `data` folder (sellers, accessories and promotions) and creates the remaining data files as they are needed.

### Main menu

- **1. Products** — register a video game, register a console, list all products
- **2. People** — register a customer, list customers, list sellers
- **3. Sales** — register a sale (the menu asks whether to add the extended warranty to each console and prints the receipt), view all sales, view sales by customer, view sales by seller, view the detail (receipt) of a sale
- **4. Returns** — register a return, view all returns, view returns by customer, view returns by sale
- **5. Accessories** — register a controller, a cable or a memory, list all accessories, list accessories by type, consult the accessories compatible with a console
- **6. Reports** — monthly balance with the total sales, the total returns and the net balance
- **7. Gestión de garantías** — consult the warranty of a product in a sale, list all warranties, list active warranties, list warranties expiring soon (Spanish submenu, as required by the exam statement)
- **8. Gestión de promociones** — register a percentage, category or bulk purchase promotion, list all promotions, list the promotions that are active today (Spanish submenu)
- **0. Exit**

## Project structure

```
com.gamezone
├── model         Person, Customer, Seller, Product, ProductCategory, VideoGame, Console, Accessory, Controller,
│                 Cable, Memory, Sale, Return, Warranty, BasicWarranty, ExtendedWarranty,
│                 Promotion, PercentageDiscount, CategoryDiscount, BulkPurchaseDiscount
├── persistence   PersonRepository, ProductRepository, AccessoryRepository, SaleRepository,
│                 ReturnRepository, PromotionRepository, WarrantyRepository (JSON with Gson),
│                 JsonFileStore, LocalDateAdapter, ProductDeserializer (shared by the repositories)
├── service       PersonService, ProductService, ProductFactory, AccessoryService, PromotionService,
│                 WarrantyService, SaleService, ReturnService, InventoryService, MonthlyBalanceService
├── validation    ProductValidator, PersonValidator, SaleValidator, AccessoryValidator, PromotionValidator
├── exceptions    GameZoneException, ResourceNotFoundException, BusinessRuleException,
│                 InvalidDataException, PersistenceException
├── ui            ConsoleMenu (main loop), Menu, ConsoleInput, ConsoleErrorReporter, and one menu per module:
│                 ProductMenu, PersonMenu, SaleMenu, ReturnMenu, ReportMenu, AccessoryMenu, WarrantyMenu,
│                 PromotionMenu
└── Main.java     entry point; wires repositories, services and the menus
```

The layered architecture restricts the dependencies between layers: the user interface only talks to the services, the services call the validators and persist changes through the repositories, the repositories read and write the JSON files, and the model classes contain no file or user interface logic.

### Exceptions and validation layer

The `exceptions` package is transversal: any layer can throw its classes. The `validation` package sits between the service and the model layers: the services invoke the validators before any operation, the validators only depend on the model and the exceptions, and they never call the services or the repositories (the data they need is passed to them as parameters).

| Exception | Raised when | Message shown by the menu |
|---|---|---|
| `ResourceNotFoundException` | a product, customer, seller or sale does not exist | `No se encontró el recurso solicitado: <detail>` |
| `BusinessRuleException` | a business rule is broken (insufficient stock, duplicated identifier, sale without products) | `No se puede completar la operación: <detail>` |
| `InvalidDataException` | an input does not meet the format or range (blank field, non-positive price, negative stock, malformed e-mail) | `Los datos ingresados no son válidos: <detail>` |
| `PersistenceException` | a data file cannot be read or written | `Ocurrió un problema al acceder a los datos del sistema. Contacte al administrador.` |

The repositories of the product, person and sale modules catch `IOException` and rethrow it as `PersistenceException` with the file name and the operation. `ConsoleErrorReporter.runGuarded` handles all the categories in one place, and `Main` shows the persistence message if a data file cannot be read at startup. The accessory, promotion, return and warranty modules still report their errors with the standard Java exceptions, and the menu keeps handling them as before.

### GRASP correction (Requirement 7)

The code was audited against the nine GRASP patterns (`docs/grasp-audit/consolidated-audit.md`, 19 violations) and corrected without changing what the user sees. The design changes, grouped by theme:

| Theme | Change | Patterns |
|---|---|---|
| Stock | `Product.increaseStock` and `decreaseStock` replace the arithmetic that the services did outside the entity. `InventoryService` finds an item in the product or accessory inventory and changes its stock where it is kept, so `SaleService` and `ReturnService` no longer know the two inventories | Information Expert, Indirection |
| Warranties and types | `Product.supportsWarranty()`, `Accessory.getAccessoryType()` and `Warranty.getTypeCode()` replace the `instanceof` checks; `CategoryDiscount` uses `getCategory()` only | Polymorphism |
| Products | `ProductService` keeps one `List<Product>` and registers any product with `registerProduct`; `ProductFactory` builds each subtype; `ProductCategory` is the single list of categories | Polymorphism, Protected Variations |
| Sales | `Sale.hasDiscount()`, `applyPromotion()`, `countUnits()` and `contains()` answer what the services and the menu used to compute; `SaleService` tells the menu which items can receive an extended warranty | Information Expert, Controller |
| Reports | `MonthlyBalanceService` builds the monthly report that `ReturnService` used to contain; `ReturnService` no longer depends on `PersonService` | High Cohesion, Low Coupling |
| Persistence | `JsonFileStore` reads and writes the data files for the repositories, and `LocalDateAdapter` and `ProductDeserializer` are shared | Pure Fabrication |
| Validation | `AccessoryValidator` and `PromotionValidator` hold the data rules that the two services contained | Pure Fabrication, High Cohesion |
| User interface | `ConsoleMenu` only runs the main loop; each module has its own menu behind the `Menu` interface, and `ConsoleInput` and `ConsoleErrorReporter` hold the input reading and the error messages | High Cohesion, Protected Variations |

Every change is recorded with its violation, pattern, commit and responsible in `docs/grasp-refactor-log.md`, and the classes before and after are in `docs/grasp-class-diagram.md`.

### Non-regression check

`scripts/regression/` replays the console menu with fixed inputs against a copy of fixed data and compares the output and the data files with a reference run, without touching the real `data/` folder. It needs a Bash shell (Git Bash on Windows), a JDK and the Gson jar from the local Maven repository:

```bash
bash scripts/regression/run.sh after
bash scripts/regression/compare.sh baseline-pre-grasp-refactor after
```

`reference/baseline-pre-grasp-refactor` is the run taken on `develop` before the GRASP correction, and the expected result is no difference in any of the 15 scenarios. `reference/pre-exception-handling` is the older run taken before the exception layer of Requirement 6, kept as a record; its scenarios are not the same as the current ones.

## Data files

The application persists all information under `data/`:

- `sellers.json` — preloaded sellers (three on the first run)
- `customers.json` — registered customers
- `videogames.json` and `consoles.json` — product inventory
- `accessories.json` — accessory inventory (controllers, cables and memories; one of each type is preloaded)
- `sales.json` — registered sales, including the applied promotion, its discount and the extra cost of the extended warranties
- `promotions.json` — promotions (four preloaded: percentage, category of video games, category of accessories and bulk purchase)
- `warranties.json` — warranties generated by console sales, one record per warranty with a `BASIC`/`EXTENDED` type discriminator (they only store the sale and product identifiers; the end date is calculated from the start date and the warranty type)
- `return.json` — registered returns, including the subtotal and discount of the original sale and the refunded warranty cost

## Documentation

See `docs/` for the design and analysis artifacts:

- `analysis.md` — answers to the eleven design orienting questions of the Taller
- `class-diagram.md`, `hierarchy-diagram.md` — class diagram and inheritance hierarchy of the base system (Mermaid)
- `accessory-analysis.md`, `accessory-class-diagram.md` — accessories: design questions and class diagram
- `promotion-analysis.md`, `promotion-class-diagram.md` — promotions: design questions and class diagram
- `warranty-analysis.md`, `warranty-class-diagram.md` — warranties: design questions and class diagram
- `return-analysis.md`, `return-class-diagram.md` — returns: design questions and class diagram
- `integration-analysis.md` — the integration adjustments A1 to A7: cause and solution of each one
- `integrated-class-diagram.md` — single class diagram of the integrated system with the four layers (Mermaid)
- `layers-diagram.md` — layered architecture and allowed dependencies (Mermaid)
- `exception-analysis.md`, `exception-class-diagram.md` — validation and exception layer: design questions and class diagram
- `grasp-analysis.md` — answers to the five orienting questions of the GRASP audit
- `grasp-audit/` — the individual GRASP audit reports and the consolidated audit of the team
- `grasp-refactor-log.md` — non-regression strategy, change log of the GRASP correction and final verification
- `grasp-class-diagram.md` — classes involved in the violations, before and after the correction (Mermaid)
- `ai-usage/` — personal AI usage logs for each team member

## Team

Roles, class distribution and committed activities are described in [TEAM.md](TEAM.md).
