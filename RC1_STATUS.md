# NexoGo Platform — RC1 Status Board

**Rol:** Android Release Manager  
**Fecha:** 2026-09-23  
**Fuente de verdad release:** `RC1_CHECKLIST.md` · `FIREBASE_PRODUCTION_AUDIT.md`

---

## Board producto (alcance de módulos)

| Ítem | Board | Estado Release Manager |
|------|-------|------------------------|
| Multiempresa | ✓ | **✓ Código** — QA + deploy rules pendientes |
| Usuarios | ✓ | **✓ Código** — QA pendiente |
| Roles | ✓ | **✓ Código** — QA pendiente |
| Permisos | ✓ | **✓ Código** — nav OK; writes server-side sin rol |
| Clientes | ✓ | **✓ Código** — QA + 2-tenant pendientes |
| Expedientes | ✓ | **✓ Código** — QA pendiente |
| Documentos | ✓ | **✓ Código** — QA + tope upload pendientes |
| Seguridad Firebase (críticos) | ✓ | **✓ Código** — FR-01/02, AU-01/02 **CLOSED**; falta **deploy** |
| Navegación unificada | ✓ | **✓ Código** — QA back-stack pendiente |

### Pendiente (fuera de RC1 — correcto)

| Ítem | Estado |
|------|--------|
| IA avanzada | Fuera de RC1 |
| CRM completo | Fuera de RC1 |
| Facturación | Fuera de RC1 |
| Automatizaciones | Fuera de RC1 |

---

## Seguridad Firebase — CRITICAL

| ID | Estado |
|----|--------|
| FR-01 self-join membership | **CLOSED** (repo) |
| FR-02 `mensajes` open | **CLOSED** (repo) |
| AU-01 / AU-02 `isApproved` | **CLOSED** (política Platform) |
| Deploy rules/indexes cloud | **Pendiente** (bloqueante efectividad) |

---

## Board RC1 entregable

```
NexoGo Platform RC1

✓ Multiempresa
✓ Usuarios
✓ Roles
✓ Permisos
✓ Clientes
✓ Expedientes
✓ Documentos
✓ Navegación unificada
✓ Seguridad CRITICAL (código): FR-01 · FR-02 · AU-01 · AU-02

☐ Deploy firestore.rules (cloud)
☐ Build firmado 0.1.0-rc1
☐ QA dispositivo + 2-tenant

Pendiente post-RC1:
- IA avanzada
- CRM completo
- Facturación
- Automatizaciones
```

**Veredicto:** CRITICAL de seguridad **cerrados en código**. RC1 entregable cuando: deploy + APK firmado + QA (`RC1_CHECKLIST.md` §8).

---

*Board oficial stakeholders · 2026-09-23*
