# Accessory Analysis — GameZone Unicesar

> Orientadora: este archivo se completa entre los **tres integrantes del equipo**, igual que se hizo con `analysis.md` del Taller 1. Las respuestas deben apoyarse en el código ya implementado (modelo, repositorio y servicio de accesorios, PR #22) y en la integración del líder técnico (rama `feature/accessories-integration`).

## Accesorios

### Q1: ¿Qué atributos comparten todos los accesorios y cuáles son específicos de cada tipo? ¿Cómo se refleja esta distinción en la jerarquía de clases?

(Respuesta del equipo: …)

### Q2: ¿Por qué la clase `Accessory` debe ser abstracta y no instanciable directamente? ¿Qué implicación tiene esta decisión sobre el uso de `Product` en el resto del sistema?

(Respuesta del equipo: …)

### Q3: ¿Cómo se modela la compatibilidad de un accesorio con las consolas? ¿Qué tipo de relación es y por qué se almacenan solo los identificadores de consola en lugar de objetos `Console`?

(Respuesta del equipo: …)

## Integración con el flujo de ventas

### Q4: ¿Cómo se integró el flujo de ventas con los accesorios de forma aditiva, sin romper el comportamiento existente con videojuegos y consolas? ¿Qué capas y clases participan en la validación de stock y en el descuento de inventario?

(Respuesta del equipo: …)

### Q5: ¿Qué decisiones de persistencia se tomaron para que las ventas que incluyen accesorios se guarden y recarguen correctamente? ¿Por qué es necesario un deserializador polimórfico que reconstruya `Controller`, `Cable` o `Memory`?

(Respuesta del equipo: …)