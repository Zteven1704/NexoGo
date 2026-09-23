# Clients Module Implementation Report (v1)

**Fecha:** 2026-09-18  
**Alcance:** Módulo limpio Clients (multiempresa)  
**Compilación:** `:app:compileDebugKotlin --rerun-tasks` — **BUILD SUCCESSFUL**  
**Legacy:** `Patient` / `SimplePatient` / `PatientViewModel` **no reutilizados ni modificados**

---

## Objetivo

Crear el módulo **Clientes** v1, genérico y multiindustria, con aislamiento obligatorio por Company — sin migrar ni acoplar el dominio Pacientes legacy.

---

## Entregables

| Entregable | Ubicación | Estado |
|------------|-----------|--------|
| **ClientModel** | `platform/clients/model/ClientModels.kt` (`typealias ClientModel = Client`) | ✅ |
| **ClientRepository** | `platform/clients/data/ClientRepository.kt` | ✅ |
| **ClientViewModel** | `platform/clients/viewmodel/ClientViewModel.kt` | ✅ |
| Contactos (propietarios) | `ClientContact` + APIs en repository | ✅ |

---

## Diseño multiindustria

| Caso | `ClientType` | Cómo se modela |
|------|--------------|----------------|
| **Personas** / pacientes humanos | `PERSON` | Client + `profile` (documentId, allergies, …) |
| **Empresas** | `ORG` | Client + `taxId` + profile (accountCode, legalName, …) |
| **Pacientes (vet)** = mascotas | `PET` | Client sujeto de servicio; species/breed en `profile` |
| **Propietarios de mascotas** | — | `ClientContact` (`OWNER` / `GUARDIAN`) vinculado vía `clientIds` |

Tipos adicionales preparados: `ASSET`, `CUSTOM`.

Principio: el core **no** tiene campos fijos `species`/`breed`; viven en `profile` (pack veterinary).

---

## Multiempresa

- `Client` y `ClientContact` implementan `TenantAwareEntity` (`companyId` obligatorio).
- Persistencia: `companies/{companyId}/clients/{id}` y `…/contacts/{id}`.
- `ClientRepository` extiende `TenantAwareRepository` + `TenantIsolationGuard`.
- `ClientViewModel` exige empresa activa (`TenantContext` / `CompanySessionManager`); sin company → error explícito.

---

## API principal

### Repository

- Clients: `create` / `update` / `get` / `list` / `search` / `archive` / `listenClients`
- Contacts: `create` / `update` / `get` / `list` / `listContactsForClient`
- `createPetWithOwner(...)` — PET + OWNER en una operación

### ViewModel

- `bindCompany` / `loadClients` / `observeClients`
- `createPerson` / `createOrganization` / `createPetWithOwner`
- `saveClient` / `archiveClient` / `selectClient` / filtros y búsqueda
- `ClientsUiState` para UI futura (aún **no** cableado a NavGraph)

### Factories

`ClientFactories.person` · `organization` · `pet` · `petOwnerContact`

---

## Separación limpia vs legacy

| Legacy (intactos) | Nuevo módulo |
|-------------------|--------------|
| `core.models.Patient` / `SimplePatient` | `platform.clients.model.Client` |
| `PatientViewModel` / DataStore | `ClientViewModel` + Firestore tenant |
| Ruta UI `patients` | Sin pantallas nuevas en v1 |

No hay imports del modelo Patient en el paquete `platform.clients`.

---

## Archivos nuevos

```
app/src/main/java/com/example/nexogo/platform/clients/
  model/ClientModels.kt
  data/ClientRepository.kt
  viewmodel/ClientViewModel.kt
```

---

## Compilación

```bash
bash ./gradlew :app:compileDebugKotlin --no-daemon --rerun-tasks
# BUILD SUCCESSFUL
```

---

## Fuera de alcance (siguiente)

- Pantallas Compose / ruta `clients` en NavGraph
- Reemplazar Home “Pacientes” → “Clientes”
- Migración de datos DataStore → `companies/{id}/clients`
- Re-apuntar citas / expedientes a `clientId`
- Security Rules por tenant

---

## Veredicto

**Clients Module v1 completo y compilando.** Modelo limpio multiindustria, repository multiempresa y ViewModel listos; legacy de pacientes sin tocar.
