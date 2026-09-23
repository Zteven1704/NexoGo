# RELEASE PREPARATION PLAN — NexoGo

**Rol:** Android Release Manager  
**Fecha:** 2026-09-23  
**Tipo:** Plan operativo (no modifica código)  
**Producto actual:** Beta-1 / piloto cerrado → preparación hacia release interno y, después, Play Store  
**Referencias:** `GO_LIVE_CHECKLIST.md` · `PILOT_DEPLOYMENT_PLAN.md` · `PILOT_AGREEMENT.md` · `BETA_1_SCOPE.md` · `KNOWN_ISSUES.md`

---

## 0. Estado actual (baseline del repo)

| Ítem | Valor hoy | Implicación release |
|------|-----------|---------------------|
| `applicationId` | `com.example.nexogo` | **No apto** para Play Store pública; cambiar antes de listing |
| `versionCode` / `versionName` | `1` / `1.0` | Definir esquema semántico + canal |
| `minSdk` / `targetSdk` / `compileSdk` | 26 / 36 / 36 | OK para Play 2026 (target 35+); mantener alineado a política Google |
| Firma release | No documentada en Gradle (sin `signingConfigs` visibles) | Crear keystore + CI secrets |
| `minifyEnabled` (release) | `false` | Activar R8 antes de store |
| Firebase Analytics | Dependencia BOM + `FirebaseConfig` | Presente; política de eventos pendiente |
| Crashlytics | **No** en `build.gradle.kts` | Añadir en fase de implementación (este plan solo prepara) |
| Usage Analytics | Firestore interno (`UsageAnalytics`) | No es Google Analytics; documentar en privacidad |
| Permisos | Amplios (cámara, mic, teléfono, media, biometría…) | Justificar o recortar antes de store |
| Activities exportadas | `MainActivity` + `FirebaseFullTestActivity` (`exported=true`) | Test activity **fuera** de release store |
| Cleartext | `usesCleartextTraffic="true"` | Cerrar para producción |
| Entrega piloto recomendada | Sideload / track interno | Play pública = fase posterior |

**Decisión de canales**

| Canal | Uso | Store |
|-------|-----|-------|
| **Pilot** | 1 company, APK/AAB firmado, Firebase piloto | Sideload o Play **internal testing** |
| **Staging** | QA pre-prod | Internal / closed testing |
| **Production** | Clientes de pago / GA | Production track (solo tras gates) |

---

## 1. Versionado

### 1.1 Esquema recomendado

| Campo | Regla |
|-------|-------|
| `versionName` | `MAJOR.MINOR.PATCH[-canal]` — ej. `0.1.0-pilot`, `1.0.0` |
| `versionCode` | Entero **monótono** (+1 por cada AAB subido a Play) |
| Tag git | `release/X.Y.Z` o `beta-1-YYYYMMDD` (piloto) + commit SHA en registro |

### 1.2 Mapa de canales

| Canal | `versionName` ejemplo | `versionCode` | Notas |
|-------|----------------------|---------------|-------|
| Pilot | `0.1.0-pilot` | 100+ | Sideload OK; mismo code space que staging |
| Staging | `0.1.0-rc1` | 101+ | Closed testing |
| Production | `1.0.0` | 200+ (salto claro) | Solo tras V1 gates |

### 1.3 Checklist versionado

| ID | Tarea | ☐ |
|----|-------|---|
| V1 | Definir tabla versionCode por canal (evitar colisiones Play) | ☐ |
| V2 | Registro de releases: versionName, versionCode, git tag, SHA, fecha, Firebase projectId | ☐ |
| V3 | Política: todo AAB de Play incrementa `versionCode` aunque sea hotfix | ☐ |
| V4 | Hotfix: `PATCH+1`; features: `MINOR+1`; breaking store/id: `MAJOR+1` | ☐ |
| V5 | Build.display: mostrar versionName en About / soporte (cuando se implemente; no en este doc) | ☐ |

---

## 2. Firma APK / AAB

### 2.1 Artefactos

| Artefacto | Uso |
|-----------|-----|
| **AAB** | Play Store (obligatorio para publicación) |
| **APK** firmado release | Sideload piloto / emergencia |
| **APK debug** | Solo ingeniería; **nunca** al cliente |

### 2.2 Keystore

| ID | Tarea | ☐ |
|----|-------|---|
| K1 | Crear **upload keystore** release (Android Studio / `keytool`) — no usar debug keystore | ☐ |
| K2 | Guardar keystore + passwords en **gestor de secretos** (1Password/Vault); **nunca** en git | ☐ |
| K3 | Backup offline cifrado del keystore (2 custodios NexoGo) | ☐ |
| K4 | Documentar: alias, validity, algoritmo (Google recomienda ≥ 2048 RSA / AES) | ☐ |
| K5 | Play App Signing: enrollar app; conservar upload key; Google guarda app signing key | ☐ |
| K6 | CI: inyectar `storeFile` / passwords por secrets; no hardcode en `build.gradle` | ☐ |
| K7 | Verificar: `jarsigner`/`apksigner` + fingerprint SHA-1/256 registrados en Firebase | ☐ |

### 2.3 Fingerprints Firebase

Registrar en Firebase Console (Android app) para **debug y release**:

- SHA-1  
- SHA-256  

Sin esto fallan Auth (si se usa Google) y algunas APIs; Email/Password no depende de SHA, pero conviene dejarlo listo.

### 2.4 Checklist build firmado

| ID | Tarea | ☐ |
|----|-------|---|
| B1 | `./gradlew :app:bundleRelease` produce AAB firmado | ☐ |
| B2 | Instalar APK release en dispositivo limpio (no debug) | ☐ |
| B3 | Confirmar `applicationId` + firma coinciden con Firebase Android app | ☐ |
| B4 | Release **sin** `FirebaseFullTestActivity` exportada / sin mocks en cold start | ☐ |

---

## 3. Firebase Production

### 3.1 Proyectos

| Entorno | Proyecto Firebase | `google-services.json` |
|---------|-------------------|------------------------|
| Dev | Sandbox ingeniería | Solo máquinas dev |
| Pilot | Proyecto dedicado piloto | APK/AAB piloto |
| Production | Proyecto prod (separado) | AAB production |

**Nunca** mezclar `google-services.json` de prod en builds de debug diarios.

### 3.2 Checklist Firebase prod / piloto

| ID | Tarea | ☐ |
|----|-------|---|
| F1 | Proyecto dedicado (pilot o prod) creado; billing Blaze si Storage/Functions lo requieren | ☐ |
| F2 | App Android registrada con package final (hoy `com.example.nexogo` — planificar rename) | ☐ |
| F3 | Auth: Email/Password ON; providers no usados OFF | ☐ |
| F4 | `firestore.rules` del tag release **desplegadas** | ☐ |
| F5 | `storage.rules` del tag release **desplegadas** | ☐ |
| F6 | `firestore.indexes.json` desplegado; indexes **Enabled** | ☐ |
| F7 | Sin rules emergency / `allow all` | ☐ |
| F8 | IAM: Owners solo NexoGo; sin Owner al cliente | ☐ |
| F9 | Alertas budget 50% / 90% | ☐ |
| F10 | App Check: planificar (no bloquea piloto sideload; **recomendado** antes de prod abierta) | ☐ |
| F11 | Smoke E2E en proyecto destino: company → users → clients → records → docs | ☐ |

Alineado a `GO_LIVE_CHECKLIST.md` § SEGURIDAD / OPERACIÓN.

---

## 4. Crashlytics

### 4.1 Estado

No hay dependencia Crashlytics en el `build.gradle.kts` actual. Plan de preparación:

| ID | Tarea | ☐ |
|----|-------|---|
| C1 | Añadir plugin + dependencia Crashlytics (fase implementación futura; fuera de este doc) | ☐ |
| C2 | Habilitar Crashlytics en Firebase Console del proyecto pilot/prod | ☐ |
| C3 | Verificar crash de prueba en build release (no solo debug) | ☐ |
| C4 | Mapeos ProGuard/R8 subidos automáticamente (cuando `minifyEnabled=true`) | ☐ |
| C5 | Convención: **no** loguear PII (email, nombres de clientes, paths con datos clínicos) en custom keys | ☐ |
| C6 | Proceso: crash nuevo en piloto → ticket soporte L2 &lt; SLA (`PILOT_SUPPORT_PLAN.md`) | ☐ |
| C7 | Dashboard: responsable semanal revisa non-fatals top 5 | ☐ |

### 4.2 Política de releases con crashes

| Severidad | Acción |
|-----------|--------|
| Crash startup &gt; umbral (ej. 1% sesiones) | Bloquear promoción a Production track |
| Crash en flujo C/E/D | Hotfix o rollback APK |
| Non-fatal aislado | Backlog; no bloquea piloto |

---

## 5. Analytics

Hay **dos capas**; no confundirlas en privacidad ni en ops.

| Capa | Qué es | Dónde | Uso release |
|------|--------|-------|-------------|
| **Firebase Analytics** | SDK Google (`firebase-analytics`, `FirebaseConfig`) | Google | Product analytics / funnels |
| **Usage Analytics** | Eventos propios NexoGo → Firestore | Tenant / company | Ops piloto interno |

### 5.1 Checklist Analytics

| ID | Tarea | ☐ |
|----|-------|---|
| A1 | Decidir: Firebase Analytics **ON** en pilot/prod o solo Usage Firestore | ☐ |
| A2 | Si GA ON: documentar eventos permitidos (login, screen_view hub, create client/record/doc) — sin contenido de expedientes | ☐ |
| A3 | Deshabilitar recolección de Advertising ID si no hay ads (Data Safety / manifest) | ☐ |
| A4 | Usage Analytics: confirmar que no escribe datos clínicos en metadata | ☐ |
| A5 | Consola GA4 / Firebase: acceso solo NexoGo | ☐ |
| A6 | Retención y exportación: política alineada a privacidad (§6) | ☐ |
| A7 | No mezclar Analytics de proyecto dev con prod | ☐ |

### 5.2 Eventos recomendados (mínimos, sin PII)

- `login_success` / `login_failure` (sin email)  
- `company_ready`  
- `client_created` / `record_created` / `document_uploaded` (ids opacos)  
- `screen_view`: home, clients, records, documents, users  

---

## 6. Políticas de privacidad

### 6.1 Documentos obligatorios antes de Play (y recomendados en piloto)

| Documento | Audiencia | ☐ |
|-----------|-----------|---|
| Política de privacidad (URL pública HTTPS) | Usuarios finales / Play | ☐ |
| Términos de uso / acuerdo piloto | Champion | ☐ |
| Aviso de tratamiento de datos (clientes/pacientes vet) | Clínica | ☐ |
| Data Safety form (Play Console) | Google | ☐ |

### 6.2 Contenido mínimo de la política (checklist)

| ID | Debe declarar | ☐ |
|----|---------------|---|
| P1 | Quién es el responsable (NexoGo / razón social) | ☐ |
| P2 | Datos: cuenta (email, nombre), datos de negocio que el cliente carga (clientes, expedientes, archivos) | ☐ |
| P3 | Proveedores: Google Firebase (Auth, Firestore, Storage, Analytics/Crashlytics si activos) | ☐ |
| P4 | Finalidad: prestar el servicio SaaS, soporte, seguridad, mejora del producto | ☐ |
| P5 | Conservación y baja: al terminar piloto / contrato | ☐ |
| P6 | Derechos del titular (acceso, rectificación, eliminación — según jurisdicción) | ☐ |
| P7 | Contacto privacidad (email) | ☐ |
| P8 | Que **no** se venden datos; que el contenido clínico/operativo es del cliente | ☐ |
| P9 | Permisos del dispositivo y por qué (cámara/archivos/notificaciones) | ☐ |
| P10 | Enlace en Play listing + (ideal) pantalla About / registro | ☐ |

### 6.3 Piloto (sin Play)

Aunque sea sideload: entregar URL o PDF de privacidad + alcance Beta-1 (`PILOT_AGREEMENT.md`) al champion.

---

## 7. Permisos Android

### 7.1 Inventario actual (`AndroidManifest.xml`)

| Permiso | ¿Necesario para Beta-1 hub? | Recomendación release |
|---------|----------------------------|------------------------|
| `INTERNET` / `ACCESS_NETWORK_STATE` | Sí | Mantener |
| `CAMERA` | Solo si hay captura en flujo docs | Justificar o quitar si solo file picker |
| `READ_EXTERNAL_STORAGE` / `WRITE` (max 28) | Legacy | Revisar; preferir SAF / Photo Picker |
| `READ_MEDIA_IMAGES/VIDEO/AUDIO` | Posible para uploads | Declarar en Data Safety; acotar a images si basta |
| `POST_NOTIFICATIONS` | Si FCM activo | Mantener si hay push; si no, quitar |
| `WAKE_LOCK` / `RECEIVE_BOOT_COMPLETED` | Típico FCM | Solo si messaging en release |
| `CALL_PHONE` | No en hub C/E/D | **Quitar** o justificar fuerte |
| `RECORD_AUDIO` | Chat voz legacy | **Quitar** del build store si chat fuera |
| `USE_FINGERPRINT` / `USE_BIOMETRIC` | MFA no en Beta-1 | Quitar hasta feature real |
| `usesCleartextTraffic=true` | Inseguro | **false** en release |
| `FirebaseFullTestActivity` exported | Riesgo | Remover de release / `exported=false` + no launcher |

### 7.2 Checklist permisos

| ID | Tarea | ☐ |
|----|-------|---|
| R1 | Matriz permiso → feature → pantalla (solo lo usado en el canal) | ☐ |
| R2 | Runtime permissions: flujos de denegación no rompen login/hub | ☐ |
| R3 | Data Safety Play alineado 1:1 con permisos restantes | ☐ |
| R4 | `tools:node="remove"` en manifest release para permisos de debug/test si se usan flavors | ☐ |
| R5 | Camera `required=false` (ya está) — mantener | ☐ |

---

## 8. Play Store readiness

### 8.1 Bloqueadores conocidos (hoy)

| Bloqueador | Acción previa |
|------------|---------------|
| `applicationId` = `com.example.*` | Rename a id de productor (ej. `com.nexogo.app`) + migrar Firebase app |
| KI seguridad abiertos (`KNOWN_ISSUES`) | No Production track hasta CRÍTICOS cerrados / waiver CTO |
| Superficie legacy / test activity | Grafo y activities limpios en AAB store |
| Sin privacidad URL | Bloqueo listing |
| Sin Crashlytics / minify | No bloquea 100%, pero es gate interno recomendado |
| Cleartext traffic | Corregir antes de review |

### 8.2 Fases Play

| Fase | Track | Criterio de entrada |
|------|-------|---------------------|
| 0 | Ninguno (sideload) | `GO_LIVE_CHECKLIST` piloto |
| 1 | **Internal testing** | AAB firmado + Firebase pilot/staging + privacidad draft |
| 2 | **Closed testing** | 5–20 testers; 0 S1 seguridad; Crashlytics ON |
| 3 | **Open testing** (opcional) | Estabilidad 14 días |
| 4 | **Production** | V1 comercial gates (`NEXOGO_V1_GAP_ANALYSIS.md`) + Data Safety + store assets |

### 8.3 Checklist Play Console

| ID | Tarea | ☐ |
|----|-------|---|
| PS1 | Cuenta Play Console (organización) + aceptados acuerdos | ☐ |
| PS2 | App creada con applicationId **final** | ☐ |
| PS3 | AAB subido a Internal testing | ☐ |
| PS4 | Ficha short/full description (español; alcance honesto Beta/V1) | ☐ |
| PS5 | Gráficos: icono 512, feature graphic, 2–8 screenshots teléfono | ☐ |
| PS6 | Categoría / contacto / email soporte | ☐ |
| PS7 | Privacy policy URL | ☐ |
| PS8 | Data Safety questionnaire completo y veraz | ☐ |
| PS9 | Content rating questionnaire | ☐ |
| PS10 | Target audience / news apps / COVID (N/A declarado) | ☐ |
| PS11 | Countries / pricing (si pago vía Play o externo documentado) | ☐ |
| PS12 | Release notes por versionCode | ☐ |
| PS13 | Pre-launch report revisado (crashes/ANRs) | ☐ |
| PS14 | Declarar permisos sensibles (si quedan cámara/mic/teléfono) | ☐ |

### 8.4 Store listing — mensaje de producto (piloto/V1)

Evitar promesas fuera de `BETA_1_SCOPE`:

> Gestión multiempresa de clientes, expedientes y documentos para equipos pequeños.

No listar citas, inventario, facturación o IA si no están en el AAB comercial.

---

## 9. Hardening de build release (checklist ops)

| ID | Tarea | ☐ |
|----|-------|---|
| H1 | `minifyEnabled` + shrinkResources en release (tras prueba smoke) | ☐ |
| H2 | ProGuard keep rules Firebase / Compose verificadas | ☐ |
| H3 | Sin cleartext; HTTPS only | ☐ |
| H4 | Sin credenciales / admin hardcode / MockData en cold start | ☐ |
| H5 | Sin activity de test en release | ☐ |
| H6 | `allowBackup` revisado (datos sensibles tenant) | ☐ |
| H7 | Tag git + AAB archivado (GCS/Drive interno) por versionCode | ☐ |
| H8 | Smoke post-install: login → hub → C → E → D | ☐ |

---

## 10. Calendario sugerido (sin fechas absolutas)

| Semana | Entregable |
|--------|------------|
| W0 | Keystore + registro versiones + Firebase project matrix |
| W1 | AAB firmado pilot; Crashlytics plan; permisos matriz |
| W2 | Privacidad URL + Data Safety borrador; Internal testing |
| W3 | Closed testing; monitoreo crashes/analytics |
| W4+ | Decisión Production vs seguir sideload según V1 gaps |

---

## 11. Roles y responsabilidades

| Rol | Responsable de |
|-----|----------------|
| Android Release Manager | Versionado, firma, AAB, Play tracks |
| Firebase owner | Proyectos, rules, indexes, budgets |
| CTO | Go/No-Go Production; waiver KIs |
| CSM / Legal | Privacidad, acuerdo piloto, Data Safety copy |
| Ingeniería | Crashlytics wiring, minify, quitar test surfaces (cuando se autorice código) |

---

## 12. Go / No-Go por canal

### Sideload piloto

**GO** si: `GO_LIVE_CHECKLIST` completo + APK/AAB firmado + Firebase piloto + soporte canal.

### Play Internal

**GO** si: sideload GO + privacidad draft URL + AAB + sin test activity exportada + Analytics/Crashlytics decisión documentada.

### Play Production

**GO** solo si: KIs CRÍTICO cerrados o waiver escrito · applicationId final · Data Safety · Crashlytics activo · 0 incidentes S1 en closed testing · alcance listing = binario.

---

## 13. Registro de release (plantilla)

```
versionName:
versionCode:
gitTag / SHA:
canal: pilot | staging | production
Firebase projectId:
AAB path / hash:
keystore alias: (nombre, no password)
Crashlytics: on/off
Analytics: GA on/off | Usage on/off
privacidad URL:
Play track:
aprobado por (CTO):
fecha:
```

---

*Plan de preparación de release. No modifica código; define gates operativos hasta sideload, Internal testing y Production.*
