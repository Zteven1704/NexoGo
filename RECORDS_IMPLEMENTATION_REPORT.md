# Records Module Implementation Report (v1)

**Fecha:** 2026-09-18  
**Alcance:** Expedientes universales (Records) multiempresa  
**Compilación:** `:app:compileDebugKotlin --rerun-tasks` — **BUILD SUCCESSFUL**  
**Legacy:** `clinical_records` / `medical_records` / History UI **no migrados ni reutilizados**

---

## Objetivo

Implementar el módulo **Expedientes** v1 como contenedor universal de casos, capaz de cubrir historias clínicas, contratos, facturas, informes y documentación técnica — siempre bajo Company + Client.

---

## Entregables

| Entregable | Ubicación | Estado |
|------------|-----------|--------|
| **RecordModel** | `platform/records/model/RecordModels.kt` (`typealias RecordModel = Record`) | ✅ |
| **RecordRepository** | `platform/records/data/RecordRepository.kt` | ✅ |
| **RecordViewModel** | `platform/records/viewmodel/RecordViewModel.kt` | ✅ |

---

## Tipos de expediente

| `RecordType` | Uso |
|--------------|-----|
| `CLINICAL_HISTORY` | Historias clínicas |
| `CONTRACT` | Contratos (vigencia, parties) |
| `INVOICE` | Facturas / comprobantes de expediente (`saleId` opcional; no reemplaza Ventas) |
| `REPORT` | Informes |
| `TECHNICAL_DOC` | Documentación técnica |
| `ATTACHMENT_PACK` / `NOTE` / `CUSTOM` | Preparados |

Ciclo de vida: `DRAFT` → `FINAL` → `ARCHIVED`  
Confidencialidad: `STANDARD` | `SENSITIVE` | `LEGAL_HOLD`

Bytes de archivo: solo referencias (`documentIds` / `primaryDocumentId`) — DMS Documentos pendiente.

---

## Multiempresa

- `Record` implementa `TenantAwareEntity` (`companyId` obligatorio).
- `clientId` obligatorio (módulo Clients, no Patient legacy).
- Path: `companies/{companyId}/records/{recordId}`.
- `RecordRepository` extiende `TenantAwareRepository` + `TenantIsolationGuard`.
- `RecordViewModel` exige empresa activa; sin Company → error explícito.

---

## API principal

### Repository

- CRUD: `createRecord` / `updateRecord` / `getRecord` / `listRecords` / `searchRecords`
- Lifecycle: `finalizeRecord` / `archiveRecord`
- Docs: `attachDocumentId` (referencia, no upload)
- Realtime: `listenRecords(companyId, clientId?)`

### ViewModel

- `bindCompany` / `loadRecords` / `observeRecords`
- Factories: `createClinicalHistory` · `createContract` · `createInvoice` · `createReport` · `createTechnicalDoc`
- `saveRecord` / `finalizeRecord` / `archiveRecord` / `attachDocument`
- Filtros por tipo / cliente + búsqueda
- **Sin** cableado a NavGraph todavía

### Factories

`RecordFactories` + `RecordPayloadKeys` (anamnesis, folio, findings, …)

---

## Separación vs legacy

| Legacy (intactos) | Nuevo módulo |
|-------------------|--------------|
| `clinical_records` / `medical_records` | `companies/{id}/records` |
| History / ClinicalRecord ViewModels | `RecordViewModel` |
| Modelo solo clínico | Tipos universales + `industryPayload` |

---

## Archivos nuevos

```
app/src/main/java/com/example/nexogo/platform/records/
  model/RecordModels.kt
  data/RecordRepository.kt
  viewmodel/RecordViewModel.kt
```

---

## Compilación

```bash
bash ./gradlew :app:compileDebugKotlin --no-daemon --rerun-tasks
# BUILD SUCCESSFUL
```

---

## Fuera de alcance (siguiente)

- Pantallas Compose / ruta `records`
- Unificar UIs History/Clinical/Medical → Records
- Módulo Documentos (bytes + versiones)
- Migración de datos legacy → `records`
- Security Rules tenant-scoped

---

## Veredicto

**Records Module v1 completo y compilando.** Expedientes universales multiempresa listos; legacy clínico sin tocar.
