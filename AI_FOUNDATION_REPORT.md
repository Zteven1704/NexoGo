# AI Document Foundation Report

**Fecha:** 2026-09-18  
**Alcance:** Infraestructura IA sobre Documentos (sin consumo de API)  
**Compilación:** `:app:compileDebugKotlin --rerun-tasks` — **BUILD SUCCESSFUL**  
**OpenAI:** Preparado vía adapter; **NoOp** en dispositivo (sin llamadas de red)

---

## Objetivo

Permitir analizar documentos empresariales mediante una base multiempresa de jobs, análisis, resúmenes y entidades — lista para Cloud Functions + OpenAI, sin ejecutar la API desde la app.

---

## Entregables

| Entregable | Ubicación | Estado |
|------------|-----------|--------|
| **AIAnalysis** | `platform/ai/model/AiModels.kt` | ✅ |
| **DocumentSummary** | mismo archivo | ✅ |
| **ExtractedEntity** | mismo archivo | ✅ |
| **AIJob** | mismo archivo | ✅ |
| OpenAI prep | `platform/ai/openai/OpenAiClient.kt` | ✅ |
| **AIRepository** | `platform/ai/data/AIRepository.kt` | ✅ |

---

## Funciones futuras (encoladas, no ejecutadas)

| Función | `AIJobType` | Enqueue API |
|---------|-------------|-------------|
| Resumir PDF / docs | `SUMMARIZE` | `enqueueSummarize` |
| Extraer datos | `EXTRACT` | `enqueueExtract` |
| Buscar información | `SEARCH` | `enqueueSearch` |
| Preguntas sobre docs | `DOC_CHAT` | `enqueueDocChat` |
| Análisis compuesto | `ANALYZE` | `enqueueAnalyze` |

Ciclo de job: `QUEUED` → `RUNNING` → `SUCCEEDED` / `FAILED`

---

## Integración OpenAI (preparada)

| Pieza | Rol |
|-------|-----|
| `OpenAiConfig` | base URL, modelos, timeouts, `OPENAI_API_KEY` env (server) |
| `OpenAiClient` | contrato `chatCompletion` / `embed` |
| `NoOpOpenAiClient` | `isEnabled=false`; falla explícito sin red |
| `OpenAiPromptBuilder` | prompts summarize / extract / doc_chat / search |
| DTOs | `OpenAiChatRequest/Response`, embeddings, summary/extract/chat results |

Principio: **cero API key en Android**; ejecución en backend.

`buildOpenAiRequestPreview(job)` arma el request que el worker enviará.  
`tryExecuteOnDevice` documenta el bloqueo on-device.

---

## Persistencia (Company-scoped)

```
companies/{companyId}/ai_jobs/{id}
companies/{companyId}/ai_analyses/{id}
companies/{companyId}/document_summaries/{id}
companies/{companyId}/extracted_entities/{id}
```

Jobs validan `documentId` vía `DocumentRepository` + tenant guard.

---

## Modelos clave

- **AIJob** — input, status, usage, resultRef  
- **AIAnalysis** — agrega summaryId + entityIds + highlights  
- **DocumentSummary** — short/long + sections  
- **ExtractedEntity** — PERSON, ORG, DATE, AMOUNT, CLAUSE, …

---

## Archivos

```
app/src/main/java/com/example/nexogo/platform/ai/
  model/AiModels.kt
  openai/OpenAiClient.kt
  data/AIRepository.kt
```

**Actualizado:** `TenantCollections` (AI_JOBS, AI_ANALYSES, DOCUMENT_SUMMARIES, EXTRACTED_ENTITIES)

---

## Compilación

```bash
bash ./gradlew :app:compileDebugKotlin --no-daemon --rerun-tasks
# BUILD SUCCESSFUL
```

---

## Fuera de alcance (siguiente)

- Cloud Function worker que consuma OpenAI
- Ingestion / OCR / chunking / embeddings
- Vector index por tenant
- UI de resumen / chat documental
- Metering de cuotas por Company

---

## Veredicto

**AI Document Foundation lista y compilando.** Modelos + jobs + adapter OpenAI (NoOp); sin consumo de API.
