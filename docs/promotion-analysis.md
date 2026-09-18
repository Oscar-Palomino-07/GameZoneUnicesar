# Promotion Analysis — GameZone Unicesar

> Instructions: the three team members must discuss and answer the five orienting questions below.
> The answers must be left documented **in English** in this file, using the implemented code as evidence
> (see `docs/promotion-development-guide.md` for the module contract).

## Promotion hierarchy and polymorphism

### Q1: The three promotions have different calculation rules but share common attributes and behaviors. How is this situation reflected in the class hierarchy design? Which object-oriented programming mechanism allows each promotion type to calculate its discount differently without the rest of the system having to know the concrete types?

(Team answer: …)

### Q2: The base class `Promotion` cannot implement the discount calculation method because each type has different logic. How is this method declared in the base class, and what does this declaration guarantee with respect to the subclasses?

(Team answer: …)

## Selection and business rules

### Q3: The business rule states that only the promotion with the highest discount is applied. Which class hosts this selection logic, and why is that placement coherent with the layered-architecture principle? Why should this logic NOT be in the `Sale` class or in the console menu?

(Team answer: …)

## Integration with the sales flow

### Q4: What modifications are required in the `Sale` class and in the `generateReceipt` method so that the receipt shows the applied discount? Do these modifications break any existing behavior of the system?

(Team answer: …)

### Q5: Active promotions are determined by comparing the current date with each promotion's start and end dates. Where is this validation performed — in the `Promotion` class, in the `PromotionService`, or in both? Justify.

(Team answer: …)