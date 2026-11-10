# Fitness Tracker & Workout Session Companion

## Resumen ejecutivo
**Fitness Tracker & Workout Session Companion** es un proyecto académico de desarrollo multi-dispositivo para Android que demuestra comunicación confiable entre un smartphone (**módulo `mobile`**) y un smartwatch (**módulo `wear`**, Wear OS) usando **Google Play Services Wearable Data Layer**.

La solución implementa un flujo de entrenamiento donde:
- El **reloj (Wear OS)** actúa como control remoto de sesión (iniciar, pausar, detener).
- El **teléfono (Mobile)** mantiene el motor de estado/cronómetro y publica métricas.
- Ambos dispositivos mantienen una vista sincronizada del estado del entrenamiento mediante mensajes y datos reactivos.

## Integrantes
| Integrante | GitHub | Rol principal |
|---|---|---|
| Alejandro Chan | [@alejandr003](https://github.com/alejandr003) | Backend/Mobile authority, setup, integración Wearable en `mobile` |
| Neyzer | [@NeyzerJss](https://github.com/NeyzerJss) | Cliente Wear OS (UI, listeners, despacho de comandos) |

## Arquitectura de comunicación (Wearable Data Layer)

### Feature 1 — Comandos desde Wear OS al teléfono (`MessageClient`)
- **Path:** `/workout/action`
- **Dirección:** `wear` → `mobile`
- **Propósito:** enviar acciones de control de la sesión de entrenamiento.
- **Payload esperado (conceptual):** comandos como `START`, `PAUSE`, `STOP`.
- **Componentes clave:**
  - **Wear OS:** descubre el nodo del teléfono con `NodeClient` y envía el mensaje con `MessageClient`.
  - **Mobile:** escucha mensajes entrantes (listener/servicio según implementación) y actualiza el motor de estado del entrenamiento.

### Feature 2 — Sincronización reactiva de métricas (`DataClient`)
- **Path:** `/fitness/metrics`
- **Dirección:** `mobile` → `wear` (publicación de estado; consumo reactivo en reloj)
- **Propósito:** publicar estado actual, cronómetro/métricas y meta diaria para que Wear OS refleje cambios en tiempo real.
- **Mecanismo:** el teléfono escribe/actualiza un `DataItem` (`PutDataMapRequest` / `DataMap`); Wear OS implementa `DataClient.OnDataChangedListener` (`onDataChanged`) para deserializar y actualizar la UI.

> Nota: los nombres exactos de keys del `DataMap`, modelos serializables y constantes compartidas se formalizan en la **Issue 1** (setup/core).

## Estructura del repositorio
- `mobile/` — Aplicación Android (teléfono): dashboard, motor de sesión/cronómetro, publicación de métricas y recepción de comandos.
- `wear/` — Aplicación Wear OS: UI optimizada para muñeca, controles táctiles y sincronización reactiva.
- Configuración Gradle en raíz (`build.gradle.kts`, `settings.gradle.kts`, `gradle/libs.versions.toml`).

## Roadmap / Checklist de Issues
> Issues **#1–#8** creadas en GitHub y vinculadas al plan de trabajo. Se actualizan con PRs/commits conforme avance el desarrollo (`Closes #N`).

- [x] **#1 — [Setup] Core architecture, dependencies and shared Wearable constants** (`@alejandr003`) — *completada, PR [#9](https://github.com/alejandr003/Fitness-Tracker-Workout-Session-Companion/pull/9) mergeada; Issue cerrada*  
  Dependencias `play-services-wearable` y `kotlinx-coroutines-play-services` en ambos módulos; paths/keys compartidos (`/workout/action`, `/fitness/metrics`) y modelos serializables.
- [x] **#2 — [Mobile] UI dashboard, timer state engine & workout session controller** (`@alejandr003`) — *completada, PR [#11](https://github.com/alejandr003/Fitness-Tracker-Workout-Session-Companion/pull/11) mergeada; Issue cerrada*  
  Pantalla principal, cronómetro, selector de meta diaria, motor de estado (`START/PAUSE/STOP`) y base para comandos entrantes.
- [x] **#3 — [Mobile] Wearable communication layer (MessageClient listener & DataClient publisher)** (`@alejandr003`) — *completada, PR [#12](https://github.com/alejandr003/Fitness-Tracker-Workout-Session-Companion/pull/12) mergeada; Issue cerrada*  
  Listener `MessageClient` `/workout/action` (`START/PAUSE/STOP`), integración con controlador y publicación `DataClient` `/fitness/metrics` (por evento + periódica throttled).
- [ ] **#4 — [WearOS] Wrist UI layout & workout controls screen** (`@NeyzerJss`)  
  UI para pantalla circular/cuadrada con controles accesibles y visualización de métricas.
- [ ] **#5 — [WearOS] Command dispatcher via MessageClient** (`@NeyzerJss`)  
  Descubrimiento de nodo con `NodeClient` y envío de comandos al accionar controles.
- [ ] **#6 — [WearOS] Reactive sync consumer via DataClient** (`@NeyzerJss`)  
  Listener `onDataChanged` para deserializar métricas y actualizar UI en tiempo real.
- [ ] **#7 — [QA] End-to-end integration testing on paired emulators** (`@alejandr003`, `@NeyzerJss`)  
  Validación del ciclo completo en emuladores pareados (Bluetooth bridge).
- [ ] **#8 — [Docs] Demo video recording & academic project report** (`@alejandr003`, `@NeyzerJss`)  
  Demo (máx. 3 min) con explicación de código + reporte académico formal.

## Estado actual
- [x] **Fase 1 — Inicialización y sincronización de Git:** repositorio local inicializado, `.gitignore` estándar Android aplicado, commit inicial realizado y rama `main` sincronizada con el remoto.
- [x] **Fase 2 — Documentación base:** `README.md` creado/actualizado.
- [x] **Fase 3 — Issues en GitHub:** issues **#1–#8** creadas, etiquetadas y asignadas en el repositorio remoto.
- [x] **Fase 4 — Avance de features:** lado **Mobile (`@alejandr003`) completado** — **#1** (PR [#9](https://github.com/alejandr003/Fitness-Tracker-Workout-Session-Companion/pull/9)), **#2** (PR [#11](https://github.com/alejandr003/Fitness-Tracker-Workout-Session-Companion/pull/11)), **#3** (PR [#12](https://github.com/alejandr003/Fitness-Tracker-Workout-Session-Companion/pull/12)). Pendiente: **#4–#6** Wear OS (`@NeyzerJss`), luego **#7** QA y **#8** demo/reporte.

## Requisitos técnicos (referencia)
- Android Studio (versión estable reciente) con SDK/emuladores para Phone y Wear OS.
- **Play Services Wearable** (`com.google.android.gms:play-services-wearable`).
- Coroutines para integración asíncrona con Tasks de Play Services (`kotlinx-coroutines-play-services`).
- Emuladores **pareados** (Phone + Wear OS) para pruebas de comunicación.

## Notas de colaboración
- Convención de commits recomendada: **Conventional Commits** (ej. `feat(shared): ...`, `chore: ...`, `docs: ...`).
- Flujo sugerido: rama por issue/feature → PR a `main` con referencia `Closes #N` → revisión y merge.

---
*Proyecto académico — Smart Devices Development Final Project.*
