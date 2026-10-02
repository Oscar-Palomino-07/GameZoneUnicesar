# Warranty Class Diagram — GameZone Unicesar

Warranty module and its integration with the existing sales flow. Classes that already existed are shown only with the members involved in the integration.

```mermaid
classDiagram

    %% ===== MODEL LAYER =====
    class Warranty {
        <<abstract>>
        -id: String
        -product: Product
        -sale: Sale
        -startDate: LocalDate
        -endDate: LocalDate
        +getId() String
        +getProduct() Product
        +getSale() Sale
        +getStartDate() LocalDate
        +getEndDate() LocalDate
        +getDurationInMonths() int*
        +getWarrantyType() String*
        +getAdditionalCost() double*
        +isActive(date: LocalDate) boolean
        +generateWarrantyCertificate() String
    }
    class BasicWarranty {
        +getDurationInMonths() int
        +getWarrantyType() String
        +getAdditionalCost() double
    }
    class ExtendedWarranty {
        +getDurationInMonths() int
        +getWarrantyType() String
        +getAdditionalCost() double
    }
    class Product {
        <<abstract>>
        -id: String
        -title: String
        -price: double
        -stock: int
    }
    class Console
    class Sale {
        -id: String
        -date: LocalDate
        -products: List~Product~
        -warrantyExtraCost: double
        +getWarrantyExtraCost() double
        +setWarrantyExtraCost(warrantyExtraCost: double) void
        +calculateTotal() double
    }

    Warranty <|-- BasicWarranty
    Warranty <|-- ExtendedWarranty
    Product <|-- Console
    Warranty "*" --> "1" Product
    Warranty "*" --> "1" Sale
    Sale "1" o-- "1..*" Product

    %% ===== PERSISTENCE LAYER =====
    class WarrantyRepository {
        -saleRepository: SaleRepository
        +saveAll(warranties: List~Warranty~) void
        +loadAll() List~Warranty~
    }
    class SaleRepository {
        +saveAll(sales: List~Sale~) void
        +loadAll() List~Sale~
    }

    WarrantyRepository ..> Warranty
    WarrantyRepository --> SaleRepository

    %% ===== SERVICE LAYER =====
    class WarrantyService {
        -repository: WarrantyRepository
        +assignBasicWarranty(product: Product, sale: Sale, startDate: LocalDate) BasicWarranty
        +assignExtendedWarranty(product: Product, sale: Sale, startDate: LocalDate) ExtendedWarranty
        +findWarrantyByProduct(productId: String, saleId: String) Warranty
        +listAllWarranties() List~Warranty~
        +listActiveWarranties() List~Warranty~
        +listWarrantiesExpiringSoon(daysAhead: int) List~Warranty~
    }
    class SaleService {
        -saleRepository: SaleRepository
        -warrantyService: WarrantyService
        +registerSale(customerId: String, sellerId: String, productIds: List~String~) Sale
        +registerSale(customerId: String, sellerId: String, productIds: List~String~, productIdsWithExtendedWarranty: List~String~) Sale
    }

    WarrantyService "1" --> "1" WarrantyRepository
    SaleService "1" --> "1" SaleRepository
    SaleService "1" --> "1" WarrantyService
    SaleService ..> Sale
    SaleService ..> Console : basic warranty only for consoles
    WarrantyService ..> Warranty

    %% ===== UI LAYER =====
    class ConsoleMenu {
        -saleService: SaleService
        -warrantyService: WarrantyService
        +start() void
    }

    ConsoleMenu "1" --> "1" SaleService
    ConsoleMenu "1" --> "1" WarrantyService

    %% ===== ENTRY POINT =====
    class Main {
        +main(args: String[]) void
    }

    Main ..> WarrantyRepository
    Main ..> WarrantyService
    Main ..> ConsoleMenu
```

## Integration notes

- `SaleService.registerSale` creates one `BasicWarranty` for every `Console` in the sale and one `ExtendedWarranty` for each console whose identifier is listed in `productIdsWithExtendedWarranty`. The cost of the extended warranties is stored in `Sale.warrantyExtraCost` and included in `Sale.calculateTotal()`.
- `WarrantyRepository` persists only the identifiers of the sale and the product (`data/warranties.csv`, with a `BASIC`/`EXTENDED` discriminator) and rebuilds the references through `SaleRepository` when loading.
- The original three-parameter `registerSale` remains and delegates to the new overload, so the existing behavior is not broken.
