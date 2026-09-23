# Document Management Module Report (v1)

**Fecha:** 2026-09-18  
**Alcance:** Repositorio documental empresarial multiempresa  
**Compilación:** `:app:compileDebugKotlin --rerun-tasks` — **BUILD SUCCESSFUL**  
**IA / OCR:** No implementada (por diseño)

---

## Objetivo

Permitir almacenar y organizar documentos empresariales (PDF, Word, Excel, imágenes) con carpetas, categorías, etiquetas y vínculos a Company / Client / Record / Employee — metadatos en Firestore, bytes preparados para Firebase Storage.

---

## Entregables

| Entregable | Ubicación | Estado |
|------------|-----------|--------|
| **DocumentModel** | `platform/documents/model/DocumentModels.kt` | ✅ |
| **DocumentCategory** | mismo archivo | ✅ |
| **DocumentFolder** | mismo archivo | ✅ |
| **DocumentTag** | mismo archivo | ✅ |
| **DocumentRepository** | `platform/documents/data/DocumentRepository.kt` | ✅ |
| Storage prep | `platform/documents/storage/DocumentStoragePaths.kt` | ✅ |
| Extras v1 | `DocumentVersion`, `DocumentLink`, MIME helpers | ✅ |

---

## Tipos de archivo

| Familia | Extensiones / MIME |
|---------|-------------------|
| **PDF** | `.pdf` |
| **Word** | `.doc`, `.docx` |
| **Excel** | `.xls`, `.xlsx`, `.csv` |
| **Imágenes** | `.jpg`, `.png`, `.webp`, `.gif`, `.heic`, … |

`DocumentMimeTypes` deriva `fileFamily`; uploads de tipo `OTHER` se rechazan en `registerUpload`.

---

## Multiempresa & Storage

- Todas las entidades implementan `TenantAwareEntity` (`companyId` obligatorio).
- Firestore:
  - `companies/{companyId}/documents/{id}` (+ subcolección `versions`)
  - `…/document_links/{id}`
  - `…/folders/{id}`
  - `…/document_tags/{id}`
  - `…/document_categories/{id}`
- Storage path canónico:
  - `companies/{companyId}/documents/{documentId}/v{n}/{fileName}`
  - thumbs / tmp vía `DocumentStoragePaths`
- Flujo v1: `prepareUploadPath` → (caller putFile) → `registerUpload` / `addVersion`

---

## Relaciones

| `DocumentEntityType` | Uso |
|----------------------|-----|
| `COMPANY` | Docs de la empresa |
| `CLIENT` | INE, consentimientos, fotos |
| `RECORD` | Adjuntos de expediente |
| `EMPLOYEE` | Legajo / HR (`entityId` = uid membership) |
| `SALE` | Preparado (comprobantes) |

`DocumentLink` + `relation` (`PRIMARY`, `ATTACHMENT`, `EVIDENCE`, `CONTRACT`, `IDENTITY`, …).

---

## Organización

| Pieza | Rol |
|-------|-----|
| **DocumentFolder** | Árbol; `contextType` LIBRARY / CLIENT / RECORD / EMPLOYEE / SALE / ORG |
| **DocumentCategory** | Taxonomía de biblioteca (código, padre, color) |
| **DocumentTag** | Multi-etiqueta flexible (`LEGAL`, `CLINICAL`, `FINANCE`, `HR`, `GENERAL`) |

---

## API repository (resumen)

- Docs: `prepareUploadPath`, `registerUpload`, `addVersion`, `list` / `search` / `archive`, `listVersions`
- Links: `linkDocument`, `listLinksForEntity`, `listLinksForDocument`, `unlink`
- Categories / Folders / Tags: create / list / update (+ `ensureContextFolder`)

Sin UI Compose / sin NavGraph / sin IA.

---

## Archivos

```
app/src/main/java/com/example/nexogo/platform/documents/
  model/DocumentModels.kt
  data/DocumentRepository.kt
  storage/DocumentStoragePaths.kt
```

**Actualizado:** `platform/tenant/data/TenantCollections.kt` (folders, tags, categories, links, versions)

---

## Compilación

```bash
bash ./gradlew :app:compileDebugKotlin --no-daemon --rerun-tasks
# BUILD SUCCESSFUL
```

---

## Fuera de alcance (siguiente)

- Upload UI + `FirebaseStorage.putFile`
- Preview PDF/imágenes
- ViewModel / pantallas de biblioteca
- OCR / IA / full-text
- Security Rules Storage + Firestore
- Virus scan / cuotas enforcement

---

## Veredicto

**Document Management Module v1 completo y compilando.** Metadatos, taxonomía, vínculos y paths Storage listos; sin IA.
