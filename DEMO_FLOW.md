# DEMO FLOW — NexoGo Beta-1

**Audiencia:** Cliente potencial / piloto  
**Duración máxima:** **10 minutos**  
**Objetivo:** Mostrar el núcleo real: empresa → equipo → cliente → expediente → documento  
**Alcance:** Solo Beta-1 (`BETA_1_SCOPE.md`)  
**Código:** No se modifica — guion de demostración  

---

## Mensaje de apertura (15 s)

> “En los próximos minutos vamos a montar una empresa en NexoGo, invitar a un colaborador y completar el ciclo operativo: cliente, expediente y documento — todo aislado en esa empresa.”

---

## Preparación (antes de la reunión)

| Ítem | ☐ |
|------|---|
| App instalada; Firebase rules + indexes del entorno demo | |
| Wi‑Fi estable; no molestar en el dispositivo | |
| Email A libre (administrador): p.ej. `demo.admin@…` | |
| Email B libre (usuario): p.ej. `demo.staff@…` | |
| Password listas (≥ 6 caracteres) | |
| Un PDF o foto &lt; 2 MB en el teléfono | |
| Guion impreso o segunda pantalla con este doc | |

**Tip:** Si solo hay un dispositivo, el paso 3 usa logout/login rápido. Con dos teléfonos la demo fluye mejor.

---

## Cronómetro (estricto)

| Tiempo | Pasos | Qué ve el cliente |
|--------|-------|-------------------|
| 0:00–0:15 | Apertura | Promesa |
| 0:15–1:45 | 1 + 2 | Empresa + admin en Home |
| 1:45–4:00 | 3 | Usuario invitado / dentro |
| 4:00–5:30 | 4 | Cliente creado |
| 5:30–7:00 | 5 | Expediente creado |
| 7:00–9:00 | 6 + 7 | Documento subido y consultado |
| 9:00–10:00 | Cierre + 1 pregunta | Valor + siguiente paso |

Si el reloj pasa de **4:00** sin terminar el paso 3: muestra la invitación pendiente y continúa con el admin; el accept de B se resume en una frase (“el colaborador acepta con el mismo email y entra a la misma empresa”).

---

## Paso 1 — Crear empresa (~1 min)

1. Abrir NexoGo → **Registrarse**.  
2. Nombre, **email A**, contraseña, confirmar → registrar.  
3. Pantalla **Configurar empresa**:  
   - Nombre comercial (ej. “Clínica Demo NexoGo”)  
   - Industria (p.ej. Veterinaria)  
   - Continuar → elegir plan inicial (Free/Pro; **sin cobro en la demo**)  

**Éxito:** Avanza al paso de administrador.

---

## Paso 2 — Crear administrador (~30 s)

1. Confirmar nombre del administrador y email de contacto.  
2. Pulsar **Crear empresa y entrar**.  
3. Señalar en Home el **nombre de la empresa** en la barra.  

**Éxito:** Sesión activa; esta cuenta es el **ADMIN** de la empresa.  
*(Administrador = la misma cuenta que se registró; no hay un “segundo alta” aparte.)*

---

## Paso 3 — Crear usuario (~2 min)

**Como admin:**

1. Abrir **Administración** → **Usuarios de la empresa**.  
2. Invitar: **email B**, rol **EMPLOYEE**, nombre opcional → **Enviar invitación**.  
3. Mostrar la invite en “pendientes”.  

**Como usuario B** (mismo u otro dispositivo):

4. Registro o login con **exactamente email B**.  
5. En invitaciones → **Aceptar e ingresar**.  
6. Home con la **misma** empresa (no crear otra).  

**Éxito:** Dos personas en un solo tenant.  

**Frase si hay poco tiempo:**  
> “El segundo usuario recibe la invitación, entra con su email y queda dentro de la empresa sin ver datos de otras.”

---

## Paso 4 — Crear cliente (~1 min)

1. (Admin o staff) Home → **Clientes** (no “Pacientes”).  
2. Nuevo → nombre (ej. “María Pérez”) → crear.  

**Éxito:** Cliente en la lista.  
**Narrativa:** “Este es el contacto o dueño con el que trabaja el equipo.”

---

## Paso 5 — Crear expediente (~1 min)

1. Home → **Expedientes**.  
2. Nuevo → seleccionar el cliente → título (ej. “Consulta inicial”) → crear.  

**Éxito:** Expediente listado, ligado al cliente.  
**Narrativa:** “El expediente es el caso o historial de trabajo sobre ese cliente.”

---

## Paso 6 — Subir documento (~1 min)

1. Home → **Documentos**.  
2. Subir → elegir el PDF/foto preparado.  
3. Esperar confirmación de éxito.  

**Éxito:** Archivo en la lista (nombre, tipo, tamaño).  
**Narrativa:** “Los archivos quedan bajo la empresa, no en un Drive genérico suelto.”

---

## Paso 7 — Consultar documento (~45 s)

1. En la lista de **Documentos**, señalar la tarjeta del archivo recién subido.  
2. Leer en voz alta: nombre y tipo.  
3. Opcional: salir y volver a Documentos → el ítem **sigue ahí**.  

**Éxito:** Consulta = ver y confirmar persistencia en el listado.  
**Límite honesto (si preguntan):** “En Beta-1 la consulta es desde el listado; el visor PDF completo viene después.”

---

## Cierre (1 min)

> “En menos de diez minutos: empresa, administrador, colaborador, cliente, expediente y documento — en un espacio aislado por empresa. Eso es el núcleo de NexoGo que queremos validar con ustedes en piloto.”

**Pregunta de cierre (una sola):**  
> “Si tuvieran esto mañana en su operación, ¿quién sería el primer usuario además del dueño?”

---

## Qué no mostrar

| Evitar | Motivo |
|--------|--------|
| Pacientes / Historial clínico / Citas / Inventario / Ventas / Chat | Fuera de Beta-1; confunden |
| Aprobación de usuarios “legacy” | No es el invite de empresa |
| Prometer billing, Drive backup o citas | No están en el contrato demo |

---

## Plan B (si algo falla)

| Problema | Qué hacer en vivo |
|----------|-------------------|
| Invite no aparece | Verificar email B carácter a carácter; seguir con admin |
| Error de permisos | No tocar rules; usar la cuenta ya dentro de la empresa |
| Upload falla | Reintentar con JPG/PDF pequeño |
| Se acaba el tiempo | Completar hasta cliente + expediente; documento en 30 s o screenshot previo |

---

## Checklist post-demo

- [ ] ¿El cliente vio empresa + equipo + C–E–D?  
- [ ] ¿Quedó claro el aislamiento por empresa?  
- [ ] ¿Siguiente paso acordado (piloto / segunda reunión)?  

---

*Demostración comercial Beta-1 · máximo 10 minutos · sin cambios de código.*
