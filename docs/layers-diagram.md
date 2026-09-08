# Layer Dependency Diagram — GameZone Unicesar

```mermaid
flowchart TD
    UI[ui] --> SERVICE[service]
    SERVICE --> PERSISTENCE[persistence]
    SERVICE --> MODEL[model]
    PERSISTENCE --> MODEL
```
