# GRASP Analysis — GameZone Unicesar

> Answers to the five orienting questions of the GRASP audit (exam Requerimiento 7).
> Each question is answered by the team member assigned to it. The answers refer to the
> individual reports in `docs/grasp-audit/` and, when it exists, to the consolidated audit.

## Assignment of the questions

| Question | Topic | Assigned to |
|---|---|---|
| Q1 | Criteria for a violation, a correct application or "not applicable", and the minimum evidence | Oscar Palomino (Technical Lead) |
| Q2 | Boundary findings between the code of two team members | Oscar Palomino (Technical Lead) |
| Q3 | Fragments that break more than one pattern | Manuel Ospino (Developer 1) |
| Q4 | Strategy to preserve the external behavior | Veronica Padilla (Developer 2) |
| Q5 | Order of the corrections and coordination in the shared branch | Oscar Palomino (Technical Lead) |

Each assigned member writes the answer in the section of the question, in English and in their
own words. The Technical Lead answers Q2 and Q5 after the consolidated audit is merged, because
both depend on the final list of violations.

---

## Q1: What criteria did the team use to decide whether a fragment of code is a violation of a GRASP pattern, whether the pattern is correctly applied, or whether it does not apply? What minimum evidence must accompany each verdict to be accepted in the consolidation?

**Assigned to:** Oscar Palomino (Technical Lead).

For each pattern we started from the question that the pattern answers (for example, for Information Expert, "who has the information needed for this responsibility?") and applied it to each class and method of the code under our responsibility. The verdict depends on the answer:

- **Violation.** The responsibility exists in the code, it is placed in a class that does not own the information or the role that the pattern asks for, and that placement has a concrete cost: duplicated knowledge, a rule that lives in two places, or a change that is foreseeable for this business (new product types, new modules, a different storage) that would force editing several classes. A style preference with no such cost is not a violation. For example, the service that computes `item.getStock() - 1` outside `Product` was recorded as a violation of Information Expert because the same arithmetic is repeated in three classes.
- **Correct.** The responsibility is in the class that the pattern designates, and we can point at the class and the method where it happens. For example, `SaleService.registerSale` creates the `Sale` because it records the sales and has all the data to build it (Creator).
- **Not applicable.** The class has no point where the pattern could be applied, and we say why. For example, Creator does not apply to `Sale` because it creates no domain object, only a date.

We also decided not to force violations. A conditional that chooses a concrete product class while reading a JSON file (`SaleRepository.ProductDeserializer`) was judged correct for Polymorphism, because that is the only place where the type of the object has to be chosen. Creator was fully correct in the code of the Technical Lead, and the report says so.

The minimum evidence that a verdict needs is:

1. The **class and the method** involved, with the **exact lines** of the audited commit, which is stated at the top of each report.
2. For a violation, the **fragment of code** or the exact statement that shows it, an **explanation** of why it breaks the question of the pattern, and the **concrete consequence** today or under a foreseeable change.
3. For a violation, a **proposed solution** that names the pattern that would be applied and says whether it requires changing code of another team member.
4. For a correct verdict, the **place where the pattern is applied**; for a not applicable verdict, the **reason** why there is no application point.

Before consolidating, every line number and every quoted statement is checked against the code, and a finding without that evidence, or with the wrong classification, is returned to its author to be fixed in their own report.

---

## Q2: Some violations appear on the border between the code of two team members, for example when a class of one member depends in an inappropriate way on a class of another. What findings of this kind did the team identify? How were they registered in the consolidation and who was assigned their correction? Present at least one concrete case or, if there was none, explain how we verified that none existed.

**Assigned to:** Oscar Palomino (Technical Lead).

*Pending: to be written after the consolidated audit is merged.*

---

## Q3: Some patterns are closely related. Did the team find fragments of code that break more than one pattern at the same time? How were they registered without duplicating findings, and how was it decided which pattern guides the correction?

**Assigned to:** Manuel Ospino (Developer 1).

*Pending.*

---

## Q4: The correction must preserve the external behavior of the system. What strategy does the team follow to guarantee that all the menu operations keep working the same? What evidence is shown at the end to prove that nothing was broken?

**Assigned to:** Veronica Padilla (Developer 2).

*Pending.*

---

## Q5: In what order are the violations corrected and why? When the correction of a violation involves code of more than one team member, how is the work coordinated to avoid conflicts in the shared branch?

**Assigned to:** Oscar Palomino (Technical Lead).

*Pending: to be written after the consolidated audit is merged.*
