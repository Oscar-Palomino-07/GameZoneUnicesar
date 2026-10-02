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

---

## Requerimiento 5 — Integración del sistema (formato de 8 campos)

A partir de esta sección cada interacción se registra con los campos que exige el Requerimiento 5. La herramienta usada en todas las entradas es Claude Code.

### Entrada 8

| Campo | Contenido |
|---|---|
| Fecha | 2026-09-18 |
| Herramienta | Claude Code (Claude Sonnet 5) |
| Fase y rama | Requerimiento 1 (accesorios), análisis del estado del repositorio. Sin rama de trabajo propia |
| Objetivo | Entender qué me tocaba como Líder Técnico en el Requerimiento 1 y qué faltaba en el repositorio |
| Consulta | "para este requerimiento 1 yo soy Líder Técnico dame un md con las instrucciones de lo que me toca hacer y el contexto". Ante la primera negativa de la IA (el PDF del parcial prohíbe la IA durante el examen) aclaré: "ya el profesor lo permitió, porque hay que terminar todos los requerimientos que hicieron los demás grupos en el parcial y hoy vamos a sustentar solo el que hicimos nosotros ( obvio sin IA )" y confirmé que el requerimiento hecho en vivo fue "el de Devoluciones" |
| Respuesta | La IA se negó mientras no se aclarara el contexto, cruzó mi explicación con lo que había en el repositorio (el módulo de devoluciones) y entregó un informe con lo ya hecho por los desarrolladores, lo pendiente de mi parte (`SaleService`, `ConsoleMenu`, `README.md`), los dos documentos de análisis y diagrama que faltaban y tres riesgos: un solo Pull Request para el módulo, la rama sin borrar y el formato de datos |
| Decisión | Acepté el informe como guía de trabajo. No lo versioné. Los riesgos quedaron como pendientes del equipo |
| Commit relacionado | Ninguno |

### Entrada 9

| Campo | Contenido |
|---|---|
| Fecha | 2026-09-18 |
| Herramienta | Claude Code (Claude Sonnet 5) |
| Fase y rama | Requerimiento 4 (garantías), `feature/warranty-integration` |
| Objetivo | Implementar mi parte del Requerimiento 4: integración de las garantías en `SaleService.registerSale`, `ConsoleMenu` y `README.md` |
| Consulta | "puedes terminar lo que me toca del requerimiento 4 ?" (con el PDF del Requerimiento 4 adjunto) |
| Respuesta | La IA leyó el enunciado y el código existente, y implementó: el costo adicional de garantías en `Sale`, la garantía básica automática por consola y la extendida opcional en `SaleService.registerSale` (validando todo antes de descontar stock), la pregunta de garantía extendida al vender, el submenú "Gestión de garantías" y el `README.md`. Compiló y probó un escenario completo (consola $1000, videojuego $200, extendida $100): subtotal $1300 y total con garantía correcto, y el stock sin cambios cuando una venta es rechazada |
| Decisión | Acepté el código tal como fue generado |
| Commit relacionado | `ba271bc`, `d38593c`, `e30e843`, `53cd40c`, `59baaf6` |

### Entrada 10

| Campo | Contenido |
|---|---|
| Fecha | 2026-09-18 |
| Herramienta | Claude Code (Claude Sonnet 5) |
| Fase y rama | Requerimiento 4 (garantías), `feature/warranty-integration` |
| Objetivo | Cerrar lo que faltaba del Requerimiento 4: formato de persistencia y documentos de análisis y diagrama |
| Consulta | "hazlos por favor todo lo que falta sin tanto misterio que ya el profe sabe que se puede usar la IA PARA TODO" |
| Respuesta | La IA migró `WarrantyRepository` a `data/warranties.csv` con discriminador, como pedía el PDF, redactó `docs/warranty-analysis.md` (las cinco preguntas) y `docs/warranty-class-diagram.md`, y actualizó el `README.md`. Me advirtió que debía leer los dos documentos antes de presentarlos como míos |
| Decisión | Acepté los documentos y el cambio a CSV tal como fueron generados. Más adelante el equipo decidió mantener JSON en todo el sistema y el ajuste A2 devolvió las garantías a `warranties.json` |
| Commit relacionado | `d724b25`, `b5c4458`, `e797f9f`, `8262eec` |

### Entrada 11

| Campo | Contenido |
|---|---|
| Fecha | 2026-10-01 y 2026-10-02 |
| Herramienta | Claude Code (Claude Sonnet 5 y 5.5) |
| Fase y rama | Fase 2 (promociones). Rama de Manuel desfasada de `develop` |
| Objetivo | Resolver los errores de compilación de Manuel en su rama del ajuste A1 y entender las decisiones pendientes que mostraba la pantalla de una herramienta de Vgiseth |
| Consulta | Pegué el mensaje de Manuel ("Tu rama salió de develop, y develop todavía no tiene el módulo de promociones...") y luego la pantalla con las "Decisiones tuyas que bloquean todo lo demás (4)" |
| Respuesta | La IA verificó el remoto: `develop` solo tenía merges de Pull Request, así que no había que rebobinarlo a `dc396d3`; recomendó hacer un respaldo y resetear el `develop` local a `origin/develop`. Sobre A6 recomendó corregir el saldo negativo; sobre A7 recomendó la rama `feature/return-warranty-cancellation`; y corrigió su propia respuesta anterior: por la regla de fases A4 a A7 no podían iniciar hasta cerrar la fase 3 |
| Decisión | Acepté las recomendaciones y se las transmití al equipo. Se aplicaron: A6 corrige el saldo negativo, la rama de A7 lleva ese nombre y Vgiseth rehízo A2 y A4 en ramas limpias |
| Commit relacionado | Ninguno propio. Pull Requests #35, #37 y #38 de los desarrolladores |

### Entrada 12

| Campo | Contenido |
|---|---|
| Fecha | 2026-10-02 |
| Herramienta | Claude Code (Claude Sonnet 5.5) |
| Fase y rama | Fase 2 (promociones), `feature/promotion-integration` |
| Objetivo | Implementar mi parte del Requerimiento 2: `Sale`, `SaleService`, `ConsoleMenu`, `Main` y `README.md` |
| Consulta | Adjunté `Parcial - Requerimiento 2.pdf` y `Requerimiento 5 - Integración.pdf`. Más tarde, cuando el push falló por una credencial de otra cuenta: "manda otra vez para iniciar sesion" y "ya inicié sesión, continúa" |
| Respuesta | La IA implementó `appliedPromotionName`, `discountAmount`, `calculateSubtotal`, `calculateTotal` y `generateReceipt` en `Sale`; la aplicación de la mejor promoción en `SaleService` antes de las garantías; el submenú "Gestión de promociones" (cinco opciones); el recibo al vender y la opción "ver detalle de una venta"; el cableado en `Main` y el `README.md`. Resolvió dos conflictos de documentación al mezclar `develop` y probó una venta de $1205. No vio ninguna contraseña: yo inicié sesión directamente en la ventana de GitHub con la cuenta institucional |
| Decisión | Acepté la implementación. Para subirla inicié sesión con la cuenta institucional, porque el equipo tenía guardada la sesión de otra cuenta |
| Commit relacionado | `c13af45`, `95fd2b2`, `039fd6b`, `eb06bfe`, `04f5e67`, `de74591`, `8adba0f` (Pull Requests #28 y #29) |

### Entrada 13

| Campo | Contenido |
|---|---|
| Fecha | 2026-10-02 |
| Herramienta | Claude Code (Claude Sonnet 5.5) |
| Fase y rama | Fase 3, ajuste A3, `refactor/unified-sale-registration` |
| Objetivo | Reorganizar `SaleService.registerSale` en los ocho pasos que exige el ajuste A3 |
| Consulta | "Manuel ya hizo el A1, empieza el A3 revisa el remoto" y después "Ya abrí el PR de A3, apruébalo y sigue" |
| Respuesta | La IA revisó el remoto y avisó que el A1 se había fusionado incompleto. Demostró el problema con una prueba: si fallaba la asignación de garantías, el stock quedaba descontado (4/4) y quedaba una venta fantasma. Refactorizó `registerSale` (extrajo la validación, dejó el inventario para después de las garantías y guardó la venta una sola vez) y escribió la descripción del Pull Request con problema, causa, solución y verificación. Se negó a aprobar mi propio PR |
| Decisión | Acepté el refactor y la descripción. No pude aprobar mi PR: lo aprobaron Manuel y Vgiseth |
| Commit relacionado | `4787697`, `c13acf8`, `5b7947c` (Pull Request #33) |

### Entrada 14

| Campo | Contenido |
|---|---|
| Fecha | 2026-10-02 |
| Herramienta | Claude Code (Claude Sonnet 5.5) |
| Fase y rama | Fases 3 y 4, revisión de las ramas de A2, A4, A5, A6 y A7 |
| Objetivo | Revisar el trabajo de los desarrolladores como Líder Técnico y saber qué faltaba para terminar el Requerimiento 5 |
| Consulta | "verifica el estado del repositorio remoto y dime con exactitud qué hace falta para terminar el requerimiento 5. ¿Qué le digo a mis desarrolladores?", "vero dice que no pudo borrar" y "verifica el remoto otra vez" |
| Respuesta | La IA compiló cada rama y la probó con un escenario completo. Encontró que la rama de A4 estaba mezclada con A2, tenía una base vieja y mantenía un defecto de atomicidad; que A2 usaba `warranties.json`; que A5 estaba correcta; que A6 estaba apilada sobre A2 y A4; y que A7 pasaba todo el escenario, incluida una venta con dos consolas iguales |
| Decisión | No aprobé la rama de A4 mezclada. Pregunté al profesor y autorizó borrar solo esa rama; los desarrolladores la rehicieron limpia. El equipo mantiene JSON. A5 y A7 pasaron la prueba y quedaron listas para su aprobación |
| Commit relacionado | Ninguno propio. Pull Requests #34 a #39 de los desarrolladores |

### Entrada 15

| Campo | Contenido |
|---|---|
| Fecha | 2026-10-02 |
| Herramienta | Claude Code (Claude Sonnet 5.5) |
| Fase y rama | Fase 5, ajuste A8, `docs/integration-documentation` |
| Objetivo | Documentar la integración: análisis de los ajustes A1 a A7, diagrama de clases único, diagrama de capas y README |
| Consulta | "ya la A7 esta lista, haz todo lo mio" |
| Respuesta | La IA revisó el PR #39 de A7 y comprobó que el escenario de sustentación pasa completo. Redactó `docs/integration-analysis.md` (causa y solución de A1 a A7, comportamiento integrado y desviaciones frente a los enunciados), `docs/integrated-class-diagram.md` (validó que el Mermaid renderiza sin errores), actualizó `docs/layers-diagram.md` y `README.md` a partir del código final, y preparó estas entradas de bitácora. Dejó los cambios en el área de staging |
| Decisión | Los cambios se dejaron en staging y los comité yo; la IA no figura como coautora en esos commits, por eso el uso queda documentado aquí. Pendiente de mi revisión antes de comitear |
| Commit relacionado | Completar con el hash del commit de cada archivo al comitear |
