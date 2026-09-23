# RC1 CHECKLIST — NexoGo Release Candidate 1

**Rol:** Android Release Manager  
**Fecha:** 2026-09-23  
**Definición RC1:** APK/AAB **candidato a piloto cerrado** (1 company), no Play Production.  
**Alcance funcional:** Splash → Login → Company → Home → Clients / Records / Documents (+ Users/Roles si admin).  
**Referencias:** `V1_INTEGRATION_REPORT.md` · `FIREBASE_PRODUCTION_AUDIT.md` · `KNOWN_ISSUES.md` · `RELEASE_PREPARATION_PLAN.md` · `GO_LIVE_CHECKLIST.md`

---

## Veredicto RC1 (hoy)

| Estado | Detalle |
|--------|---------|
| **Código / nav V1** | Listo para QA (grafo Platform único) |
| **RC1 firmable / entregable** | **NO** — falta deploy rules, build firmado, QA |
| **CRITICAL seguridad (código)** | **CLOSED** — FR-01 · FR-02 · AU-01 · AU-02 |
| **Bloqueantes restantes** | §8.1 #4–7 |

---

## 0. Identidad del build RC1

| Campo | Valor objetivo RC1 | Hoy | ☐ |
|-------|-------------------|-----|---|
| `versionName` | `0.1.0-rc1` | `1.0` | ☐ |
| `versionCode` | ≥ 101 (monótono) | `1` | ☐ |
| Tag git | `rc1-YYYYMMDD` | — | ☐ |
| Artifact | APK **o** AAB firmado **release** | Debug típico / sin signingConfig en Gradle | ☐ |
| Firebase project | Piloto dedicado | `nexogo-82003` (único en repo) | ☐ |
| Package | Aceptable sideload | `com.example.nexogo` | ☐ waiver sideload |

---

## 1. Crashes (estabilidad)

| ID | Verificación | Cómo | Hoy | ☐ |
|----|--------------|------|-----|---|
| C1 | Cold start sin crash → Splash → Login o Home | Dispositivo físico / emulador release | **Pendiente QA** | ☐ |
| C2 | Login OK / fail no crashea | Credenciales válidas e inválidas | **Pendiente QA** | ☐ |
| C3 | Crear empresa (onboarding) sin crash | Flujo wizard | **Pendiente QA** | ☐ |
| C4 | Clients / Records / Documents list+create sin crash | Happy path | **Pendiente QA** | ☐ |
| C5 | Upload documento &lt; 5 MB OK; &gt; tope no OOM | Política piloto | **Código:** `readBytes()` ilimitado (KI-017) — **FALTA** harden o tope duro | ☐ |
| C6 | Logout → Login sin crash / back stack limpio | | **Pendiente QA** | ☐ |
| C7 | Crashlytics en build RC1 | Consola Firebase recibe crash de prueba | **FALTA** dependencia Crashlytics | ☐ |
| C8 | 0 crash startup en sesión de QA (≥ 30 min uso) | Manual / pre-launch | **Pendiente** | ☐ |

**Falta crashes:** C5 (mitigar), C7 (integrar o waiver explícito), C1–C4/C6/C8 (evidencia QA).

---

## 2. Navegación

| ID | Verificación | Hoy | ☐ |
|----|--------------|-----|---|
| N1 | Único grafo: Splash, Login, Register, Onboarding, Home, C/R/D, Users/Roles/Permissions, Profile, Settings, Help, About | **OK** (`Screen.kt` / `NavGraph`) | ☐ |
| N2 | Sin menú legacy (citas/pacientes/inventario/chat) | **OK** (cleanup + hub Platform) | ☐ |
| N3 | Deep back: Documents → Home → Settings → About → back coherente | **Pendiente QA** | ☐ |
| N4 | Sin company: pane onboarding + invitaciones; no hub vacío infinito | **OK en código**; **Pendiente QA** | ☐ |
| N5 | `FirebaseFullTestActivity` no launcher / no exported | **OK** (`exported=false`) | ☐ |

**Falta navegación:** solo evidencia QA N3–N4 (código listo).

---

## 3. Login

| ID | Verificación | Hoy | ☐ |
|----|--------------|-----|---|
| L1 | Email/password login funciona contra Firebase proyecto RC1 | **Pendiente** (env) | ☐ |
| L2 | Registro → sesión → Home u Onboarding | **Pendiente QA** | ☐ |
| L3 | Splash con sesión restaurada → Home | **OK código** (`SessionSplashScreen`); **Pendiente QA** | ☐ |
| L4 | Política de acceso: FirebaseAuth + membership ACTIVE + company session (sin `isApproved`) | **OK** (KI-001/003 cerrados) | ☑ |
| L5 | No auto-crear admin hardcodeado | **OK** (S0) | ☐ |

**Falta login:** L4 (obligatorio para RC1); L1–L3 evidencia en proyecto Firebase RC1.

---

## 4. Company

| ID | Verificación | Hoy | ☐ |
|----|--------------|-----|---|
| CO1 | Crear empresa (onboarding) → membership ACTIVE → Home con nombre | **Pendiente QA** | ☐ |
| CO2 | Aceptar invite (mismo email) → misma company | **Pendiente QA** | ☐ |
| CO3 | Sin membership ACTIVE no opera C/R/D | **OK app**; rules OK si no self-join | ☐ |
| CO4 | Self-join membership (KI-002 / FR-01) | **CLOSED** en rules (repo) — falta deploy | ☑ código |
| CO5 | Rules + indexes **desplegados** en proyecto RC1 | **FALTA verificación deploy** (no hay `firebase.json` en repo) | ☐ |
| CO6 | No compartir `companyId` públicamente (mitigación si CO4 waiver) | Operativo | ☐ |

**Falta company:** CO4 (fix preferido), CO5 (deploy), CO1–CO2 QA.

---

## 5. Clients

| ID | Verificación | Hoy | ☐ |
|----|--------------|-----|---|
| CL1 | Home → Clientes → listar | **Pendiente QA** | ☐ |
| CL2 | Crear cliente → aparece en lista | **Pendiente QA** | ☐ |
| CL3 | Datos bajo `companies/{id}/clients` | **OK código** | ☐ |
| CL4 | Otro usuario otra company no ve clientes | **Pendiente** prueba 2-tenant (tras CO4) | ☐ |

**Falta clients:** QA CL1–CL2; aislamiento CL4 tras fix membership.

---

## 6. Records

| ID | Verificación | Hoy | ☐ |
|----|--------------|-----|---|
| R1 | Home → Expedientes → listar | **Pendiente QA** | ☐ |
| R2 | Crear expediente ligado a cliente | **Pendiente QA** | ☐ |
| R3 | Picker clientes limitado a 8 (KI-009) — aceptado en RC1 | **Waiver OK** si ≤8 clientes demo | ☐ |
| R4 | Path `companies/{id}/records` | **OK código** | ☐ |

**Falta records:** QA R1–R2; R3 waiver documentado.

---

## 7. Documents

| ID | Verificación | Hoy | ☐ |
|----|--------------|-----|---|
| D1 | Home → Documentos → listar | **Pendiente QA** | ☐ |
| D2 | Subir PDF/JPG &lt; 5 MB → lista | **Pendiente QA** | ☐ |
| D3 | Path Storage `companies/{id}/documents/...` | **OK código + rules tenant** | ☐ |
| D4 | Política tamaño / no `readBytes` ilimitado | **FALTA** (KI-017) o waiver + instrucción piloto | ☐ |
| D5 | No reintentar upload en loop si falla (KI-010) | Runbook soporte | ☐ |

**Falta documents:** QA D1–D2; D4 harden o waiver.

---

## 8. Qué falta exactamente para RC1

### 8.1 Bloqueantes (must-have) — sin esto **no** hay RC1

| # | Falta | Tipo | Owner |
|---|-------|------|-------|
| 1 | ~~Fix rules membership self-join (FR-01)~~ | **HECHO** (repo) — falta deploy §4 | Firebase |
| 2 | ~~Deny `mensajes` (FR-02)~~ | **HECHO** (repo) — falta deploy §4 | Firebase |
| 3 | ~~Política Auth (AU-01/02)~~ | **HECHO** — Auth + membership ACTIVE + company session | Android |
| 4 | **Deploy verificado** de `firestore.rules` + `storage.rules` + indexes en proyecto RC1 | Ops | Firebase owner |
| 5 | **Build release firmado** con `versionName=0.1.0-rc1` y `versionCode` nuevo | Release | Android RM |
| 6 | **QA manual evidenciado** (matriz §1–§7 en dispositivo) — checklist firmado | QA | QA + RM |
| 7 | **Smoke 2 cuentas** (aislamiento) tras deploy FR-01 | QA | QA |

### 8.2 Fuertemente recomendados (should-have) — RC1 “débil” sin ellos

| # | Falta | Nota |
|---|-------|------|
| 8 | Crashlytics en RC1 | Sin telemetría de crashes en campo |
| 9 | Tope upload / evitar OOM (KI-017) | O waiver + max 5 MB comunicado |
| 10 | `usesCleartextTraffic=false` en release | Hardening |
| 11 | Recortar permisos no usados (teléfono/mic/biometría) | Manifest aún amplio |
| 12 | Tag git + registro artifact (hash AAB/APK) | Trazabilidad |

### 8.3 Expresamente fuera de RC1 (no bloquean)

| Ítem | Motivo |
|------|--------|
| Play Store listing / rename `applicationId` | RC1 = sideload / internal |
| MFA / App Check | Post-RC1 |
| Paginación / limits PERFORMANCE H0 | Waiver RC1 si volumen bajo |
| Unificar doble `FirebaseRepository` | Deuda S1 cleanup |
| Billing, CRM, Chat, AI | Fuera de alcance |

### 8.4 Ya cumplido en código (no vuelve a pedir como “falta”)

- Nav Platform única (Clients/Records/Documents/Users/Roles/Permissions)  
- Legacy UI modules eliminados del árbol navegable  
- Company session ACTIVE gate en Home  
- Compile debug exitoso post V1 + cleanup  
- Test activity no exported  

---

## 9. Matriz QA rápida (copiar al acta RC1)

| Flujo | Pass | Fail | Notas |
|-------|------|------|-------|
| Cold start | ☐ | ☐ | |
| Login | ☐ | ☐ | |
| Register + onboarding company | ☐ | ☐ | |
| Invite staff | ☐ | ☐ | |
| Crear cliente | ☐ | ☐ | |
| Crear expediente | ☐ | ☐ | |
| Subir documento | ☐ | ☐ | |
| Roles / permisos (admin) | ☐ | ☐ | |
| Logout | ☐ | ☐ | |
| Aislamiento 2 users / 2 companies | ☐ | ☐ | Requiere fix KI-002 |

**Dispositivo:** ________ **Android:** ________ **Build:** `0.1.0-rc1` (#____)  
**QA:** ________ **Fecha:** ________

---

## 10. Definición de Done — RC1

RC1 está **listo para entregar al piloto** solo si:

1. §8.1 ítems **1–7** en ☑ (o waiver CTO escrito para #1 con mitigación).  
2. Acta §9 sin Fail en happy path.  
3. Artifact firmado + tag git archivados.  
4. Canal soporte + `PILOT_AGREEMENT` / `PILOT_SUPPORT_PLAN` activos.

**Firma RC1**

| Rol | Nombre | Fecha | Go / No-Go |
|-----|--------|-------|------------|
| Android Release Manager | | | |
| Firebase owner | | | |
| CTO | | | |

---

## 11. Una línea para el equipo

> **Para RC1 falta:** deploy rules/indexes, firmar build `0.1.0-rc1`, y QA en dispositivo (Login → Company → Clients → Records → Documents + 2-tenant). **FR-01, FR-02, AU-01, AU-02 = CLOSED** en código.

---

*RC1 Checklist · Release Manager · sideload/piloto, no Production Play.*
