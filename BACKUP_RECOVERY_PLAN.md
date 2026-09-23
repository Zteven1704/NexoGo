# BACKUP & RECOVERY FOUNDATION — Plan de diseño

**Rol:** Product / Platform Architect  
**Fecha:** 2026-09-20  
**Estado:** **Solo diseño** — no implementar todavía  
**Contexto:** post Company Onboarding + User Management v1; tenants bajo `companies/{companyId}`  
**Complementa:** `DOCUMENT_MANAGEMENT.md`, `MULTITENANT_ARCHITECTURE.md`, `USER_MANAGEMENT_REPORT.md`, `BETA_HARDENING_REPORT.md`

---

## 1. Objetivo

Dar a cada **company** una base confiable para:

1. **Exportar** datos críticos de negocio (clientes, expedientes, metadata de documentos).
2. **Preparar** destinos de respaldo: **Google Drive** y **almacenamiento externo** (USB / carpeta del dispositivo / SAF).
3. Dejar el camino abierto a **recovery** (import / restore) en fases posteriores, sin acoplar aún billing ni multi-región.

**Fuera de v1 foundation (explícito):**

- Restore completo automático a Firestore/Storage  
- Backup de bytes de archivos (PDF/imágenes) en el primer corte — solo **metadata** de documentos  
- Backup de chat, CRM, AI, audit logs (fase 2+)  
- Snapshots server-side programados (Cloud Functions / GCS) — diseño preparado, no requerido para foundation

---

## 2. Principios

| # | Principio | Implicación |
|---|-----------|-------------|
| 1 | Tenant-first | Todo export lleva `companyId`, `exportedAt`, `schemaVersion` |
| 2 | Metadata ≠ bytes | Documentos: exportar Firestore metadata (+ `storagePath`); bytes = fase “Media backup” |
| 3 | Portableidad | Formatos abiertos: **JSON** (canónico) + **CSV** (legible humano para clientes/expedientes) |
| 4 | Destinos enchufables | Un `BackupSink` abstracto; Drive y External son implementaciones |
| 5 | Permisos explícitos | Solo ADMIN (PermissionEngine) + membership ACTIVE |
| 6 | No secretos en el paquete | Sin API keys; tokens OAuth de Drive en Credential Manager / Account |
| 7 | Idempotencia de diseño | Exports son snapshots read-only; no mutan datos de origen |
| 8 | Recovery desacoplado | Export hoy; import/restore es módulo hermano con validación estricta |

---

## 3. Alcance de datos (Foundation)

### 3.1 Incluido

| Dominio | Fuente Firestore | Contenido export |
|---------|------------------|------------------|
| **Clientes** | `companies/{id}/clients` (+ opcional `contacts`) | Perfil, tipo, emails/phones, status, searchTokens, timestamps |
| **Expedientes** | `companies/{id}/records` | title, type, status, clientId, summary, sections metadata, industryPayload, timestamps |
| **Documentos (metadata)** | `companies/{id}/documents` (+ opcional `versions`, `document_links`) | name, mime, size, storagePath, folderId, categoryId, fileFamily, versionActual, links a client/record |

### 3.2 Excluido en foundation (documentar en manifiesto)

- Objetos binarios en Firebase Storage (`DocumentStoragePaths`)  
- Memberships / invites / role_assignments (salvo export admin “IAM pack” futuro)  
- Settings, branding, company root (opcional en “Company pack” fase 2)  
- Módulos legacy no tenant (`usuarios`, clinical legacy)

### 3.3 Manifiesto del paquete

Todo backup genera un archivo raíz:

```json
{
  "schemaVersion": "1.0",
  "product": "NexoGo",
  "companyId": "cmp_…",
  "companyName": "…",
  "exportedAt": "2026-09-20T23:00:00Z",
  "exportedBy": "uid_…",
  "modules": ["clients", "records", "documents_metadata"],
  "counts": { "clients": 12, "records": 40, "documents": 8 },
  "includesFileBytes": false,
  "sink": "local_saf" | "google_drive" | "unknown",
  "checksum": { "algo": "sha256", "value": "…" }
}
```

---

## 4. Arquitectura propuesta (sin implementar)

```
platform/backup/
  model/
    BackupManifest.kt
    BackupModule.kt          // CLIENTS | RECORDS | DOCUMENTS_META
    BackupPackage.kt         // in-memory / streaming descriptor
  export/
    ClientExportEncoder      // → clients.json + clients.csv
    RecordExportEncoder
    DocumentMetaExportEncoder
    BackupPackageBuilder     // orquesta lectura tenant-scoped
  sink/
    BackupSink               // interface
    ExternalStorageSink      // SAF / MediaStore / app-specific external
    GoogleDriveSink          // Drive API v3 (prep)
    LocalCacheSink           // staging en cacheDir antes de subir
  ui/                        // fase implementación
    BackupCenterScreen
  permission/
    BackupPermissionGate     // PermissionEngine + ADMIN
```

**Dependencias internas existentes (solo lectura):**

- `ClientRepository` / `RecordRepository` / `DocumentRepository`  
- `CompanySessionManager` / `TenantContext` / `TenantIsolationGuard`  
- `NavPermissionFactory.showAdmin` / `PermissionEngine`

**No** escribir en repos de negocio durante export.

---

## 5. Formatos de export

### 5.1 Paquete ZIP (recomendado)

```
nexogo-backup-{companyId}-{yyyyMMdd-HHmm}.zip
├── manifest.json
├── clients/
│   ├── clients.json
│   └── clients.csv
├── records/
│   ├── records.json
│   └── records.csv
└── documents/
    ├── documents.json
    ├── versions.json          # opcional
    └── links.json             # opcional
```

### 5.2 JSON canónico

- Array de objetos alineados a modelos actuales (`Client`, `Record`, `Document`)  
- Campos desconocidos en restore futuro → `extensions` / ignore  
- `schemaVersion` por archivo módulo: `"clients/1"`

### 5.3 CSV (human / Excel)

| Módulo | Columnas mínimas |
|--------|------------------|
| Clients | id, displayName, clientType, status, emails, phones, createdAt |
| Records | id, title, recordType, status, clientId, summary, updatedAt |
| Documents | id, name, mimeType, size, storagePath, folderId, updatedAt |

CSV **no** es fuente de verdad para restore; solo conveniencia.

---

## 6. Destinos

### 6.1 Almacenamiento externo (prioridad foundation)

**Objetivo:** usuario elige carpeta/archivo vía **Storage Access Framework (SAF)**.

| Capacidad | Diseño |
|-----------|--------|
| Export | `ActivityResultContracts.CreateDocument` → `application/zip` |
| Permisos | Sin `WRITE_EXTERNAL_STORAGE` legacy en API 29+; SAF suficiente |
| Staging | Construir ZIP en `cacheDir` / `filesDir/backups/` luego copiar a Uri |
| Retención local | LRU de últimos N ZIPs en app storage (configurable, default 3) |
| UX | “Guardar en Descargas / USB / Files” |

**Preparación técnica (checklist implementación futura):**

- [ ] `BackupSink.write(package: BackupPackage, target: Uri)`  
- [ ] Progress callback (lectura Firestore paginada)  
- [ ] Validar espacio libre estimado antes de ZIP  
- [ ] Mostrar path/nombre final al usuario  

### 6.2 Google Drive (preparación, no go-live)

**Objetivo:** mismo paquete ZIP → carpeta Drive de la company o del admin.

| Tema | Decisión de diseño |
|------|-------------------|
| API | Google Drive API v3 + Google Sign-In / Identity services |
| Scope | `drive.file` (solo archivos creados por la app) — **preferido** vs `drive` full |
| Carpeta | `NexoGo Backups/{companyName}/` creada por la app |
| Auth | Cuenta Google del **usuario admin**; no service account en cliente |
| Offline | Si no hay red → solo sink externo; Drive en cola (`WorkManager`) |
| Cumplimiento | Aviso: el backup sale del tenant Firebase hacia Google del usuario |

**Preparación (sin implementar aún):**

```
platform/backup/sink/drive/
  DriveBackupConfig.kt       // folder name, mime, appData vs My Drive
  DriveAuthGateway.kt        // interface; stub + real later
  DriveBackupSink.kt         // upload resumable para ZIP grandes
```

**Dependencias Gradle (cuando se implemente):** Play Services Auth, Drive API client o REST con token.  
**Play Console / Cloud Console:** OAuth client Android, pantalla de consentimiento, scopes restringidos.

**No** embeber refresh tokens en Firestore.

### 6.3 Matriz de sinks

| Sink | Foundation design | Implementación sugerida |
|------|-------------------|-------------------------|
| External (SAF) | ✅ obligatorio v1 | Sprint backup-1 |
| App cache / Files | ✅ staging | backup-1 |
| Google Drive | ✅ interfaz + config | backup-2 |
| Email attachment | ❌ no (tamaño) | — |
| S3 / GCS server | ⏳ fase server | backup-3 ops |

---

## 7. Seguridad y permisos

| Control | Regla |
|---------|--------|
| Quién exporta | `PermissionEngine` + `showAdmin` / futuro `backup.export` |
| Tenant | Solo `CompanySession.activeCompanyId`; repos con `TenantIsolationGuard` |
| Auditoría | `AuditLogger` event `BACKUP_EXPORTED` (companyId, modules, sink, checksum) |
| Datos sensibles | Records pueden ser SENSITIVE — banner de advertencia antes de export |
| Compartir ZIP | Responsabilidad del admin; marcar archivo como confidencial en UI |
| Drive | Scope mínimo `drive.file`; no pedir Drive full |

**Firestore rules:** export es lectura ya permitida a miembros; no requiere rules nuevas para foundation.  
**Opcional futuro:** claim `backup:export` o solo ADMIN en rules custom.

---

## 8. Recovery (diseño adelantado, no foundation build)

### 8.1 Niveles

| Nivel | Descripción | Fase |
|-------|-------------|------|
| R0 | Export only (este plan) | Foundation |
| R1 | Import JSON a **nueva** company (dry-run + apply) | Recovery v1 |
| R2 | Merge a company existente (dedupe por id / externalKey) | Recovery v2 |
| R3 | Rehidratación de bytes desde Drive/SAF usando `storagePath` map | Media recovery |
| R4 | Point-in-time server snapshots | Ops |

### 8.2 Garantías R1 (cuando se implemente)

- Validar `manifest.schemaVersion` y `companyId` (o remap)  
- Transacciones por lote (batches Firestore 500)  
- No sobrescribir sin flag `overwrite=true`  
- Reportar conflictos (ids existentes)  

---

## 9. UX (futuro)

**Backup Center** (ruta sugerida: `platform_backup`):

1. Selección de módulos (clients / records / documents meta) — default todos  
2. Destino: “Archivo en dispositivo” | “Google Drive” (Drive disabled hasta OAuth listo)  
3. Progress + resumen counts  
4. Historial local de exports (nombre, fecha, checksum, sink)  
5. Deep link “Compartir” el ZIP  

Entrada: Admin hub (junto a User Management), gated ADMIN.

---

## 10. No funcionales

| Métrica | Target foundation |
|---------|-------------------|
| Clientes ≤ 5k / records ≤ 10k | Export en &lt; 2 min en red normal (paginado) |
| Memoria | Streaming / paginación; no cargar toda la company en RAM |
| ZIP size metadata-only | Típicamente &lt; 20 MB → SAF y Drive OK |
| Cuando `includesFileBytes=true` (futuro) | WorkManager + upload resumable obligatorio |

---

## 11. Fases de entrega (roadmap)

| Fase | Entrega | Implementar |
|------|---------|-------------|
| **F0 — este doc** | Plan + contratos | ❌ código |
| **F1 — Export core** | Encoders + ZIP + SAF sink + Backup Center mínimo + audit | Sí |
| **F2 — Drive prep→live** | OAuth `drive.file` + DriveBackupSink + cola offline | Sí |
| **F3 — Recovery R1** | Import a company nueva + dry-run | Sí |
| **F4 — Media backup** | Opción “incluir archivos” (Storage download → ZIP) | Sí |
| **F5 — Server snapshots** | Cloud Scheduler → GCS por tenant (ops) | Opcional |

---

## 12. Contratos de interfaz (borrador)

```kotlin
enum class BackupModule { CLIENTS, RECORDS, DOCUMENTS_METADATA }

interface BackupSink {
    suspend fun publish(
        companyId: String,
        zipBytesOrFile: BackupArtifact,
        manifest: BackupManifest
    ): Result<BackupPublishResult>
}

data class BackupPublishResult(
    val sinkId: String,
    val displayLocation: String, // path or Drive file id
    val checksumSha256: String
)

interface BackupPackageBuilder {
    suspend fun build(
        companyId: String,
        modules: Set<BackupModule>,
        exportedBy: String
    ): Result<BackupArtifact>
}
```

---

## 13. Riesgos y mitigaciones

| Riesgo | Mitigación |
|--------|------------|
| Export incompleto por índice / timeout | Paginación + reintento por colección |
| Admin exporta a Drive personal y pierde acceso | Documentar ownership; carpeta Shared Drive futuro |
| CSV pierde Unicode / comas | UTF-8 BOM + quoting RFC 4180 |
| Confusion metadata vs archivos | `includesFileBytes=false` visible en UI y manifiesto |
| Invitee / EMPLOYEE intenta export | Gate ADMIN + ocultar nav |
| ZIP corrupto | SHA-256 en manifiesto + verificación post-write |

---

## 14. Criterios de aceptación (cuando se implemente F1)

1. ADMIN de company ACTIVE exporta clients + records + documents metadata a ZIP vía SAF.  
2. `manifest.json` presente con counts coherentes.  
3. CSV y JSON abren sin error en al menos un visor estándar.  
4. EMPLOYEE no ve / no puede lanzar export.  
5. Evento de auditoría registrado.  
6. Google Drive aparece en UI como “Próximamente” o stub deshabilitado hasta F2.  

---

## 15. Decisión explícita

| Pregunta | Respuesta |
|----------|-----------|
| ¿Implementar ahora? | **No** |
| ¿Formato canónico? | ZIP + JSON (+ CSV derivado) |
| ¿Documentos? | **Solo metadata** en foundation |
| ¿Drive? | Interfaz + diseño OAuth; implementación F2 |
| ¿Externo? | SAF como sink primario F1 |
| ¿Restore? | Diseñado (R1+); no en foundation build |

---

*Documento de fundación Backup & Recovery. No modifica código ni dependencias.*
