# NEXOGO V1 — GAP ANALYSIS

**Rol:** CTO  
**Fecha:** 2026-09-21  
**Fuentes:** `REAL_IMPLEMENTATION_AUDIT.md` (baseline foundation 2026-09-19) · `TOP_20_NEXT_ACTIONS.md` · `BETA_1_SCOPE.md`  
**Verificación:** Estado post S0 Secure / S1 Spine / S2 Cutover (código + reports)  
**Regla:** Sin módulos nuevos. Solo gaps reales entre lo construido y una **V1 comercial**.

---

## Veredicto

**Beta-1 (piloto cerrado)** es operable en el núcleo Company → Users → Clientes → Expedientes → Documentos.  
**V1 comercial** aún **no**. El cutover cerró el hueco “repo sin UI” del núcleo; quedan seguridad crítica (KI), superficie legacy confusa, IAM incompleto, y foundations (CRM/Tasks/AI/Chat platform/Billing) sin producto vendible.

| Capa | Estado |
|------|--------|
| Foundation Platform (repos) | Amplia (~12 dominios con CRUD) |
| Producto Beta-1 (contrato) | Núcleo C/E/D + company/users **vivo** |
| Producción multi-tenant GA | **No** — bloqueadores CRÍTICO/ALTO abiertos |
| Avance audit (histórico) | ~39% foundation / ~18% producto / 0% prod |
| Lectura CTO hoy | Núcleo ~piloto-ready condicionado; **V1 comercial bloqueada** |

---

## Definición usada de “V1 comercial”

No es “todo lo que compila”. Para este análisis, V1 comercial del **núcleo Platform** exige:

1. Aislamiento tenant **servidor** sin bypass conocidos.  
2. Superficie de producto **honesta** (sin menús legacy/NoOp como si fueran el producto).  
3. Flujo E2E del núcleo estable (CRUD usable, no solo create/list mínimo frágil).  
4. IAM que gobierne al menos UI + camino de writes sensibles.  
5. Operación vendible: onboarding, usuarios, soporte, sin puerta trasera ni rules abiertas.  
6. Foundations extra (CRM, Tasks, AI, Chat platform, Billing) **o** quedan explícitamente fuera de V1 — pero no pueden aparecer como listas.

Beta-1 acepta waivers operativos; **V1 comercial no**.

---

## 1. Funcionalidades diseñadas pero no conectadas

Código de dominio existe (modelos + repository / engine); **sin ruta de producto** (UI/nav/auth) o sin delivery real.

| ID | Ítem | Evidencia | Gap V1 | Sev |
|----|------|-----------|--------|-----|
| G1.1 | **CRM platform** (`CrmRepository`) | Audit §3/§8; Beta-1 §5 bloqueado | Repo leads/opportunities sin NavGraph / pantalla | **BAJO*** |
| G1.2 | **Tasks** (`TaskRepository`) | Audit; Beta-1 bloqueado | Sin hub / ruta | **BAJO*** |
| G1.3 | **AI** (`AIRepository` + `NoOpOpenAiClient`) | Audit §2; TOP_20 excluye HTTP client | Jobs/meta sin ejecución ni UI | **BAJO*** |
| G1.4 | **Chat platform** | Audit; UI usa chat **legacy** | Dos mundos; platform chat sin Compose | **MEDIO** |
| G1.5 | **Dashboard platform** | Audit; hub beta = Home | Snapshots/widgets sin UI Platform | **BAJO*** |
| G1.6 | **Notifications center** | `NotificationRepository` sin inbox; dispatchers NoOp push/email | Persistencia posible; nadie escucha en UI | **MEDIO** |
| G1.7 | **AuditViewer** | Clase estado/queries; sin Screen | Logs se escriben (login); no se consultan | **MEDIO** |
| G1.8 | **Role editor / catálogo UI** | `RoleRepository` CRUD; seed vía spine | Admin no edita catálogo en pantalla | **MEDIO** |
| G1.9 | **PermissionEngine en writes** | Engine en hub nav (experimental); repos platform no enforcean rol | Diseñado como IAM; conectado solo a menú | **ALTO** |
| G1.10 | **Billing** | Solo arquitectura `.md` | Sin código de cobro/planes reales | **ALTO**** |
| G1.11 | **Backup & Recovery UI** | Solo `BACKUP_RECOVERY_PLAN.md` | Sin export/restore producto | **MEDIO** |
| G1.12 | **Appointments / Sales / Inventory tenant** | Solo constantes `TenantCollections` | Sin repos platform; legacy root sigue vivo | **BAJO*** |
| G1.13 | **Company switcher** | `listUserCompanyRefs` existe; TOP_20 #20 | Sin UI de cambio de empresa | **BAJO** |
| G1.14 | **Push / email dispatchers** | NoOp con `Result.success` | “Enviado” mentiroso si se cableara hoy | **MEDIO** |

\*Severidad **para V1 del núcleo**: no bloquean vender Clientes/Expedientes/Documentos **si permanecen fuera de contrato y ocultos**. Suben a ALTO si se muestran o se venden.  
\*\*Bloquea V1 **comercial de pago**; un V1 “free/pilot paid offline” puede diferir billing con waiver explícito.

### Contraste con el audit

El audit marcó Clients / Records / Documents como **Diseñado+Implementado / no Conectado**.  
**Eso ya no es gap:** S2 + `BETA_1_SCOPE` §1 los conectaron (`Platform*Screen`, `uploadAndRegister`, indexes).  
No reabrirlos como “diseñados sin conectar”.

---

## 2. Funcionalidades implementadas pero no visibles

Lógica lista y, en varios casos, parcial o internamente usada; **el cliente no las ve** (o no debe verlas en Beta-1).

| ID | Ítem | Qué hay | Por qué no visible / no producto | Sev V1 |
|----|------|---------|----------------------------------|--------|
| G2.1 | **Usage Analytics** | Contadores día en Firestore | Sin UI; solo NexoGo | **BAJO** |
| G2.2 | **AuditLogger** (más allá login) | Repo + logger | Sin viewer; create/update/delete de C/E/D no son contrato visible | **MEDIO** |
| G2.3 | **PermissionEngine / NavPermissionFactory** | Filtra drawer/quick actions | Experimental; usuario no gestiona permisos; legacy sigue apareciendo si el rol lo permite | **ALTO** |
| G2.4 | **Plan / limits en onboarding** | `planId` guardado | Sin enforcement ni cobro | **ALTO** |
| G2.5 | **Platform Chat / CRM / Tasks / AI repos** | Compilan | Ocultos a propósito (TOP_20 #15, Beta-1 §5) — correctos para beta; **deuda** si V1 “suite” los espera | **BAJO*** |
| G2.6 | **Indexes compuestos** (`firestore.indexes.json`) | Archivo en repo | Depende de **deploy** al proyecto; no es UI pero es “invisible” hasta ops | **ALTO** |
| G2.7 | **In-app notifications** | Repo `listenForUser` | TOP_20 #16 no es criterio de beta; sin centro = no visible | **MEDIO** |
| G2.8 | **TenantIsolationGuard / TenantFirestore** | Infra en repos | Efecto solo si rules + callers correctos; no hay pantalla “aislamiento” | — (infra) |

---

## 3. Funcionalidades visibles pero incompletas

Están en nav o en el flujo vivo; **no cumplen calidad V1**.

| ID | Ítem | Visible | Incompleto | Sev |
|----|------|---------|------------|-----|
| G3.1 | **Clientes platform** | Home → Clientes | List + create; edit/archive UI limitado (Beta-1 §4) | **ALTO** |
| G3.2 | **Expedientes platform** | Home → Expedientes | Create/list; picker `take(8)` (KI-009); sin profundidad clínica vendible | **ALTO** |
| G3.3 | **Documentos platform** | Home → Documentos | Upload + list; sin visor PDF rico; riesgo orphan Storage (KI-010); upload carga bytes en RAM (KI-017) | **ALTO** |
| G3.4 | **User Management** | Admin → Usuarios | Invite/rol/status vivos; KI-005/006/019 (mirror, re-accept REVOKED, assignment silencioso) | **ALTO** |
| G3.5 | **Company onboarding / sesión** | Wizard + gate Home | Sin switcher; bind preferido sin fallback robusto (KI-004); 1 company/usuario en piloto | **MEDIO** |
| G3.6 | **Login / registro** | Rutas auth vivas | KI-001/003 (`isApproved` divergente); spill de logins experimentales si no se empaquetan fuera | **CRÍTICO** |
| G3.7 | **Nav hub (Home)** | Menú + quick actions | **Legacy + platform juntos** (KI-013): Pacientes, Citas, Inventario, Chat, Reportes siguen alcanzables | **CRÍTICO** |
| G3.8 | **PermissionEngine en hub** | Filtra por rol | No enforcea writes (KI-007); deep links `platform_*` sin gate fuerte | **ALTO** |
| G3.9 | **Chat legacy** | Nav | No tenant; no es el chat platform; datos root | **MEDIO** |
| G3.10 | **Ajustes / Admin legacy** | Rutas existentes | Mezcla User Approval legacy (KI-008) con User Management platform | **ALTO** |
| G3.11 | **Documents “consulta”** | Lista | No hay experiencia de lectura/compartir comparable a Drive | **MEDIO** |

---

## 4. Dependencias pendientes

Prerrequisitos técnicos/operativos **ya identificados** (TOP_20 / KNOWN_ISSUES / Beta-1), no features nuevas.

| ID | Dependencia | Bloquea | Sev |
|----|-------------|---------|-----|
| G4.1 | **Fix rules membership self-join (KI-002)** | Aislamiento real; cualquier V1 multi-tenant | **CRÍTICO** |
| G4.2 | **Alinear login/registro con política de acceso (KI-001/003)** o retirar `isApproved` del camino vivo | Auth confiable | **CRÍTICO** |
| G4.3 | **Deploy rules + indexes + storage** en proyecto de producción (no solo repo) | Listados, uploads, aislamiento | **CRÍTICO** |
| G4.4 | **Empaquetado nav Beta/V1**: ocultar legacy + FirebaseTest + logins extra (TOP_20 #14–15) | Expectativa comercial; soporte | **CRÍTICO** |
| G4.5 | **IAM writes + roles seed consistentes** (TOP_20 #8–9; KI-007/019) | Multi-rol vendible | **ALTO** |
| G4.6 | **Estabilizar invite lifecycle** (KI-005/006) | Offboarding/re-onboarding | **ALTO** |
| G4.7 | **Límites/paginación listados** (TOP_20 #18) | Coste Firebase + UX con volumen | **MEDIO** |
| G4.8 | **Audit/notify en mutaciones C/E/D** (TOP_20 #17) | Trazabilidad B2B | **MEDIO** |
| G4.9 | **Inbox in-app** (TOP_20 #16) o compromiso explícito “sin notificaciones V1” | Comunicación in-product | **MEDIO** |
| G4.10 | **Congelar chat a una fuente** (TOP_20 #19) | Integridad de mensajes | **MEDIO** |
| G4.11 | **MFA / App Check** (Beta-1 bloqueados) | Postura seguridad comercial | **ALTO** |
| G4.12 | **Backup export operable** (plan existe; UI no) | Continuidad / enterprise light | **MEDIO** |
| G4.13 | **Billing o modelo comercial offline documentado** | Cobro SaaS | **ALTO** |
| G4.14 | **Cerrar KI-010/017** (orphan + OOM upload) | Docs confiables | **ALTO** |
| G4.15 | **S3 Harden completo** (TOP_20 #15–19) | Honestidad de producto | **ALTO** |
| G4.16 | **Play Store / distribución + proyecto Firebase prod dedicado** | Entrega comercial | **ALTO** |

---

## 5. Bloqueadores para una versión V1 comercial

Solo lo que **impide vender/operar en serio** el núcleo (o provoca riesgo inaceptable). No incluye “falta CRM”.

### CRÍTICO — no ship comercial

| # | Bloqueador | Por qué |
|---|------------|---------|
| B1 | **KI-002 self-join membership** | Cualquier Auth con `companyId` puede entrar al tenant |
| B2 | **Superficie dual legacy + platform (KI-013) sin packaging** | Cliente paga por C/E/D y opera pacientes/citas root sin aislamiento; soporte imposible |
| B3 | **Auth approval inconsistente (KI-001/003)** o política no definida en el camino vivo | Control de acceso no auditable |
| B4 | **Rules/indexes/storage no desplegados / no verificados en prod** | El código del repo no aísla el proyecto del cliente |
| B5 | **Credenciales / mocks / emergency rules** (TOP_20 #3–5) si aún presentes en build de release | Compromiso o DB abierta |

### ALTO — V1 del núcleo queda “beta eterna”

| # | Bloqueador | Por qué |
|---|------------|---------|
| B6 | **Writes sin enforce de rol (KI-007)** | IAM de marketing, no de producto |
| B7 | **User Management frágil (KI-005/006/019)** | No se puede administrar equipo con confianza |
| B8 | **C/E/D incompletos para uso diario serio** (edit limitado, picker 8, docs sin visor, orphans/OOM) | Churn en las primeras semanas de pago |
| B9 | **Sin MFA/App Check ni postura mínima anti-abuso** | Due diligence de cliente B2B |
| B10 | **Sin billing ni waiver comercial explícito** | No hay V1 “SaaS” cobrable en producto |
| B11 | **S3 Harden / alcance honesto no cerrado** | Features NoOp o menús confusos en build comercial |

### MEDIO — no bloquean un V1 núcleo estrecho, sí escalan mal

| # | Ítem |
|---|------|
| B12 | Sin AuditViewer / inbox notificaciones |
| B13 | Sin company switcher (multi-empresa real) |
| B14 | Sin backup UI |
| B15 | Chat legacy vs platform sin cutover |
| B16 | Paginación/límites insuficientes a escala |

### BAJO — fuera de V1 núcleo (no inventar; no abrir)

| # | Ítem |
|---|------|
| B17 | CRM / Tasks / AI / Dashboard platform / Billing engine / Inventory-Sales-Appointments tenant |

Abrir B17 **sin** cerrar B1–B11 convierte V1 en el mismo error del audit: foundation sin producto.

---

## Mapa resumen (las 5 preguntas)

| # | Pregunta | Respuesta corta |
|---|----------|-----------------|
| 1 | Diseñadas no conectadas | CRM, Tasks, AI, Chat platform, Dashboard platform, Notification inbox, AuditViewer, Role UI, Billing, Backup UI, switcher, push/email reales; **PermissionEngine en writes** |
| 2 | Implementadas no visibles | Usage analytics, audit parcial, plan/limits sin enforce, repos bloqueados a propósito, indexes pendientes de deploy |
| 3 | Visibles incompletas | Clientes, Expedientes, Documentos, Users, Login, **Home con legacy**, permisos solo menú |
| 4 | Dependencias pendientes | Fix KI-002/001/003; packaging nav; deploy rules/indexes; IAM writes; invites; S3 harden; MFA/App Check; billing o waiver; upload robusto |
| 5 | Bloqueadores V1 comercial | **B1–B5 CRÍTICO**; **B6–B11 ALTO**; resto no define el núcleo |

---

## Relación con TOP_20 (estado conceptual)

| Bloque TOP_20 | Intención | Gap residual hacia V1 |
|---------------|-----------|------------------------|
| S0 #1–5 Secure | Rules, storage, secretos, mock, emergency | Verificar en **release + KI-002** aún abierto |
| S1 #6–9 Spine | Sesión hard, hub, permisos, seed | Permisos parciales; hub no limpio de legacy |
| S2 #10–13 Cutover | C/E/D + indexes | **Hecho en código**; calidad V1 incompleta (G3.x) |
| S3 #15–19 Harden | Honestidad + audit/notif + límites | **Pendiente** como paquete V1 |
| S4 #20 Switcher | Multi-empresa UX | Opcional post-V1 núcleo |

---

## Qué NO es un gap de V1 (evitar scope creep)

- Construir inventario/citas/ventas bajo `companies/{id}` (no hay repos; sería módulo nuevo).  
- Implementar `HttpOpenAiClient` o SMTP para “completar” AI/email.  
- Migrar todo el legacy vet a Platform en el mismo release.  
- Tratar el audit del 19-sep como estado actual de C/E/D (ya cutover).

---

## Criterio CTO de salida a V1 comercial (núcleo)

V1 comercial del núcleo **solo** cuando:

1. KI-002 cerrado y prueba manual 2-tenant en **prod**.  
2. Build comercial **sin** Pacientes/Citas/Inventario/Chat/Reportes/FirebaseTest en nav (o feature-flag off).  
3. Política de auth única (sin divergencia `isApproved`).  
4. Clients/Records/Documents con edición básica usable + uploads sin orphans/OOM rutinarios.  
5. Invites activate/deactivate/revoke confiables.  
6. Writes sensibles alineados a rol (app + rules donde aplique).  
7. Billing conectado **o** modelo comercial explícito fuera de app.  
8. Go-live checklist + support plan aplicados a clientes de pago (no solo piloto).

Hasta entonces: **Beta-1 / piloto cerrado** — no V1.

---

*Gap analysis CTO. Fuentes: audit + TOP_20 + Beta-1 + verificación post-cutover. Sin diseño de módulos nuevos.*
