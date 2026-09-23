# NexoGo Platform — Clients Module Migration

> **Migración completa: Pacientes → Clientes** (multiindustria).  
> Fecha: 2026-09-18  
> **No modifica código.**  
>  
> Fuentes: `MASTER_ARCHITECTURE.md` · `BUSINESS_PLATFORM_ARCHITECTURE.md` · `MODULE_DESIGN.md` ·  
> `COMPANY_IMPLEMENTATION_PLAN.md` · `ROLE_SYSTEM.md` · `MIGRATION_MASTER_PLAN.md`

---

## 1. Objetivo

Sustituir el dominio **Pacientes** (modelo veterinario + DataStore local) por el módulo **Clientes**, genérico y multiindustria, de modo que la misma plataforma sirva a:

| Vertical | El “Cliente” representa |
|----------|-------------------------|
| **Veterinarias** | Mascota (sujeto) + tutor(es) |
| **Médicos** | Paciente humano |
| **Bodegas** | Cuenta compradora / razón social / punto de entrega |
| **Empresas** | Cuenta B2B (empresa cliente) |
| **Talleres** | Vehículo / activo + propietario |
| **Consultoras** | Cliente persona u organización + proyecto/contacto |

Principio:

> El **core** modela relación operativa y comercial.  
> Lo específico de industria vive en **`clientType` + `profile` (schema del pack)** — nunca campos fijos `species`/`breed` en el núcleo.

---

## 2. Estado actual (análisis)

### 2.1 Qué hay hoy

| Pieza | Ubicación | Comportamiento |
|-------|-----------|----------------|
| UI activa | `PatientsScreen`, `CreateEditPatientScreen` | Ruta `patients` desde Home |
| Estado | `viewmodel.PatientViewModel` | Singleton + **DataStore** |
| Modelo UI | `SimplePatient` + `SimplePet[]` | Dueño humano + lista de mascotas |
| Samples | Hardcoded en ViewModel | Si DataStore vacío |
| Firestore tipado | `repository.FirebaseRepository` → `patients` | **No usado por la UI activa** |
| Módulo paralelo | `modules/patients/*` | Fuera del NavGraph vivo |
| Citas / clínico | Usan `PatientViewModel` | Acoplados al modelo local |

### 2.2 Limitaciones del modelo actual

```
SimplePatient { name, phone, email, address, pets[] }
SimplePet     { name, species, breed, age }
```

| Limitación | Impacto |
|------------|---------|
| Asume dueño + mascotas | Inútil para médicos, bodegas, consultoras |
| Sin `companyId` | No SaaS |
| Solo local | No multi-dispositivo / backup cloud |
| Doble camino DataStore vs Firestore | Migración ambigua |
| Naming “Patient/Pet” | Contamina Agenda, Expedientes, Ventas |

### 2.3 Dependencias que hay que re-apuntar

| Consumidor | Uso actual de Pacientes | Destino |
|------------|-------------------------|---------|
| `CreateAppointmentScreen` | Lista pacientes/mascotas | Lista **Clientes** (+ label del pack) |
| Clinical / History | `patientId` / owner | `clientId` |
| Home / nav | “Pacientes” | “Clientes” |
| Reports placeholders | patients_reports | clients_reports |
| Future Ventas/CRM/Docs | — | Links a `clientId` |

---

## 3. Modelo objetivo: Client (core)

### 3.1 Entidad Client

```
Client
├── id                          # clientId
├── companyId                   # tenant (obligatorio)
├── displayName                 # nombre mostrado en listas
├── clientType                  # ver §3.2
├── status                      # ACTIVE | INACTIVE | ARCHIVED
├── primaryContactId?           # Contact
├── emails[] / phones[]         # canales directos opcionales
├── addresses[]                 # facturación / entrega / sede
├── taxId?                      # NIT/RFC/CIF cuando aplica
├── tags[]
├── media
│     avatarUrl? / avatarDocumentId?
├── profile                     # map/JSON validado por pack (§4)
├── searchTokens[]              # displayName, taxId, placas, etc.
├── linkedUserId?               # uid portal CLIENT (membership)
├── createdAt / updatedAt
├── createdBy / updatedBy
└── archivedAt?
```

### 3.2 clientType (núcleo cerrado)

| Tipo | Uso típico |
|------|------------|
| `PERSON` | Médicos, consultora persona, contacto principal |
| `PET` | Veterinaria (mascota como sujeto de servicio) |
| `ASSET` | Talleres (vehículo, máquina), a veces bodega (unidad) |
| `ORG` | Empresas, bodegas (cuenta), consultora corporativa |
| `CUSTOM` | Extensión futura con schema pack |

### 3.3 Contact (separado del Client)

Evita el anti-patrón actual “paciente = humano que contiene pets”.

```
Contact
├── id
├── companyId
├── clientIds[]                 # puede vincularse a N clients
├── fullName
├── email? / phone? / whatsapp?
├── role                        # OWNER | GUARDIAN | BILLING | EMERGENCY | BUYER | DRIVER | OTHER
├── isPrimary?
├── notes?
└── timestamps
```

**Ejemplos:**

| Vertical | Client | Contact |
|----------|--------|---------|
| Vet | PET “Max” | GUARDIAN “María” |
| Médico | PERSON “María” | (opcional familiar) |
| Taller | ASSET “ABC-123” | OWNER “Juan” |
| Empresa | ORG “Acme SA” | BUYER “Compras” |
| Bodega | ORG “Retail X” | DRIVER / BILLING |
| Consultora | ORG o PERSON | BILLING + sponsor |

### 3.4 Relación 360°

```
Client 1──* Appointment
Client 1──* Record (Expediente)
Client 1──* Sale
Client 1──* DocumentLink
Client 1──* Opportunity (CRM)
Client 0──* Conversation (chat context)
Client *──* Contact
```

---

## 4. Perfiles por industria (`profile` + packs)

El core **no** define `species`. Cada Company activa packs (`Company.industryPacks`) que aportan schema de `profile`.

### 4.1 Veterinarias (`pack: veterinary`)

**clientType preferido:** `PET`  
**Contacts:** GUARDIAN / OWNER / EMERGENCY

```json
"profile": {
  "species": "dog",
  "breed": "Labrador",
  "sex": "M",
  "birthDate": "2021-05-01",
  "weightKg": 28.5,
  "chipId": "...",
  "neutered": true,
  "color": "dorado"
}
```

Migración desde `SimplePet` + dueño → ver §7.

### 4.2 Médicos (`pack: clinic` / `medical`)

**clientType:** `PERSON`

```json
"profile": {
  "documentId": "CC/DNI",
  "birthDate": "...",
  "sex": "...",
  "bloodType": "...",
  "allergies": [],
  "insuranceProvider": "...",
  "insuranceNumber": "..."
}
```

UI labels: “Paciente” como **display del pack**, ruta interna sigue siendo Clientes.

### 4.3 Bodegas (`pack: warehouse`)

**clientType:** `ORG` (cuenta) o `PERSON` (mostrador)

```json
"profile": {
  "accountCode": "C-1042",
  "creditLimit": 5000000,
  "paymentTermsDays": 30,
  "deliveryPreference": "PICKUP" | "DELIVERY",
  "warehouseNotes": "..."
}
```

Addresses: sede fiscal + puntos de entrega.

### 4.4 Empresas (`pack: business_b2b`)

**clientType:** `ORG`

```json
"profile": {
  "industry": "manufacturing",
  "employeeCountRange": "50-200",
  "accountManagerUserId": "uid_...",
  "contractStart": "...",
  "slaTier": "gold"
}
```

taxId + legalName en core (`displayName` / `taxId` / address BILLING).

### 4.5 Talleres (`pack: workshop` / `automotive`)

**clientType:** `ASSET` (vehículo) + Contact OWNER

```json
"profile": {
  "assetKind": "VEHICLE",
  "plate": "ABC123",
  "vin": "...",
  "make": "Toyota",
  "model": "Corolla",
  "year": 2019,
  "color": "gris",
  "odometerKm": 84000,
  "engine": "..."
}
```

`displayName` sugerido: `"ABC123 · Corolla"` o alias del cliente.

### 4.6 Consultoras (`pack: consulting`)

**clientType:** `ORG` o `PERSON`

```json
"profile": {
  "sector": "fintech",
  "engagementType": "RETAINER" | "PROJECT",
  "primaryProjectCode": "PRJ-22",
  "ndaSigned": true,
  "billingCurrency": "USD"
}
```

### 4.7 Registro de schemas

```
companies/{companyId}/client_profile_schemas/{packId}
  o platform packs globales versionados
```

Validación en backend/app según packs activos de la Company.  
Campos desconocidos: rechazar en write estricto **o** permitir en `profile` con warn (decidir por plan).

---

## 5. Firestore & Storage (post-Company)

### 5.1 Paths

```
companies/{companyId}/clients/{clientId}
companies/{companyId}/contacts/{contactId}
```

### 5.2 Índices sugeridos

| Query | Campos |
|-------|--------|
| Lista activa | `status` + `updatedAt` |
| Por tipo | `clientType` + `status` + `displayName` |
| Búsqueda | `searchTokens` array-contains |
| Por tag | `tags` array-contains |
| Portal | `linkedUserId` == uid |

### 5.3 Storage

```
companies/{companyId}/clients/{clientId}/profile/{file}
companies/{companyId}/clients/{clientId}/gallery/{file}
```

Adjuntos pesados → módulo Documentos + `DocumentLink` entityType `CLIENT`.

---

## 6. UI / navegación / permisos

### 6.1 Renombres

| Antes | Después |
|-------|---------|
| Ruta `patients` | `clients` |
| `PatientsScreen` | `ClientsScreen` |
| `CreateEditPatientScreen` | `CreateEditClientScreen` |
| Home “Pacientes” | “Clientes” (o label pack: “Pacientes”, “Unidades”, “Cuentas”) |

**Label pack:** `Company.industryPacks` define string UI sin cambiar el módulo.

### 6.2 Pantallas del módulo

| Pantalla | Función |
|----------|---------|
| Lista Clientes | Buscar, filtrar tipo/tag/status |
| Crear/Editar | Core fields + formulario dinámico `profile` |
| Detalle / 360° | Contactos, citas, expedientes, ventas, docs |
| Contactos | CRUD vinculados |
| Selector (picker) | Usado por Agenda/Ventas |

### 6.3 Formulario dinámico

```
CoreForm (displayName, type, status, phones, taxId…)
+ ProfileForm(packId)  // composables por vertical
+ ContactsSection
```

Pack veterinaria muestra species/breed; pack taller muestra placa/VIN; pack médico muestra documento/seguros.

### 6.4 Permisos (`ROLE_SYSTEM`)

| Permiso | ADMIN | MANAGER | EMPLOYEE | CLIENT |
|---------|:-----:|:-------:|:--------:|:------:|
| `clients.client.read` | ✓ | ✓ | ✓ | own |
| `clients.client.create` | ✓ | ✓ | ✓ | — |
| `clients.client.update` | ✓ | ✓ | ✓ | own† |
| `clients.client.delete` | ✓ | ✓ | — | — |
| `clients.contact.manage` | ✓ | ✓ | ✓ | — |

---

## 7. Migración de datos Pacientes → Clientes

### 7.1 Fuentes de origen

| Fuente | Prioridad | Notas |
|--------|-----------|-------|
| A. DataStore (`PatientViewModel` / `AppDataStore`) | Alta (UI viva) | Export en dispositivo o script de sync |
| B. Firestore `patients` (si hay datos) | Media | API tipada legacy |
| C. Samples hardcoded | Ignorar | No migrar |

### 7.2 Estrategia veterinaria (caso actual NexoGo)

El modelo actual mezcla **dueño + pets**. En destino:

**Opción recomendada (canónica plataforma):**

1. Cada **`SimplePet`** → Client `clientType=PET`  
2. Datos del **`SimplePatient`** → Contact `role=GUARDIAN` (+ opcional Client `PERSON` si se quiere ficha del tutor)  
3. `primaryContactId` del PET apunta al Contact  
4. `profile` = species, breed, age→birthDate aproximado  

**Opción alternativa (menos limpia):**  
Un Client PERSON “familia” con pets solo en `profile.pets[]` — **no recomendada** (rompe Agenda/Expediente por sujeto).

#### Mapeo campo a campo (vet)

| Origen | Destino |
|--------|---------|
| `SimplePet.id` | `client.id` (nuevo id si colisión; guardar `legacyPetId`) |
| `SimplePet.name` | `displayName` |
| `species/breed/age` | `profile.*` |
| `SimplePatient.name/phone/email/address` | `Contact` + address |
| — | `companyId` = company activa / default |
| — | `clientType` = `PET` |
| — | `tags` = `["migrated:patients"]` |

Si un dueño tiene N pets → N Clients + 1 Contact compartido (`clientIds` incluye todos).

### 7.3 Migración para otros verticales (datos nuevos)

No hay datos legacy; al activar pack:

| Pack | Seed / import |
|------|----------------|
| Médicos | Import CSV persona → `PERSON` |
| Bodegas / Empresas | Import cuentas → `ORG` + taxId |
| Talleres | Import vehículos → `ASSET` + Contact owner |
| Consultoras | Import mixto ORG/PERSON |

Plantillas CSV por pack en Configuración.

### 7.4 Proceso técnico de migración (oleada)

```
1. Requisito: Company + membership ACTIVE (COMPANY_IMPLEMENTATION_PLAN)
2. Feature flag: clientsModule = dual | tenant | legacy
3. Export DataStore → JSON staging (por dispositivo / herramienta admin)
4. Backfill Function/script → companies/{id}/clients + contacts
5. Dual-write temporal (opcional) si aún hay UI vieja
6. Cutover UI: ClientsScreen lee solo Firestore tenant
7. Reapuntar Agenda/Clinical a clientId
8. Apagar PatientViewModel DataStore writes
9. Archivar colección root patients
10. Actualizar MASTER_ARCHITECTURE.md
```

### 7.5 IDs y referencias

| Legacy | Acción |
|--------|--------|
| Citas con `patientId` / nombre mascota | Tabla `legacy_id_map` petId→clientId; job update appointments |
| Clinical records `patientId` | Igual |
| DataStore ids "1","2" | Regenerar ULID; mapear en `legacy_id_map` |

```
companies/{companyId}/migration_maps/patients_to_clients
  entries: { legacyPatientId, legacyPetId, newClientId, newContactId }
```

### 7.6 Rollback

- Flag vuelve a `legacy`  
- DataStore no se borra hasta N días post-cutover  
- Firestore clients quedan (no destructivo)

---

## 8. Cambios en módulos dependientes

| Módulo | Cambio |
|--------|--------|
| **Agenda** | FK `clientId`; picker Clientes; label pack (“Mascota”, “Paciente”, “Vehículo”, “Cuenta”) |
| **Expedientes** | `clientId` obligatorio; subject = Client.displayName |
| **Ventas** | `clientId` en sale; cuentas ORG para B2B |
| **Documentos** | Links `entityType=CLIENT` |
| **CRM** | Oportunidades sobre Client ORG/PERSON |
| **Chat** | Contexto `clientId` |
| **IA** | Corpus filtrable por client links |
| **Dashboard** | KPI “Clientes activos” |

---

## 9. Orden de implementación (sin código aún)

| Paso | Trabajo | Depende de | Done when |
|------|---------|------------|-----------|
| CL0 | Congelar modelo Client + Contact + profile schemas packs | Docs Business/Module | Spec aprobada |
| CL1 | Company activa disponible | COMPANY plan C1–C2 | `companyId` en sesión |
| CL2 | Repos/paths Firestore clients+contacts | CL1 | CRUD cloud |
| CL3 | UI Clientes (lista/detalle/form dinámico) | CL2 | Nav `clients` |
| CL4 | Pack veterinary profile form | CL3 | Paridad con pets actuales |
| CL5 | Migración DataStore → cloud | CL2–CL4 | Datos reales en tenant |
| CL6 | Rebind Agenda + Expedientes a clientId | CL5 | Sin PatientViewModel en create cita |
| CL7 | Labels multi-pack + schemas médicos/taller/… | CL3 | 6 verticales configurables |
| CL8 | Apagar patients legacy + rules | CL6 | Master actualizado |
| CL9 | Import CSV por industria | CL7 | Onboarding no-vet |

**No** hacer CL5 sin CL1 (perdería tenancy).  
**No** hardcodear species en Client core en CL2.

---

## 10. Matriz multiindustria (aceptación)

| Capacidad | Vet | Médicos | Bodegas | Empresas | Talleres | Consultoras |
|-----------|:---:|:-------:|:-------:|:--------:|:--------:|:-----------:|
| Lista + búsqueda | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| clientType adecuado | PET | PERSON | ORG | ORG | ASSET | ORG/PERSON |
| Contacts útiles | Tutor | Familiar† | Buyer/Driver | Buyer | Owner | Sponsor |
| Profile schema | Pet | Clinical | Credit/delivery | B2B | Vehicle | Engagement |
| Agenda sobre client | ✓ | ✓ | † | † | ✓ | ✓ |
| Expedientes | Clínico animal | Clínico humano | † notas | † | OT servicio | Entregables |
| Ventas | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Portal CLIENT | Tutor | Paciente | — / buyer | — | Owner | Sponsor |

† = opcional según proceso de la Company.

---

## 11. Riesgos

| Riesgo | Mitigación |
|--------|------------|
| Seguir modelando “dueño contiene pets” | Forzar PET + Contact en migración vet |
| Meter `species` en core | Review gate; solo `profile` |
| Migrar sin Company | Bloqueado por plan migración |
| Citas rotas post-cutover | `migration_maps` + job update |
| DataStore solo en un dispositivo | Flujo export explícito / avisar usuario |
| Pack label vs ruta | i18n pack; rutas siempre `clients` |
| Duplicar modules/patients y ui/clients | Freeze list; un solo módulo vivo |

---

## 12. Criterios de aceptación globales

1. No queda ruta de producción llamada “pacientes” (salvo label pack médico).  
2. Todos los clients tienen `companyId` y viven bajo `companies/{id}/clients`.  
3. Veterinaria: mascotas migradas como `PET` + contactos tutor.  
4. Activar pack médico/taller/bodega/empresa/consultora solo cambia forms/`profile`, no el módulo.  
5. Agenda crea citas con `clientId` válido.  
6. `PatientViewModel` + DataStore **no** escriben en el camino vivo.  
7. Tests: 2 companies no comparten clients.  
8. Vista 360° muestra al menos citas y expedientes (cuando existan).

---

## 13. Qué no hacer en esta migración

- Renombrar solo strings y dejar `SimplePet` en core.  
- Unificar “paciente humano” y “mascota” en un solo record sin `clientType`.  
- Migrar a Firestore root `clients` sin Company.  
- Implementar CRM/IA dentro del módulo Clientes.  
- Copiar campos clínicos completos al Client (van a Expedientes).

---

## 14. Relación con otros documentos

| Documento | Rol |
|-----------|-----|
| `BUSINESS_PLATFORM_ARCHITECTURE.md` | Definición Pacientes→Clientes |
| `MODULE_DESIGN.md` | Contrato módulo Clientes |
| `COMPANY_IMPLEMENTATION_PLAN.md` | Tenant obligatorio |
| `MIGRATION_MASTER_PLAN.md` | Oleada M3.4 / orden global |
| **`CLIENTS_MODULE_MIGRATION.md`** | Detalle de esta migración multiindustria |

---

## 15. Resumen ejecutivo

La migración **Pacientes → Clientes** reemplaza un modelo local vet-only (`SimplePatient` + pets) por un **Client multi-tipo** (`PERSON | PET | ASSET | ORG`) con **Contacts** y **`profile` por pack**, bajo **Company**.

Así NexoGo sirve veterinarias, médicos, bodegas, empresas, talleres y consultoras con el mismo módulo, cambiando schemas y labels — no el núcleo.

Orden: **Company → Client core → UI → pack vet + DataStore migrate → rebind Agenda/Expedientes → otros packs → apagar legacy**.

---

*Fin de CLIENTS_MODULE_MIGRATION.md.*
