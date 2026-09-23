# PILOT SUPPORT PLAN — NexoGo Beta-1

**Audiencia:** Champion del cliente + Customer Success + Ingeniería  
**Duración:** **30 días** de piloto cerrado (1 empresa)  
**Producto en alcance:** Login, Usuarios (admin), Clientes, Expedientes, Documentos  
**Fuera de soporte prioritario:** Módulos legacy, CRM, Chat, AI, Billing, Citas, Inventario  

Este plan define **cómo pedir ayuda**, **qué esperar**, y **qué hacer si algo falla** durante el piloto.

---

## 1. Principios

1. Un solo canal oficial — no tickets dispersos por WhatsApp personal.  
2. El **champion** es el primer filtro (L1). El staff reporta al champion; el champion reporta a NexoGo.  
3. Nunca compartir **contraseñas** ni códigos de verificación en el canal.  
4. Severidad la declara NexoGo (L2/L3), no el volumen de mensajes.  
5. Preferimos **recuperar y continuar** antes que borrar datos o reabrir reglas de seguridad.

---

## 2. Canal de soporte

| Ítem | Acuerdo piloto |
|------|----------------|
| Canal | Grupo dedicado (WhatsApp **o** Telegram **o** Slack) — **solo este piloto** |
| Nombre sugerido | `NexoGo · [Nombre clínica] · Piloto` |
| Participantes | Champion cliente + 1 backup · CSM NexoGo · L2 Ingeniería · CTO (solo lectura / L3) |
| Idioma | Español |
| Horario base | Lun–Vie, **09:00–18:00** (hora del champion, acordada en Día 0) |
| Fuera de horario | Solo **críticos** (ver §4); respuesta best-effort hasta el siguiente día laborable |

### 2.1 Cobertura por fase (30 días)

| Días | Intensidad | Qué incluye |
|------|------------|-------------|
| **0–3** | Reforzada | Check-ins diarios; respuesta extendida en franja acordada (ej. +2 h) |
| **4–14** | Estándar + check semanal | SLA §4; reunión 15 min semanal |
| **15–28** | Estándar | SLA §4; 1 check-in a mitad de periodo |
| **29–30** | Cierre | Retrospectiva + decisión continue / expand / stop |

### 2.2 Qué NO es el canal

- Pedidos de funciones nuevas fuera de Beta-1 (se anotan; no bloquean soporte).  
- Soporte a usuarios que no estén en la empresa piloto.  
- Temas de facturación comercial (canal aparte si aplica).

---

## 3. Cómo reportar errores

### 3.1 Flujo

```
Staff → Champion (L1) → Canal piloto (L2) → CTO (L3 si escala)
```

1. El usuario intenta de nuevo (cerrar sesión / reabrir app / otra red).  
2. Si falla, avisa al **champion** con captura.  
3. El champion envía **un solo mensaje** al canal con la plantilla (§3.2).  
4. L2 confirma recepción (“recibido, severidad X, ETA”) en &lt; tiempo de §4.

### 3.2 Plantilla de reporte (copiar/pegar)

```
[REPORTE]
Severidad sugerida: Crítico / Alto / Medio / Bajo
Cuándo: AAAA-MM-DD HH:MM (zona horaria)
Quién: nombre + correo de login
Empresa: nombre de la clínica (o companyId si lo saben)
Dispositivo: marca/modelo
Android: versión
App: versión / fecha de instalación del APK
Qué hacía: (1–2 frases)
Pasos para reproducir:
1.
2.
3.
Qué esperaba:
Qué pasó (mensaje de error exacto si hay):
¿Otros usuarios afectados? Sí/No
Adjunto: captura o video corto
```

### 3.3 Checklist antes de enviar

- [ ] No hay contraseña en el mensaje ni en la captura.  
- [ ] El correo es el de login (exacto).  
- [ ] Si es un archivo: tamaño aproximado y tipo (PDF/JPG).  
- [ ] Si es permisos: rol del usuario (Admin / Staff).

### 3.4 Severidades (guía para el champion)

| Severidad | Ejemplo | Acción del champion |
|-----------|---------|---------------------|
| **Crítico** | Nadie puede entrar; sospecha de ver datos de otra empresa; pérdida masiva de archivos | Avisar ya; marcar `CRÍTICO` |
| **Alto** | Un rol completo no puede trabajar (ej. nadie sube documentos); invitaciones rotas | Reportar el mismo día |
| **Medio** | Un usuario puntual; un expediente no abre; un upload falla a veces | Reportar en horario laboral |
| **Bajo** | Texto confuso; sugerencia; cosmético | Anotar; no urge |

---

## 4. Tiempos de respuesta (SLA piloto)

Horario laboral = Lun–Vie 09:00–18:00 (hora acordada).  
“Respuesta” = acuse + severidad confirmada + siguiente paso (no necesariamente solución final).

| Severidad | Primera respuesta (L2) | Actualización | Objetivo de resolución* |
|-----------|------------------------|---------------|-------------------------|
| **Crítico** | **&lt; 4 horas** laborables | Cada 4 h laborables hasta contención | Contención el mismo día; fix o workaround ≤ 2 días laborables |
| **Alto** | **&lt; 1 día** laborable | Diario laborable | ≤ 5 días laborables o waiver documentado |
| **Medio** | ≤ 2 días laborables | Al avanzar | Dentro del piloto o backlog post-piloto |
| **Bajo** | ≤ 5 días laborables | Si aplica | Backlog / mejora |

\*Resolución en piloto puede ser **workaround** (instrucción + APK previo + desactivar usuario), no siempre un fix de código inmediato.

### 4.1 Compromisos del cliente

| Compromiso | Detalle |
|------------|---------|
| Champion disponible | Al menos 1 persona responde en el canal en &lt; 4 h laborables |
| Ventana de prueba | Facilitar 1 dispositivo de prueba si L2 lo pide |
| No escalar por volumen | Un hilo por incidente; evitar spam de “¿hay novedades?” antes del ETA |

### 4.2 Fuera de alcance del SLA

- Redes Wi‑Fi / datos del cliente.  
- Dispositivos Android &lt; 8 o almacenamiento lleno.  
- Uso de módulos no incluidos en Beta-1.  
- Cambios de alcance comercial a mitad del piloto.

---

## 5. Procedimiento de recuperación

Orden preferido: **contener → recuperar acceso → recuperar datos → comunicar**.

### 5.1 Matriz rápida

| Situación | Qué hace el champion | Qué hace NexoGo |
|-----------|----------------------|-----------------|
| No puede iniciar sesión | Verificar correo; “Olvidé contraseña”; otra red | Revisar Auth; reset asistido; no abrir rules |
| No ve la empresa / invitación | Confirmar email exacto de la invitación | Revisar invite + membership ACTIVE |
| “Sin permiso” / PERMISSION_DENIED | Confirmar que el usuario está activo | Verificar rules desplegadas + rol; **no** “abrir rules” |
| Falló un documento al subir | No reintentar en bucle; archivo &lt; 5 MB preferible | Revisar Storage + red; limpiar huérfanos si aplica |
| Datos incorrectos de un cliente/expediente | Corregir en la app si puede | Corrección puntual en consola **con respaldo previo** |
| App se cierra sola (crash loop) | Anotar desde cuándo; no borrar datos aún | Redistribuir APK del tag anterior si hace falta |
| Sospecha de ver datos de otra clínica | **Dejar de usar la app**; avisar CRÍTICO | Pausa piloto + rollback de rules (L3) |

### 5.2 Recuperación de acceso (usuario)

1. Champion: User Management → confirmar usuario **activo** (no desactivado / revocado).  
2. Usuario: restablecer contraseña desde la pantalla de login.  
3. Si sigue fallando: L2 revisa Auth (usuario existe, email verificado si aplica).  
4. Último recurso: nueva invitación **solo** si L2 lo indica (evitar ciclo revoke→re-invite sin guía).

### 5.3 Recuperación de app (cliente)

1. Instalar APK/AAB del **tag conocido bueno** (el de go-live o el anterior).  
2. Confirmar que es el build del proyecto piloto (no mezclar con demos).  
3. Borrar datos de la app **solo** si L2 lo pide (último recurso; puede perder sesión local).

### 5.4 Recuperación de datos

| Nivel | Disponibilidad en piloto | Acción |
|-------|--------------------------|--------|
| Corrección de un documento | Sí | Editar en app o consola asistida |
| Export asistido (CSV/JSON puntual) | Bajo demanda, L2/L3 | Antes de cambios mayores si el champion lo pide |
| Restore automático point-in-time | **No** (aún no hay producto de backup UI) | Contención + reconstrucción manual guiada |
| Wipe de empresa / proyecto | Solo con **aprobación escrita** champion + CTO | Extremadamente excepcional |

**Regla de oro:** no borrar la company ni el proyecto Firebase para “arreglar” un bug.

### 5.5 Comunicación en incidente

1. L2/L3 declara severidad.  
2. **Un mensaje** en el canal: qué pasó · impacto · qué hacer ahora · ETA.  
3. Al cerrar: mensaje de cierre + (si Crítico/Alto) resumen breve en retrospectiva.

---

## 6. Escalamiento

### 6.1 Niveles

| Nivel | Quién | Responsabilidad |
|-------|-------|-----------------|
| **L1** | Champion (cliente) | Triage, capturas, reintentos, filtrar ruido |
| **L2** | Ingeniería NexoGo (+ CSM en la comunicación) | Diagnóstico, workaround, fix, APK |
| **L3** | CTO | Seguridad, pérdida de datos, rollback, pausa del piloto |

### 6.2 Cuándo escalar L1 → L2

- Tras 1 intento de guía básica (login / reabrir / otro usuario).  
- Cualquier **Crítico** o **Alto**.  
- Más de 2 usuarios con el mismo síntoma.

### 6.3 Cuándo escalar L2 → L3 (inmediato)

- Sospecha de **fuga entre empresas** (cross-tenant).  
- Pérdida o corrupción **masiva** de clientes / expedientes / archivos.  
- Imposibilidad total de operar &gt; 4 h laborables sin workaround.  
- Necesidad de rollback de rules, wipe, o pausa formal del piloto.

### 6.4 Cadena de contacto (rellenar en Día 0)

| Rol | Nombre | Canal | Backup |
|-----|--------|-------|--------|
| Champion L1 | | | |
| Backup L1 | | | |
| CSM | | Canal piloto | |
| Ingeniería L2 | | Canal piloto | |
| CTO L3 | | Canal + teléfono de emergencia | |

Teléfono de emergencia L3: **solo Críticos**, y solo si el canal no responde en la ventana de §4.

### 6.5 Pausa del piloto

CTO puede declarar **pausa** si:

- Hay riesgo de seguridad de datos, o  
- No hay workaround viable para operación diaria.

Durante la pausa: mensaje claro al champion; no pedir al staff que “siga intentando”; plan de reanudación con fecha.

---

## 7. Rituales de Customer Success (30 días)

| Día | Ritual | Dueño |
|-----|--------|-------|
| **0** | Kickoff: canal, plantilla, contactos, franja reforzada | CSM |
| **1** | Check-in 15 min: ¿login + 1 cliente + 1 doc OK? | CSM + Champion |
| **3** | Cierre ventana reforzada; resumen de tickets abiertos | CSM |
| **7 / 14 / 21** | Health check 15 min (uso + dolores + KI conocidos) | CSM + L2 |
| **28–30** | Retrospectiva: seguir / expandir / parar | CSM + CTO + Champion |

### 7.1 Criterios de “soporte saludable”

- ≤ 2 Críticos abiertos a la vez.  
- Ningún Crítico sin acuse fuera de SLA.  
- Champion usa la plantilla en ≥ 80 % de reportes.  
- Al menos 1 usuario staff además del admin opera Clientes → Expedientes → Documentos en la semana 2.

---

## 8. Runbook de respuestas frecuentes (L1)

| Síntoma | Primera ayuda |
|---------|----------------|
| “No me deja entrar” | Correo exacto · restablecer contraseña · probar datos móviles |
| “No veo la clínica” | ¿Aceptó la invitación? · ¿mismo correo? · pedir a admin reenviar |
| “No puedo subir PDF” | Archivo más liviano · buena red · no reintentar 10 veces · reportar |
| “No aparece mi cliente en expedientes” | Crear el cliente primero · en beta la lista puede ser corta · avisar a soporte |
| “La app se ve rara / menús de más” | Usar solo Clientes, Expedientes, Documentos (Beta-1) · reportar confusión como Bajo |

Guías: `STAFF_GUIDE.md` (staff) · `ADMIN_GUIDE.md` (admin).

---

## 9. Cierre del piloto (día 30)

1. Lista de incidentes abiertos / cerrados / waivers.  
2. Encuesta breve al champion (1–5): ¿útiles Clientes / Expedientes / Documentos?  
3. Decisión documentada: **continuar**, **expandir usuarios**, o **detener**.  
4. Si continúan: nuevo acuerdo de soporte (ya no “ventana reforzada día 0–3” por defecto).

---

## 10. Resumen ejecutivo (fijar en el canal el Día 0)

> **Canal:** grupo dedicado del piloto.  
> **Reportar:** plantilla §3.2 · sin contraseñas.  
> **SLA:** Crítico &lt; 4 h laborables · Alto &lt; 1 día laborable.  
> **Recuperar:** acceso → APK previo → corrección puntual · **nunca** abrir rules ni borrar la empresa.  
> **Escalar a CTO:** fuga de datos, pérdida masiva, o app inutilizable sin workaround.

---

*Plan de soporte para piloto de 30 días · alineado con `PILOT_DEPLOYMENT_PLAN.md` §8–9 y `GO_LIVE_CHECKLIST.md` (SOPORTE).*
