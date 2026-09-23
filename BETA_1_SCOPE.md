# BETA-1 SCOPE — NexoGo Platform

**Roles:** CTO · Product Manager  
**Fecha:** 2026-09-20  
**Audiencia:** Cliente piloto (1 company)  
**Principio:** Beta **pequeña, estable y usable**. **Sin módulos nuevos.**  
**Referencias:** `PILOT_DEPLOYMENT_PLAN.md`, `DEMO_FLOW.md`, `IDEAL_PILOT_PROFILE.md`, `KNOWN_ISSUES.md`, `S2_CUTOVER_REPORT.md`

---

## Definición

**Beta-1** es el contrato de producto para un piloto cerrado:

1. Crear / entrar a una **empresa**  
2. Gestionar **usuarios** del equipo  
3. Operar **Clientes → Expedientes → Documentos**

### Promesa

> Alta de empresa, equipo e invitación, y trabajo diario de clientes, expedientes y archivos — aislado por empresa.

### No-promesa

> No es el ERP/clínica completa ni el reemplazo de módulos legacy (citas, inventario, ventas, chat, etc.).

**ICP preferido:** veterinaria pequeña (ver `IDEAL_PILOT_PROFILE.md`).

---

## 1. Funcionalidades disponibles

Únicas capacidades **documentadas, capacitadas y soportadas** en el piloto.

| # | Funcionalidad | Entrada | Uso real |
|---|---------------|---------|----------|
| 1 | Registro + login email/password | Splash → Login / Register | Auth Firebase |
| 2 | Company onboarding | Wizard / “Crear mi empresa” | Nombre, industria, plan, admin |
| 3 | Aceptar invitación | Banner sin company | Mismo email Auth que la invite |
| 4 | Hub Home (company ACTIVE) | Post-login | Gate de sesión tenant |
| 5 | Clientes (platform) | Home → Clientes | Listar + crear |
| 6 | Expedientes (platform) | Home → Expedientes | Listar + crear ligado a cliente |
| 7 | Documentos (platform) | Home → Documentos | Listar + subir |
| 8 | Usuarios de la empresa | Admin → Usuarios | Invitar, activar, desactivar, rol, revocar |
| 9 | Perfil / logout | Home | Operación diaria |
| 10 | Ajustes básicos | Home → Ajustes | Solo lo existente; sin features nuevas |

### Flujo feliz

```
Registro → Onboarding company → Home
  → Invitar staff → Staff acepta
  → Cliente → Expediente → Documento
```

Guion corto: `DEMO_FLOW.md` (&lt; 10 min).

---

## 2. Funcionalidades ocultas

Presentes en binario/nav por compatibilidad; **no** forman parte del contrato Beta-1. No capacitar ni demo.

| Área | Ejemplos | Motivo |
|------|----------|--------|
| Pacientes legacy | Patients, create/edit | Duplica Clientes platform |
| Clínico / historias legacy | ClinicalRecords, History*, MedicalRecords | Paralelo a Expedientes |
| Citas | Appointments | Sin cutover tenant en Beta-1 |
| Inventario / categorías | Inventory, CategoryManagement | Fuera del núcleo |
| Ventas / servicios | Sales, ServicesManagement | Fuera del núcleo |
| Chat / chatbot | ChatList, NewChat, ChatbotConfig | No estable como producto |
| Reportes | Reports* | Legacy / no KPI piloto |
| Dashboard como hub | Dashboard | Home es el único hub |
| Aprobación usuarios global | UserApproval / pending `usuarios` | Distinto de invite company |
| Firebase Test / logins experimentales | Fuera del grafo beta | Mantener fuera |

**Regla:** si preguntan por citas/inventario → “no incluidos en Beta-1”.

---

## 3. Funcionalidades experimentales

Pueden ejecutarse; **no se venden** ni se prometen. Soporte best-effort.

| Ítem | Qué hace | Tratamiento piloto |
|------|----------|-------------------|
| PermissionEngine en nav | Filtra drawer/quick actions | Experimental; writes no enforceados por rol |
| Plan en onboarding | Guarda `planId` / limits | Sin cobro; placeholder comercial |
| Usage Analytics | Contadores diarios en Firestore | Interno NexoGo; sin UI cliente |
| AuditLogger | Eventos (p.ej. login) | Interno; sin viewer piloto |
| Industry packs extra | Modelo / radios onboarding | Experimental |
| Deep link a `platform_*` | Rutas sin gate de permiso | Evitar compartir URLs internas |

---

## 4. Funcionalidades listas para uso real

“Uso real” = operable bajo `companies/{companyId}` con rules deny-by-default y sesión ACTIVE, en el alcance de create/list/(upload).

| Capacidad | Listo | Límite aceptado |
|-----------|-------|-----------------|
| Company + sesión + onboarding | **Sí** | Sin switcher multi-company |
| Auth email/password | **Sí** | Ver riesgos KI-001/003 |
| User Management (invite/roles/status) | **Sí** | ≤ ~15 users; ver KI-005/006 |
| Clients platform (list + create) | **Sí** | Edit/archive UI limitado |
| Records platform (list + create) | **Sí** | Picker ≤ 8 clientes (KI-009) |
| Documents platform (list + upload) | **Sí** | Consulta = lista; sin visor PDF rico |
| Aislamiento tenant (rules desplegadas) | **Sí** | Condicionado a deploy + KI-002 |

---

## 5. Funcionalidades bloqueadas para beta

**No exponer, no demo, no incluir en alcance comercial** de Beta-1. Código puede existir (foundation); el piloto no las “abre”.

| Bloqueado | Motivo |
|-----------|--------|
| CRM (leads, opportunities, journeys) | Sin cutover UX en contrato |
| Tasks | Foundation sin hub Beta-1 |
| Platform Chat / push chat | No producto estable |
| AI / OpenAI | Costo, riesgo, sin UI piloto |
| Notifications center / dispatchers | Sin centro de notificaciones Beta-1 |
| Dashboard widgets / snapshots | No hub |
| Billing / pagos reales | Solo docs de arquitectura |
| Backup & Recovery UI / Drive | Solo plan (`BACKUP_RECOVERY_PLAN.md`) |
| Company switcher | Fuera de TOP_20 ejecutado |
| Role editor / catálogo UI avanzado | Solo seed + membership roles |
| MFA / App Check como producto | No configurados para piloto |
| Migración masiva / import Excel | No soportado |
| Inventario / citas / ventas / reportes como “platform” | Bloqueados vía §2 ocultas |

Cualquier entrada de nav legacy que siga visible en el APK se trata como **fuera de contrato**; packaging ideal = ocultarla (sin módulos nuevos).

---

## 6. Riesgos conocidos

Fuente: `KNOWN_ISSUES.md` (defectos confirmados en código). Impacto en el piloto:

| ID | Riesgo | Sev | Mitigación operativa Beta-1 |
|----|--------|-----|------------------------------|
| KI-002 | Self-join membership ACTIVE vía rules | CRÍTICO | No compartir `companyId`; deploy rules; monitoreo; fix rules prioritario |
| KI-001 / KI-003 | Login/registro no alineados con `isApproved` | CRÍTICO | No usar aprobación legacy como control; owner vía onboarding |
| KI-006 | Re-accept tras REVOKED falla | ALTO | Evitar “eliminar acceso” + re-invitar en demo; recrear invite flow con cuidado |
| KI-005 | Mirror membership desfasado | ALTO | Tras suspend/revoke, validar sesión del usuario afectado |
| KI-007 | Writes platform sin chequeo de rol en UI | ALTO | Pocos roles; no dar CLIENT con acceso a rutas; ideal ADMIN+EMPLOYEE |
| KI-004 | Bind company preferida sin fallback | ALTO | Piloto 1 company por usuario |
| KI-008 | Approve Admin (DataStore) ≠ Firestore | ALTO | No usar User Approval legacy en demo |
| KI-009 | Picker expedientes `take(8)` | MEDIO | Demo con pocos clientes |
| KI-010 | Orphan Storage si falla register | MEDIO | Archivos pequeños PDF/JPG |
| KI-011 | Listeners → lista vacía en error | MEDIO | Reintentar / chequear red y rules |
| KI-012 | Splash siempre Login | MEDIO | Esperado en demo |
| KI-013 | Nav legacy + platform | MEDIO | No abrir Pacientes/Historial en demo |
| KI-019 | Invite OK pero assignment IAM puede fallar en silencio | MEDIO | Verificar rol/nav tras accept |

### Riesgos de producto (no solo bugs)

| Riesgo | Mitigación |
|--------|------------|
| Expectativa de citas/inventario (vet) | Contrato escrito + `IDEAL_PILOT_PROFILE.md` |
| Confusión “Pacientes” vs “Clientes” | Guion demo; ocultar legacy si hay build piloto |
| Volumen docs / archivos grandes | Límites acordados (ICP) |
| Soporte multi-sede / &gt;15 users | Fuera de Beta-1 |

---

## Empaquetado recomendado (sin módulos nuevos)

| Acción | Propósito |
|--------|-----------|
| Tag `beta-1-YYYYMMDD` | Trazabilidad |
| Nav reducida a Home + C/E/D + Admin usuarios | Estabilidad percibida |
| Capacitación solo §1 | Expectativa clara |
| Gate go-live `PILOT_DEPLOYMENT_PLAN.md` | Rules/indexes/rollback |

---

## Criterios de entrega Beta-1

1. Owner completa onboarding → Home &lt; 10 min.  
2. Invitee con mismo email acepta y entra a la misma company.  
3. ≥ 1 cliente, 1 expediente, 1 documento sin `PERMISSION_DENIED`.  
4. Trabajo diario **sin** pacientes/citas/inventario.  
5. 0 incidentes S1 de fuga cross-tenant en el piloto.

---

## Resumen ejecutivo

| # | Pregunta | Respuesta Beta-1 |
|---|----------|------------------|
| 1 | Disponibles | Auth, Company, Invites/Users, Clients, Records, Documents |
| 2 | Ocultas | Legacy: pacientes, clínico, citas, inventario, ventas, chat, reportes, approval global |
| 3 | Experimentales | Plan sin billing, PermissionEngine parcial, usage/audit internos |
| 4 | Listas uso real | Company, Users, Clients, Records, Documents (+ rules desplegadas) |
| 5 | Bloqueadas | CRM, Tasks, AI, Chat platform, Billing, Backup UI, Dashboard, switcher, MFA… |
| 6 | Riesgos | KI-001…019; priorizar KI-002 y superficie nav |

**Decisión:** Beta-1 = **núcleo tenant operable**. Pequeña a propósito. Éxito del piloto = validar ese núcleo antes de reabrir módulos.

---

*Alcance de producto. No agrega módulos; delimita lo construido para un cliente piloto.*
