# Warranty Analysis — GameZone Unicesar

## Q1: How is the difference between the two warranty types reflected in the class hierarchy, and which object-oriented mechanism lets each type have its own duration without duplicating code?

Both warranty types share the same data (identifier, covered product, sale, start date and end date) and the same behavior (checking whether they are active on a date, generating a certificate), but they differ in duration, type name and additional cost. This is modeled with an abstract `Warranty` class in the model layer that holds the shared attributes and concrete methods, and two subclasses, `BasicWarranty` and `ExtendedWarranty`, that only declare what changes.

The mechanism is abstraction combined with polymorphism. `Warranty` declares three abstract methods, `getDurationInMonths()`, `getWarrantyType()` and `getAdditionalCost()`, and each subclass implements them with `@Override`. Shared code such as `isActive(LocalDate)` and `generateWarrantyCertificate()` is written once in `Warranty` and calls those abstract methods, so it automatically uses the right duration and cost for whichever subtype the object really is. `Warranty` is abstract because a warranty without a type has no duration and cannot exist in the business.

## Q2: In which layer is the rule "only consoles get an automatic basic warranty" placed, and which Java mechanism verifies the real type of a product?

The rule lives in the service layer. `SaleService.registerSale` decides when a warranty must be created while it processes the sale, and `WarrantyService` enforces it again in `validateWarrantyRequest`, rejecting any product that is not a console. It does not belong to the model, because `Warranty` should not know how sales work, and it does not belong to the UI, because the UI only collects input and must not hold business rules.

The mechanism is the `instanceof` operator. A sale holds `Product` references, so the static type does not say whether an item is a `Console`, a `VideoGame` or an `Accessory`; `product instanceof Console` checks the real runtime type. Accessories are also products, so the check matters: only consoles receive warranties.

## Q3: How is the expiration date calculated in each subclass, and should the calculation be in the constructor or in a separate method?

The end date is calculated once, inside the constructor of `Warranty`, with `startDate.plusMonths(getDurationInMonths())`. The subclasses do not repeat the calculation: they only return 6 (`BasicWarranty`) or 12 (`ExtendedWarranty`) from `getDurationInMonths()`, and the base constructor uses that value.

Doing it in the constructor guarantees that every warranty is created in a consistent state: it can never exist without an end date, and the end date can never be out of sync with the start date because there is no setter for it. A separate method would allow objects that were built but never initialized. It also keeps persistence simple: the repository only stores the start date, and the end date is rebuilt automatically when the warranty is loaded. The trade-off is that a constructor calls an overridable method, which is safe here because the subclasses return constants and do not depend on their own fields.

## Q4: At which point of the sale registration flow is the extended warranty cost calculated and applied, and what changes are needed in `SaleService.registerSale`?

The cost is applied after the sale has been validated and created. The flow is: validate the customer, seller, products and stock; validate that every extended warranty request refers to a console that is part of the sale; discount the stock; create and save the `Sale`; assign one basic warranty to each console and one extended warranty to each console that requested it; add the `getAdditionalCost()` of the extended warranties to the sale and save it again.

The required changes are additive. `registerSale` gets an overload with a new parameter, `List<String> productIdsWithExtendedWarranty`, and the original three-parameter version delegates to it with an empty list, so existing behavior is untouched. `SaleService` receives `WarrantyService` as a new dependency. `Sale` gets a `warrantyExtraCost` attribute that is included in `calculateTotal()`, so the total shown to the user and the stored total both reflect the extra cost. The extended warranty request is validated before any stock is discounted, so a rejected request leaves the inventory unchanged. The sale is saved before the warranties are created because `WarrantyRepository` rebuilds its references by looking the sale up in the stored sales.

## Q5: In which class is the "warranties expiring soon" query placed, what dependencies does it need, and why is this location consistent with the layered architecture?

The query is `WarrantyService.listWarrantiesExpiringSoon(int daysAhead)` in the service layer. It needs the in-memory list of warranties, which the service loads from `WarrantyRepository` when it is created, the `getEndDate()` of each `Warranty` from the model, and the current date from `LocalDate.now()`. It returns the warranties whose end date is between today and today plus `daysAhead`, ignoring the ones that already expired, and rejects a negative number of days.

This location is consistent with the layers. The filtering is a business query, so it belongs in the service layer and not in the repository, which only saves and loads files, nor in the model, which must not depend on persistence. The UI reaches it only through `WarrantyService` from the warranty submenu of `ConsoleMenu`, so the dependency direction stays `ui → service → persistence → model` and the user interface never touches the repository.
