# CRM Foundation Module Report

**Fecha:** 2026-09-18  
**Alcance:** Fundación CRM comercial multiempresa  
**Compilación:** `:app:compileDebugKotlin --rerun-tasks` — **BUILD SUCCESSFUL**  
**Integración:** Clients Module (`convertLeadToClient`)

---

## Objetivo

Implementar la base CRM: prospectos (Lead), pipeline (Opportunity + stages), recorrido (CustomerJourney) y seguimientos (FollowUp), con conversión Lead → Client.

---

## Entregables

| Entregable | Ubicación | Estado |
|------------|-----------|--------|
| **Lead** | `platform/crm/model/CrmModels.kt` | ✅ |
| **Opportunity** | mismo archivo | ✅ |
| **CustomerJourney** | mismo archivo | ✅ |
| **FollowUp** | mismo archivo | ✅ |
| **PipelineStage** | mismo archivo (pipeline comercial) | ✅ |
| **CrmRepository** | `platform/crm/data/CrmRepository.kt` | ✅ |

---

## Pipeline comercial

Etapas default (`ensureDefaultPipeline`):

| Código | Nombre | Prob. |
|--------|--------|-------|
| `qualification` | Calificación | 10% |
| `proposal` | Propuesta | 40% |
| `negotiation` | Negociación | 70% |
| `won` | Ganada | 100% |
| `lost` | Perdida | 0% |

APIs: `getPipelineBoard`, `moveOpportunityStage`, `listOpportunities`

---

## Seguimientos (FollowUp)

Tipos: `CALL`, `EMAIL`, `VISIT`, `MEETING`, `WHATSAPP`, `TASK`, `OTHER`  
Estados: `PENDING` → `DONE` / `SKIPPED` / `CANCELLED`  

Vínculos soft: `leadId`, `clientId`, `opportunityId`, `journeyId`, `conversationId`, `taskId`

---

## Conversión de prospectos

`convertLeadToClient(companyId, leadId, …)`:

1. Crea **Client** (PERSON u ORG) vía `ClientRepository` / `ClientFactories`
2. Marca Lead `CONVERTED` + `convertedClientId`
3. Opcional: abre **Opportunity** en primera etapa abierta
4. Crea/actualiza **CustomerJourney** → etapa `ONBOARDING`

Retorna `LeadConversionResult(lead, clientId, opportunityId?, journeyId?)`

---

## CustomerJourney

Etapas: `AWARENESS` → `CONSIDERATION` → `DECISION` → `ONBOARDING` → `RETENTION` → `ADVOCACY`  
Steps default al crear; `advanceJourneyStage` para progreso.

---

## Multiempresa (Firestore)

```
companies/{companyId}/leads/{id}
companies/{companyId}/opportunities/{id}
companies/{companyId}/pipeline_stages/{id}
companies/{companyId}/customer_journeys/{id}
companies/{companyId}/follow_ups/{id}
```

---

## Archivos

```
app/src/main/java/com/example/nexogo/platform/crm/
  model/CrmModels.kt
  data/CrmRepository.kt
```

**Actualizado:** `TenantCollections` (LEADS, CUSTOMER_JOURNEYS, FOLLOW_UPS), `TenantFirestore.leads/opportunities`

---

## Compilación

```bash
bash ./gradlew :app:compileDebugKotlin --no-daemon --rerun-tasks
# BUILD SUCCESSFUL
```

---

## Fuera de alcance (siguiente)

- UI pipeline kanban / ViewModel
- Cierre Opportunity → Sale
- Scoring automático / campañas
- Sync FollowUp ↔ Task module
- Security Rules

---

## Veredicto

**CRM Foundation completa y compilando.** Lead, Opportunity, Journey, FollowUp y conversión a Clients listos.
