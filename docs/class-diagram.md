# Full Class Diagram — GameZone Unicesar

```mermaid
classDiagram

    %% ===== MODEL LAYER =====
    class Person {
        <<abstract>>
        -id: String
        -firstName: String
        -lastName: String
        -phone: String
    }
    class Customer {
        -email: String
    }
    class Seller {
        -employeeCode: String
        -shift: String
    }
    class Product {
        <<abstract>>
        -id: String
        -title: String
        -price: double
        -stock: int
        +getDescription() String*
        +updateStock(quantity: int) void
        +getStock() int
        +getId() String
        +getPrice() double
    }
    class VideoGame {
        -platform: String
        -genre: String
        -ageRating: String
        +getDescription() String
    }
    class Console {
        -brand: String
        -model: String
        -generation: String
        +getDescription() String
    }
    class Sale {
        -id: String
        -date: LocalDate
        -customer: Customer
        -seller: Seller
        -products: List~Product~
        +calculateTotal() double
        +canBeReturned() boolean
    }
    class Return {
        -id: String
        -date: LocalDate
        -customer: Customer
        -seller: Seller
        -products: List~Product~
        +calculateRefundAmount() double
        +generateReturnReceipt() String
    }

    Person <|-- Customer
    Person <|-- Seller
    Product <|-- VideoGame
    Product <|-- Console
    Sale "1" --> "1" Customer
    Sale "1" --> "1" Seller
    Sale "1" o-- "1..*" Product
    Return "1" --> "1" Customer
    Return "1" --> "1" Seller
    Return "1" o-- "1..*" Product

    %% ===== PERSISTENCE LAYER =====
    class ProductRepository {
        +saveAllVideoGames(videoGames: List~VideoGame~) void
        +loadAllVideoGames() List~VideoGame~
        +saveAllConsoles(consoles: List~Console~) void
        +loadAllConsoles() List~Console~
    }
    class PersonRepository {
        +saveAllCustomers(customers: List~Customer~) void
        +loadAllCustomers() List~Customer~
        +saveAllSellers(sellers: List~Seller~) void
        +loadAllSellers() List~Seller~
    }
    class SaleRepository {
        +saveAll(sales: List~Sale~) void
        +loadAll() List~Sale~
    }
    class ReturnRepository {
        +saveAll(returns: List~Return~) void
        +loadAll() List~Return~
    }

    ProductRepository ..> VideoGame
    ProductRepository ..> Console
    PersonRepository ..> Customer
    PersonRepository ..> Seller
    SaleRepository ..> Sale
    ReturnRepository ..> Return

    %% ===== SERVICE LAYER =====
    class ProductService {
        -repository: ProductRepository
        +registerVideoGame(id: String, title: String, price: double, stock: int, platform: String, genre: String, ageRating: String) void
        +registerConsole(id: String, title: String, price: double, stock: int, brand: String, model: String, generation: String) void
        +listAllProducts() List~Product~
        +findById(id: String) Product
        +updateStock(productId: String, quantity: int) void
        +restoreStock(productId: String, quantity: int) void
    }
    class PersonService {
        -repository: PersonRepository
        +registerCustomer(id: String, firstName: String, lastName: String, phone: String, email: String) void
        +listAllCustomers() List~Customer~
        +listAllSellers() List~Seller~
        +findCustomerById(id: String) Optional~Customer~
        +findSellerById(id: String) Optional~Seller~
    }
    class SaleService {
        -saleRepository: SaleRepository
        -productService: ProductService
        -personService: PersonService
        +registerSale(customerId: String, sellerId: String, productIds: List~String~) Sale
        +viewAllSales() List~Sale~
        +viewSalesByCustomer(customerId: String) List~Sale~
        +viewSalesBySeller(sellerId: String) List~Sale~
        +findById(saleId: String) Sale
    }
    class ReturnService {
        -returnRepository: ReturnRepository
        -saleService: SaleService
        -personService: PersonService
        -productService: ProductService
        +registerReturn(saleId: String, productIds: List~String~) Return
        +viewAllReturns() List~Return~
        +viewReturnsByCustomer(customerId: String) List~Return~
        +viewReturnsBySeller(sellerId: String) List~Return~
        +generateMonthlyBalance(month: int, year: int) double
        +save() void
    }

    ProductService "1" --> "1" ProductRepository
    PersonService "1" --> "1" PersonRepository
    SaleService "1" --> "1" SaleRepository
    SaleService "1" --> "1" ProductService
    SaleService "1" --> "1" PersonService
    ReturnService "1" --> "1" ReturnRepository
    ReturnService "1" --> "1" SaleService
    ReturnService "1" --> "1" ProductService
    ReturnService "1" --> "1" PersonService

    %% ===== UI LAYER =====
    class ConsoleMenu {
        -productService: ProductService
        -personService: PersonService
        -saleService: SaleService
        -returnService: ReturnService
        +start() void
    }

    ConsoleMenu "1" --> "1" ProductService
    ConsoleMenu "1" --> "1" PersonService
    ConsoleMenu "1" --> "1" SaleService
    ConsoleMenu "1" --> "1" ReturnService

    %% ===== ENTRY POINT =====
    class Main {
        +main(args: String[]) void
    }

    Main ..> ConsoleMenu
```
