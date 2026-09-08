# Analysis — GameZone Unicesar

## People

### Q1: What attributes are common to all people who interact with the store, and which are specific to each type of person? How is this distinction reflected in a class hierarchy?

Common attributes: name, identification, and phone number. Specific attributes: customers have an email and a purchase history, while sellers have an employee code and a work shift. We reflect this using inheritance: we create a base class called `Person` for the common data, and two subclasses (`Customer` and `Seller`) that extend `Person` to add their specific data.

### Q2: Should there be a class representing a "generic person" without specifying its role? Why or why not? What implication does this decision have on the possibility of instantiating this class?

No, we shouldn't be able to create a "generic person". In the store, everyone is either a `Customer` or a `Seller`. Because of this, the `Person` class must be declared `abstract`. This allows us to share attributes with the subclasses, but prevents the program from instantiating a `Person` directly.

## Products

### Q3: What characteristics do all products sold by the store share, regardless of type? Which characteristics are specific to each product type?

Common characteristics: identifier, title, price, and stock (quantity available). Specific to video games: platform, genre, and age rating. Specific to consoles: brand, model, and generation.

### Q4: Each type of product must be able to present a description that integrates its particular characteristics. How should this behavior be declared in the base class to guarantee that all subclasses implement it in their own way? What object-oriented programming mechanism enables this?

We need to create an abstract method in the base `Product` class (for example, `public abstract String getDescription();`). Since the method is abstract, the subclasses (`VideoGame` and `Console`) are forced to write their own implementation for it, using `@Override`. The OOP mechanism that allows this behavior is polymorphism.

## Sales and relationships

### Q5: A sale involves a customer, a seller, and one or more products. What kinds of relationships exist between the class representing the sale and the other classes of the system? Are these relationships of inheritance, association, composition, or another type? Justify.

`Sale` is related to `Customer` and `Seller` through simple association: a `Sale` references one `Customer` and one `Seller`, but neither depends on the sale to exist — the same customer or seller takes part in many other independent sales over time. `Sale` is related to `Product` through aggregation: a sale holds a collection of products, but those products belong to the store's inventory and continue to exist there independently, even after the sale record is removed. There is no inheritance here, since a `Sale` is not a kind of `Person` or `Product` — it is a transactional entity that links them together.

### Q6: Should the sale be responsible for calculating its own total, or should this responsibility fall on another class? Justify your decision.

The `Sale` class should calculate its own total. Since the `Sale` object contains the list of products being bought, it already has direct access to their prices. It is simpler and makes more sense to loop through the products and sum the prices right there in the class.

## Business constraints

### Q7: How does the design guarantee that a sale cannot be registered without at least one product? At what point in the system should this rule be validated?

This rule must be checked before saving the sale. We validate this in the service layer (`SaleService`). When the user tries to create a sale, the service checks if the product list is empty. If it is, the service rejects the operation and shows an error message.

### Q8: How does the design reflect the automatic update of inventory when a sale is registered? Which classes are involved in this operation?

When a sale is confirmed, `SaleService` needs to reduce the stock of the sold products. It calls `ProductService` to update the quantity, and then the persistence classes save the new stock in the files. The classes involved are `SaleService`, `ProductService`, `Product`, and the persistence classes.

## Layered organization

### Q9: The system must be organized into four layers: model, persistence, services, and user interface. What type of classes belong to each layer? What criterion allows one to decide in which layer a class should be placed?

Model: the core objects like `Person`, `Product`, and `Sale`. Persistence: the classes that read and write data to the text files. Service: the classes that contain the rules (like validating stock or rejecting empty sales). User interface (UI): the console menus that talk to the user. The criterion is what each class actually does — we place classes in layers based on their responsibility, so the code stays organized instead of mixed together.

### Q10: Why should the logic for saving and retrieving data from files not be inside the domain classes? What problems arise when these responsibilities are mixed?

If we put the file logic inside the domain classes (model), the code becomes messy and hard to read. Also, if we want to change how we save files later, we would have to modify our main objects. It is better to keep the model clean and leave the file operations to the persistence layer.

### Q11: What dependencies are allowed between the layers, and which are forbidden? Justify the meaning of the allowed dependencies.

Allowed: `ui` depends on `service`; `service` depends on `persistence` and `model`; `persistence` depends on `model`. Forbidden: `ui` cannot talk directly to `persistence`, and `model` cannot depend on any other layer. This flow protects the model: the core of the program shouldn't care whether we are using a console menu or saving to text files.
