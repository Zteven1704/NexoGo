# PILOT DEPLOYMENT PLAN — NexoGo

**Rol:** CTO  
**Fecha:** 2026-09-20  
**Objetivo:** Desplegar NexoGo a **una empresa piloto real** (piloto cerrado), no GA multi-tenant.  
**Naturaleza:** Plan **operativo** — sin código.  
**Base de producto asumida:** S0 Secure · S1 Spine · S2 Cutover · Company Onboarding · User Management v1 · Usage Analytics Foundation.

---

## 1. Veredicto de go / no-go

| Pregunta | Respuesta |
|----------|-----------|
| ¿Listo para GA / muchas empresas? | **No** |
| ¿Listo para **1 company piloto** con 1–15 usuarios internos? | **Sí, condicionado** a checklist de esta sección |
| ¿Qué debe poder hacer el piloto? | Crear company → invitar staff → clientes → expedientes → documentos → login diario |

### Gate de salida (obligatorio antes del día 0)

- [ ] Rules Firestore + Storage **desplegadas** en el proyecto piloto (deny-by-default + membership `ACTIVE`)
- [ ] Indexes de `firestore.indexes.json` desplegados (o creados; sin errores de índice en listados)
- [ ] Build **release o debug firmado** instalable en dispositivos del piloto
- [ ] Owner piloto identificado (admin) + email de soporte
- [ ] Backup operativo acordado (export manual / consola) hasta Backup F1
- [ ] Plan de rollback firmado (sección 8)
- [ ] Datos de prueba / credenciales de demo **fuera** del proyecto piloto

**No-go si:** rules de emergencia abiertas, proyecto Firebase compartido con desarrollo ruidoso, o el piloto necesita invite multi-usuario sin rules/indexes de `company_invites`.

---

## 2. Alcance del piloto

### 2.1 Incluido

| Área | Uso esperado |
|------|----------------|
| Auth email/password | Registro + login |
| Company onboarding | Crear empresa, plan inicial, admin |
| User management | Invitar por email, roles, activar/desactivar |
| Clients / Records / Documents | CRUD mínimo (create + list + upload) |
| Hub Home | Única entrada post-login |
| Usage analytics | Contadores diarios (observabilidad interna) |

### 2.2 Excluido / no prometido

- Billing cobro real / pasarela  
- Google Drive backup automático  
- Switcher multi-company  
- Módulos legacy como fuente de verdad (pacientes/citas legacy)  
- SLA 99.9% / soporte 24×7  
- Migración masiva desde Excel (salvo import manual asistido)

### 2.3 Perfil de la empresa piloto

| Criterio | Recomendación |
|----------|----------------|
| Tamaño | 1 sede, ≤ 15 usuarios |
| Vertical | Preferible veterinaria/clínica (industry pack actual) |
| Madurez digital | Tolera UX cutover (pantallas mínimas) |
| Datos | Acepta que es piloto; no historial clínico legal completo en v1 |
| Contacto | Un **champion** admin + un backup |

---

## 3. Prerrequisitos

### 3.1 Organización / negocio

| Ítem | Owner | Notas |
|------|-------|-------|
| Acuerdo de piloto (carta / email) | CTO + piloto | Alcance, duración (ej. 4–6 semanas), confidencialidad |
| DPA / tratamiento de datos (mínimo) | Legal / CTO | Especialmente si hay datos de pacientes/clientes |
| Lista de usuarios iniciales | Champion | Emails + roles (ADMIN / EMPLOYEE…) |
| Ventana de go-live | Ambos | Día D + horario de soporte reforzado |
| Criterios de éxito | CTO | Ver §9 |

### 3.2 Ingeniería

| Ítem | Estado esperado |
|------|-----------------|
| Repo estable (tag `pilot-YYYYMMDD`) | Congelar features no críticas 48h antes |
| `firestore.rules` / `storage.rules` revisadas | S0 + invites User Management |
| `firestore.indexes.json` | Incluye clients/records/docs/invites |
| App ID / firma | Documentar `applicationId` (`com.example.nexogo` hoy) y keystore release |
| Dispositivos piloto | Android 8+; red estable |
| Proyecto Firebase **dedicado** o entorno `prod-pilot` | No mezclar con sandbox de desarrollo diario |

### 3.3 Personas (RACI resumido)

| Rol | Responsabilidad |
|-----|-----------------|
| **CTO** | Go/no-go, rollback, comunicación ejecutiva |
| **Tech lead / Firebase owner** | Deploy rules, indexes, Auth, Storage; monitoreo |
| **Champion piloto** | Onboarding usuarios, reporte de incidentes L1 |
| **Soporte L2** | Bugs, permisos, datos corruptos |

---

## 4. Configuración Firebase (proyecto)

### 4.1 Proyecto

1. Crear o designar proyecto: `nexogo-pilot` (nombre interno).  
2. Plan Blaze solo si se anticipa Storage/egress relevante; sino monitorear cuotas Spark con cuidado.  
3. Región Firestore: elegir **una** (ej. `nam5` / `southamerica-east1`) y **no cambiar** después.  
4. Deshabilitar prototipos no usados (App Check opcional en fase 2 del piloto).

### 4.2 Apps registradas

| Plataforma | Acción |
|------------|--------|
| Android | Package = `applicationId` del APK piloto; SHA-1/256 debug **y** release en consola |
| `google-services.json` | Del proyecto piloto; **nunca** mezclar con proyecto de desarrollo en el APK entregado |
| iOS / Web | Fuera de alcance salvo que el piloto lo exija |

### 4.3 Firestore

| Paso | Detalle |
|------|---------|
| Deploy rules | `firebase deploy --only firestore:rules` desde el commit tag piloto |
| Deploy indexes | `firebase deploy --only firestore:indexes` — esperar estado *Enabled* |
| Verificar | Login → crear company → crear cliente (sin `PERMISSION_DENIED` / missing index) |
| Consola | Desactivar “open rules” temporales; sin reglas de emergencia |
| Datos seed | **No** mock generators en cold start (ya removidos S0) |

Colecciones críticas a observar:

- `companies`, `company_index`, `company_invites`  
- `companies/{id}/memberships`, `clients`, `records`, `documents`  
- `users/{uid}/company_memberships`  
- `usage_events` / `usage_metrics` (telemetría)

### 4.4 IAM Firebase / Google Cloud

| Cuenta | Uso |
|--------|-----|
| Owner org | 1–2 personas ingeniería |
| Editor piloto | No dar Owner al cliente |
| Service accounts | No keys en el repo; CI solo si existe pipeline |

---

## 5. Configuración Storage

| Paso | Detalle |
|------|---------|
| Bucket default | El del proyecto piloto |
| Deploy rules | `firebase deploy --only storage` — membership `ACTIVE` bajo `companies/{companyId}/**` |
| CORS | Solo si habrá web; piloto Android típico no requiere |
| Cuotas | Alertas de uso (GB) y downloads |
| Prueba | Upload PDF/imagen &lt; 5 MB desde app → objeto bajo path tenant |
| Retención | Política piloto: no borrar bucket; soft-delete de docs en app si existe |

**Operación:** no usar el bucket de desarrollo. Documentar prefijo `companies/{companyId}/documents/…`.

---

## 6. Configuración Auth

| Paso | Detalle |
|------|---------|
| Providers | **Email/Password** habilitado |
| Otros providers | Google/Apple **off** hasta que el piloto lo pida |
| Email enumeration | Mantener defaults seguros de Firebase |
| Plantillas email | Personalizar reset password con nombre NexoGo (opcional pero recomendado) |
| Usuarios | El **primer admin** se crea vía Register → Company Onboarding |
| Staff | Invite por email (User Management); el invitee debe registrarse/login con **el mismo email** |
| Aprobación legacy (`usuarios.isApproved`) | No bloquear al owner del piloto; no depender de User Approval legacy para el happy path |
| MFA | Fuera de alcance piloto v1; documentar riesgo residual |

### 6.1 Checklist Auth día 0

1. Crear cuenta owner en dispositivo limpio.  
2. Completar wizard company (nombre + plan + admin).  
3. Invitar 1 usuario EMPLOYEE de prueba.  
4. Accept invite → verificar membership ACTIVE + acceso hub.  
5. Logout / login ambos usuarios.

---

## 7. Monitoreo

### 7.1 Qué mirar (diario primeras 2 semanas)

| Señal | Fuente | Umbral de alarma |
|-------|--------|------------------|
| Errores Auth | Firebase Auth / Crashlytics (si activo) | Spike logins fallidos |
| `PERMISSION_DENIED` | Logs app + soporte | Cualquier reporte recurrente |
| Indexes pending/error | Consola Firestore | Estado ≠ Enabled en índices usados |
| Storage failures | Consola + reportes upload | &gt; 5% uploads fallidos/día |
| Usage metrics | `usage_metrics/day_*` | loginCount = 0 varios días (abandono) o caídas bruscas |
| Costos | Billing Firebase | Alerta 50% presupuesto piloto |

### 7.2 Telemetría producto (ya en app)

- **Usage Analytics:** logins, clients/records/docs creados, DAU  
- **AuditLogger:** login (complementario)  
- Revisar semanalmente con el champion: “¿cuántos clientes reales cargaron?”

### 7.3 Crash / performance

| Acción | Notas |
|--------|-------|
| Firebase Crashlytics | Activar en build piloto si aún no está cableado en release |
| Analytics | Ya dependencia BOM; eventos custom opcionales |
| Canal Slack/WhatsApp interno | Incidents → L2 en &lt; 4h laborables |

### 7.4 Health check semanal (15 min)

1. Consola: rules vigentes = tag piloto.  
2. Un login smoke test en device de ingeniería.  
3. Revisar `usage_metrics` del día.  
4. Backlog de tickets abiertos del piloto.

---

## 8. Soporte

### 8.1 Modelo

| Nivel | Quién | SLA piloto |
|-------|-------|------------|
| L1 | Champion (cliente) | Triage, reintentos, capturas |
| L2 | Ingeniería NexoGo | Respuesta &lt; 1 día laborable; críticos &lt; 4 h |
| L3 | CTO | Seguridad, pérdida de datos, rollback |

### 8.2 Canal

- Grupo dedicado (WhatsApp/Telegram/Slack) **solo piloto**  
- Plantilla de ticket: device, Android version, user email, companyId, pasos, screenshot, hora UTC  
- Prohibido compartir passwords en el canal

### 8.3 Runbooks mínimos (operativos)

| Incidente | Acción |
|-----------|--------|
| No puede login | Verificar Auth user; reset password; no tocar rules a open |
| No ve company | Verificar invite email exacto; membership; espejo `company_memberships` |
| PERMISSION_DENIED | Confirmar deploy rules; status ACTIVE; no “fix rules” |
| Upload falla | Storage rules + tamaño archivo + red; path tenant |
| Usuario de más | User Management → Desactivar / Eliminar acceso |
| Datos incorrectos | No borrar company; corregir doc puntual en consola con backup previo |

### 8.4 Ventana de soporte reforzado

- **Día 0–3:** disponibilidad extendida (acordar franja horaria local)  
- **Semana 2–4:** horario laboral estándar  
- Cierre piloto: retrospectiva + decisión continue / expand / stop

---

## 9. Plan de rollback

### 9.1 Principios

1. Preferir **rollback de app** antes que abrir rules.  
2. Nunca desplegar `allow read, write: if true`.  
3. Datos del piloto se preservan salvo decisión explícita de wipe.

### 9.2 Triggers de rollback

| Severidad | Ejemplo | Acción |
|-----------|---------|--------|
| S1 | Filtración cross-tenant / rules rotas | Rollback rules inmediato + pausa piloto |
| S1 | Pérdida masiva de archivos | Pausar uploads; restore desde backup Storage si existe |
| S2 | App unusable (crash loop) | Revertir APK a tag anterior |
| S3 | Feature confusa | Feature flag / ocultar entrada; no rollback total |

### 9.3 Procedimientos

**A. Rollback de reglas**

1. Re-deploy del archivo rules del **tag conocido bueno** (`pilot-*` o commit previo).  
2. Verificar con cuenta de prueba read/write tenant.  
3. Comunicar al champion: “operación en modo seguro”.

**B. Rollback de app**

1. Redistribuir APK/AAB del tag anterior (sideload o store interna).  
2. Confirmar `google-services.json` sigue siendo del proyecto piloto.  
3. Pedir clear-data solo si DataStore corrupto (último recurso).

**C. Rollback de indexes**

1. Indexes sobran no rompen; indexes faltantes se re-despliegan.  
2. No eliminar indexes en caliente sin necesidad.

**D. Contención de datos**

1. Suspender memberships no esenciales (User Management).  
2. Export manual crítico (consola / CSV asistido) si el piloto lo exige antes de cambios mayores.  
3. Wipe de proyecto: **solo** con aprobación escrita CTO + champion.

### 9.4 Comunicación de incidente

1. CTO declara severidad.  
2. Mensaje único al canal piloto (qué pasó, impacto, ETA).  
3. Postmortem interno &lt; 72 h (S1/S2).

---

## 10. Secuencia de despliegue (runbook día D)

| Hora | Acción | Owner |
|------|--------|-------|
| D−7 | Tag repo; checklist gate; proyecto Firebase listo | Tech |
| D−3 | Deploy rules + indexes + storage; smoke interno | Tech |
| D−1 | Entregar APK al champion; cuentas de prueba internas OK | Tech |
| D0 AM | Owner registra + onboarding company | Champion + soporte en línea |
| D0 AM | Invitar 2–3 usuarios reales | Champion |
| D0 PM | Cargar 5–10 clientes + 1 expediente + 1 documento | Champion |
| D0 PM | Verificar usage_metrics / sin errores graves | Tech |
| D+1 | Check-in soporte; ajustar roles | Ambos |
| D+7 | Review semanal métricas + UX | CTO + champion |
| D+28 | Decisión: extender / industrializar / cerrar | CTO |

---

## 11. Criterios de éxito del piloto

| KPI | Meta orientativa (4 semanas) |
|-----|------------------------------|
| Admin puede operar sin ingeniería en el día a día | Sí |
| Staff invitados activos (DAU ≥ 2 días/semana) | ≥ 50% de invitados |
| Clientes reales creados | ≥ 20 |
| Expedientes creados | ≥ 10 |
| Documentos subidos | ≥ 10 |
| Incidentes S1 | **0** |
| Satisfacción champion (1–5) | ≥ 3.5 |

Fallo del piloto ≠ fracaso de producto si se aprende: documentar en retrospectiva.

---

## 12. Riesgos residuales (aceptados con ojos abiertos)

| Riesgo | Mitigación operativa |
|--------|----------------------|
| `applicationId` aún `com.example.nexogo` | No publicar en Play Store pública; sideload / track interno |
| Permisos granulares solo en hub | Piloto con roles simples; ADMIN mayoritario al inicio |
| Backup Drive no implementado | Export asistido / snapshot consola semanal |
| Invite depende de email Auth exacto | Capacitacion champion; runbook |
| UX cutover mínima | Expectativa explícita en acuerdo de piloto |

---

## 13. Entregables de este plan

| Artefacto | Uso |
|-----------|-----|
| Este documento | Guía operativa CTO |
| Tag git `pilot-YYYYMMDD` | Reproducibilidad |
| Checklist gate impresa/digital | Firma go-live |
| Canal soporte + runbooks | Operación |
| Retrospectiva D+28 | Decisión siguiente fase |

---

## 14. Decisión CTO

**Autorizar piloto cerrado de una company** únicamente tras completar el gate de la §1.  
**No autorizar** marketing amplio, Play Store pública, ni onboarding self-serve masivo hasta cerrar S3 Harden + evidencia de 0 S1 en el piloto.

---

*Plan operativo. No implementa código ni cambia configuración por sí mismo — requiere ejecución humana en Firebase Console / CLI / distribución de app.*
