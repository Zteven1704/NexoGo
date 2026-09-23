# IDEAL PILOT PROFILE — NexoGo

**Rol:** Product Strategist  
**Fecha:** 2026-09-20  
**Objetivo:** Elegir **un único piloto inicial** que valide la plataforma **tal como está** (Beta-1).  
**Base:** `BETA_1_SCOPE.md` — Company, Users, Clientes, Expedientes, Documentos.  
**No es:** pedido de módulos nuevos ni roadmap de verticales.

---

## 1. Qué hay que validar (no el vertical completo)

| Capacidad actual | Pregunta de aprendizaje |
|------------------|-------------------------|
| Tenant + admin + invites | ¿Operan 2+ usuarios en una company? |
| Clientes | ¿Cargan contactos reales? |
| Expedientes | ¿Abren casos ligados a cliente? |
| Documentos | ¿Suben PDF/fotos del día a día? |
| Roles básicos | ¿ADMIN vs staff es suficiente al inicio? |

**No se valida en el 1º piloto:** citas, inventario/SKU, facturación, HC humana regulada, CRM, chat, billing.

---

## 2. Comparativa (4 verticales)

Escala **1 (mejor / menor) → 5 (peor / mayor)** según el criterio de cada columna.

| Criterio | Veterinaria | Consultorio médico | Empresa administrativa | Bodega |
|----------|-------------|--------------------|------------------------|--------|
| **Compatibilidad con módulos actuales** (C–E–D*) | **1** — dueño/caso/adjuntos | 2 — paciente/HC/adjuntos | 3 — cliente/proyecto/PDF | **5** — el core es stock, no C–E–D |
| **Menor esfuerzo de implementación** | **1** — pack default + copy app | 3 — mismas pantallas, más fricción comercial/legal | 2 — genérico, poco cambio técnico | **5** — sin inventario usable = no hay producto |
| **Menor riesgo** | **2** — expectativa de citas mitigable por contrato | **5** — compliance HC humana, reputación | 3 — “¿por qué no Drive?” | 4 — piloto “falla” por gap obvio |
| **Mayor valor de aprendizaje** | **1** — valida núcleo + narrativa nativa | 2 — valida privacidad/confianza (caro de aprender primero) | 3 — valida genérico B2B | 4 — enseña sobre todo lo que **falta** |
| **Fit ICP usuarios/docs** | 3–8 users; fotos/PDF | 5–20; HC pesada | 5–15; contratos | 5–30; remisiones altas |

\*C–E–D = Clientes + Expedientes + Documentos.

### Lectura por vertical

**Veterinaria**  
- Encaje natural con Beta-1 y onboarding `veterinary`.  
- Riesgo principal: “¿dónde están las citas / el inventario de fármacos?” → manejable con alcance escrito.  
- Mejor balance esfuerzo / riesgo / aprendizaje / fit.

**Consultorio médico**  
- Mismo esqueleto técnico (paciente ≈ cliente).  
- Riesgo y expectativa (citas, privacidad, rigor clínico) altos para un *first* pilot.  
- Mejor como **2º o 3º** piloto, no el primero.

**Empresa administrativa**  
- C–E–D sirve (clientes + expedientes de asunto + PDFs).  
- Menos presión clínica; más presión de “commodity vs Drive”.  
- Buen **segundo** piloto para probar genérico B2B.

**Bodega**  
- Compatibilidad mínima: el valor del vertical no está en C–E–D.  
- Implementar “piloto bodega” hoy = demostrar ausencia de inventario, no validar la plataforma.  
- Descartada como piloto inicial.

---

## 3. Determinación por criterio

| Pregunta | Ganador | Por qué |
|----------|---------|---------|
| **Menor esfuerzo de implementación** | **Veterinaria** | Cero features nuevas; pack y UX ya orientados; mismo APK Beta-1 |
| **Menor riesgo** | **Veterinaria** (con contrato) | Menor carga regulatoria que medicina humana; gaps (citas/stock) explicitables. Admin es “seguro” pero aprende poco; bodega/médico son peores en riesgo de fracaso percibido |
| **Mayor valor de aprendizaje** | **Veterinaria** | Uso real de C–E–D + multi-usuario en el dominio para el que nació el producto; feedback accionable sin pedir un módulo inexistente |
| **Mayor compatibilidad con módulos actuales** | **Veterinaria** | Clientes/expedientes/documentos mapean dueño–caso–archivo sin traducir el producto |

**Nota:** “Empresa administrativa” empataría en *esfuerzo técnico* casi con vet, pero pierde en compatibilidad narrativa y en aprendizaje específico del producto actual.

---

## 4. Perfil numérico del piloto recomendado

| Dimensión | Valor |
|-----------|--------|
| Vertical | **Veterinaria** (1 sede, barrio / clínica pequeña) |
| Usuarios | **3–8** (1 ADMIN + staff) |
| Clientes (30 d) | 50–200 |
| Expedientes (30 d) | 20–80 |
| Documentos (30 d) | 30–150 (&lt; 5 MB c/u) |
| Champion | Dueño o gerente con Android diario |
| Debe aceptar por escrito | Sin citas, sin inventario, sin billing en Beta-1 |

---

## 5. Recomendación: un único piloto inicial

### → Veterinaria pequeña (1 sede, 3–8 usuarios)

| Motivo | Detalle |
|--------|---------|
| Compatibilidad | Máxima con Clientes / Expedientes / Documentos |
| Esfuerzo | Mínimo: no hay que construir vertical nuevo |
| Riesgo | Controlable con contrato de alcance y guion `DEMO_FLOW.md` |
| Aprendizaje | Valida la plataforma donde el producto ya “habla” el idioma del cliente |

**No empezar por:** bodega (gap de inventario) ni consultorio médico (riesgo/compliance).  
**Después del éxito vet:** empresa administrativa → (más adelante) consultorio médico → bodega solo con inventario en alcance.

---

## 6. Checklist de selección del cliente

Elegir al piloto si cumple **≥ 6 de 8**:

- [ ] Veterinaria / servicio animal  
- [ ] Una sola ubicación  
- [ ] 3–8 usuarios  
- [ ] Champion disponible semanalmente  
- [ ] Acepta por escrito el alcance Beta-1  
- [ ] Volumen docs moderado al inicio  
- [ ] No exige migración histórica completa el mes 1  
- [ ] Tolera UX cutover a cambio de aislamiento por empresa  

---

## 7. Éxito a 4 semanas (este perfil)

- Company + ≥ 2 usuarios ACTIVE  
- ≥ 20 clientes, ≥ 10 expedientes, ≥ 20 documentos  
- 0 fuga cross-tenant  
- Champion opera C–E–D **sin** módulos legacy  

Eso valida **NexoGo Platform**; no valida aún “software veterinario completo”.

---

*Perfil de piloto ideal · un solo vertical inicial: Veterinaria.*
