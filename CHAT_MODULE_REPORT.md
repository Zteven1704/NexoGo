# Enterprise Chat Module Report (v1)

**Fecha:** 2026-09-18  
**Alcance:** Messaging empresarial multiempresa  
**Compilación:** `:app:compileDebugKotlin --rerun-tasks` — **BUILD SUCCESSFUL**  
**Legacy:** pantallas / ViewModels de chat existentes **no modificados**

---

## Objetivo

Implementar **Enterprise Chat v1** con conversaciones, canales, grupos y mensajes (texto, imagen, documento, sistema), aislados por Company, preparados para push FCM y lectura de documentos compartidos del módulo Documentos.

---

## Entregables

| Entregable | Ubicación | Estado |
|------------|-----------|--------|
| **Conversation** | `platform/chat/model/ChatModels.kt` | ✅ |
| **Message** | mismo archivo | ✅ |
| **Channel** | mismo archivo | ✅ |
| **GroupChat** | mismo archivo | ✅ |
| **ChatRepository** | `platform/chat/data/ChatRepository.kt` | ✅ |
| Push prep | `platform/chat/push/ChatPushEnvelope.kt` | ✅ |
| Document share | `platform/chat/docs/ChatDocumentShare.kt` | ✅ |
| Storage paths | `platform/chat/storage/ChatStoragePaths.kt` | ✅ |

---

## Tipos de mensaje

| `MessageType` | Uso |
|---------------|-----|
| `TEXT` | Mensajes de texto |
| `IMAGE` | Imágenes (Storage path + attachment) |
| `DOCUMENT` | Documento compartido (`sharedDocumentId` → Document Management) |
| `SYSTEM` | Eventos (canal/grupo creado, doc compartido, …) |

---

## Modelo de hilos

| Entidad | Rol | Conversation.type |
|---------|-----|-------------------|
| **Conversation** | Hilo canónico + participantes + preview | DIRECT / GROUP / CHANNEL / CLIENT_CONTEXT |
| **Channel** | Canal de empresa (topic/team) | CHANNEL (+ `channelId`) |
| **GroupChat** | Grupo multipersona | GROUP (+ `groupChatId`) |

Paths Firestore:

```
companies/{companyId}/conversations/{id}
companies/{companyId}/conversations/{id}/messages/{id}
companies/{companyId}/chat_channels/{id}
companies/{companyId}/chat_groups/{id}
```

---

## Multiempresa

- Todas las entidades son `TenantAwareEntity` (`companyId` obligatorio).
- Queries scoped vía `TenantAwareRepository` / `TenantIsolationGuard`.
- Participantes siempre dentro del tenant de la conversación.

---

## Push notifications (preparado)

- `ChatPushTopics` — topics por company / conversation / channel / group / user
- `ChatPushEnvelope` + `toFcmDataMap()` + deep link `nexogo://company/{id}/chat/{cv}`
- `ChatPushFactory.fromMessage(...)`
- `ChatPushDispatcher` + `NoOpChatPushDispatcher` (enqueue sin envío real en v1)
- Repository encola push tras mensajes no-SYSTEM

---

## Documentos compartidos

- `sendDocumentMessage` valida el doc con `ChatDocumentShare.resolveSharedDocument`
- Attachment `DOCUMENT_REF` + `sharedDocumentId`
- Mensaje SYSTEM `document_shared`
- Listo para abrir/leer vía Document Management (sin IA)

---

## API repository (resumen)

- DM: `createDirectConversation`, `findDirectBetween`
- Channel / Group: `createChannel`, `createGroupChat`, list/get
- Messages: `sendTextMessage`, `sendImageMessage`, `sendDocumentMessage`, `sendSystemMessage`, `listMessages`, `listenMessages`, `markRead`
- Inbox: `listConversationsForUser`, `listenConversationsForUser`

---

## Archivos

```
app/src/main/java/com/example/nexogo/platform/chat/
  model/ChatModels.kt
  data/ChatRepository.kt
  push/ChatPushEnvelope.kt
  docs/ChatDocumentShare.kt
  storage/ChatStoragePaths.kt
```

**Actualizado:** `TenantCollections` (`CHAT_CHANNELS`, `CHAT_GROUPS`)

---

## Compilación

```bash
bash ./gradlew :app:compileDebugKotlin --no-daemon --rerun-tasks
# BUILD SUCCESSFUL
```

---

## Fuera de alcance (siguiente)

- UI Compose / NavGraph enterprise chat
- FCM real (Cloud Functions / token registry)
- Upload de imágenes a Storage desde UI
- Migración del chat legacy
- Moderación / chatbot / IA

---

## Veredicto

**Enterprise Chat v1 completo y compilando.** Conversations, Channels, Groups y Messages multiempresa listos; push y documentos compartidos preparados.
