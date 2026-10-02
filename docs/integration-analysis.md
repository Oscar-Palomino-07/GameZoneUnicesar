# Integration Analysis — GameZone Unicesar

## 1. Context

The four extensions of the system (accessories, promotions, returns and warranties) were first built and validated one by one. Each of them changed the same central pieces — `SaleService.registerSale`, `ReturnService`, `ConsoleMenu` and the persistence of sales — so when they ran together some behaviors were wrong or undefined. This document describes the seven integration adjustments (A1 to A7) that were needed to make the four modules work as a single system, the cause of each one and the solution that was applied. A8 is this documentation and A9 is the publication of the integrated version to `main`.

The modules were integrated in this order, because each one depends on the previous ones: accessories widen the catalog used by the others, promotions work over sales that already include accessories, warranties change the signature of `registerSale` and the total of the sale, and returns must undo the effect of all of them.

| Adjustment | Branch | Type | Owner | Pull request |
|---|---|---|---|---|
| A1 | `feature/accessory-category-discount` | Feature | Developer 1 | #30, #32 |
| A2 | `fix/warranty-circular-dependency` | Fix | Developer 2 | #35 |
| A3 | `refactor/unified-sale-registration` | Refactor | Technical Lead | #33 |
| A4 | `fix/return-accessory-stock` | Fix | Developer 2 | #37 |
| A5 | `fix/return-discounted-refund` | Fix | Developer 1 | #36 |
| A6 | `fix/monthly-balance-report` | Fix | Developer 2 | #38 |
| A7 | `feature/return-warranty-cancellation` | Feature | Developer 2 | #39 |

## 2. A1 — Category discount for accessories

**Problem.** Requirement 2 limited the target category of a `CategoryDiscount` to `VIDEOGAME` and `CONSOLE`. With accessories in the catalog the store could not launch a campaign over them.

**Cause.** The category rule was written before the accessory module existed, so the accessory hierarchy was not part of the set of categories.

**Solution.** `Product` declares the abstract method `getCategory()`, implemented by `VideoGame` (`VIDEOGAME`), `Console` (`CONSOLE`) and `Accessory` (`ACCESSORY`). Because `Controller`, `Cable` and `Memory` inherit it from `Accessory`, `CategoryDiscount.calculateDiscount` recognizes every accessory without using `instanceof`. `PromotionService.registerCategory` validates that the category is one of the three allowed values, the category promotion menu offers accessories, and `data/promotions.json` includes a preloaded "25% en accesorios" promotion valid until 2026-12-31. The first pull request (#30) delivered the model and the validation; the second one (#32) completed the menu option and the preloaded promotion.

**Verification.** A sale that contains only a $100 cable receives the "25% en accesorios" promotion with a discount of $25, instead of the 15% general promotion that would give $15. Registering a category promotion with the category `BOOKS` is rejected.

## 3. A2 — Circular dependency in the warranty module

**Problem.** The Requirement 4 statement asks `WarrantyRepository` to resolve `Sale` and `Product` references while loading. In the statement's formulation this creates the chain `SaleService → WarrantyService → WarrantyRepository → SaleService`, which cannot be built in `Main` with constructor injection. In our first implementation the chain was softened into a dependency between repositories (`WarrantyRepository` received `SaleRepository`), but the persistence class still needed another module's data to load its own.

**Cause.** The repository was responsible for rebuilding the object graph. Rebuilding references between entities of different modules is a business-layer task, not a persistence one.

**Solution.** `WarrantyRepository` now has no dependencies: it only saves and loads warranty records (type, id, product id, sale id and start date). `WarrantyService` receives `WarrantyRepository`, `SaleRepository` and `ProductService` and resolves the `Sale` and `Product` of every record when it is created. `Main` builds `WarrantyService` before `SaleService`. A record whose sale or product no longer exists is skipped with a warning. The warranty class diagram was updated with the new dependencies.

**Verification.** A sale with a console and an extended warranty creates two warranties; after restarting the application both are rebuilt from the file and the extended one is found for that product and sale.

## 4. A3 — Unified sale registration flow

**Problem.** Requirements 1, 2 and 4 modified `registerSale` independently. The inventory was updated before the sale was created, the promotion applied and the warranties assigned, and the sale was added to the registry and saved before the process finished. If a later step failed, the user saw an error but the stock was already discounted and the sale was stored.

**Cause.** The operations were added in the order in which the modules were integrated, not in the order that the business rules require.

**Solution.** `registerSale` runs, always in this order: (1) validate that the sale has at least one item; (2) resolve each item as a product or an accessory and validate its stock; (3) create the sale and calculate the subtotal; (4) find the best promotion and register the discount, calculated only over the subtotal of the items; (5) generate the basic warranty of every console and the requested extended warranties, adding their cost; (6) final total = subtotal − discount + cost of the extended warranties; (7) update the inventory through `ProductService` or `AccessoryService` according to the type of each item; (8) add the sale to the registry and persist it once. All validations, including the one about the extended warranty request, run before anything is modified. `Sale.generateReceipt` shows the subtotal, the discount with the name of the promotion, the cost of the extended warranties and the final total.

**Verification.** With a warranty service that fails on purpose, the stock stayed 5/5 and no sale was registered; before the change the stock was 4/4 and a phantom sale was stored, and a retry sold the product twice. In the normal flow the result did not change: a console ($1000), a video game ($200) and an accessory ($100) with an extended warranty on the console give subtotal $1300, promotion 15% = $195, extended warranty $100 and a final total of $1205.

## 5. A4 — Returning accessories

**Problem.** `ReturnService` restored stock only through `ProductService.restoreStock`, which does not know accessories. Returning an accessory raised `Product not found in system` and, because the validation and the stock restoration shared the same loop, a mixed return restored the stock of the items processed first and then failed, leaving inflated inventory and no return recorded.

**Cause.** The return module was written before accessories existed and assumed that every item belongs to the product inventory. Validation and restoration were not separated.

**Solution.** `AccessoryService` gets `restoreStock`, equivalent to the one in `ProductService`. `ReturnService` receives `AccessoryService` and resolves every returned item as a product or an accessory, delegating the stock restoration to the service that owns it. `ReturnRepository` rebuilds the concrete accessory types (`Controller`, `Cable`, `Memory`) when it loads returns. Every item is validated first and the stock is restored only after all of them passed, so a rejected return leaves the inventory untouched.

**Verification.** Returning a console and an accessory restores both stocks (5/5). A return of `[console, game, game]` for a sale that included only one game is rejected and the console stock does not change.

## 6. A5 — Refund of sales with a discount

**Problem.** `Return.calculateRefundAmount` added the list prices of the returned items. When the original sale had a promotion, the customer was refunded more than they paid.

**Cause.** The return did not know the discount of the sale it came from.

**Solution.** `Return` stores the subtotal and the discount of the original sale when the return is registered, and `ReturnService` provides them. The refund of each item is `price × (1 − discount / subtotal)`. `generateReturnReceipt` shows, for every item, the list price, the proportional discount and the refunded amount. The new fields are persisted by `ReturnRepository`, and returns stored before the change (without those fields) keep refunding the list price.

**Verification.** In a sale with subtotal $1200 and a $180 discount, returning the $1000 console refunds $850 (a proportional discount of $150), and the value is the same after restarting the application.

## 7. A6 — Monthly balance report

**Problem.** Requirement 3 asks to show the total sales, the total returns and the net balance, but `generateMonthlyBalance` returned only the net balance. It also threw an exception when the returns of a month were greater than its sales, which is a valid business situation. With promotions and warranties integrated, the sales total must use the final total of every sale.

**Cause.** The report was written when the total of a sale was only the sum of the prices, and it treated a negative balance as inconsistent data.

**Solution.** `ReturnService` has `calculateMonthlySales`, which adds the final total of each sale (with discounts and extended warranties), and `calculateMonthlyReturns`. `generateMonthlyBalance` keeps its signature and returns the difference between both, negative or not. The monthly balance option of the reports menu shows the three values.

**Verification.** After the scenario of the demonstration the report shows sales $1205, returns $935 and net balance $270 (before A7); with A7 the returns are $1035 and the balance $170.

## 8. A7 — Warranty cancellation when a console is returned

**Situation.** None of the requirements defines what happens with the warranty of a returned console. In the integrated system a returned console cannot keep an active warranty.

**Solution.** `WarrantyService.cancelWarranties(productId, saleId)` removes the warranties of one unit of the product in that sale (at most one basic and one extended) and returns the refundable cost: zero for the basic warranty and the additional cost for the extended one. `ReturnService.registerReturn` invokes it for every returned console and stores the value in `Return`, which includes it in the refund amount and in the receipt ("Garantías anuladas"). If two identical consoles were bought and only one is returned, the other keeps its warranties.

**Verification.** In the demonstration scenario the console return leaves 0 warranties, `findWarrantyByProduct` returns `null`, and the refund is $1035 ($850 + $85 + $100 of the extended warranty). With two identical consoles, returning one leaves one warranty, and the cancellation is kept after restarting the application.

## 9. Integrated behavior

The full demonstration scenario was run over the final code with these results:

1. Sale with a console, a video game and an accessory: the best promotion (general 15%) is applied and the receipt shows subtotal $1300, discount $195, extended warranty $100 and total $1205.
2. Extended warranty on the console: two warranties exist and both are active.
3. Partial return of the console and the accessory: refund $1035; stock of the console and of the accessory restored; the warranties of the console are cancelled.
4. Monthly balance: sales $1205, returns $1035, net balance $170.

## 10. Deviations from the written statements

- **File format.** The statements name CSV files for accessories, promotions and warranties. The team kept JSON with Gson for all the data, as in the Taller, so the whole system uses a single persistence format (`accessories.json`, `promotions.json`, `warranties.json`, `return.json`, and so on).
- **Promotion API.** The statements name `findBestPromotionFor(Sale)` and `calculateDiscount(Sale)`. The module was built from the team's development guide (`docs/promotion-development-guide.md`) with `bestPromotionFor(List<Product>)`, which returns an `Optional`, and `calculateDiscount(List<Product>)`; `discountPercentage` was moved to the base class `Promotion`. `SaleService` calls `bestPromotionFor(sale.getProducts())`.
- **Branches.** Following the instruction of the professor, merged branches were kept in the remote repository so they can be reviewed. The only exception, also authorized by the professor, was the first `fix/return-accessory-stock` branch, which had been created from a local history that mixed other adjustments; it was deleted and replaced by a clean branch with the same name (#37).
