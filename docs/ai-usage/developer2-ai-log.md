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