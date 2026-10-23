# Exception Analysis — GameZone Unicesar

> Answers to the five orienting questions of the validation and exception layer
> (exam Requerimiento 6), written against the implemented code: the
> `com.gamezone.exceptions` and `com.gamezone.validation` packages, the refactored
> `ProductService`, `PersonService`, `SaleService` and their repositories, and
> `ConsoleMenu`.

## Q1: Should `GameZoneException` be checked (`Exception`) or unchecked (`RuntimeException`)? What does that change in the signatures of services and repositories?

`GameZoneException` extends `RuntimeException`, so the whole hierarchy is unchecked. This is
the better option for this project for three reasons.

First, the existing signatures stay the same. `ProductService.registerVideoGame`,
`PersonService.registerCustomer` or `SaleService.registerSale` have no `throws` clause, and
neither do the repositories. With a checked root, every one of those methods, and every method
that calls them (the other services, `ReturnService`, `WarrantyService` and the whole of
`ConsoleMenu`), would have to declare `throws GameZoneException` or wrap each call in a
`try/catch`. The requirement asks for a refactor that keeps the external behavior, and
rewriting the signatures of the whole system is the opposite of that.

Second, the errors we model are not conditions the caller can repair on the spot. A product
that does not exist, a negative stock, a duplicated customer or an unreadable file are
reported to the user and the operation is cancelled. There is a single place that does that,
the menu, so forcing the intermediate layers to declare or rethrow the exception adds noise
without adding safety.

Third, the menu relies on it: `ConsoleMenu.runGuarded(Runnable)` runs every menu action
through a `Runnable`, and a `Runnable` cannot throw checked exceptions.

The cost of unchecked exceptions is that the compiler no longer forces anyone to handle them.
We saw it in practice: once the services were refactored and before `ConsoleMenu` was updated,
the program stopped with a stack trace on the first validation error, because the menu only
caught `IllegalArgumentException`. The mitigation is to document each exception with
`@throws` in the JavaDoc of the public methods (done in the services and validators) and to
have a regression check that exercises the error paths of every menu operation.

## Q2: Why is a hierarchy with a common root (`GameZoneException`) better than throwing a generic `IllegalArgumentException` everywhere? Which design principles apply?

Before the change, an insufficient stock, a duplicated identifier, an empty field and a missing
product were all the same `IllegalArgumentException`, so the menu could not tell whether the
mistake belonged to the user, to the inventory or to the system. The only thing it could do was
print the message.

The hierarchy gives each category of error its own type
(`ResourceNotFoundException`, `BusinessRuleException`, `InvalidDataException`,
`PersistenceException`), and the common root lets the code treat them together when it needs to.
The principles involved are:

- **Open/Closed Principle:** a new kind of error is a new subclass; the existing `catch`
  blocks and the services do not change.
- **Liskov Substitution Principle:** any subclass can be used where `GameZoneException` is
  expected, so code that only cares that "something of the application failed" can catch the
  root.
- **GRASP Protected Variations:** the menu depends on the stable abstraction (the exception
  types and their messages) and not on which validation failed or which file raised the error.
- **GRASP Information Expert and High Cohesion:** each exception carries the information it
  owns (`ResourceNotFoundException` the type and id of the resource, `InvalidDataException` the
  name of the field, `PersistenceException` the original cause, and the root an error code), so
  the message is built where that information is known.
- **Separation of concerns:** the rule that decides that something is wrong (the validator) is
  separated from the way the user is told (the menu).

One limit must be stated honestly: only the product, person and sale modules were migrated, as
the requirement asks. Accessories, promotions, returns and warranties still throw the standard
Java exceptions, and `ConsoleMenu` keeps a last `catch` block for them.

## Q3: Where in the flow are the validators invoked, and why is that location coherent with the layered architecture? Can the validators access the persistence package?

The validators are invoked from the service layer, as the first step of each business operation
and before anything is modified. For example `ProductService.registerVideoGame` calls
`ProductValidator.validateProductData`, `validateRequiredField` and `validateUniqueProduct`
before creating the `VideoGame`, and `SaleService.registerSale` validates the list of ids, the
customer and the seller, and the stock of every item before it creates the sale. Because every
check runs first, a failed validation leaves the system untouched.

This is coherent with the dependency direction `ui → service → persistence → model`: the
validation package sits between the service and the model. It depends only on `model` and on
`exceptions` (its imports are exactly those, plus `java.util.List`), and it is used only by the
services. The user interface does not know the validators, and the services do not mix the rules
into the workflow code.

The validators **cannot** access the persistence package, and they do not. Rules that need
stored data receive it as a parameter: `validateUniqueProduct(List<? extends Product> existing, String id)`
and `validateUniquePerson(List<? extends Person> existing, String id)` get the list that the
service already loaded, and `validateProductExists(Product product, String id)` gets the result
of the search. This keeps the validators static, free of side effects and easy to reason about,
and avoids two problems: the restrictions of the project forbid file access logic in the
validators, and a validator that reads files would make a business rule depend on the storage
format.

## Q4: How do we guarantee that the menu operations keep working the same? What evidence is shown at the end?

The strategy is a repeatable non-regression check in `scripts/regression/`:

1. **Reference run before touching anything.** `run.sh` compiles the code, starts the real
   application and replays fixed inputs through the console menu, saving what the program
   prints, its exit code and the content of the JSON files after each scenario. The reference
   was taken on `develop` before the exception layer (commit `74ce41b`) and is stored in
   `scripts/regression/reference/pre-exception-handling`.
2. **Coverage of the whole menu.** The scenarios exercise all 31 operations of the eight
   menus (the requirement counts 10, but the system has grown since), with the normal flow and
   with the error of every category: not found, business rule, invalid data and persistence
   (a corrupt file and a data folder that cannot be written).
3. **Isolated data.** The check works on a copy of fixed data in a temporary folder, so the real
   `data/` folder is never changed, and dates and the typed month and year are masked, so two
   runs on different days are comparable.
4. **Comparison.** `compare.sh` reports `OK` or `DIFF` per scenario, comparing the output, the
   exit code and the data files.

It was run after each section of `ConsoleMenu` was refactored and after the final change. The
final result against the reference shows only the differences that the requirement asks for:

- The text of the error messages, which are now differentiated by category.
- A customer with an invalid e-mail (`correo-invalido`) is now rejected by
  `PersonValidator`; before, it was stored. It is the only difference in the data files, and
  the later scenarios differ only because that customer no longer exists.
- A corrupt or unwritable data file now shows the persistence message instead of printing a
  technical line to the error stream and continuing with empty data.

Everything else is identical: sales, receipts, discounts, returns, refunds, the monthly
balance, warranties, accessories and promotions. The run also showed the case where the
program stopped with a stack trace when `ConsoleMenu` had not been updated yet, which is how we
confirmed that the check detects real regressions.

## Q5: `ConsoleMenu` used to catch one generic exception and now must catch four types. One `try` with several `catch` blocks, cascading `catch`, or a general `catch (GameZoneException)`? Why?

The choice is one `try` with several `catch` blocks ordered from the most specific type to the
most general one, written **once** in a private method, `ConsoleMenu.runGuarded(Runnable)`, that
every menu action uses (15 actions).

- A single `catch (GameZoneException)` would compile and be short, but it would give the same
  answer for the four categories, so the user would again not know whether the mistake was in
  the typed data, in the inventory or in the system. It would also push the code toward
  `instanceof` checks to tell them apart, which is exactly the conditional on type that
  Polymorphism advises against.
- Repeating the four `catch` blocks in each of the 15 menu actions would meet the requirement
  literally, but it would copy about 150 lines of near-identical code (five `catch` blocks of
  two lines in each action); any change to a message would have to be repeated 15 times.
- With one guarded method, the four categories are distinguished by the type system, each with
  its own message, and the message text lives in one place. The last block
  (`IllegalArgumentException | IllegalStateException`) keeps the old behavior for the modules
  that still use the standard exceptions.

The messages follow the categories: resource not found, business rule, invalid data and
persistence. The persistence message is deliberately generic ("contact the administrator"): the
user never receives a technical message or a stack trace, as the requirement demands. Because
the data files are read when the services are created, before the menu exists, `Main` handles
the persistence error at startup with the same message.
