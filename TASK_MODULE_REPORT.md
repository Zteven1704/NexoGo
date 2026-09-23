# Task Management Module Report

**Fecha:** 2026-09-18  
**Alcance:** Gestión de tareas empresariales multiempresa  
**Compilación:** `:app:compileDebugKotlin --rerun-tasks` — **BUILD SUCCESSFUL**  
**UI:** No cableada (módulo de dominio + repository)

---

## Objetivo

Implementar el módulo de **Tareas** con asignación, fechas límite, comentarios y seguimiento de estado — preparado para vínculos soft con Chat, Clientes y Expedientes.

---

## Entregables

| Entregable | Ubicación | Estado |
|------------|-----------|--------|
| **Task** | `platform/tasks/model/TaskModels.kt` | ✅ |
| **TaskComment** | mismo archivo | ✅ |
| **TaskStatus** | enum | ✅ |
| **TaskPriority** | enum | ✅ |
| **TaskRepository** | `platform/tasks/data/TaskRepository.kt` | ✅ |

---

## TaskStatus

`TODO` → `IN_PROGRESS` → `BLOCKED` / `IN_REVIEW` → `DONE` | `CANCELLED`

## TaskPriority

`LOW` | `MEDIUM` | `HIGH` | `URGENT`

---

## Capacidades

| Capacidad | API |
|-----------|-----|
| Crear / actualizar / listar / buscar | `createTask`, `updateTask`, `listTasks`, `searchTasks` |
| Asignar | `assignTask` (+ comentario ASSIGNMENT) |
| Fecha límite | `setDueDate` |
| Prioridad | `setPriority` |
| Seguimiento de estado | `updateStatus` (+ comentario STATUS_CHANGE + progress) |
| Comentarios | `addComment`, `listComments`, `listenComments` |
| Realtime inbox | `listenTasks` |

---

## Multiempresa

- `Task` / `TaskComment` implementan `TenantAwareEntity`.
- Paths:
  - `companies/{companyId}/tasks/{taskId}`
  - `companies/{companyId}/tasks/{taskId}/comments/{commentId}`
- `TenantIsolationGuard` + `TenantAwareRepository`.

---

## Integración futura (soft FKs)

| Campo | Módulo |
|-------|--------|
| `clientId` | Clients — `linkClient` / `listTasksForClient` |
| `recordId` | Records — `linkRecord` / `listTasksForRecord` |
| `conversationId` | Chat — `linkConversation` |
| `documentIds` | Documents (lista) |

Sin dependencias duras de compilación hacia esos módulos.

---

## Archivos

```
app/src/main/java/com/example/nexogo/platform/tasks/
  model/TaskModels.kt
  data/TaskRepository.kt
```

**Actualizado:** `TenantCollections` (`TASKS`, `TASK_COMMENTS`), `TenantFirestore.tasks()`

---

## Compilación

```bash
bash ./gradlew :app:compileDebugKotlin --no-daemon --rerun-tasks
# BUILD SUCCESSFUL
```

---

## Fuera de alcance (siguiente)

- UI Compose / ViewModel
- Notificaciones de vencimiento
- Crear tarea desde Chat / Expediente automáticamente
- Checklist UI / subtareas avanzadas
- Security Rules

---

## Veredicto

**Task Management Module completo y compilando.** Asignación, due dates, comentarios y tracking listos; hooks soft para Chat / Clients / Records.
