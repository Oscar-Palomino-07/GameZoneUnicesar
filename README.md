# GameZone Unicesar

Console-based inventory and sales management system for GameZone Unicesar, a video game store in the university district of Valledupar. Developed in Java with Maven and organized in four layers: model, persistence, service and user interface.

## Features

- Register video games and consoles.
- List all products available in the inventory.
- Register customers and list the registered customers and sellers.
- Register a sale with automatic stock discount and total calculation.
- Consult the complete sales history, the purchases of a specific customer and the sales attended by a specific seller.
- All information is stored in JSON files and loaded automatically at startup.

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

On the first run the application is ready to use: it loads the preloaded sellers from `data/sellers.json` and creates the remaining data files as they are needed.

### Main menu

- **1. Products** — register a video game, register a console, list all products
- **2. People** — register a customer, list customers, list sellers
- **3. Sales** — register a sale, view all sales, view sales by customer, view sales by seller
- **0. Exit**

## Project structure

```
com.gamezone
├── model         domain classes (Person, Customer, Seller, Product, VideoGame, Console, Sale)
├── persistence   JSON file-based repositories using Gson
├── service       business rules (PersonService, ProductService, SaleService)
├── ui            console menu
└── Main.java     entry point; wires repositories, services and the menu
```

The layered architecture restricts the dependencies between layers: the user interface only talks to the services, the services validate the business rules and persist changes through the repositories, the repositories read and write the JSON files, and the model classes contain no file or user interface logic.

## Data files

The application persists all information under `data/`:

- `sellers.json` — preloaded sellers (three on the first run)
- `customers.json` — registered customers
- `videogames.json` and `consoles.json` — product inventory
- `sales.json` — registered sales

## Documentation

See `docs/` for the design and analysis artifacts:

- `analysis.md` — answers to the eleven design orienting questions
- `class-diagram.md` — full class diagram (Mermaid)
- `hierarchy-diagram.md` — class inheritance hierarchy (Mermaid)
- `layers-diagram.md` — layered architecture and allowed dependencies (Mermaid)
- `ai-usage/` — personal AI usage logs for each team member

## Team

Roles, class distribution and committed activities are described in [TEAM.md](TEAM.md).