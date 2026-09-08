# Model Layer — Class Hierarchies

```mermaid
classDiagram
    class Person {
        <<abstract>>
    }
    class Customer
    class Seller
    Person <|-- Customer
    Person <|-- Seller

    class Product {
        <<abstract>>
    }
    class VideoGame
    class Console
    Product <|-- VideoGame
    Product <|-- Console
```
