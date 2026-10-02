# Promotion Module — Development Guide

Kickoff for **feature/promotion-module** (exam Requerimiento 2: Promotions).

Team: Oscar Palomino (Technical Lead, integration), Manuel Ospino (Developer 1, model),
Veronica Padilla (Developer 2, persistence + service).

## 1. Branch and Git flow

- Branch: `feature/promotion-module`, created from `develop` (tracking `origin/feature/promotion-module`).
- Everyone commits on this single feature branch; each commit is pushed **immediately** after creation.
- Commit messages: Conventional Commits, **English**, atomic (one concern per commit).
- Minimum per person: **6 atomic commits**. Minimum merged: **3 approved Pull Requests** (one per member).
  Suggested sequence of PRs `feature/promotion-module → develop`: (1) model hierarchy, (2) repository + service,
  (3) integration (Sale/SaleService/menu). Each PR is reviewed and merged before opening the next one.
- No direct commits to `main`/`develop`. At the end the feature branch merged and **deleted** from the remote.

## 2. Work split

### Developer 1 — model (`com.gamezone.model`)
- `Promotion` (abstract):
  - private `id: String`, `name: String`, `startDate: LocalDate`, `endDate: LocalDate`, `discountPercentage: double`.
  - constructor, getters, `boolean isActive(LocalDate date)` (start ≤ date ≤ end) + convenience `isActive()`.
  - `abstract double calculateDiscount(List<Product> products);`
- `PercentageDiscount extends Promotion` — no new fields. Discount = subtotal × percentage / 100.
- `CategoryDiscount extends Promotion` — new field `targetCategory: String`.
  Discount = sum of prices of products whose category equals `targetCategory`, × percentage / 100.
- `BulkPurchaseDiscount extends Promotion` — new field `minimumQuantity: int`.
  Discount = subtotal × percentage / 100 **only if** `products.size() >= minimumQuantity`, otherwise 0.
- Add `String getCategory()` to `Product` and implement it in `VideoGame` (`VIDEOGAME`), `Console` (`CONSOLE`)
  and `Accessory` (`ACCESSORY`) so category promotions can filter products without `instanceof`.
- JavaDoc in **English** on all public classes/methods; strict encapsulation.

### Developer 2 — persistence + service
- `PromotionRepository` (`com.gamezone.persistence`): `loadAll()`, `saveAll(List<Promotion>)` against
  `data/promotions.json` with Gson and a polymorphic deserializer (mirror `AccessoryRepository`):
  JSON field `targetCategory` → `CategoryDiscount`; `minimumQuantity` → `BulkPurchaseDiscount`;
  otherwise → `PercentageDiscount`.
- `PromotionService` (`com.gamezone.service`):
  - `registerPercentage(name, percentage, startDate, endDate)` → `Promotion`
  - `registerCategory(name, percentage, targetCategory, startDate, endDate)` → `Promotion`
  - `registerBulkPurchase(name, percentage, minimumQuantity, startDate, endDate)` → `Promotion`
  - `listAllPromotions()` → `List<Promotion>`
  - `listActivePromotions(LocalDate date)` → `List<Promotion>`
  - `bestPromotionFor(List<Product> products)` → `Optional<Promotion>`: among **active** promotions,
    returns the one giving the largest monetary discount (0 if none applies). **This selection logic
    stays in the service layer** (see analysis question Q3).
  - Validation (blank name, percentage 0–100, start ≤ end, non-blank category, `minimumQuantity ≥ 2`),
    ID sequence `PR-N`, persists after each registration.

### Technical Lead — integration (Oscar)
- `Sale` (additive): fields `appliedPromotionName: String`, `discountAmount: double`; keep the existing
  constructor and `calculateTotal()` (subtotal) intact; add `getSubtotal()`, `getAppliedPromotionName()`,
  `getDiscountAmount()`, `getFinalTotal()` (subtotal − discount) and `generateReceipt()`
  (subtotal, applied promotion name + discount, final total).
- `SaleService.registerSale`: after building the product list, ask `PromotionService.bestPromotionFor(...)`
  and store the applied name + amount on the new sale.
- `SaleRepository`: Gson serializes the new fields automatically; add null-safe access so legacy sales
  (without discount fields) still load. Note: a sale of accessories includes `Controller/Cable/Memory`
  (already supported by the `SaleRepository` deserializer).
- `ConsoleMenu`: submenu **"Gestión de promociones"** with 5 options (register percentage / category /
  volume, list all, list active) and sale detail/receipt showing the discount. **User-visible messages in
  Spanish** (exam rule), code comments in English.
- `Main`: wire `PromotionRepository`/`PromotionService` into `SaleService` and `ConsoleMenu`.
- `README.md` update; review the developers' Pull Requests.

## 3. Exam restrictions (summary)

- Strict encapsulation; correct use of `abstract` and `@Override`; layer dependencies `ui → service → persistence → model`.
- No file logic inside model classes.
- Identifiers, comments and commit messages in **English**; user-visible console messages in **Spanish**;
  JavaDoc in **English** on all new public classes/methods.
- Conventional Commits, ≥6 atomic commits per member, ≥3 approved merged PRs, no direct pushes to `main`/`develop`.

## 4. Open decision — file format

The exam text says `data/promotions.csv`, but this repository is JSON/Gson based and the feature branch already
ships `data/promotions.json` with three preloaded promotions (one per type, active range 2026-09-01 → 2026-12-31).
**Decision default: keep JSON** for consistency with the rest of the system. The Technical Lead must confirm with
the professor whether `.csv` is strictly required before the delivery.

## 5. Definition of Done

- Full project compiles (`javac`, Java 17 + Gson).
- End-to-end check: a sale that qualifies for more than one active promotion receives **only the largest
  discount**, the receipt shows subtotal / promotion name / discount / final total, and the data survives a
  reload from disk (legacy sales without discounts still load).
- `docs/promotion-analysis.md` answered by the three members; `docs/promotion-class-diagram.md` updated.