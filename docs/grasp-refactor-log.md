# GRASP Refactor Log — GameZone Unicesar

> Change log of the correction phase of the GRASP audit (exam Requerimiento 7). It contains the
> strategy to preserve the external behavior, the table of operations that is verified, one table of
> changes for each team member, the new classes created by the correction, the findings that are not
> corrected and the final verification. The violations are those of
> `docs/grasp-audit/consolidated-audit.md`.

* **Branch:** `feature/grasp-refactor`
* **Audited commit of `develop` (consolidated audit):** `f612269`
* **Commit of `develop` before the first correction (reference run):** `1a27972`

---

## 1. Strategy of Non-Regression Verification

It follows the answer to question Q4 of `docs/grasp-analysis.md`. The external behavior is the same when the same inputs give the same results on the console (receipts, lists, amounts and balances), the same messages appear for every error, and the files in `data/` keep their names and their JSON structure.

1. **Reference run before any correction.** `scripts/regression/run.sh` compiles the code and runs 15 scenarios against the console menu with fixed inputs: nine chained scenarios (`01-products` to `09-sale-after-everything`) and six isolated ones (corrupt files, a data folder that cannot be written and two empty identifiers that end the program). They cover the 31 operations of the eight menus of section 2, in their normal flow and with every kind of error. The run on `develop` before the first correction is stored in `scripts/regression/reference/baseline-pre-grasp-refactor`.
2. **Always the same starting data.** Each run works on a copy of `scripts/regression/fixtures/data` in a temporary folder, so it never changes the real `data/` folder and always starts from the same state. Dates (the sales and the warranties store the date of the day) and the month and year of the balance are masked when two runs are compared, so a reference run and a final run on different days are comparable.
3. **Rules that protect the data files.** Gson writes the names of the private attributes into the JSON files, so a correction must not rename or move an attribute of a persisted class (`Sale`, `Return`, the products, the promotions, the people and the warranty records). A correction that only adds a method or a class does not change the files. The comparison includes the content of every data file after each scenario.
4. **A check in every pull request.** Before each pull request the project compiles with `mvn clean compile`, and its author runs the check and compares it with the reference:

```bash
bash scripts/regression/run.sh after
bash scripts/regression/compare.sh baseline-pre-grasp-refactor after
```

   The expected result is `OK` in all the scenarios: this requirement does not change what the user sees, so there are no accepted differences. The description of the pull request lists the violation identifiers and the scenarios checked.
5. **Evidence at the end.** After the last correction the whole check is run on `develop` and the result is recorded in section 7, with the commit of `develop` of each run. In the oral defense the team runs it live.

**Scenarios of the check that depend on the corrections:** `01-products` (the listing order of V-09), `03-sales`, `04-warranties`, `05-returns` and `08-warranties-after-returns` (the extended warranty of V-01, V-02, V-03, V-04, V-12 and V-16, including its refund), `06-accessories` (V-15 and V-19), `07-promotions` (V-17 and V-19) and the balance of `05-returns` and `09-sale-after-everything` (V-14).

---

## 2. Operations Verified

The expected result of each operation is the result of the reference run. "Final" is completed after the last correction (section 7).

| # | Menu | Operation | Scenario | Key input | Expected result | Final |
|---|---|---|---|---|---|---|
| 1 | Products | Register video game | `01-products` | `V-1`, "Halo Infinite", price 200000, stock 10; duplicated id, price -5, blank title, stock -1, a typed letter as price | `Video game registered.`; each error with its message | Pending |
| 2 | Products | Register console | `01-products` | `C-2`, "PlayStation 5", 2800000, stock 3; duplicated id, blank brand | `Console registered.`; each error with its message | Pending |
| 3 | Products | List all products | `01-products` | After registering `V-7` once the consoles exist | All the video games first (including `V-7`) and then the consoles | Pending |
| 4 | People | Register customer | `02-people` | `P-1` Ana Torres, `P-2` Luis Perez; duplicated id, blank phone, e-mail without `@`, blank id | `Customer registered.`; each error with its message | Pending |
| 5 | People | List customers | `02-people` | | `P-1` and `P-2` with phone and e-mail | Pending |
| 6 | People | List sellers | `02-people` | | The three preloaded sellers `P-100`, `P-101` and `P-102` | Pending |
| 7 | Sales | Register sale | `03-sales` | `V-1` two video games; `V-2` console and cable with extended warranty; `V-5` two consoles with extended warranty; unknown customer, seller and product; insufficient stock; empty sale | Receipts: `V-1` total 280000.00, `V-2` extended warranty 250000.00 and total 2413250.00, `V-5` extended warranty 500000.00 and total 4750000.00; each error with its message | Pending |
| 8 | Sales | View all sales | `03-sales` | | The five sales with date, customer, seller, items, total and promotion | Pending |
| 9 | Sales | View sales by customer | `03-sales` | `P-1`, `P-999` | `V-1`, `V-4`, `V-5`; `No sales found for customer P-999.` | Pending |
| 10 | Sales | View sales by seller | `03-sales` | `P-100`, `S-999` | The sales of `P-100`; `No sales found for seller S-999.` | Pending |
| 11 | Sales | View sale detail | `03-sales` | `V-1`, `V-2`, `S-NOEXISTE` | The receipt; `No se encontró la venta S-NOEXISTE.` | Pending |
| 12 | Returns | Register return | `05-returns` | `V-1` product `V-1` twice; `V-2` console; `V-5` one unit; unknown sale, blank sale, no products | `D-1` refund 160000.00; the second one rejected; `D-2` and `D-4` with a refund of the extended warranty of 250000.00 and 2375000.00 in total; each error with its message | Pending |
| 13 | Returns | View all returns | `05-returns` | | The four returns with date, customer, items and refund | Pending |
| 14 | Returns | View returns by customer | `05-returns` | `P-1`, `P-2`, `P-999` | The returns of each customer; `No returns found for customer P-999.` | Pending |
| 15 | Returns | View returns by sale | `05-returns` | `V-1`, `V-2`, `S-NOEXISTE` | The returns of each sale; `No returns found for sale S-NOEXISTE.` | Pending |
| 16 | Reports | Monthly balance | `05-returns`, `09-sale-after-everything` | Current month and year; month 13; year 1999 | Sales 10220750,00, returns 5230000,00 and net balance 4990750,00 (`05-returns`); sales 10603250,00 and net balance 5373250,00 (`09-sale-after-everything`); the two invalid-period messages | Pending |
| 17 | Accessories | Register controller | `06-accessories` | "Control Pro", 300000, stock 3, `WIRELESS`; invalid connection, negative price, blank name | `Controller A-4 registered.`; each error with its message | Pending |
| 18 | Accessories | Register cable | `06-accessories` | "Cable DP", 80000, stock 5, 1.5 m, `DisplayPort`; zero length, blank connector | `Cable A-5 registered.`; each error with its message | Pending |
| 19 | Accessories | Register memory | `06-accessories` | "SSD 512", 200000, stock 2, 512 GB, `EXTERNAL`; zero capacity, blank type | `Memory A-6 registered.`; each error with its message | Pending |
| 20 | Accessories | List all accessories | `06-accessories` | | The six accessories with their description | Pending |
| 21 | Accessories | List by type | `06-accessories` | `CABLE`, `INVALID`, blank | The cables; `Tipo de accesorio no válido: INVALID. Use CONTROLLER, CABLE o MEMORY.`; `Debe indicar el tipo de accesorio.` | Pending |
| 22 | Accessories | Accessories compatible with a console | `06-accessories` | `1`, `C-2`, blank | The accessories of console `1`; none for `C-2`; the message for a blank id | Pending |
| 23 | Warranties | Warranty of a product in a sale | `04-warranties` | `V-2` and `1`; `V-1` and `V-1`; `S-NOEXISTE` | The extended certificate `W-2` (12 months, additional cost 250000.0); the two "not found" messages | Pending |
| 24 | Warranties | List all warranties | `04-warranties`, `08-warranties-after-returns` | After the sales and after the returns | The seven warranties `W-1` to `W-7` before the returns; only `W-3`, `W-6` and `W-7` after them, because the ones of the returned units are cancelled | Pending |
| 25 | Warranties | List active warranties | `04-warranties`, `08-warranties-after-returns` | | The same seven warranties before the returns and the same three after them, all active | Pending |
| 26 | Warranties | List warranties expiring soon | `04-warranties`, `08-warranties-after-returns` | 30, -1, a typed letter, 10, 3650 | `No hay garantías que venzan en los próximos 30 días.`; `El número de días no puede ser negativo.`; the same warranties for 3650 days | Pending |
| 27 | Promotions | Register percentage promotion | `07-promotions` | "Promo Test", 10%, 2026-01-01 to 2099-12-31; blank name, 150%, start after end, invalid date | `Promoción PR-5 registrada.`; each error with its message | Pending |
| 28 | Promotions | Register category promotion | `07-promotions` | "Category Promo", 5%, video games; invalid category | `Promoción PR-6 registrada.`; `Categoría no válida.` | Pending |
| 29 | Promotions | Register bulk purchase promotion | `07-promotions` | "Bulk Promo", 5%, minimum 4; minimum 0 | `Promoción PR-7 registrada.`; `La cantidad mínima de productos debe ser mayor que cero.` | Pending |
| 30 | Promotions | List all promotions | `07-promotions` | | `PR-1` to `PR-7` with percentage, validity and state | Pending |
| 31 | Promotions | List active promotions | `07-promotions` | | The seven promotions, all active | Pending |

---

## 3. Changes of the Technical Lead

One row for each change. The violation identifiers are those of the consolidated audit.

| Class | Change made | Violation ID | Principle applied | Commit / PR | Responsible |
|---|---|---|---|---|---|
| `scripts/regression` | The scenarios are extended so that the extended warranty (its cost and its refund) and the listing order of the products are verified; the reference run before the correction is added | — (verification of the phase) | — | `e723c75`, `05cda76` | Technical Lead |
| `Sale`, `ConsoleMenu` | `Sale.hasDiscount()` is added and used by `Sale.generateReceipt` and `ConsoleMenu.printSale`, so the rule that tells when a sale has a discount is in one place | V-06 | Information Expert | `cd3d001` | Technical Lead |
| `Sale`, `SaleService` | `Sale.applyPromotion(Promotion)` sets the name and the discount together; `SaleService.applyBestPromotion` calls it and the two public setters are removed | V-07 | Information Expert | `4ea510a` | Technical Lead |
| `Sale` | `countUnits(String)` and `contains(String)` are added, so the services stop reading the product list of the sale (the services are changed by Developer 2) | V-12 | Information Expert | `feb37d2` | Technical Lead |
| `SaleRepository` | The private `ProductDeserializer` and `LocalDateAdapter` are replaced by shared package-private classes; the repository keeps the same files and the same behavior | V-08 | Pure Fabrication | `0dc6d31` | Technical Lead |

---

## 4. Changes of Developer 1

| Class | Change made | Violation ID | Principle applied | Commit / PR | Responsible |
|---|---|---|---|---|---|

---

## 5. Changes of Developer 2

| Class | Change made | Violation ID | Principle applied | Commit / PR | Responsible |
|---|---|---|---|---|---|

---

## 6. New Classes Created by the Correction

Every class, interface or abstraction that a correction creates is listed here, with its package and its creator. Requirement 8 assigns its audit from this list.

| Class | Package | Created by | Violation ID |
|---|---|---|---|
| `LocalDateAdapter` | `com.gamezone.persistence` | Technical Lead | V-08 |
| `ProductDeserializer` | `com.gamezone.persistence` | Technical Lead | V-08 |

---

## 7. Findings That Are Not Corrected

These defects change what the user sees if they are corrected, so they are not corrected in this requirement (section 8 of the consolidated audit).

| # | Finding | Evidence | Justification |
|---|---|---|---|
| 1 | A blank identifier in the returns queries ends the program with a stack trace | `ConsoleMenu.viewReturnsByCustomer` and `viewReturnsBySale`; scenarios `iso-crash-returns-blank-customer` and `iso-crash-returns-blank-sale` | Handling it would replace the stack trace with a message |
| 2 | A product price of zero is rejected | `ProductValidator.validateProductData`, `price <= 0` | Accepting it again would change which inputs are valid |
| 3 | Memory and file are out of sync when a write fails | `SaleService.registerSale`, `ProductService.registerVideoGame` and `registerConsole`; scenario `iso-write-failure` | Saving before changing the memory would change what the user sees after the error |
| 4 | Only three modules use typed exceptions | `AccessoryService`, `PromotionService`, `ReturnService`, `WarrantyService` | Migrating them would change the messages that the user reads |
| 5 | I/O errors are silenced in four repositories | `AccessoryRepository`, `PromotionRepository`, `ReturnRepository`, `WarrantyRepository` | Throwing the exception would change the behavior at startup |

---

## 8. Final Verification

*To be completed by the Technical Lead after the last correction is merged: commit of `develop`, result of `compare.sh` against `baseline-pre-grasp-refactor` for the 15 scenarios, comparison of the data files, and the "Final" column of section 2.*
