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
├── model         Person, Customer, Seller, Product, VideoGame, Console, Accessory, Controller, Cable, Memory,
│                 Sale, Return, Warranty, BasicWarranty, ExtendedWarranty,
│                 Promotion, PercentageDiscount, CategoryDiscount, BulkPurchaseDiscount
├── persistence   PersonRepository, ProductRepository, AccessoryRepository, SaleRepository,
│                 ReturnRepository, PromotionRepository, WarrantyRepository (JSON with Gson)
├── service       PersonService, ProductService, AccessoryService, PromotionService,
│                 WarrantyService, SaleService, ReturnService
├── ui            ConsoleMenu
└── Main.java     entry point; wires repositories, services and the menu
```

The layered architecture restricts the dependencies between layers: the user interface only talks to the services, the services validate the business rules and persist changes through the repositories, the repositories read and write the JSON files, and the model classes contain no file or user interface logic.

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
- `ai-usage/` — personal AI usage logs for each team member

## Team

Roles, class distribution and committed activities are described in [TEAM.md](TEAM.md).
