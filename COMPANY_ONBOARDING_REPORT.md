# Company Onboarding Report

**Rol:** Product Architect  
**Fecha:** 2026-09-20  
**Compilación:** `:app:compileDebugKotlin` — **BUILD SUCCESSFUL**  
**Objetivo:** Registro → crear empresa → plan inicial → admin → entrar al hub.

---

## Resumen

| Entrega | Estado |
|---------|--------|
| `CreateCompanyFlow` | ✅ |
| `CompanySetupWizard` | ✅ |
| `InitialAdminSetup` | ✅ |
| Ruta `company_onboarding` + nav | ✅ |
| Compatibilidad (usuarios con membership ACTIVE) | ✅ |
| Sin auto-create silencioso (hardening) | ✅ |

---

## Flujo de producto

```
1. Registrarse          RegisterScreen → Auth + DataStore + setCurrentUser
2. Crear empresa        CompanySetupWizard (nombre, industria)
3. Elegir plan          Free / Pro / Business / Enterprise
4. Admin inicial        InitialAdminSetup (cuenta Auth = ADMIN)
5. Entrar               completeOnboarding → bind session → Home
```

**Login sin company:** Home muestra “Crear mi empresa” → mismo wizard.  
**Login con membership ACTIVE:** Home hub (sin wizard).

---

## Componentes

| Pieza | Path |
|-------|------|
| Modelos / planes | `platform/company/onboarding/OnboardingModels.kt` |
| Orquestador | `platform/company/onboarding/CreateCompanyFlow.kt` |
| Wizard empresa+plan | `platform/company/onboarding/CompanySetupWizard.kt` |
| Admin setup | `platform/company/onboarding/InitialAdminSetup.kt` |
| Host UI | `platform/company/onboarding/CompanyOnboardingScreen.kt` |
| Commit sesión | `CompanySessionManager.completeOnboarding` |
| Repo | `CompanyRepository.createCompany` (+ plan, limits, legalName, adminTitle) |

---

## Cambios de sesión (compat)

- `ensureSessionForUser(..., autoCreateIfMissing = false)` por defecto: **ya no crea** company sola.
- Si hay refs + membership ACTIVE → bind como antes.
- Si bind de preferida falla → **no** crea otro tenant (evita huérfanos H-12).
- `hasActiveCompany` exige membership `ACTIVE`.
- `needsOnboarding` = ready && !hasActiveCompany.
- Flag `autoCreateIfMissing = true` permanece para tests/legacy explícito.

---

## Persistencia al completar

Batch:

- `companies/{id}` — name, legalName, planId, industryPacks, limits, primaryContactEmail, status ACTIVE  
- `settings/main` — cuota AI alineada al plan  
- `company_index/{id}`  
- `memberships/{uid}` — roleCodes `[ADMIN]`, title, ACTIVE  
- espejo `users/{uid}/company_memberships/{id}`  

Luego: seed IAM + `upsertAssignment` + `TenantContext.bind`.

---

## Navegación

- `Screen.CompanyOnboarding`
- Register éxito → `company_onboarding`
- Home sin company → CTA “Crear mi empresa”
- Wizard éxito → Home (`popUpTo` onboarding)

---

## Compatibilidad

- Módulos legacy / platform clients-records-docs intactos.
- Usuarios con company existente no ven wizard.
- Register: owner local `isApproved=true` para continuar onboarding (Firestore legacy puede seguir con `false`; camino beta ya no bloquea al creador).

---

## Fuera de alcance (siguiente)

- Billing / pago real del plan  
- Invitar miembros post-onboarding  
- Editar plan desde settings  
- Sincronizar `usuarios.isApproved` con owner bootstrap en rules  

---

*Entrega Company Onboarding. Compilación verificada.*
