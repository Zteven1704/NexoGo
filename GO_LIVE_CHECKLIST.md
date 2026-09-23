# GO-LIVE CHECKLIST — Beta-1 (Veterinaria piloto)

**Rol:** CTO  
**Versión:** Beta-1  
**Cliente tipo:** Veterinaria real (1 sede) — ver `IDEAL_PILOT_PROFILE.md` / `BETA_1_SCOPE.md`  
**Regla:** Solo tareas **obligatorias** antes de entregar. **No** nuevas funcionalidades.  
**Go-live:** todas las casillas de este documento en **☐ → ☑**. Sin excepciones silenciosas.

**Firma go-live**

| Rol | Nombre | Fecha | Firma |
|-----|--------|-------|-------|
| CTO | | | |
| Tech / Firebase owner | | | |
| Champion veterinaria | | | |

---

## SEGURIDAD

| ID | Tarea | Verificación | ☐ |
|----|-------|--------------|---|
| S1 | Proyecto Firebase **dedicado** al piloto (no sandbox de desarrollo diario) | Nombre proyecto + `projectId` anotados | ☐ |
| S2 | `firestore.rules` del tag Beta-1 **desplegadas** (deny-by-default + membership ACTIVE) | Consola: rules = commit/tag acordado; smoke create cliente OK | ☐ |
| S3 | `storage.rules` del tag Beta-1 **desplegadas** (tenant `companies/{id}/**`) | Upload de prueba OK; path bajo company | ☐ |
| S4 | **No** hay rules abiertas (`allow read, write: if true`) ni emergency rules activas | Revisión visual en Consola | ☐ |
| S5 | Auth: solo **Email/Password** habilitado para el piloto | Consola Auth → Providers | ☐ |
| S6 | `google-services.json` del APK = proyecto piloto (no mezclado) | Package + projectId coinciden con APK entregado | ☐ |
| S7 | Owners Firebase: máximo ingeniería NexoGo; **sin** Owner al cliente | IAM revisado | ☐ |
| S8 | Mitigación **KI-002** (self-join membership): `companyId` **no** compartido públicamente; solo invite por email | Briefing champion + nota en acuerdo | ☐ |
| S9 | Alcance escrito: sin citas / inventario / billing / módulos legacy | PDF o email firmado / aceptado | ☐ |
| S10 | Lista `KNOWN_ISSUES` CRÍTICO/ALTO comunicada internamente (no ocultar a CTO) | Lectura CTO + tech lead | ☐ |

---

## OPERACIÓN

| ID | Tarea | Verificación | ☐ |
|----|-------|--------------|---|
| O1 | Tag git / build identificable: `beta-1-YYYYMMDD` (o equivalente) | Tag + hash en registro de entrega | ☐ |
| O2 | APK/AAB instalable en dispositivo(s) del piloto | Instalación en ≥1 Android del cliente | ☐ |
| O3 | Smoke E2E interno previo (mismo flujo que `DEMO_FLOW.md`) | Empresa → invite → cliente → expediente → doc | ☐ |
| O4 | Champion veterinaria identificado (nombre + WhatsApp/tel) | Contacto en canal soporte | ☐ |
| O5 | Ventana go-live acordada (fecha/hora + timezone) | Calendario compartido | ☐ |
| O6 | Capacitación mínima: solo Home + Clientes + Expedientes + Documentos + Usuarios | No se demuestran Pacientes/Citas/Inventario | ☐ |
| O7 | Confirmar 1 sede / ≤ 8 usuarios iniciales (ICP) | Lista de emails del día 0 | ☐ |
| O8 | Plan de rollback leído (`PILOT_DEPLOYMENT_PLAN.md` § rollback) | CTO + tech confirman | ☐ |

---

## BACKUP

*(Sin feature nueva de Backup UI — obligaciones operativas.)*

| ID | Tarea | Verificación | ☐ |
|----|-------|--------------|---|
| B1 | Procedimiento de export/snapshot **manual** documentado (Consola / export asistido) | Runbook 1 página: quién / cómo / dónde guardar | ☐ |
| B2 | Responsable de snapshot semanal asignado (NexoGo o champion) | Nombre en runbook | ☐ |
| B3 | Ubicación segura de copias (Drive/carpeta interna NexoGo, no chat) | Path/URL interna | ☐ |
| B4 | Cliente informado: **no hay backup self-service en app** en Beta-1 | Mención en acuerdo de alcance | ☐ |
| B5 | Antes del día 0: snapshot vacío/baseline del proyecto (opcional pero recomendado si ya hay datos de prueba) | Timestamp anotado | ☐ |

---

## USUARIOS

| ID | Tarea | Verificación | ☐ |
|----|-------|--------------|---|
| U1 | Owner (ADMIN) creado vía Register + Company Onboarding en el entorno piloto | Home muestra nombre de empresa | ☐ |
| U2 | Al menos **1 invite** de prueba (email real del staff) enviada y **aceptada** | Membership ACTIVE en Usuarios | ☐ |
| U3 | Roles iniciales solo **ADMIN** + **EMPLOYEE** (evitar CLIENT el día 0) | Lista de roles en Usuarios | ☐ |
| U4 | Emails de invite = emails Auth (coincidencia exacta) | Checklist de emails | ☐ |
| U5 | No usar “Aprobación legacy” / User Approval como control del piloto | Confirmado en capacitación | ☐ |
| U6 | Procedimiento: suspender / revocar acceso (User Management) explicado al champion | Champion lo repite | ☐ |
| U7 | Mitigación KI-006: evitar ciclo revoke→re-invite sin soporte en las primeras 48 h | Nota en runbook usuarios | ☐ |

---

## DOCUMENTOS

| ID | Tarea | Verificación | ☐ |
|----|-------|--------------|---|
| D1 | Upload de prueba PDF o imagen &lt; 2 MB OK | Doc visible en lista Documentos | ☐ |
| D2 | Objeto visible en Storage bajo `companies/{companyId}/…` | Consola Storage | ☐ |
| D3 | Política piloto: archivos preferentemente &lt; 5 MB; evitar lotes masivos | Comunicado al champion | ☐ |
| D4 | Tipos acordados: PDF / imagen (evitar formatos raros el día 0) | Lista en capacitación | ☐ |
| D5 | Consulta = listado en app (sin promesa de visor PDF completo) | Expectativa alineada | ☐ |
| D6 | Mitigación KI-010: si upload falla a medias, no reintentar en loop; avisar soporte | Runbook 1 línea | ☐ |

---

## SOPORTE

| ID | Tarea | Verificación | ☐ |
|----|-------|--------------|---|
| P1 | Canal dedicado (WhatsApp/Telegram/Slack) **solo piloto** | Link/grupo creado | ☐ |
| P2 | SLA piloto publicado: L2 &lt; 1 día laborable; crítico &lt; 4 h laborables | Texto en canal / acuerdo | ☐ |
| P3 | Contactos L1 (champion) + L2 (ingeniería) + L3 (CTO) | Tabla en canal | ☐ |
| P4 | Plantilla de ticket: device, Android, email, companyId, pasos, captura, hora | Fijada en el canal | ☐ |
| P5 | Prohibido compartir passwords en el canal | Regla fijada | ☐ |
| P6 | Runbook mínimo: login, invite, PERMISSION_DENIED, upload | 1 página entregada a L2 | ☐ |
| P7 | Cobertura reforzada día 0–3 acordada | Turnos anotados | ☐ |

---

## MONITOREO

| ID | Tarea | Verificación | ☐ |
|----|-------|--------------|---|
| M1 | Revisar `usage_metrics/day_*` del companyId piloto tras smoke | loginCount / creates &gt; 0 en prueba | ☐ |
| M2 | Alerta o revisión diaria (días 1–14): errores Auth / reportes PERMISSION_DENIED | Owner monitoreo asignado | ☐ |
| M3 | Consola Firestore: indexes del `firestore.indexes.json` en estado **Enabled** | Sin error missing-index en listados | ☐ |
| M4 | Consola Storage: sin spike anómalo de errores el día 0 | Check post go-live | ☐ |
| M5 | Presupuesto / billing Firebase: alerta al 50% del tope piloto (si Blaze) | Alerta configurada o tope anotado | ☐ |
| M6 | Health check semanal en calendario (15 min) | Evento recurrente 4 semanas | ☐ |
| M7 | Criterio de pausa: cualquier sospecha de fuga cross-tenant → CTO + rollback rules | Escrito en runbook | ☐ |

---

## Gate final (obligatorio)

Antes de decir “entregado al cliente”:

- [ ] **SEGURIDAD** S1–S10 completas  
- [ ] **OPERACIÓN** O1–O8 completas  
- [ ] **BACKUP** B1–B5 completas  
- [ ] **USUARIOS** U1–U7 completas  
- [ ] **DOCUMENTOS** D1–D6 completas  
- [ ] **SOPORTE** P1–P7 completas  
- [ ] **MONITOREO** M1–M7 completas  

**Decisión CTO:** ☐ GO ☐ NO-GO  

Si NO-GO: listar IDs pendientes; no entregar APK de producción piloto.

---

## Referencias

- `BETA_1_SCOPE.md` — qué está en contrato  
- `PILOT_DEPLOYMENT_PLAN.md` — operación y rollback  
- `KNOWN_ISSUES.md` — defectos conocidos  
- `DEMO_FLOW.md` — smoke / capacitación  
- `IDEAL_PILOT_PROFILE.md` — perfil veterinaria  

---

*Checklist verificable de go-live. Sin nuevas funcionalidades.*
