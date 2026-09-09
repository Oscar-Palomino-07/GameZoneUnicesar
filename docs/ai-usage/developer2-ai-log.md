# Developer 2 — AI Usage Log

Personal log of the artificial intelligence tools used during the development
of the GameZone Unicesar workshop. Each entry records the date, the tool used,
the question or doubt raised, and how the AI answer supported the work.

## 2026-09-08 — opencode

**Topic:** Implementing the person module (model, persistence and service layers).

**Questions raised & how AI helped:**

1. **Design of the person hierarchy.**
   I asked how to distribute the common attributes (name, identification,
   phone) and the specific attributes of `Customer` (email) and `Seller`
   (employee code, shift) using inheritance. The AI confirmed the design:
   an abstract base class `Person` holding the shared attributes and an
   abstract `getRole()` method implemented by each subclass. This matches
   the class diagram agreed by the team.

2. **Java syntax and API doubts.**
   - Confirmed the `instanceof` pattern matching syntax introduced in
     Java 16 (`person instanceof Customer customer`) is compatible with the
     project's Java 17 target.
   - Confirmed how to read/write UTF-8 files with `Files.newBufferedReader`
     and `Files.newBufferedWriter` instead of the older `FileReader`/`FileWriter`.
   - Confirmed that `Optional` is returned by the find methods so the sales
     module can handle the "not found" case without null checks.

3. **Compilation errors.**
   No compilation errors appeared in this session. I used the AI to review
   the written classes and identify possible improvements before committing.

4. **Identifier naming.**
   I asked for suggestions in English for method names (`registerCustomer`,
   `listCustomers`, `findCustomerByIdentification`, `findSellerByEmployeeCode`)
   following Java naming conventions.

5. **Git workflow support.**
   I used the AI to confirm the order of the atomic commits (one per logical
   change, pushed immediately to `feature/person-module`) as required by the
   Git Flow rules of the workshop.

## 2026-09-08 — opencode (JSON migration)

**Topic:** Aligning the person module with the final `class-diagram.md`.

**Questions raised & how AI helped:**

1. **Reading the remote repository.**
   I asked the AI to review the remote `develop` branch on GitHub. It read
   the actual files (`docs/analysis.md`, `docs/class-diagram.md`,
   `hierarchy-diagram.md`, `layers-diagram.md`, `TEAM.md`) through the public
   GitHub API and confirmed that the authoritative diagram uses `Person(id,
   firstName, lastName, phone)` without `getRole()`, and that
   `PersonService` declares `findCustomerById`/`findSellerById`.

2. **Migrating persistence to JSON with Gson.**
   The team decided to switch the person files from plain text to JSON. I
   asked how to serialize the two subclasses with Gson without adding a
   type discriminator; the AI explained that, since `PersonRepository`
   loads each concrete type in its own list
   (`TypeToken<List<Customer>>`, `TypeToken<List<Seller>>`), Gson does not
   need polymorphic serialization, so the files stay clean.

3. **Maven/Gson setup.**
   I asked how to declare the Gson dependency in `pom.xml` and the AI
   confirmed the coordinates (`com.google.code.gson:gson:2.10.1`) and that
   the `maven-compiler-plugin` target 17 does not require extra config.

4. **Alignment of method names.**
   I used the AI to rename the service and repository methods so they match
   the class diagram exactly (`listAllCustomers`, `listAllSellers`,
   `findCustomerById`, `findSellerById`, `saveAllCustomers`,
   `loadAllCustomers`, etc.).

5. **Git Flow integration.**
   I confirmed that merging `develop` into the feature branch (instead of
   rebasing) is the compliant way to integrate the new documentation,
   because a rebase would require a forced push, which the workshop
   forbids.

## 2026-09-08 — opencode (sync with develop after product PR)

**Topic:** Integrating the product module and unifying the persistence style.

**Questions raised & how AI helped:**

1. **Checking when to pull `develop`.**
   The team confirmed Option A and told me to merge `develop` into
   `feature/person-module` only after the product module PR (Manuel) was
   merged, so the Gson dependency is inherited from `develop`. The AI helped
   me verify the remote state first (`git fetch` + `git log
   origin/develop`), confirming PR #11 was already merged before doing the
   merge.

2. **Resolving merge conflicts.**
   The merge produced two conflicts: `.gitignore` (both branches added an
   IntelliJ comment) and `pom.xml` (Gson `2.10.1` vs `2.14.0`). The AI
   suggested resolving them by keeping a single `.gitignore` comment and
   unifying on the `develop` version `2.14.0`, so person and product modules
   share the same Gson version.

3. **Unifying the repository style.**
   The leader recommended using Manuel's `ProductRepository` as the style
   reference for `PersonRepository`. I asked the AI to refactor mine to the
   same pattern (string file constants, static `TypeToken` constants,
   private `writeList`/`readList` helpers, graceful fallback to an empty
   list) while keeping the method names defined in the class diagram
   (`saveAllCustomers`, `loadAllCustomers`, `saveAllSellers`,
   `loadAllSellers`).

4. **Regression check.**
   Since Gson was bumped to `2.14.0`, the AI compiled the module with the
   new jar and reran the previous smoke test, confirming the JSON files and
   the service operations still work.