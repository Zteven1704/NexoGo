# BETA Acceptance Tests — NexoGo

**Rol:** QA Lead  
**Alcance:** Beta piloto cerrado (post S0 Secure + S1 Spine + S2 Cutover)  
**Fecha:** 2026-09-19  
**Build mínima:** `:app:compileDebugKotlin` SUCCESS  
**Tipo:** Pruebas **manuales** en dispositivo/emulador Android + consola Firebase (Auth / Firestore / Storage)

---

## 0. Precondiciones globales

| Ítem | Requisito |
|------|-----------|
| App | Debug instalada; cold start limpio (sin auto-seed de mocks/admin) |
| Backend | `firestore.rules` y `storage.rules` desplegados (deny-by-default + membership `ACTIVE`) |
| Índices | `firestore.indexes.json` desplegado o índices listos (listados sin error de índice faltante) |
| Red | Dispositivo online |
| Datos | Preferible proyecto Firebase de **piloto**; no producción abierta |
| Cuentas | Al menos 2 usuarios Firebase Auth (A = con membership ACTIVE; B = sin membership / otro tenant) |

**Convenciones**

- **PASS** = todos los pasos cumplen resultado esperado y el criterio de aprobación del caso.
- **FAIL** = cualquier desviación de seguridad (cross-tenant), crash, o bloqueo del happy path.
- Anotar: device, build, companyId, userId, timestamps de docs creados.

---

## Criterio de release beta (gate)

La beta se considera **APROBADA** solo si:

1. Todos los casos **P0** de este documento están en **PASS**.
2. No hay lectura/escritura cross-company demostrable (P0 seguridad).
3. Happy path completo en **una misma company**: Login → Company activa → Cliente → Expediente → Documento.
4. Sin crash en los flujos listados; errores de red/permiso son visibles (no silenciosos).

Casos **P1** pueden quedar abiertos con waiver documentado; no bloquean piloto cerrado si P0 está verde.

---

## Matriz de cobertura

| Área | IDs | Prioridad dominante |
|------|-----|---------------------|
| Login | AT-LOGIN-01…04 | P0 |
| Company | AT-CO-01…04 | P0 |
| Roles | AT-ROLE-01…03 | P0 / P1 |
| Clients | AT-CL-01…03 | P0 |
| Records | AT-REC-01…03 | P0 |
| Documents | AT-DOC-01…03 | P0 |

---

## 1. Login

### AT-LOGIN-01 — Login válido → Home (P0)

**Objetivo:** Autenticación exitosa llega al hub único.

| Paso | Acción | Resultado esperado |
|------|--------|-------------------|
| 1 | Cold start de la app | Splash breve → pantalla Login (`SimpleLoginScreen`) |
| 2 | Ingresar email/password válidos de Usuario A | Sin crash; feedback de carga o éxito |
| 3 | Confirmar navegación | Destino **Home** (no Dashboard como hub; no FirebaseTest) |
| 4 | Revisar que no reaparezcan logins experimentales en el grafo beta | Solo flujo Splash → Login → (Register opcional) → Home |

**Criterio de aprobación:** Login OK y pantalla Home visible en ≤ 15 s con red normal. No hay auto-creación de admin hardcodeado.

---

### AT-LOGIN-02 — Credenciales inválidas (P0)

| Paso | Acción | Resultado esperado |
|------|--------|-------------------|
| 1 | En Login, email/password incorrectos | Mensaje de error visible |
| 2 | Confirmar navegación | Permanece en Login; **no** entra a Home |

**Criterio de aprobación:** Fallo autenticado sin crash; sin sesión company iniciada.

---

### AT-LOGIN-03 — Registro → Home (P0 / P1 si registro fuera de alcance piloto)

| Paso | Acción | Resultado esperado |
|------|--------|-------------------|
| 1 | Desde Login → Register | Formulario de registro usable |
| 2 | Crear usuario nuevo | Auth crea usuario; navega a Home |
| 3 | Observar gate de company | Home muestra loading o pane de empresa (crear/bind), no contenido tenant vacío “como si hubiera company” |

**Criterio de aprobación:** Registro no bypasea CompanySession; Home respeta `hasActiveCompany`.

---

### AT-LOGIN-04 — Logout (P0)

| Paso | Acción | Resultado esperado |
|------|--------|-------------------|
| 1 | En Home (con o sin company), Cerrar sesión | Vuelve a Login |
| 2 | Intentar volver atrás (si aplica) | No reabre datos de la sesión anterior sin re-auth |

**Criterio de aprobación:** Sesión Auth cerrada; company context limpio en el siguiente login.

---

## 2. Company

### AT-CO-01 — Sesión con membership ACTIVE (P0)

**Precondición:** Usuario A con `companies/{companyId}/memberships/{uid}` y `status == "ACTIVE"`.

| Paso | Acción | Resultado esperado |
|------|--------|-------------------|
| 1 | Login como Usuario A | Home carga |
| 2 | Esperar bind de company | `CompanySession` ready **con** company activa |
| 3 | Observar UI | Nombre de empresa visible en AppBar/hub; drawer/quick actions disponibles según permisos |

**Criterio de aprobación:** `hasActiveCompany == true`; usuario puede abrir Clientes / Expedientes / Documentos.

---

### AT-CO-02 — Sin membership ACTIVE → bloqueo hub (P0)

**Precondición:** Usuario B autenticado **sin** membership ACTIVE (o membership inexistente).

| Paso | Acción | Resultado esperado |
|------|--------|-------------------|
| 1 | Login como B | Auth OK |
| 2 | Observar Home | Contenido tenant **bloqueado**; mensaje tipo “Se requiere una empresa activa…” (o `errorMessage` de sesión) |
| 3 | Acciones disponibles | **Reintentar** y **Cerrar sesión** visibles |
| 4 | Intentar no hay bypass a Clientes/Docs vía UI normal | No se listan datos de otra company |

**Criterio de aprobación:** Sin company ACTIVE no hay acceso operativo a módulos platform. Fail-open de Auth **no** implica fail-open de tenant.

---

### AT-CO-03 — Bootstrap / create company (P0 si piloto crea empresa en app)

| Paso | Acción | Resultado esperado |
|------|--------|-------------------|
| 1 | Usuario nuevo sin company | Flujo de creación/bind dispara |
| 2 | Crear company | Doc `companies/{id}` + membership propia `ACTIVE` |
| 3 | Re-bind | Home desbloqueado con esa company |

**Criterio de aprobación:** Tras create, membership ACTIVE del creator; Storage/Firestore bajo ese `companyId` accesibles solo a miembros.

**Verificación Firebase (recomendada):**  
`companies/{id}/memberships/{uid}.status == "ACTIVE"`.

---

### AT-CO-04 — Aislamiento tenant (P0 seguridad)

| Paso | Acción | Resultado esperado |
|------|--------|-------------------|
| 1 | Como Usuario A, anotar `companyId` y crear un cliente de prueba | Cliente visible en lista de A |
| 2 | Login como Usuario B (otra company o sin membership) | No ve el cliente de A en UI |
| 3 | (Opcional QA) Intento lectura directa en consola/rules | Rules denegan lectura cross-tenant |

**Criterio de aprobación:** Cero filtración de datos entre companies en UI y rules.

---

## 3. Roles

### AT-ROLE-01 — Seed IAM en bindCompany (P0)

**Precondición:** Usuario A con company ACTIVE; primer bind o re-bind.

| Paso | Acción | Resultado esperado |
|------|--------|-------------------|
| 1 | Login + bind exitoso | Sin crash aunque seed falle parcialmente (warning log OK) |
| 2 | En Firestore (consola) | Existen docs de foundation: catálogo / roles base / role_permissions (create-only) |
| 3 | Assignment | Usuario tiene assignment con `roleCodes` del membership (fallback `ADMIN` si vacío) |

**Criterio de aprobación:** Seed idempotente (segundo login no rompe ni duplica de forma destructiva); assignment presente.

---

### AT-ROLE-02 — PermissionEngine filtra navegación (P0)

| Paso | Acción | Resultado esperado |
|------|--------|-------------------|
| 1 | Login con rol que tenga permisos clients/documents | Entradas **Clientes** / **Documentos** (y Expedientes si expuesto) visibles en drawer/quick actions |
| 2 | (Si hay fixture) Usuario con rol restringido sin permiso clients | Entrada Clientes **no** visible o no navegable según motor |

**Criterio de aprobación:** Nav hub respeta `NavPermissionFactory` / PermissionEngine; no se muestran acciones admin a roles sin permiso.

**Nota beta:** Si el piloto solo usa `ADMIN`, documentar como “PASS con rol único” y marcar AT-ROLE-02b (rol restringido) como P1 diferido.

---

### AT-ROLE-03 — Legacy role bridge (P1)

| Paso | Acción | Resultado esperado |
|------|--------|-------------------|
| 1 | Usuario con `UserRole` legacy y membership sin `roleCodes` ricos | Hub aún construye motor vía `LegacyRoleBridge` |
| 2 | Navegación | No crash; permisos coherentes con bridge |

**Criterio de aprobación:** Compatibilidad legacy sin romper Home.

---

## 4. Clients

### AT-CL-01 — Abrir módulo desde Home (P0)

| Paso | Acción | Resultado esperado |
|------|--------|-------------------|
| 1 | Home con company ACTIVE | Entrada **Clientes** visible |
| 2 | Abrir Clientes | `PlatformClientsScreen`; título refleja contexto / nombre company si aplica |
| 3 | Volver | Regresa a Home sin crash |

**Criterio de aprobación:** Ruta `platform_clients` alcanzable solo con sesión company válida.

---

### AT-CL-02 — Crear cliente (P0)

| Paso | Acción | Resultado esperado |
|------|--------|-------------------|
| 1 | FAB / Nuevo cliente | Formulario (nombre, etc.) |
| 2 | Guardar con nombre válido | Mensaje de éxito o cliente aparece en lista |
| 3 | Pull / reabrir pantalla | Cliente persiste |
| 4 | Firestore | Doc bajo `companies/{companyId}/clients/{id}` con `companyId` correcto |

**Criterio de aprobación:** Cliente creado **dentro** de la company activa; visible en lista del mismo usuario.

---

### AT-CL-03 — Validación / error (P1)

| Paso | Acción | Resultado esperado |
|------|--------|-------------------|
| 1 | Intentar crear con nombre vacío | Botón deshabilitado o error; no se escribe basura |
| 2 | Sin red (opcional) | Error visible; UI no queda en loading infinito |

**Criterio de aprobación:** Fallos no silenciosos.

---

## 5. Records (Expedientes)

### AT-REC-01 — Abrir módulo (P0)

| Paso | Acción | Resultado esperado |
|------|--------|-------------------|
| 1 | Home → **Expedientes** | `PlatformRecordsScreen` |
| 2 | Lista | Carga records de la company (vacía OK al inicio) |

**Criterio de aprobación:** Ruta `platform_records` con bind al `companyId` activo.

---

### AT-REC-02 — Crear expediente ligado a cliente (P0)

**Precondición:** Al menos un cliente en la company (AT-CL-02).

| Paso | Acción | Resultado esperado |
|------|--------|-------------------|
| 1 | FAB Nuevo expediente | Formulario: selector de cliente + título (+ resumen) |
| 2 | Elegir cliente de la lista | Cliente seleccionado indicado en UI |
| 3 | Título válido → Crear | Expediente aparece en lista |
| 4 | Firestore | Doc en `companies/{companyId}/records/{id}` con `clientId` del cliente elegido |

**Criterio de aprobación:** Expediente creado y asociado al cliente correcto **de la misma company**.

---

### AT-REC-03 — Sin clientes previos (P1 UX)

| Paso | Acción | Resultado esperado |
|------|--------|-------------------|
| 1 | Company sin clientes → Expedientes → crear | Aviso tipo “Crea un cliente primero…” |
| 2 | Crear deshabilitado sin cliente/título | No se puede guardar expediente huérfano inválido |

**Criterio de aprobación:** UX guía al flujo Clientes → Records.

---

## 6. Documents

### AT-DOC-01 — Abrir módulo y listar (P0)

| Paso | Acción | Resultado esperado |
|------|--------|-------------------|
| 1 | Home → **Documentos** | `PlatformDocumentsScreen` |
| 2 | Lista | Documentos de la company (vacía OK) |

**Criterio de aprobación:** Bind a company activa; sin datos de otro tenant.

---

### AT-DOC-02 — Subir documento (P0)

| Paso | Acción | Resultado esperado |
|------|--------|-------------------|
| 1 | FAB subir | Abre picker (`OpenDocument`) |
| 2 | Elegir PDF o imagen pequeña | Indicador uploading; luego mensaje de éxito |
| 3 | Lista | Documento aparece con nombre |
| 4 | Storage | Objeto bajo path tenant (`companies/{companyId}/…` / `DocumentStoragePaths`) |
| 5 | Firestore | Metadata registrada (documento + versión) bajo la company |

**Criterio de aprobación:** Bytes en Storage **y** metadata en Firestore; ambos scoped al `companyId` activo. Sin `PERMISSION_DENIED` para miembro ACTIVE.

---

### AT-DOC-03 — Fallo de upload (P1)

| Paso | Acción | Resultado esperado |
|------|--------|-------------------|
| 1 | Cancelar picker | No crash; sin doc fantasma |
| 2 | Usuario sin membership (si alcanzable) | Upload denegado / error visible |
| 3 | Archivo ilegible / vacío | Error claro; no metadata huérfana preferible |

**Criterio de aprobación:** Errores visibles; no estado inconsistente grave (metadata sin archivo) en happy path.

---

## 7. Happy path E2E (P0 — obligatorio)

### AT-E2E-01 — Cliente → Expediente → Documento en una company

| Paso | Acción | Resultado esperado |
|------|--------|-------------------|
| 1 | Login Usuario A | Home + company ACTIVE |
| 2 | Crear cliente “QA Beta Client” | Visible en Clientes |
| 3 | Crear expediente “QA Beta Record” ligado a ese cliente | Visible en Expedientes |
| 4 | Subir documento de prueba | Visible en Documentos; Storage OK |
| 5 | Logout / login de nuevo | Los tres recursos siguen visibles solo para A en esa company |
| 6 | Usuario B | No ve los tres recursos |

**Criterio de aprobación:** Flujo completo PASS en una sesión; persistencia post re-login; aislamiento vs B.

---

## 8. Plantilla de evidencias

Para cada caso P0 FAIL o para el gate E2E, adjuntar:

```
Caso: AT-XXX-XX
Resultado: PASS | FAIL | BLOCKED
Build / device:
companyId / userId:
Notas:
Screenshots / video:
Firestore path verificado:
Storage path verificado:
```

---

## 9. Checklist de firma QA

| Gate | PASS? | Firma / fecha |
|------|-------|---------------|
| Login P0 | ☐ | |
| Company P0 (incl. aislamiento) | ☐ | |
| Roles P0 (seed + nav) | ☐ | |
| Clients P0 | ☐ | |
| Records P0 | ☐ | |
| Documents P0 | ☐ | |
| E2E AT-E2E-01 | ☐ | |
| **BETA APROBADA** | ☐ | |

---

*Documento de aceptación manual. No sustituye tests instrumentados; define el mínimo verificable para piloto cerrado post-S2.*
