# Developer 2 — AI Usage Log

**Name:** Veronica Padilla
**Role:** Developer 2 (Repositories and services; integration adjustments A2, A4, A6 and A7)
**Branch:** `feature/person-module` (Taller 1) and `feature/accessory-module`, `feature/promotion-module`, `feature/warranty-module` (Taller 2)

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

---

# Requerimiento 5 — Integración del sistema (fases 1 a 5)

Bitácora de la semana del 21 al 26 de septiembre de 2026. Cada entrada sigue los ocho
campos exigidos por el enunciado: **Fecha**, **Herramienta**, **Fase y rama**, **Objetivo**,
**Consulta**, **Respuesta**, **Decisión** y **Commit relacionado**. Las entradas anteriores
del Taller 1 se conservan con su formato original.

## Entrada R5-01 — 21-09-2026

| Campo | Detalle |
|---|---|
| Fecha | 21-09-2026 |
| Herramienta | opencode (CLI sobre Windows PowerShell) |
| Fase y rama | Fases 1 y 2 (preparación de la base), sobre `develop` |
| Objetivo | Saber en qué estado estaba el repositorio antes de empezar los ajustes, porque tenía que integrar los módulos de promociones y garantías que seguían sin fusionar, y decidir qué huecos del enunciado faltaban |
| Consulta | "Analiza el estado del repositorio GameZoneUnicesar: qué ramas existen, cuáles están fusionadas en `develop`, qué commits hay sin fusionar, qué archivos de código y de documentación exige el enunciado que no existen, y si el proyecto compila" |
| Respuesta | `develop` tenía fusionados solo los módulos de accesorios y devoluciones; `feature/promotion-module` (16 commits) y `feature/warranty-module` (22 commits) estaban en el remoto sin fusionar. Faltaban `docs/return-analysis.md` y `docs/return-class-diagram.md`. La integración de promociones con la venta no existía en ninguna rama. Además `ConsoleMenu.java` tenía dos líneas pegadas por error (`import git config user.name`) que rompían la compilación, y las ramas de promociones y garantías anexaban al final del mismo `docs/hierarchy-diagram.md`, por lo que fusionar garantías después de promociones genera conflicto en ese archivo y en la bitácora de Dev 1 |
| Decisión | Descarté las dos líneas pegadas con `git checkout --` en lugar de editarlas a mano, porque no eran código del taller. Fusioné primero promociones y después garantías, y resolví el conflicto de los dos documentos guardando los dos bloques (el de promociones y el de garantías), renumerando como 07 y 08 las dos entradas que Dev 1 tituló ambas "Entry 07". Añadí los dos documentos que faltaban del Requerimiento 3 en una rama `docs/return-module-docs` aparte, porque `feature/return-module` ya estaba fusionada y el enunciado exige borrar la rama del remoto después del merge |
| Commit relacionado | `1826b56` (merge de promociones), `cdadda6` (merge de garantías), `451f2a9` (merge de la documentación de devoluciones) |

## Entrada R5-02 — 21-09-2026

| Campo | Detalle |
|---|---|
| Fecha | 21-09-2026 |
| Herramienta | opencode (CLI sobre Windows PowerShell) |
| Fase y rama | Fases 2 y 3 (preparación de la base), sobre `develop` |
| Objetivo | Averiguar cómo verificar el código en esta máquina, porque el enunciado pide compilar antes de cada commit y aquí no hay Maven instalado |
| Consulta | "En esta máquina hay JDK 21 pero `mvn` no está en el PATH. ¿Cómo compilo y ejecuto un proyecto Maven con Java 17 sin instalar Maven, usando las dependencias que ya están en `~/.m2`?" |
| Respuesta | Se puede invocar `javac` directamente pasando como classpath el jar de Gson que ya estaba descargado en `~/.m2/repository/com/google/code/gson/gson/2.14.0/gson-2.14.0.jar`, y ejecutar con `java -cp "carpeta_de_clases;jar" com.gamezone.Main`. Para no ensuciar los datos reales del repositorio, la aplicación se debe ejecutar desde una copia de la carpeta `data/` en un directorio temporal, porque los repositorios leen y escriben con rutas relativas al directorio de trabajo |
| Decisión | Adopté `javac` más `java` con el jar de `.m2` como método de verificación de toda la semana, y siempre con una copia de `data/` en el directorio temporal. Descarté instalar Maven porque modificaría el sistema y el enunciado no lo pide. La verificación real con `mvn clean package` queda como paso mío antes de abrir cada PR en GitHub |
| Commit relacionado | `f2e75de` (primer commit de A2, verificado con `javac`) |

## Entrada R5-03 — 23-09-2026

| Campo | Detalle |
|---|---|
| Fecha | 23-09-2026 |
| Herramienta | opencode (CLI sobre Windows PowerShell) |
| Fase y rama | Fase 3, rama `fix/warranty-circular-dependency` (ajuste A2) |
| Objetivo | Entender el problema de dependencia circular del enunciado antes de tocar código, y comprobar si el repositorio realmente tenía el ciclo `SaleService → WarrantyService → WarrantyRepository → SaleService` |
| Consulta | "En el enunciado del Requerimiento 5 el ajuste A2 dice que existe un ciclo `SaleService → WarrantyService → WarrantyRepository → SaleService`. Revisa el código real de `WarrantyRepository`, `WarrantyService` y `Main`, y dime si ese ciclo existe tal cual o si el acoplamiento es otro, y cuál es la forma mínima de romperlo respetando `ui → service → persistence → model`" |
| Respuesta | El ciclo literal no existe: `WarrantyRepository` recibía un `SaleRepository`, no un `SaleService`, así que el grafo sí se podía construir con inyección por constructor. El problema real es de capa: la resolución de referencias (buscar la `Sale` y el `Product` de cada garantía guardada) estaba dentro del repositorio, es decir en la capa de persistencia, que es la capa que no debe conocer el flujo de ventas. La solución que propone el enunciado —el repositorio guarda y carga solo identificadores y el servicio resuelve— es exactamente la corrección de ese problema, y además deja el repositorio sin dependencias, lo que garantiza que ningún repositorio pueda depender de un servicio |
| Decisión | Implementé el A2 tal como lo pide el enunciado: `WarrantyRepository` sin constructor con parámetros, que lee y escribe `WarrantyRecord` (tipo, identificador, id de producto, id de venta y fecha de inicio); `WarrantyService(WarrantyRepository, SaleRepository, ProductService)` que resuelve con `SaleRepository.loadAll()` y `ProductService.findById()`; y el orden de construcción en `Main`. Descarté resolver el producto buscándolo dentro de la venta, como hacía el código anterior, porque `ProductService` cubre los tres inventarios (videojuegos, consolas y accesorios) y devuelve el objeto vivo del catálogo en vez de la copia congelada dentro de la venta. En la descripción del PR describí el acoplamiento que realmente encontré y no un ciclo inventado, porque en la sustentación tengo que poder defenderlo |
| Commit relacionado | `f2e75de`, `7715e82`, `447e4b6` |

## Entrada R5-04 — 23-09-2026

| Campo | Detalle |
|---|---|
| Fecha | 23-09-2026 |
| Herramienta | opencode (CLI sobre Windows PowerShell) |
| Fase y rama | Fase 3, rama `fix/warranty-circular-dependency` (ajuste A2) |
| Objetivo | Comprobar que el cambio no rompió la carga de garantías y decidir qué hacer con una garantía cuya venta o cuyo producto ya no existen |
| Consulta | "Después de mover la resolución al servicio, ¿cómo verifico que las garantías se siguen reconstruyendo al reiniciar? ¿Qué debería pasar si una garantía guardada apunta a una venta que ya no está en `data/sales.json`?" |
| Respuesta | La verificación se hace en dos ejecuciones: la primera registra una venta con consola y pide la garantía extendida, y la segunda arranca la aplicación de nuevo y lista las garantías. Si la reconstrucción falla, el servicio no encuentra la venta o el producto y se ve en la salida de error. Para el caso roto, las opciones son lanzar la excepción, que rompe el arranque, o saltar la garantía y avisar, que es lo que ya hacía el código anterior |
| Decisión | Mantuve el comportamiento de saltar y avisar, pero con un mensaje que incluye el identificador de venta y el de producto para poder diagnosticarlo, porque una garantía sin venta ni producto no tiene sentido de negocio y una sola referencia rota no debe impedir abrir el programa. Verifiqué las dos situaciones: tras registrar una venta con consola y garantía extendida, `data/warranties.json` quedó con solo identificadores y al reiniciar se listaron las dos garantías (W-1 básica y W-2 extendida, ambas vigentes); y con una venta inexistente el servicio saltó las dos garantías con un mensaje claro y siguió funcionando |
| Commit relacionado | `f2e75de`, `7715e82` |