# AI Usage Log — Technical Lead

**Name:** Oscar Palomino
**Role:** Leader (System-wide Architecture & Module Integration)
**Branch:** `feature/sale-module` (primary branch for coordination)

This log records AI-assisted decisions taken by the Technical Lead role during the development of GameZone Unicesar reference implementation.

### Entry 1

**Date:** 2026-09-02
**Tool used:** Claude Code

**Reason for use:**
Set up the Maven project descriptor and the four-layer package skeleton so subsequent module work has a compilable base to build on.

**Problem faced:**
The repository still contained a NetBeans-generated pom.xml and a `game.gamezoneunicesar` package left over from project creation, which used the wrong groupId, wrong Java version property style, and the wrong root package for the `com.gamezone` architecture defined in CLAUDE.md.

**Prompt used:**
ejecuta la fase 4

**Solution obtained and decision taken:**
Replaced pom.xml with a descriptor using groupId `com.gamezone`, artifactId `gamezone-unicesar`, version `1.0.0-SNAPSHOT`, Java 17 source/target properties, and the exec-maven-plugin (3.1.0) configured with `com.gamezone.Main` as the main class. Deleted the obsolete `game.gamezoneunicesar` package and created the four target packages (`model`, `persistence`, `service`, `ui`) under `com.gamezone` with `.gitkeep` placeholders, a `data/` folder for file persistence, and a `Main` stub that prints a pending-initialization message. Verified the setup with `mvn clean compile`, which returned `BUILD SUCCESS`.

### Entry 2

**Date:** 2026-09-08
**Tool used:** Claude Code (Claude Sonnet, environment-integrated coding assistant)

**Topic:** Initial Git/GitHub configuration (Part 4 — Version Control with Git and GitHub).

**Questions raised & how AI helped:**

1. **"How do I start the repo, branches, and protection?"**
   I asked for an explanation of the Git commands to create the repository, configure `main` and `develop`, and enable branch protection rules with mandatory approval. The AI explained the Git Flow simplified model required by the workshop (protected `main` and `develop`, feature branches derived from `develop`, merge through Pull Requests) and the exact commands to set it up.

2. **"Maven structure for the four layers?"**
   I asked for help assembling the initial `pom.xml` and the `model`/`persistence`/`service`/`ui` package folders. The workshop text describes this structure explicitly, so the AI only helped translate that textual specification into the Maven project layout and the correct `com.gamezone` root package.

### Entry 3

**Date:** 2026-09-08
**Tool used:** Claude Code

**Topic:** Attempts to have the AI generate the system design — **rejected by the AI**.

**What happened:**

1. I shared an `analysis.md` and the three diagrams already made ("as guidance") asking the AI to include them modified. The AI refused, explaining this is exactly the forbidden use described in the workshop policy: asking the AI to generate the complete system design (diagrams, hierarchy, or layer structure).
2. I insisted a second time with the same diagrams in a different format. It was rejected again for the same reason.
3. After those rejections, the AI only explained OOP concepts (aggregation vs. composition vs. association, when to use `abstract`) and my team and I answered the eleven `analysis.md` questions ourselves; the AI only reviewed the logical consistency of our answers afterward.

**Decision taken:**
The design (hierarchy-diagram, class-diagram, layers-diagram and the eleven analysis questions) was produced entirely by the team; the AI contributed no design element.

### Entry 4

**Date:** 2026-09-08
**Tool used:** Claude Code

**Topic:** Technical implementation decisions (allowed use: Java/Maven guidance and architecture consultations).

**Questions raised & how AI helped:**

1. **JSON vs. plain text vs. CSV for persistence.**
   I asked for a technical recommendation. The AI initially suggested plain text (fewer dependencies), but after weighing maintainability and structure we decided on **JSON with Gson**.

2. **List of IDs or full objects in `registerSale`?**
   I asked for an architectural recommendation on the `SaleService.registerSale` signature. The AI explained the tradeoff (decoupling vs. convenience) and recommended passing **IDs** and resolving them inside the service, which the team adopted.

3. **Does the repository filter or the service filter?**
   For the sales history by customer/seller, I asked for a comparison between filtering in the repository or in the service. The decision was to keep the **repository simple** (`saveAll`/`loadAll`) and let the **service filter** (`viewSalesByCustomer`, `viewSalesBySeller`).

4. **Polymorphism of Product/Person in JSON.**
   The AI warned that a single file mixing polymorphic types complicates Gson deserialization and recommended storing each concrete type in its own file. The team decided to separate `videogames.json` and `consoles.json` (and customers/sellers separately), which keeps the JSON clean and type-safe.

### Entry 5

**Date:** 2026-09-08
**Tool used:** Claude Code

**Topic:** Code review of the team's code (allowed use: review of my/our own code).

**What was reviewed and found:**

1. **Review of Veronica's person module (Developer 2).**
   The AI review found it deviated from the agreed class diagram: `name`/`identification` instead of `id`/`firstName`/`lastName`, an un-agreed `getRole()` method, and a file format different from the one the team had decided. The code was corrected to match the diagram.

2. **Review of Manuel's product module (Developer 1).**
   The AI review found a real inconsistency: `ProductService` did not throw exceptions on validation errors, unlike the other two services, which would have broken the error handling in the UI when registering duplicate IDs or negative prices. Manuel corrected it (`refactor: make ProductService throw IllegalArgumentException on validation errors`).

3. **Review during ConsoleMenu construction.**
   The AI review found that `registerVideoGame` and `registerConsole` did not catch the `IllegalArgumentException` now thrown by `ProductService` — a real bug that would have crashed the application when a product with a repeated ID or negative price was registered. It was fixed before testing.

### Entry 6

**Date:** 2026-09-08
**Tool used:** Claude Code

**Topic:** Attempts to have the AI write code or documents — **rejected by the AI**.

**What happened:**

1. I asked the AI to implement my sales module (`Sale`, `SaleService`, `ConsoleMenu`) directly. The AI rejected it, explaining that these are my assigned classes and I must be able to explain them in the oral defense. Instead, it guided me step by step while I wrote the code.
2. I asked the AI to write the final `README.md` and my own AI usage log. It rejected both, because they are deliverables the workshop requires to be drafted with personal intervention — and the AI log in particular loses its meaning if written by the same AI it is supposed to audit.

**Decision taken:**
The sales module code and all project documentation were produced by my own hands, with the AI limited to guidance, explanations and review.

### Entry 7

**Date:** 2026-09-08
**Tool used:** Claude Code

**Topic:** Technical validation of the integrated system.

**What the AI did:**
The AI compiled the full project with `javac` (using the Gson 2.14.0 jar) and executed the application with test data (registering a product, a customer and a sale) to confirm that the three layers — model, persistence and service — integrate correctly. It ran without errors, confirming the round-trip persistence of sales and the stock discount logic.