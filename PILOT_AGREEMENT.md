# PILOT AGREEMENT — NexoGo Beta-1

**Tipo:** Documento operativo de expectativas (no es contrato legal)  
**Producto:** NexoGo · Beta-1  
**Duración típica:** 30 días  
**Perfil:** 1 empresa piloto (preferencia: veterinaria pequeña)  
**Referencias:** `BETA_1_SCOPE.md` · `PILOT_SUPPORT_PLAN.md` · `KNOWN_ISSUES.md` · `ADMIN_GUIDE.md` · `STAFF_GUIDE.md`

---

## Propósito

Alinear a **NexoGo** y al **cliente piloto** sobre qué se prueba, qué no se promete, y quién hace qué.

Al firmar/aceptar este documento (email o canal de soporte), ambas partes confirman que:

1. Entienden el alcance Beta-1.  
2. Aceptan las limitaciones conocidas.  
3. Usarán el canal y los SLA de `PILOT_SUPPORT_PLAN.md`.

> **Esto no sustituye** un contrato comercial, NDA, ni términos legales. Es la “hoja de expectativas” del piloto.

---

## 1. Alcance Beta-1

### 1.1 Qué es Beta-1

Un piloto **cerrado** para validar el núcleo multi-empresa:

```
Alta de empresa → Equipo (invitar / activar) → Clientes → Expedientes → Documentos
```

Todo el trabajo operativo queda **aislado por empresa** (`companies/{companyId}`).

### 1.2 Qué no es Beta-1

- No es el ERP / clínica completa.  
- No sustituye citas, inventario, facturación, chat ni reportes legacy.  
- No es un lanzamiento público ni multi-tenant masivo.  
- No es historial clínico regulado ni archivado legal certificado.

### 1.3 Condiciones del piloto

| Condición | Acuerdo |
|-----------|---------|
| Empresas | **1** company en el entorno piloto |
| Usuarios | Orientativo **1–15** (ideal 3–8) |
| Plataforma | Android (APK/AAB entregado) |
| Auth | Email + contraseña |
| Duración | **30 días** desde go-live (D0), salvo extensión acordada |
| Datos | Datos reales o de práctica; el cliente decide qué cargar |
| Éxito | Uso diario del flujo Clientes → Expedientes → Documentos + feedback |

---

## 2. Funcionalidades disponibles

Capacidades **soportadas, capacitadas y con compromiso de respuesta** durante el piloto:

| # | Funcionalidad | Qué puede hacer el piloto |
|---|---------------|---------------------------|
| 1 | **Registro e inicio de sesión** | Crear cuenta e ingresar con email/password |
| 2 | **Crear empresa (onboarding)** | Nombre, datos básicos, primer administrador |
| 3 | **Aceptar invitación** | Unirse a la empresa con el mismo email de la invitación |
| 4 | **Home de la empresa** | Entrar al espacio de trabajo de la company activa |
| 5 | **Clientes** | Listar y crear clientes |
| 6 | **Expedientes** | Listar y crear expedientes ligados a un cliente |
| 7 | **Documentos** | Listar y subir archivos asociados al trabajo diario |
| 8 | **Usuarios (admin)** | Invitar, activar, desactivar, cambiar rol, revocar acceso |
| 9 | **Perfil / cerrar sesión** | Uso diario básico |
| 10 | **Ajustes existentes** | Solo lo ya presente en la app (sin features nuevas) |

**Flujo feliz acordado:**

Registro → Onboarding → Home → Invitar staff → Cliente → Expediente → Documento.

Guías: `ADMIN_GUIDE.md` (admin) · `STAFF_GUIDE.md` (staff) · `DEMO_FLOW.md` (recorrido corto).

---

## 3. Funcionalidades no disponibles

### 3.1 Fuera de contrato (aunque aparezcan en el menú)

Pueden verse entradas legacy por compatibilidad. **No se capacitan, no se demuestran como producto y no tienen SLA prioritario.**

| Área | Ejemplos |
|------|----------|
| Pacientes legacy | Pantallas “Pacientes” distintas de Clientes platform |
| Historias / clínico legacy | Historial, medical records legacy |
| Citas | Agenda / appointments |
| Inventario | Stock, categorías, SKU |
| Ventas / servicios | Cobros, catálogo de servicios |
| Chat / chatbot | Mensajería interna, bot |
| Reportes / dashboard widgets | KPIs legacy, hub Dashboard |
| Aprobación global de usuarios | User Approval distinto de invitaciones de empresa |

**Respuesta estándar si preguntan:**  
> “No está incluido en Beta-1. El piloto valida empresa, equipo, clientes, expedientes y documentos.”

### 3.2 Bloqueadas / no abiertas en el piloto

| Capacidad | Estado |
|-----------|--------|
| CRM (leads, oportunidades) | No incluida |
| Tareas (Tasks) como producto | No incluida |
| Chat platform / push | No incluida |
| AI / OpenAI | No incluida |
| Centro de notificaciones | No incluida |
| Billing / pagos reales | No incluida |
| Backup & Recovery con UI | No incluida (solo plan interno) |
| Cambio entre varias empresas (switcher) | No incluida |
| Editor avanzado de roles/permisos | No incluida |
| MFA / App Check como producto | No incluida |
| Importación masiva (Excel/CSV) | No incluida |
| iOS / Web como cliente piloto | Fuera de alcance salvo acuerdo aparte |

### 3.3 Experimentales (pueden existir; sin promesa)

PermissionEngine en navegación, planes/límites en onboarding, analytics de uso y auditoría interna: **uso interno o best-effort**, no compromiso de producto al cliente.

---

## 4. Limitaciones conocidas

El piloto acepta operar con estas limitaciones. Detalle técnico: `KNOWN_ISSUES.md`.

### 4.1 Producto / UX

| Limitación | Impacto práctico |
|------------|------------------|
| UX de cutover mínima | Algunas pantallas se sienten “básicas” o mixtas con menús legacy |
| Edición/archivo de clientes limitado | Enfoque en crear y listar |
| Picker de clientes en expedientes puede mostrar pocos | Ideal trabajar con un conjunto reducido al inicio |
| Documentos = lista + subida | Sin visor PDF rico ni gestión avanzada de versiones |
| Preferencia de archivos pequeños | Orientativo **&lt; 5 MB**; evitar lotes masivos |
| Un usuario ↔ una empresa en el piloto | No hay switcher multi-company |
| Roles simples | Ideal ADMIN + staff; no permisos granulares por pantalla en writes |

### 4.2 Operación / seguridad (mitigaciones)

| Tema | Acuerdo operativo |
|------|-------------------|
| No usar “aprobación legacy” como control | El acceso al equipo es por **invitación de empresa** |
| No compartir `companyId` ni links internos | Reduce riesgo de acceso indebido |
| Evitar ciclo revoke → re-invitar sin soporte | Puede fallar; pedir ayuda a L2 |
| Tras desactivar/revocar, validar sesión del usuario | El espejo de membresía puede desfasarse |
| Si un upload falla a medias | No reintentar en bucle; avisar a soporte |
| Sospecha de ver datos de otra empresa | Parar uso y escalar **Crítico** de inmediato |

### 4.3 Lo que el piloto NO garantiza

- Disponibilidad 99.9 % ni soporte 24×7.  
- Restore automático point-in-time de todos los datos.  
- Cumplimiento normativo de historia clínica humana.  
- Migración completa desde otro sistema en el día 0.

---

## 5. Responsabilidades del cliente piloto

| # | Responsabilidad |
|---|-----------------|
| 1 | Designar un **champion** (y un backup) con autoridad para decidir y reportar |
| 2 | Usar el **canal oficial** de soporte y la plantilla de reporte (`PILOT_SUPPORT_PLAN.md`) |
| 3 | Capacitar al staff **solo** en el alcance Beta-1 (Clientes / Expedientes / Documentos + login) |
| 4 | No tratar menús legacy como parte del producto entregado |
| 5 | Proporcionar dispositivos Android adecuados (Android 8+) y red estable |
| 6 | Decidir qué datos reales cargar; no subir información que no deban tener en un entorno beta |
| 7 | No compartir contraseñas en el canal; gestionar altas/bajas de su equipo |
| 8 | Responder check-ins acordados (días 0–3, semanales) en horario laboral |
| 9 | Reportar incidentes con evidencia (pasos, captura, usuario, hora) |
| 10 | Participar en la retrospectiva de cierre (días 28–30) y dar feedback honesto |
| 11 | No redistribuir el APK fuera del equipo autorizado del piloto |
| 12 | Aceptar que NexoGo puede **pausar** el piloto ante riesgo de seguridad de datos |

---

## 6. Responsabilidades de NexoGo

| # | Responsabilidad |
|---|-----------------|
| 1 | Entregar un build instalable del entorno **piloto** (APK/AAB) alineado a Beta-1 |
| 2 | Mantener proyecto Firebase/rules/indexes del piloto según `PILOT_DEPLOYMENT_PLAN.md` y go-live checklist |
| 3 | Capacitar al champion (y, si aplica, sesión corta al staff) en el alcance acordado |
| 4 | Operar el canal de soporte y cumplir los **SLA del piloto** (Crítico &lt; 4 h laborables; Alto &lt; 1 día laborable) |
| 5 | Diagnosticar y contener incidentes; ofrecer workaround o APK previo cuando aplique |
| 6 | No desplegar reglas abiertas (`allow all`) para “arreglar” un bug |
| 7 | Comunicar con claridad cambios que afecten operación (mensaje único en el canal) |
| 8 | Proteger el aislamiento por empresa como prioridad máxima; escalar a CTO si hay sospecha de fuga |
| 9 | Documentar limitaciones conocidas y no vender capacidades fuera de §2 |
| 10 | Facilitar check-ins CSM y retrospectiva de cierre con decisión continue / expand / stop |
| 11 | Preservar datos del piloto salvo wipe **explícitamente aprobado** por champion + CTO |
| 12 | Tratar feedback del piloto como entrada de producto; no prometer fechas de features fuera de alcance |

---

## 7. Soporte y comunicación (resumen)

| Tema | Acuerdo |
|------|---------|
| Canal | Grupo dedicado del piloto |
| Horario | Lun–Vie 09:00–18:00 (hora acordada en D0) |
| Refuerzo | Días 0–3 con cobertura extendida acordada |
| Detalle | Ver `PILOT_SUPPORT_PLAN.md` (reporte, recuperación, escalamiento) |

---

## 8. Datos, confidencialidad operativa y salida

| Tema | Expectativa |
|------|-------------|
| Propiedad de datos del negocio | Del cliente piloto |
| Uso por NexoGo | Soporte, diagnóstico, mejora del producto; no reventa de datos del cliente |
| Export | Asistido bajo demanda (limitado); sin UI completa de backup en Beta-1 |
| Fin del piloto | Decisión conjunta: continuar, expandir usuarios, o detener |
| Si se detiene | Acceso se cierra según acuerdo; datos no se borran sin aprobación escrita |

---

## 9. Criterios de éxito (compartidos)

El piloto se considera **útil** si al día 30:

1. El owner completó onboarding y opera en Home.  
2. Al menos un staff (además del admin) usó Clientes → Expedientes → Documentos.  
3. No hubo incidente de fuga cross-tenant sin contención.  
4. Hay feedback escrito del champion (qué sirve / qué falta / si continuarían).

El piloto **no falla** solo porque falten citas, inventario o facturación: eso está fuera de alcance por diseño.

---

## 10. Aceptación operativa

Completar al inicio del piloto (D0 o antes):

| Campo | Cliente piloto | NexoGo |
|-------|----------------|--------|
| Organización | | NexoGo |
| Nombre | | |
| Rol | Champion / Owner | CSM / CTO |
| Email | | |
| Canal de soporte | | |
| Fecha inicio (D0) | | |
| Fecha fin prevista (+30) | | |

**Confirmación:**

> Hemos leído este documento. Entendemos el alcance Beta-1, las funcionalidades incluidas y excluidas, las limitaciones conocidas, y las responsabilidades de cada parte. Este texto es un acuerdo operativo de expectativas, no un contrato legal.

| | Firma / “Acepto” (email o mensaje en canal) | Fecha |
|--|---------------------------------------------|-------|
| Cliente piloto | | |
| NexoGo | | |

---

*Acuerdo operativo de piloto · SaaS Product Management · alineado a Beta-1.*
