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
        -warrantyRefund: double
        +getId() String
        +getSaleId() String
        +getDate() LocalDate
        +getCustomer() Customer
        +getSeller() Seller
        +getProducts() List~Product~
        +getWarrantyRefund() double
        +setWarrantyRefund(warrantyRefund: double) void
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
    class Accessory
    class Controller
    class Cable
    class Memory

    Product <|-- Console
    Product <|-- VideoGame
    Product <|-- Accessory
    Accessory <|-- Controller
    Accessory <|-- Cable
    Accessory <|-- Memory
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
    class AccessoryRepository {
        +saveAll(accessories: List~Accessory~) void
        +loadAll() List~Accessory~
    }

    ReturnRepository ..> Return
    ReturnRepository ..> Product : polymorphic deserializer rebuilds VideoGame, Console, Controller, Cable or Memory
    ProductRepository ..> Console
    ProductRepository ..> VideoGame

    %% ===== SERVICE LAYER =====
    class ReturnService {
        -returnRepository: ReturnRepository
        -saleService: SaleService
        -personService: PersonService
        -productService: ProductService
        -accessoryService: AccessoryService
        -warrantyService: WarrantyService
        +registerReturn(saleId: String, productIds: List~String~) Return
        +viewAllReturns() List~Return~
        +viewReturnsByCustomer(customerId: String) List~Return~
        +viewReturnsBySale(saleId: String) List~Return~
        +calculateMonthlySales(month: int, year: int) double
        +calculateMonthlyReturns(month: int, year: int) double
        +generateMonthlyBalance(month: int, year: int) double
        +save() void
    }
    class AccessoryService {
        -repository: AccessoryRepository
        +findById(accessoryId: String) Accessory
        +updateStock(accessoryId: String, quantity: int) void
        +restoreStock(accessoryId: String, quantity: int) void
    }
    class WarrantyService {
        +cancelWarranties(productId: String, saleId: String) double
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
    ReturnService "1" --> "1" AccessoryService : resolves and restores accessory items
    ReturnService "1" --> "1" WarrantyService : cancels the warranties of returned consoles
    ProductService "1" --> "1" ProductRepository
    AccessoryService "1" --> "1" AccessoryRepository
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
- A returned item is resolved through `ReturnService.resolveItem`, which looks in
  `ProductService` first and falls back to `AccessoryService`, so a sale that mixes video games,
  consoles and accessories can be returned in full. The stock is then restored through the service
  that owns the item type: `AccessoryService.restoreStock` for accessories and
  `ProductService.restoreStock` for products. Both methods increment the current stock, unlike
  their `updateStock` counterparts, which replace it with an absolute value.
- A return is stored with the identifiers of the sale and of the items, plus the customer
  and the seller copies needed to print the receipt; `ReturnRepository` rebuilds the
  polymorphic `Product` hierarchy with a Gson deserializer that covers the whole catalog
  (`VideoGame`, `Console`, `Controller`, `Cable` and `Memory`), using the same discriminators
  already applied in `SaleRepository`.
- `Return` keeps a reference to the products it returns, and `ReturnService` obtains them
  from `ProductService`, so the return always reflects the current catalog data instead of
  duplicating the prices.
- The monthly balance is derived from the sales owned by `SaleService` and from the returns
  owned by `ReturnService`, which is why the report lives in `ReturnService`. Since adjustment
  A6 the report is split into three methods: `calculateMonthlySales` adds the final total of
  every sale of the month (`Sale.calculateTotal()`, that is, subtotal minus the promotion
  discount plus the extended warranty cost), `calculateMonthlyReturns` adds the refund of every
  return of the month, and `generateMonthlyBalance` keeps its signature and returns the
  difference between both. The balance may be negative, for example when the returns of a month
  belong to sales of the previous month, so it is reported instead of raising an exception.
  `ConsoleMenu.monthlyBalance` prints the three values.
- A returned console cannot keep an active warranty (adjustment A7). After the stock is restored,
  `ReturnService.registerReturn` calls `WarrantyService.cancelWarranties(productId, saleId)` once
  per returned console unit and stores the sum of the returned values in
  `Return.warrantyRefund`. `Return.calculateRefundAmount()` adds that value to the refund of the
  items, and `Return.generateReturnReceipt()` shows it on its own line.
