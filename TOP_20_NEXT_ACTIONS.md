# TOP 20 NEXT ACTIONS — Beta usable por clientes reales

**Fuente:** `REAL_IMPLEMENTATION_AUDIT.md`  
**Fecha:** 2026-09-19  
**Regla:** No inventar módulos nuevos. Solo cablear, endurecer y recortar lo ya implementado.  
**Meta:** Beta multi-empresa donde un cliente real puede operar sin fuga de datos ni pantallas muertas.

### Criterio de impacto

| Prioridad | Significado |
|-----------|-------------|
| **CRÍTICO** | Sin esto no se puede abrir beta (seguridad / integridad / secretos) |
| **ALTO** | Sin esto la beta no es usable como producto Platform (flujo E2E) |
| **MEDIO** | Mejora estabilidad, honestidad de features o operación |
| **BAJO** | Pulido beta; no bloquea primer piloto controlado |

---

## Ranking (1 → 20)

| # | Acción | Prioridad | Impacto | Módulo existente | Resultado beta |
|---|--------|-----------|---------|------------------|----------------|
| 1 | Reescribir `firestore.rules`: deny-by-default; reglas membership en `companies/{companyId}/**`; **eliminar** catch-all `auth != null` | **CRÍTICO** | Bloquea fuga cross-tenant | tenant / company | Datos de empresa aislados en servidor |
| 2 | Añadir `storage.rules` para `companies/{companyId}/…` (membership); mantener deny en el resto | **CRÍTICO** | Sin esto no hay archivos tenant | documents / storage paths | Uploads posibles y seguros |
| 3 | Retirar del cliente credenciales hardcodeadas (`admin@nexogo.com` / `123456`) y auto-creación de admin | **CRÍTICO** | Cuenta conocida = compromiso | auth legacy | Beta sin puerta trasera |
| 4 | Limpiar `MainActivity`: quitar `MockDataGenerator`, diagnostics, permission testers y seed de citas de prueba en cold start | **CRÍTICO** | Contamina Firebase de clientes | app entry | Datos reales no mezclados con mock |
| 5 | No desplegar / archivar `firestore_rules_emergency.rules` (`if true`) | **CRÍTICO** | Riesgo operativo | ops | Evita abrir DB por error |
| 6 | Sesión Company **obligatoria** post-login: si no hay membership/company, bloquear hub (no fail-open silencioso) | **CRÍTICO** | Tenant cosmético hoy | company / session | Toda sesión beta tiene `companyId` |
| 7 | Un solo hub post-login (Home **o** Dashboard); deprecar el otro en rutas de login beta | **ALTO** | Dual entry confunde onboarding | nav | Entrada única |
| 8 | Cablear `PermissionEngine` / `PermissionChecker` al drawer/acciones del hub (ocultar lo no permitido) | **ALTO** | IAM existe pero no gobierna UI | role | Menú coherente por rol |
| 9 | `RoleRepository.seedPlatformFoundation()` + asignación de rol en create/bind Company | **ALTO** | Sin seed, engine vacío en Firestore | role / company | Roles base disponibles en beta |
| 10 | Exponer **Clients** en NavGraph usando `ClientViewModel` + `ClientRepository` existentes (list/create/edit) | **ALTO** | Primer cutover Platform→producto | clients | Empresa gestiona clientes tenant |
| 11 | Exponer **Records** en NavGraph usando `RecordViewModel` + `RecordRepository` existentes | **ALTO** | Segundo flujo E2E tenant | records | Expedientes bajo `companies/{id}` |
| 12 | Completar upload de documentos: `putFile` → `prepareUploadPath` + `registerUpload` / `addVersion` (código ya modelado) | **ALTO** | Metadata sin archivo = feature rota | documents | Docs reales en Storage tenant |
| 13 | Crear `firestore.indexes.json` para queries usadas por Clients/Records/Documents/Tasks (orderBy + where) | **ALTO** | Sin indexes fallan listados | firestore ops | Listas estables en beta |
| 14 | Quitar de navegación beta: `FirebaseTest`, pantallas de diagnóstico y flujos de login experimentales no usados | **ALTO** | Reduce superficie de error | nav | App piloto limpia |
| 15 | Definir y aplicar **alcance beta**: ocultar en nav AI, CRM platform, Chat platform, Dashboard platform (repos quedan; no se exponen) | **MEDIO** | Evita features NoOp presentadas como listas | ai / crm / chat / dashboard | Expectativas honestas |
| 16 | Inbox in-app mínimo con `NotificationRepository` + `listenForUser` (sin exigir push/email aún) | **MEDIO** | Canal interno ya persistible | notifications | Alertas internas usables |
| 17 | Disparar `notify*` / `AuditLogger` create-update-delete solo en Clients/Records/Documents cableados | **MEDIO** | Trazabilidad del piloto | audit / notifications | Quién hizo qué |
| 18 | Límites (`limit`) y paginación básica en listados de repos que se cableen (evitar full-scan) | **MEDIO** | Coste y latencia | clients / records / docs | Beta no se cae con 1k docs |
| 19 | Congelar stacks duplicados en beta: **una** fuente de chat (legacy actual) y **no** abrir `platform.chat` en UI hasta cutover | **MEDIO** | Evita dos mundos de mensajes | chat | Datos de conversación no partidos |
| 20 | Company switcher mínimo si el usuario tiene >1 membership (`listUserCompanyRefs` ya existe) | **BAJO** | Multi-empresa real; un solo tenant basta para piloto 1:1 | company | Cambio de empresa sin reinstall |

---

## Detalle operativo (por acción)

### 1–5 · Cierre de seguridad (CRÍTICO)
Sin rules tenant + sin secretos/mock, **cualquier beta es irresponsable**. Orden estricto: rules Firestore → Storage → credentials → MainActivity → emergency rules.

### 6–9 · Spine de producto (CRÍTICO → ALTO)
Company session hard + un hub + permisos sembrados. Sin esto, Clients/Records cableados siguen siendo UI sobre un tenant opcional.

### 10–13 · Cutover mínimo usable (ALTO)
Solo módulos **ya implementados** con VM/repo: Clients, Records, Documents (upload). Indexes acompañan el cutover, no al final.

### 14–19 · Recorte y honestidad (ALTO → MEDIO)
Beta ≠ exponer todo lo que compila. Ocultar NoOp (AI, push, chat platform, CRM). Notificaciones/audit solo donde haya UI.

### 20 · Multi-empresa cómoda (BAJO)
Piloto de una sola company puede vivir sin switcher; priorizar después del primer cliente estable.

---

## Fuera de estas 20 (explícitamente)

- No construir módulos nuevos (billing engine, inventory/sales tenant repos, appointments platform, etc.).
- No implementar `HttpOpenAiClient` ni email SMTP para beta-1.
- No migrar inventario/ventas/citas a `companies/{id}` en este lote (siguen legacy **solo si** el alcance beta lo documenta y las rules legacy se endurecen en el mismo P0).
- No más foundations ni reportes de diseño: ejecución de cutover.

---

## Definición de “beta usable” (exit criteria)

Una empresa piloto puede:

1. Registrarse / login **sin** admin hardcodeado ni mock en arranque.  
2. Quedar atada a una `companyId` obligatoria.  
3. Ver un solo hub con menú filtrado por `PermissionEngine`.  
4. CRUD de **Clients** y **Records** bajo `companies/{id}/…`.  
5. Subir al menos un **documento** a Storage tenant y verlo listado.  
6. No leer datos de otra company (verificado con rules + prueba manual de 2 tenants).  
7. No ver AI / CRM platform / chat platform / FirebaseTest en navegación.

Cuando 1–7 se cumplen → beta-1. Las acciones 16–20 mejoran el piloto; no lo definen.

---

## Secuencia sugerida (sprints)

| Sprint | Acciones | Entrega |
|--------|----------|---------|
| **S0 Secure** | #1–#5 | Proyecto seguro para datos reales |
| **S1 Spine** | #6–#9, #14 | Hub + roles + nav limpia |
| **S2 Cutover** | #10–#13 | Clients + Records + Docs + indexes |
| **S3 Harden** | #15–#19 | Alcance honesto + audit/notif + límites |
| **S4 Multi** | #20 | Switcher si el piloto lo pide |

---

## Métrica de avance esperada (post-ejecución)

Referencia audit: Platform ~39% foundation / ~18% producto / **0%** producción.

Tras completar **#1–#13** (sin módulos nuevos):  
estimado **~55–65%** producto beta-usable en Clients/Records/Docs + company/IAM; producción multi-tenant pasa de 0% a **viable para piloto cerrado** (aún no GA).

---

*Documento de acción. No propone features nuevas: solo hace usable y seguro lo que `REAL_IMPLEMENTATION_AUDIT.md` demostró que ya existe en código.*
