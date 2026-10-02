# Return Class Diagram — GameZone Unicesar

Return module and its integration with the sales flow. Classes that already existed are shown
only with the members involved in the integration.

```mermaid
classDiagram

    %% ===== MODEL LAYER =====
    class Return {
        -id: String
        -saleId: String
        -date: LocalDate
        -customer: Customer
        -seller: Seller
        -products: List~Product~
        +getId() String
        +getSaleId() String
        +getDate() LocalDate
        +getCustomer() Customer
        +getSeller() Seller
        +getProducts() List~Product~
        +calculateRefundAmount() double
        +generateReturnReceipt() String
    }
    class Sale {
        -id: String
        -date: LocalDate
        -customer: Customer
        -seller: Seller
        -products: List~Product~
        -warrantyExtraCost: double
        +getProducts() List~Product~
        +calculateTotal() double
        +canBeReturned() boolean
    }
    class Product {
        <<abstract>>
        -id: String
        -title: String
        -price: double
        -stock: int
        +getId() String
        +getPrice() double
        +getStock() int
        +updateStock(quantity: int) void
    }
    class Console
    class VideoGame

    Product <|-- Console
    Product <|-- VideoGame
    Sale "1" o-- "1..*" Product
    Return "1" --> "1" Customer
    Return "1" --> "1" Seller
    Return "1" o-- "1..*" Product

    %% ===== PERSISTENCE LAYER =====
    class ReturnRepository {
        +saveAll(returns: List~Return~) void
        +loadAll() List~Return~
    }
    class ProductRepository {
        +saveAllVideoGames(videoGames: List~VideoGame~) void
        +loadAllVideoGames() List~VideoGame~
        +saveAllConsoles(consoles: List~Console~) void
        +loadAllConsoles() List~Console~
    }

    ReturnRepository ..> Return
    ReturnRepository ..> Product : polymorphic deserializer rebuilds VideoGame or Console
    ProductRepository ..> Console
    ProductRepository ..> VideoGame

    %% ===== SERVICE LAYER =====
    class ReturnService {
        -returnRepository: ReturnRepository
        -saleService: SaleService
        -personService: PersonService
        -productService: ProductService
        +registerReturn(saleId: String, productIds: List~String~) Return
        +viewAllReturns() List~Return~
        +viewReturnsByCustomer(customerId: String) List~Return~
        +viewReturnsBySale(saleId: String) List~Return~
        +generateMonthlyBalance(month: int, year: int) double
        +save() void
    }
    class SaleService {
        -saleRepository: SaleRepository
        +findById(saleId: String) Sale
        +viewAllSales() List~Sale~
    }
    class ProductService {
        -repository: ProductRepository
        +findById(productId: String) Product
        +updateStock(productId: String, quantity: int) void
        +restoreStock(productId: String, quantity: int) void
    }

    ReturnService "1" --> "1" ReturnRepository
    ReturnService "1" --> "1" SaleService
    ReturnService "1" --> "1" PersonService
    ReturnService "1" --> "1" ProductService
    ProductService "1" --> "1" ProductRepository
    SaleService ..> Sale

    %% ===== UI LAYER =====
    class ConsoleMenu {
        -returnService: ReturnService
        +returnMenu() void
        +registerReturn() void
        +viewAllReturns() void
        +viewReturnsByCustomer() void
        +viewReturnsBySale() void
        +reportsMenu() void
        +monthlyBalance() void
    }

    ConsoleMenu "1" --> "1" ReturnService

    %% ===== ENTRY POINT =====
    class Main {
        +main(args: String[]) void
    }

    Main ..> ReturnRepository
    Main ..> ReturnService
    Main ..> ConsoleMenu
```

## Integration notes

- `ReturnService.registerReturn` is the only entry point of the module. Its order matters:
  validate the arguments, locate the sale, verify the return period with
  `Sale.canBeReturned()`, verify that every item was part of the sale and was not already
  returned, and only then restore the stock and persist the new return. A rejected return
  therefore leaves the inventory untouched.
- A return is stored with the identifiers of the sale and of the items, plus the customer
  and the seller copies needed to print the receipt; `ReturnRepository` rebuilds the
  polymorphic `Product` hierarchy with a Gson deserializer.
- `Return` keeps a reference to the products it returns, and `ReturnService` obtains them
  from `ProductService`, so the return always reflects the current catalog data instead of
  duplicating the prices.
- The monthly balance is derived from the sales owned by `SaleService` and from the returns
  owned by `ReturnService`, which is why the report lives in `ReturnService`.