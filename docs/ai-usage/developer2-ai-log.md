# Developer 2 — AI Usage Log

**Name:** Veronica Padilla
**Role:** Developer 2 (Person Module)
**Branch:** `feature/person-module`

Personal log of the artificial intelligence tools used during the development
of the GameZone Unicesar workshop. Each entry records the date, the tool used,
the question or doubt raised, and how the AI answer supported the work.

## 2026-09-08 — opencode

**Topic:** Implementing the person module (model, persistence and service layers).

**Questions raised & how AI helped:**

1. **Abstract class in Java and `super()` constructors.**
   I was designing `Person` as the base class but was not sure if declaring
   it abstract was the right way to prevent direct instantiation while still
   sharing attributes between `Customer` and `Seller`. I asked: "If I declare
   a class as abstract in Java, can it still have a constructor that
   subclasses call with super()?" The AI confirmed that abstract classes can
   have constructors called via `super()`. This allowed me to write a single
   constructor in `Person` for the shared fields (id, firstName, lastName,
   phone) and call it from `Customer` and `Seller` without duplicating code.

2. **Java 17 syntax for `instanceof` pattern matching.**
   I wanted to use `person instanceof Customer customer` in the service layer
   but was not sure if this syntax was available in Java 17 or required a
   preview flag. The AI confirmed it works natively in Java 17 (stable since
   Java 16). I used it in `PersonService` to cast and access the `email`
   field without an explicit `(Customer)` cast.

3. **Choosing the right file I/O API.**
   I was using `FileReader`/`FileWriter` but had read that
   `Files.newBufferedReader`/`newBufferedWriter` is preferred for UTF-8. The
   AI explained the difference and confirmed that the `Files` API defaults to
   UTF-8 and is the modern alternative. I switched to it in
   `PersonRepository` for both reading and writing.

4. **Return type for the find methods.**
   I needed `findCustomerById` and `findSellerById` but was not sure whether
   to return `null` or `Optional` when the person is not found. The AI
   recommended `Optional<Customer>` / `Optional<Seller>`: the sales module
   can handle `.isPresent()` without null checks, and it signals at the API
   level that the result might be absent.

5. **English method names following Java conventions.**
   I had method names in Spanish and needed proper English camelCase names. I
   asked for suggestions for methods that register a customer, list all
   customers, and find a customer by ID. I adopted `registerCustomer`,
   `listAllCustomers`, `findCustomerById`, `listAllSellers`,
   `findSellerById` — matching the class diagram.

6. **Commit strategy for the feature branch.**
   The workshop requires one atomic commit per logical change. I asked
   whether to commit after each class or batch them. The AI confirmed one
   commit per class, pushed immediately. I committed `Person`, then
   `Customer`, then `Seller`, then `PersonRepository`, then `PersonService`
   — each as a separate atomic commit on `feature/person-module`.

## 2026-09-08 — opencode (JSON migration)

**Topic:** Aligning the person module with the final `class-diagram.md`.

**Questions raised & how AI helped:**

1. **Verifying the final design from `docs/class-diagram.md`.**
   The class diagram on `develop` had been updated and I needed to confirm
   the final method signatures for `Person` and `PersonService` before
   writing code. I asked the AI to read the latest diagram and tell me if
   `Person` has a `getRole()` method or not. It does NOT have `getRole()` in
   the final diagram, so I removed it from my implementation. I also
   confirmed `PersonService` uses `findCustomerById` and `findSellerById`.

2. **Serializing `Customer` and `Seller` lists to JSON without a type
   discriminator.**
   The team decided to migrate from plain text to JSON using Gson. I had two
   separate lists (`List<Customer>` and `List<Seller>`) and was not sure if
   Gson needed a type field to distinguish them. The AI confirmed no
   discriminator is needed: each list goes to its own file
   (`data/customers.json`, `data/sellers.json`) and
   `TypeToken<List<Customer>>` handles the deserialization directly.

3. **Adding the Gson dependency to `pom.xml`.**
   I had never added Gson to a Maven project before and needed the correct
   coordinates. The AI provided `com.google.code.gson:gson:2.14.0` and
   confirmed no additional compiler configuration was needed.

4. **Renaming methods after the diagram update.**
   I had named my methods `findCustomerByIdentification` and
   `findSellerByEmployeeCode`, but the updated diagram uses
   `findCustomerById` and `findSellerById`, and I also needed to align the
   repository method names. To integrate the updated documentation without
   rewriting history, I asked about merge vs. rebase: merge is safe (no
   force-push needed), while rebase would rewrite history and require
   `--force`. I used `git merge develop` to integrate the updated
   documentation into the feature branch.

## 2026-09-08 — opencode (sync with develop after product PR)

**Topic:** Integrating the product module and unifying the persistence style.

**Questions raised & how AI helped:**

1. **Checking the remote state before merging.**
   I needed to verify that Manuel's product module PR had been merged into
   `develop` before pulling, so I would inherit the Gson dependency. The AI
   showed me how to check without opening GitHub in the browser:
   `git fetch` + `git log origin/develop --oneline` confirmed PR #11 was
   merged, and I proceeded with the merge.

2. **Resolving merge conflicts after integrating develop.**
   The merge produced two conflicts: `.gitignore` had duplicate IntelliJ
   comments, and `pom.xml` had different Gson versions (2.10.1 vs 2.14.0). I
   asked which version to keep; the decision was to keep 2.14.0 (the
   `develop` version) because it is the newer one and both modules should use
   the same version. For `.gitignore` I kept a single IntelliJ comment block.