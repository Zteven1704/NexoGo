# NexoGo Platform — Records Module Migration

> **Migración: Historial Clínico → Expedientes (Records).**  
> Fecha: 2026-09-18  
> **No incluye código.**  
>  
> Fuentes: `MASTER_ARCHITECTURE.md` · `BUSINESS_PLATFORM_ARCHITECTURE.md` · `MODULE_DESIGN.md` ·  
> `DOCUMENT_MANAGEMENT.md` · `CLIENTS_MODULE_MIGRATION.md` · `COMPANY_IMPLEMENTATION_PLAN.md` ·  
> `MIGRATION_MASTER_PLAN.md`

---

## 1. Objetivo

Unificar todos los caminos de “historial / clínico / medical records” en un único módulo **Expedientes (`records`)**.

Un Expediente es el **contenedor de caso / servicio / gestión documental estructurada** ligado a un **Cliente** (y opcionalmente a Agenda/Ventas), capaz de almacenar y organizar:

| Contenido | `recordType` sugerido | Notas |
|-----------|----------------------|--------|
| Historias clínicas | `CLINICAL_HISTORY` | Pack salud/vet; secciones + payload |
| Contratos | `CONTRACT` | Metadatos + PDF/Word vía Documentos |
| Facturas | `INVOICE` | Puede vincular Venta; no reemplaza módulo Ventas |
| Informes | `REPORT` | Informes técnicos, periciales, de avance |
| Documentos técnicos | `TECHNICAL_DOC` | Manuales, planos, specs |
| Adjuntos | *(cualquier tipo)* | Archivos ligados vía módulo **Documentos** |

Principio rector:

> El **Expediente** define *qué es el caso* (tipo, estado, cliente, fechas, resumen, secciones).  
> Los **bytes** (PDF, Word, Excel, imágenes) viven en **Documentos**, vinculados al expediente.  
> No duplicar un DMS completo dentro de `records`.

---

## 2. Estado actual (análisis)

### 2.1 Fragmentación en el código vivo

| Camino | Ruta / UI | Persistencia | ViewModel |
|--------|-----------|--------------|-----------|
| A | `clinical_records` → `ClinicalHistoryScreen` | Módulo history | `ClinicalHistoryViewModel` |
| B | `history_list` → `SimpleHistoryListScreen` | `clinical_records` vía `core.FirebaseRepository` | Firebase directo |
| C | `history_edit` / detail / PDF | `HistoryRepository` + PDF utils | `HistoryViewModel` |
| D | `create_edit_clinical_record` / view | Local / tipado mixto | `ClinicalRecordViewModel` |
| E | `medical_records` | Pantalla aparte | `AuthViewModel` (débil) |
| F | `repository.FirebaseRepository` | Colección `medical_records` | Tipado legacy |

### 2.2 Problemas

1. **Varias UIs** para el mismo concepto clínico.  
2. **Dos colecciones** (`clinical_records` / `medical_records`).  
3. Modelo **solo clínico** (examen físico, Dx…) — no sirve para contratos/facturas/informes.  
4. PDF/adjuntos **ad hoc**, sin módulo Documentos.  
5. Sin `companyId` / sin `clientId` canónico (aún patients).  
6. Duplicación de pantallas Simple* vs Clinical* vs Medical*.

### 2.3 Activos reutilizables

- Generadores PDF del módulo `history` → semilla de `RECORD_PDF` en Documentos.  
- Concepto de registro con fecha, paciente, vet, notas.  
- Timeline mental “por mascota/paciente” → timeline por **Cliente**.

---

## 3. Estado objetivo: módulo Expedientes

### 3.1 Responsabilidades

| Incluye | No incluye |
|---------|------------|
| CRUD de expedientes por Company + Client | Cobrar / stock (Ventas / Inventario) |
| Tipos: clínico, contrato, factura, informe, técnico, genérico | Calendario (Agenda) |
| Estados DRAFT → FINAL → ARCHIVED | Repositorio de archivos sin expediente (eso es Biblioteca Documentos) |
| Secciones estructuradas + payload de pack | OCR/IA (módulo IA consume records/docs) |
| Adjuntos vía Documentos | Pipeline CRM |

### 3.2 Modelo Record

```
Record (Expediente)
├── id                          # recordId
├── companyId                   # tenant
├── clientId                    # obligatorio (CLIENTS migration)
├── appointmentId?              # origen Agenda
├── saleId?                     # vínculo opcional a Venta (facturas)
├── title
├── recordType                  # ver §3.3
├── category?                   # subtipo libre / tag de negocio
├── status                      # DRAFT | FINAL | ARCHIVED
├── summary                     # texto corto
├── serviceDate                 # fecha del acto / vigencia inicio
├── validFrom? / validTo?       # contratos, pólizas
├── currency? / amount?         # facturas / informes económicos
├── parties[]                   # [{ role, name, contactId?, clientId? }]
├── sections[]                  # bloques tipados o rich-text
│     id, key, title, body, data
├── industryPayload             # schema pack (clínico, etc.)
├── documentIds[]               # denorm. docs vinculados (opcional)
├── primaryDocumentId?          # PDF/oficial principal
├── tags[]
├── confidentiality             # STANDARD | SENSITIVE | LEGAL_HOLD
├── authoredBy                  # uid
├── finalizedBy? / finalizedAt?
├── templateId? / templateVersion?
├── searchTokens[]
├── createdAt / updatedAt
└── legacySource?               # clinical_records | medical_records | …
```

### 3.3 Catálogo `recordType` (core)

| Código | Contenido | Contenido típico estructurado | Adjuntos típicos |
|--------|-----------|-------------------------------|------------------|
| `CLINICAL_HISTORY` | Historia clínica | Anamnesis, examen, Dx, plan | Imágenes, lab PDF |
| `CONTRACT` | Contrato | Partes, objeto, vigencia, cláusulas ref | PDF/DOCX firmado |
| `INVOICE` | Factura / comprobante de expediente | Folio, montos, ítems ref | PDF factura, XML |
| `REPORT` | Informe | Hallazgos, conclusiones | PDF, anexos |
| `TECHNICAL_DOC` | Documento técnico | Versión doc, sistema, alcance | PDF, DWG→PDF, XLSX |
| `ATTACHMENT_PACK` | Expediente solo de adjuntos | Lista indexada | Cualquier archivo |
| `NOTE` | Nota / acta breve | Texto | Opcional |
| `CUSTOM` | Pack-defined | Schema externo | Según pack |

> **Facturas:** el módulo **Ventas** sigue siendo el sistema de cobro.  
> `recordType=INVOICE` cubre: (a) copia/archivo fiscal ligado al cliente, (b) factura externa importada, (c) expediente que *referencia* `saleId`.  
> No crear un segundo motor de facturación dentro de Records.

### 3.4 Adjuntos (obligatorio vía Documentos)

```
Record 1──* DocumentLink (entityType = RECORD)
Document 1──* DocumentVersion
```

Flujos:

1. Usuario adjunta archivo al expediente → crea **Document** + **DocumentLink** + opcional añade `documentIds`.  
2. “Exportar PDF del expediente” → Document `generator=RECORD_PDF` + link + `primaryDocumentId`.  
3. Contrato: el Word/PDF firmado es Document; el Record guarda metadatos de vigencia/partes.

Sin Documentos (fase previa): permitir `sections` + Storage temporal **solo** como puente; cutover a Documentos en cuanto exista M5.

---

## 4. Separación Records vs Documentos vs Ventas

| Pregunta | Records | Documentos | Ventas |
|----------|---------|------------|--------|
| ¿Qué pasó / qué caso es? | Sí | No | No |
| ¿Dónde está el PDF? | Referencia | Sí (bytes+versiones) | Comprobante de cobro |
| ¿Cuánto se cobró / stock? | Soft link | No | Sí |
| ¿Historia clínica estructurada? | Sí (`industryPayload`) | Solo archivo escaneado | No |
| ¿Biblioteca general de la empresa? | No | Sí | No |

---

## 5. Firestore & Storage

### 5.1 Paths (Company)

```
companies/{companyId}/records/{recordId}
companies/{companyId}/record_templates/{templateId}
```

Templates:

```
RecordTemplate
├── companyId | platformPackId
├── recordType
├── name, version
├── sectionsSchema
├── industryPayloadSchema
└── defaultConfidentiality
```

### 5.2 Índices

| Query | Campos |
|-------|--------|
| Por cliente | `clientId` + `serviceDate` desc |
| Por tipo | `recordType` + `status` + `updatedAt` |
| Por estado | `status` + `updatedAt` |
| Búsqueda | `searchTokens` |
| Por cita | `appointmentId` |
| Por venta | `saleId` |

### 5.3 Storage

Bytes **no** en subcolección inventada del record; usar Documentos:

```
companies/{companyId}/documents/{documentId}/v{n}/...
```

Alias contextual opcional (espejo):

```
companies/{companyId}/records/{recordId}/files/...
```

siempre registrado en `documents` + link.

---

## 6. Templates por tipo de expediente

| recordType | Template core mínimo |
|------------|----------------------|
| `CLINICAL_HISTORY` | reason, anamnesis, findings, diagnosis, plan (+ pack vet/clinic exam) |
| `CONTRACT` | parties, object, validFrom/To, renewal, governingLaw |
| `INVOICE` | folio, issuer, receptor clientId, lines[], subtotal, tax, total, currency, saleId? |
| `REPORT` | subject, methodology, findings, conclusions, recommendations |
| `TECHNICAL_DOC` | systemName, revision, author, approvalStatus |
| `ATTACHMENT_PACK` | index[] de documentIds + descriptions |
| `NOTE` | body |

Packs verticales **extienden** `industryPayload` (ej. signos vitales solo en clínico salud).

---

## 7. UI / navegación

### 7.1 Consolidación de rutas

| Antes | Después |
|-------|---------|
| `clinical_records`, `history_*`, `medical_records` | `records` (lista) |
| create/edit clinical / history_edit | `records/edit?id=&type=` |
| history_pdf_preview | Preview Document / Record detail |
| Labels “Historial clínico” | “Expedientes” (+ filtro tipo “Clínicos”) |

### 7.2 Pantallas objetivo

| Pantalla | Función |
|----------|---------|
| Lista Expedientes | Filtros: tipo, cliente, estado, fechas, tags |
| Crear | Wizard: tipo → cliente → template → datos |
| Detalle | Resumen, secciones, timeline adjuntos, acciones |
| Editor | Según `recordType` (form dinámico) |
| Adjuntos | Lista Documents linked + upload |
| Embebido en Cliente 360° | Tab Expedientes |

### 7.3 Filtros rápidos UX

Chips: Clínicos · Contratos · Facturas · Informes · Técnicos · Adjuntos.

---

## 8. Permisos

Alineado a `ROLE_SYSTEM` / Company:

| Permiso | Uso |
|---------|-----|
| `records.record.read` | Listar/ver |
| `records.record.create` | Alta DRAFT |
| `records.record.update` | Editar DRAFT |
| `records.record.finalize` | DRAFT→FINAL |
| `records.record.delete` | Archivar |
| `records.record.export` | PDF |
| `records.record.read_own` | Portal CLIENT |
| `records.type.clinical` | (opcional) restringir tipos sensibles |
| `records.type.legal` | Contratos |
| `records.type.financial` | Facturas-expediente |

Policy sugerida: `EMPLOYEE` crea/edita DRAFT; `MANAGER`/`ADMIN` finaliza; `CLIENT` solo FINAL propios + no SENSITIVE según flags.

---

## 9. Migración de datos (Historial → Records)

### 9.1 Fuentes

| Fuente | Mapeo |
|--------|-------|
| `clinical_records` | → `records` con `recordType=CLINICAL_HISTORY` |
| `medical_records` | → igual o merge si duplicados |
| History module docs | Campos clínicos → `sections` + `industryPayload` |
| PDFs ya en Storage | → Document + Link RECORD + `primaryDocumentId` |
| Adjuntos sueltos | → Documents linked |

### 9.2 Mapeo campo típico (clínico)

| Legacy (conceptual) | Record destino |
|---------------------|----------------|
| patientId / patientName | `clientId` (+ lookup migration_maps) |
| date | `serviceDate` |
| reason / anamnesis / exam… | `sections[]` o `industryPayload` |
| vetId / vetName | `authoredBy` + parties |
| attachments urls | DocumentLinks |
| — | `companyId` |
| — | `legacySource`, `legacyId` |

### 9.3 Oleada (proceso)

```
1. Prereq: Company + Clients migrados (clientId estable)
2. Crear record_templates CLINICAL_HISTORY (+ otros vacíos)
3. Dual-write opcional clinical_records + records
4. Backfill job: clinical/medical → companies/{id}/records
5. Re-link appointments que apuntaban a history ids
6. Mover/registrar PDFs en Documentos
7. Cutover UI única Records
8. Feature flag off history/clinical/medical routes
9. Archivar colecciones legacy
10. Actualizar MASTER_ARCHITECTURE.md
```

### 9.4 Expedientes no clínicos en la migración inicial

En el cutover **no** hay contratos/facturas/informes legacy tipados.  
Se habilitan **tipos + templates vacíos** para uso inmediato post-migración; import CSV/PDF después.

### 9.5 Rollback

- Flag `recordsModule=legacy|dual|tenant`  
- Mantener `clinical_records` read-only 2–4 semanas  
- No borrar Storage hasta checksum de DocumentLinks

---

## 10. Integraciones

| Módulo | Integración |
|--------|-------------|
| **Clientes** | Hard FK `clientId`; tab 360° |
| **Agenda** | Al completar cita → “Crear expediente” (tipo default del servicio) |
| **Ventas** | `saleId` opcional; factura de cobro ≠ expediente INVOICE (pueden linkearse) |
| **Documentos** | Adjuntos + PDF oficial |
| **CRM** | Soft: oportunidad cerrada puede abrir REPORT |
| **IA** | Summarize/extract sobre docs del record; indexable si política lo permite |
| **Inventario** | Soft: consumos en payload clínico (pack), no stock engine |

---

## 11. Orden de implementación (sin código)

| Paso | Trabajo | Depende de | Done when |
|------|---------|------------|-----------|
| R0 | Spec Record + recordTypes + separación Documentos | Docs Module/Documents | Aprobado |
| R1 | Paths tenant `records` + templates | Company + Clients | CRUD cloud |
| R2 | UI lista/detalle/editor genérico por tipo | R1 | Un Nav `records` |
| R3 | Template CLINICAL_HISTORY + forms pack | R2 | Paridad clínica básica |
| R4 | Backfill clinical/medical → records | R1 + Clients map | Datos migrados |
| R5 | Integrar Documentos (adjuntos + PDF) | Documentos M5 | Adjuntos no ad-hoc |
| R6 | Tipos CONTRACT/INVOICE/REPORT/TECHNICAL/ATTACHMENT | R2 | Crear no clínico |
| R7 | Atajo Agenda → Expediente | Agenda + R2 | Flujo día servicio |
| R8 | Apagar history/clinical/medical routes | R4–R5 | Master actualizado |
| R9 | Permisos por tipo sensible | Roles | Legal/financiero acotado |

**No** implementar R6 UI completa antes de R1–R3 (cimientos).  
**No** meter blobs grandes solo en campos Firestore del Record.

---

## 12. Criterios de aceptación

1. Un solo módulo de producción: **Expedientes**; sin tres historiales paralelos.  
2. Todo record tiene `companyId` + `clientId`.  
3. Se pueden crear expedientes de tipos: clínico, contrato, factura, informe, técnico, paquete de adjuntos.  
4. Adjuntos siempre auditables en Documentos (versión, mime, autor).  
5. Historia clínica legacy visible como `CLINICAL_HISTORY` post-backfill.  
6. PDF exportado aparece como Document vinculado.  
7. Cliente portal solo ve FINAL permitidos.  
8. Dos companies no comparten records.  
9. Ventas sigue siendo dueña del cobro; expediente INVOICE no descuenta stock.

---

## 13. Riesgos

| Riesgo | Mitigación |
|--------|------------|
| Convertir Records en file manager | Forzar DocumentLinks |
| Duplicar facturación | `saleId` + doc claro en UX |
| Migrar sin Clients | Bloquear R4 |
| Perder PDFs legacy | Job de registro en Documentos |
| Mantener SimpleHistory en Nav | Freeze + remove en R8 |
| Payload clínico en core rígido | `industryPayload` + templates |
| CONFIDENTIAL clínico expuesto a CLIENT | `confidentiality` + permisos |

---

## 14. Qué no hacer

- Solo renombrar “Historial” → “Expedientes” sin unificar colecciones.  
- Guardar Word/PDF enteros en base64 dentro del doc Record.  
- Eliminar Ventas porque exista `recordType=INVOICE`.  
- Seguir escribiendo en `medical_records` y `clinical_records` en paralelo sin fecha de corte.  
- Empezar IA sobre clinical legacy sin tenant/records canónicos.

---

## 15. Relación con otros documentos

| Documento | Relación |
|-----------|----------|
| `BUSINESS_PLATFORM_ARCHITECTURE.md` | Historial → Expedientes |
| `MODULE_DESIGN.md` | Contrato módulo records |
| `DOCUMENT_MANAGEMENT.md` | Adjuntos y versiones |
| `CLIENTS_MODULE_MIGRATION.md` | Prereq `clientId` |
| `COMPANY_IMPLEMENTATION_PLAN.md` | Prereq tenant |
| `MIGRATION_MASTER_PLAN.md` | Oleada records en M3/M4 |
| **`RECORDS_MODULE_MIGRATION.md`** | Plan detallado de esta migración |

---

## 16. Resumen ejecutivo

La migración concentra historial/clínico/medical en **Expedientes (`records`)**: contenedores tipados (clínico, contrato, factura, informe, técnico, adjuntos) bajo **Company + Cliente**, con archivos en **Documentos**.

Orden: **spec → tenant CRUD → UI unificada → template clínico + backfill → Documentos → tipos no clínicos → apagar legacy**.

---

*Fin de RECORDS_MODULE_MIGRATION.md.*
