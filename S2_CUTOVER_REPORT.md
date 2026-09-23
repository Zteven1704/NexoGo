# S2 Cutover Report

**Sprint:** S2 Cutover (`TOP_20_NEXT_ACTIONS.md` #10, #11, #12, #13)  
**Fecha:** 2026-09-19  
**Compilación:** `:app:compileDebugKotlin` — **BUILD SUCCESSFUL**  
**Objetivo:** Un usuario autenticado con company activa puede crear cliente, crear expediente y subir documento dentro del tenant.

---

## Resumen

| # | Acción | Estado |
|---|--------|--------|
| 10 | Exponer Clients en navegación | ✅ |
| 11 | Exponer Records en navegación | ✅ |
| 12 | Completar uploads de Documents | ✅ |
| 13 | Crear `firestore.indexes.json` | ✅ |

---

## #10 Clients en navegación

- Ruta: `Screen.PlatformClients` → `platform_clients`
- UI: `platform/clients/ui/PlatformClientsScreen.kt` (lista + crear cliente)
- Cableado: `NavGraph` composable + callbacks desde `HomeScreen` (drawer y quick actions)
- Scope: `ClientViewModel.bindCompany(companyId)` vía `LocalActiveCompany` / `CompanySessionManager`

---

## #11 Records en navegación

- Ruta: `Screen.PlatformRecords` → `platform_records`
- UI: `platform/records/ui/PlatformRecordsScreen.kt` (lista + crear expediente ligado a cliente)
- Picker de cliente simplificado (lista clickable; sin ExposedDropdownMenu API frágil)
- Scope: `RecordViewModel` + `ClientViewModel` en la misma company activa

---

## #12 Uploads de Documents

- Repo: `DocumentRepository.uploadAndRegister` — `putBytes` a Storage tenant + `registerUpload` (+ link opcional)
- VM: `DocumentViewModel.uploadFromUri` lee bytes del `Uri` y llama al repo
- UI: `PlatformDocumentsScreen` con `OpenDocument` picker + FAB subir
- Ruta: `Screen.PlatformDocuments` → `platform_documents`

---

## #13 firestore.indexes.json

Archivo en raíz del repo con índices compuestos para:

- `clients` (companyId + clientType / status)
- `records` (companyId + clientId / recordType)
- `documents` (companyId + folderId / categoryId / fileFamily)
- `document_links`, `notifications`, `tasks`, `audit_logs`, CRM (`leads` / `opportunities`)

Deploy: `firebase deploy --only firestore:indexes` cuando el proyecto Firebase esté enlazado.

---

## Flujo E2E (tenant)

1. Login → hub Home con company ACTIVE (S1).
2. **Clientes** → crear cliente.
3. **Expedientes** → elegir cliente → crear expediente.
4. **Documentos** → seleccionar archivo → upload Storage + metadata Firestore bajo `companies/{companyId}/…`.

---

## Archivos principales

```
navigation/Screen.kt
navigation/NavGraph.kt
ui/screens/home/HomeScreen.kt
platform/clients/ui/PlatformClientsScreen.kt
platform/records/ui/PlatformRecordsScreen.kt
platform/documents/ui/PlatformDocumentsScreen.kt
platform/documents/viewmodel/DocumentViewModel.kt
platform/documents/data/DocumentRepository.kt
firestore.indexes.json
```

---

## Notas

- Compat: módulos `modules/*` / pantallas legacy no eliminados; cutover expone `platform.*` en el hub.
- Upload usa `putBytes` (equivalente práctico a `putFile` para contenido leído desde content URI).
- Compile warnings residuales: iconos AutoMirrored deprecados en `HomeScreen` (no bloquean).
