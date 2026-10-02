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

## Entrada R5-05 — 24-09-2026

| Campo | Detalle |
|---|---|
| Fecha | 24-09-2026 |
| Herramienta | opencode (CLI sobre Windows PowerShell) |
| Fase y rama | Fase 4, rama `fix/return-accessory-stock` (ajuste A4) |
| Objetivo | Averiguar por qué el enunciado pide un ajuste de "devolución de accesorios", porque el Requerimiento 3 ya restauraba stock y parecía que la devolución de accesorios funcionaba |
| Consulta | "El enunciado del ajuste A4 dice que al devolver un accesorio su stock no se restaura. Revisa `ReturnService.registerReturn`, `AccessoryService` y `ReturnRepository` y dime qué falla realmente, si falla, y cómo lo corrigió el módulo de ventas con el mismo problema" |
| Respuesta | Falla por dos motivos, y el primero es más grave de lo que dice el enunciado. `ReturnService.registerReturn` resuelve el ítem con `productService.findById`, y `ProductService` solo maneja videojuegos y consolas, así que devolver un accesorio lanzaba `Product not found in system: A-1`: la devolución de accesorios era directamente imposible, no solo mal contabilizada. Segundo, `AccessoryService` solo tenía `updateStock`, que reemplaza el stock por un valor absoluto, de modo que aunque se encontrara el accesorio, reponer stock con ese método habría sobrescrito el inventario en vez de aumentarlo. Como referencia, `SaleService` ya resolvía bien el caso mixto: tiene un `resolveItem` que consulta primero `ProductService` y luego `AccessoryService`, y un `discountStock` que delega según el tipo del ítem. Además, el `ProductDeserializer` de `ReturnRepository` solo distinguía `VideoGame` de `Console`, así que un accesorio guardado se reconstruía como `Console` al recargar y perdía su tipo; el de `SaleRepository` ya cubría las cinco clases. Mi hipótesis inicial, guiada por el nombre del ajuste, era que solo faltaba un método para reponer stock |
| Decisión | Copié el patrón que ya existía en `SaleService` en lugar de inventar uno nuevo, para que el módulo de devoluciones se lea igual que el de ventas: `resolveItem` y `restoreStock` delegando por tipo. Agregué `AccessoryService.restoreStock` con la misma firma y semántica de `ProductService.restoreStock`, porque el nombre del ajuste me hizo sospechar primero que el problema era el método equivocado y resultó ser además un problema de resolución. No limpié el `data/return.json` ni el `data/accessories.json` porque el enunciado no lo pide y sus datos obsoletos no afectan el ajuste. Verifiqué con una copia de `data/` en un directorio temporal: con el build anterior la devolución falla con `Product not found in system: A-1` y el stock del accesorio queda en 4; con el build corregido la devolución se registra y el stock vuelve de 4 a 5 sin tocar el stock de la consola. Y con un arnés que carga `data/return.json`, el accesorio se reconstruye como `Console` antes del commit y como `Controller` con su `connectionType` después |
| Commit relacionado | `f79934a`, `1083b58`, `6c5067e` |

## Entrada R5-06 — 02-10-2026

| Campo | Detalle |
|---|---|
| Fecha | 02-10-2026 |
| Herramienta | opencode (CLI sobre Windows PowerShell) |
| Fase y rama | Fase 4, rama `fix/return-accessory-stock`, antes de publicar el PR (ajuste A4) |
| Objetivo | Revisar los cinco commits del A4 contra `develop` y comprobar que el build compila y que el ajuste hace lo que dice el enunciado, para no abrir un PR roto |
| Consulta | "Revisa el diff completo de los 5 commits de A4 contra `develop`: que `AccessoryService.restoreStock` incremente y persista, que `ReturnService` reciba `AccessoryService` por constructor y delegue por `instanceof Accessory`, que `ReturnRepository` resuelva accesorios al cargar y que el cableado de `Main` esté bien; y compila con `javac` con una copia de `data/` en un directorio temporal" |
| Respuesta | El diff cubre los cuatro puntos: `restoreStock` suma al stock y llama a `save()`, `ReturnService` recibe el servicio por constructor y delega con `instanceof Accessory` hacia `AccessoryService` o `ProductService`, el `ProductDeserializer` de `ReturnRepository` ya distingue `Controller`, `Cable` y `Memory` antes de `VideoGame` y `Console`, y `Main` pasa el `accessoryService` que ya construía. Compiló sin warnings con `javac` y el jar de Gson 2.14.0 de `.m2`. Con un arnés propio sobre una copia de `data/` en el temporal, 24 comprobaciones: el stock de `A-1` baja de 5 a 4 al vender, vuelve de 4 a 5 al devolver, la consola queda en 11 sin tocarse, los tres tipos de accesorio se recargan con su tipo y sus campos (`connectionType`, `lengthInMeters`, `capacityInGb`), las devoluciones de videojuegos y consolas siguen funcionando, un identificador desconocido sigue fallando con `Product not found in system`, y `updateStock(99)` sigue reemplazando mientras `restoreStock(2)` suma. Dos cosas que reporto y no toqué: `Main.java` es un archivo del Líder Técnico y el enunciado exige mencionarlo en el PR, y los cinco commits no están firmados |
| Decisión | Subí los commits tal como estaban, sin `--force` y sin reescribir historia, porque la regla del curso es subir cada commit justo después de crearlo, compactarlos reduce el conteo de commits del Dev 2 y ninguno de los cinco mensajes se sale de la convención. No firmé porque en esta máquina no hay GPG instalado ni llave en `.ssh`, y generar una llave y registrarla en GitHub es una decisión mía que requiere el navegador; mientras tanto la atribución por integrante depende de que `vgisethpadilla@unicesar.edu.co` esté verificado en la cuenta, y eso lo compruebo yo en GitHub. También corregí un error mío: la primera ejecución del arnés corrió desde la raíz del repo y los repositorios escribieron sobre el `data/` real; lo restauré con `git checkout -- data` y volví a ejecutarlo con el directorio de trabajo en el temporal, que es justo lo que exige la Entrada R5-02 |
| Commit relacionado | `f79934a`, `1083b58`, `6c5067e`, `0d04d0f`, `9203793` (los cinco commits revisados) |

## Entrada R5-07 — 02-10-2026

| Campo | Detalle |
|---|---|
| Fecha | 02-10-2026 |
| Herramienta | Claude Code (CLI en sesión remota) |
| Fase y rama | Fases 3 y 4, ramas `fix/warranty-circular-dependency` (A2) y `fix/return-accessory-stock` (A4) |
| Objetivo | Revisar el estado de mis ajustes contra los Requerimientos 5 y 6 y llevar A2 y A4 al `develop` del repositorio del equipo |
| Consulta | "Revisa todo, soy Dev 2" con los dos enunciados adjuntos; luego "empieza a hacer, los commits, PR y push los hago yo" y "todo con JSON" |
| Respuesta | Encontró que los PR de A2 y de la documentación de devoluciones se fusionaron en mi fork y no en `Oscar-Palomino-07/GameZoneUnicesar`, que la rama de A4 llevaba A2 adentro y que el enunciado del Req. 6 trae el mismo texto del Req. 5. Propuso rehacer A2 con `cherry-pick` sobre el `develop` actual, resolver el conflicto de `Main` (conservar `PromotionService` y el nuevo orden de construcción) y luego A4. Encontró además un defecto en `ReturnService.registerReturn`: restauraba el stock dentro del ciclo de validación, así que una devolución rechazada por el segundo ítem dejaba restaurado el stock del primero. También señaló mensajes al usuario en inglés, el identificador `ventaOriginal` en español y la falta de JavaDoc en `ReturnRepository` |
| Decisión | Acepté rehacer A2 y A4 con `cherry-pick` para conservar mis commits originales y abrir un PR por ajuste. Mantuve JSON para las garantías, como en todo el proyecto, y corregí el `README.md` que todavía nombraba `warranties.csv`. Acepté mover la restauración de stock después de todas las validaciones, traducir los mensajes y renombrar `ventaOriginal` a `originalSale`. Lo verifiqué con un programa de prueba sobre una copia de `data/`: una devolución con un producto inexistente es rechazada y el stock de la consola sigue igual |
| Commit relacionado | `fix: validate every returned item before restoring stock`; `fix: show return validation messages in Spanish`; `docs: add JavaDoc to ReturnRepository` |

## Entrada R5-08 — 02-10-2026

| Campo | Detalle |
|---|---|
| Fecha | 02-10-2026 |
| Herramienta | Claude Code (CLI en sesión remota) |
| Fase y rama | Fase 4, rama `fix/monthly-balance-report` (ajuste A6) |
| Objetivo | Separar el reporte mensual en total de ventas, total de devoluciones y balance neto, y usar el total final de cada venta |
| Consulta | "Implementa A6: `calculateMonthlySales` y `calculateMonthlyReturns` en `ReturnService`, `generateMonthlyBalance` con la misma firma y el menú con los tres valores" |
| Respuesta | Propuso dos métodos públicos que comparten una validación del período (`validatePeriod`) y un filtro por mes y año (`isInPeriod`), y que `generateMonthlyBalance` reste uno del otro. Señaló que la excepción por balance negativo era un error: una venta del 30 de un mes devuelta el 2 del siguiente deja el segundo mes en negativo de forma legítima |
| Decisión | Acepté la separación y eliminé la `IllegalStateException` por balance negativo, porque el enunciado dice que el método retorna la diferencia. El total de ventas usa `Sale.calculateTotal()`, que ya resta el descuento y suma las garantías extendidas desde A3. El menú muestra los tres valores en español. Actualicé `return-class-diagram.md` y la pregunta Q5 de `return-analysis.md`. Lo verifiqué con el programa de prueba: ventas 2325.00, devoluciones y balance coherentes, y el mes 13 es rechazado con mensaje en español |
| Commit relacionado | `fix: add calculateMonthlySales to ReturnService`; `fix: add calculateMonthlyReturns to ReturnService`; `fix: return the sales minus returns difference in generateMonthlyBalance`; `fix: show sales, returns and net balance in the monthly balance menu` |
