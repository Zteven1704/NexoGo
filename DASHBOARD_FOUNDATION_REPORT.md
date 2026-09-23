# Dashboard Foundation Report

**Fecha:** 2026-09-18  
**Alcance:** Fundación de Dashboard / KPIs multiempresa  
**Compilación:** `:app:compileDebugKotlin --rerun-tasks` — **BUILD SUCCESSFUL**  
**UI:** No cableada (fundación de datos + agregación)

---

## Objetivo

Crear la base extensible de Dashboard: widgets, métricas y repositorio que agregan lecturas soft de módulos de plataforma (Clientes, Documentos, Inventario, Ventas, actividad reciente), diseñada para crecer con packs y nuevos KPIs.

---

## Entregables

| Entregable | Ubicación | Estado |
|------------|-----------|--------|
| **DashboardWidget** | `platform/dashboard/model/DashboardModels.kt` | ✅ |
| **DashboardMetrics** | mismo archivo | ✅ |
| **DashboardRepository** | `platform/dashboard/data/DashboardRepository.kt` | ✅ |
| Catalog + layout + snapshot | mismo paquete | ✅ |
| Extensibilidad | `DashboardMetricProvider` | ✅ |

---

## Widgets iniciales

| Código | Título | Tipo |
|--------|--------|------|
| `clients_kpi` | Clientes | KPI |
| `documents_kpi` | Documentos | KPI |
| `inventory_kpi` | Inventario | KPI |
| `sales_kpi` | Ventas | KPI |
| `recent_activity` | Actividad reciente | LIST |

Placeholders futuros (ocultos): Agenda, Expedientes, CRM, Chat unread.

---

## DashboardMetrics

Campos v1:

- `clientsTotal` / `clientsActive`
- `documentsTotal` / `documentsActive`
- `inventoryProductsTotal` / `inventoryLowStock`
- `salesCount` / `salesAmount` / `salesCurrency`
- `recentActivity[]` (`DashboardActivityItem`)
- `extras` — mapa extensible para packs / providers

Períodos: `TODAY` | `WEEK` | `MONTH` | `QUARTER` | `YEAR` | `ALL`

---

## Multiempresa & persistencia

- Layouts: `companies/{companyId}/dashboard_layouts/{layoutId}`
- Snapshots opcionales: `companies/{companyId}/dashboard_snapshots/{period}`
- Agregación live soft-read de:
  - `clients`, `documents`, `products`, `sales`, `records`, `conversations`
- Módulo ausente / error → ceros (no rompe el dashboard)

---

## Crecimiento futuro

| Mecanismo | Uso |
|-----------|-----|
| `DashboardWidgetCatalog.futurePlaceholders()` | Nuevos widgets sin romper layout |
| `DashboardMetricProvider` | Packs / módulos inyectan KPIs en `extras` |
| `DashboardWidgetType` CUSTOM / CHART / ALERT | Tipos UI futuros |
| `packId` en widget | Widgets verticales (vet, warehouse, …) |
| `DashboardAudience` STAFF / CLIENT / ADMIN | Homes por rol |
| Snapshots | Cache precomputado (Cloud Functions) |

---

## API repository

- `catalogWidgets` / `defaultStaffLayout`
- `getOrCreateLayout` / `saveLayout`
- `loadMetrics` / `loadDashboard` → `DashboardBundle`
- `saveSnapshot` / `getSnapshot`
- `mapWidgetValues` — valores listos para UI

---

## Archivos

```
app/src/main/java/com/example/nexogo/platform/dashboard/
  model/DashboardModels.kt
  data/DashboardRepository.kt
```

---

## Compilación

```bash
bash ./gradlew :app:compileDebugKotlin --no-daemon --rerun-tasks
# BUILD SUCCESSFUL
```

---

## Fuera de alcance (siguiente)

- Pantallas Compose / Home widgets
- Gráficos reales de ventas
- Providers de Inventory/Sales tipados cuando existan módulos platform
- Refresh programado / Cloud Function snapshots
- Home cliente (`DashboardAudience.CLIENT`)

---

## Veredicto

**Dashboard Foundation completa y compilando.** Widgets iniciales, métricas y repositorio multiempresa listos para crecimiento.
