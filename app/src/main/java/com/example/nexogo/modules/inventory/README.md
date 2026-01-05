# Módulo: Inventario 🧴 (NexoGo)

## 🔗 Conexión Firebase
- **Firestore**: `/inventory`
- **Storage**: `/inventory/{productId}/main.jpg`

## 🧩 Funcionalidades
- ✅ CRUD completo de productos
- ✅ Carga de imágenes
- ✅ Categorías dinámicas
- ✅ Control de stock
- ✅ Integración con módulo de Ventas
- ✅ Actualización en tiempo real

## 🧠 Roles y Permisos

| Rol | Leer | Crear/Editar | Eliminar | Subir Imágenes |
|-----|------|---------------|----------|----------------|
| Admin | ✅ | ✅ | ✅ | ✅ |
| Vet | ✅ | ✅ | ❌ | ✅ |
| Assistant | ✅ | ❌ | ❌ | ❌ |
| Patient | ❌ | ❌ | ❌ | ❌ |

## 🏗️ Estructura del Módulo

```
modules/inventory/
├── data/
│   └── InventoryRepository.kt
├── ui/
│   ├── InventoryListScreen.kt
│   ├── InventoryEditScreen.kt
│   ├── CategorySelector.kt
│   └── components/
│       └── ProductCard.kt
├── model/
│   ├── Product.kt
│   └── Category.kt
├── viewmodel/
│   └── InventoryViewModel.kt
├── utils/
│   └── InventoryUtils.kt
├── tests/
│   └── InventoryModuleTest.kt
└── README.md
```

## 📊 Modelo de Datos

### Product.kt
```kotlin
data class Product(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val categoryId: String = "",
    val categoryName: String = "",
    val unitPrice: Double = 0.0,
    val quantity: Int = 0,
    val imageUrl: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)
```

### Category.kt
```kotlin
data class Category(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val isActive: Boolean = true,
    val productCount: Int = 0
)
```

## 🔥 Firebase Integration

### Colección Firestore: `inventory`
```json
{
  "id": "prod123",
  "name": "Vacuna Antirrábica",
  "description": "Protege contra el virus de la rabia.",
  "categoryId": "vacunas",
  "categoryName": "Vacunas",
  "unitPrice": 25000,
  "quantity": 40,
  "imageUrl": "gs://nexogo.appspot.com/inventory/prod123.jpg",
  "createdAt": "2025-10-07T12:00:00Z",
  "updatedAt": "2025-10-07T12:10:00Z"
}
```

### Ruta en Storage:
```
/inventory/{productId}/main.jpg
```

### Categorías (Firestore):
```
/categories/{categoryId}
{
  "id": "vacunas",
  "name": "Vacunas",
  "description": "Productos biológicos veterinarios."
}
```

## 🔒 Firebase Rules

### 📘 Firestore (firestore.rules)
```javascript
match /databases/{database}/documents {
  match /inventory/{productId} {
    allow read: if request.auth != null
      && get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role in ['admin','vet','assistant'];
    
    allow create, update, delete: if request.auth != null
      && get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role in ['admin','vet'];
  }
}
```

### 📘 Storage (storage.rules)
```javascript
match /b/{bucket}/o/inventory/{productId}/{fileName} {
  allow read: if request.auth != null
    && get(/databases/(default)/documents/users/$(request.auth.uid)).data.role in ['admin','vet','assistant'];
  
  allow write, delete: if request.auth != null
    && get(/databases/(default)/documents/users/$(request.auth.uid)).data.role in ['admin','vet'];
}
```

## 🧰 InventoryRepository

### Métodos principales:
- `getAllProducts()`: Obtiene todos los productos
- `getProductById(id)`: Obtiene un producto específico
- `createProduct(product)`: Crea un nuevo producto
- `updateProduct(id, product)`: Actualiza un producto
- `deleteProduct(id)`: Elimina un producto
- `uploadProductImage(id, uri)`: Sube imagen del producto
- `reduceStock(id, amount)`: Reduce stock (usado por Ventas)
- `getLowStockProducts(threshold)`: Obtiene productos con stock bajo
- `getProductsByCategory(categoryId)`: Filtra por categoría

## 🧪 Pruebas Automáticas

### Ejecutar pruebas:
```bash
./gradlew test --tests *InventoryModuleTest
```

### Pruebas incluidas:
- ✅ Crear producto nuevo y verificar en Firestore
- ✅ Subir imagen correctamente a Storage
- ✅ Actualizar stock y confirmar sincronización
- ✅ Eliminar producto y limpiar Storage
- ✅ Validar permisos por rol
- ✅ Filtrar productos por categoría
- ✅ Obtener productos con stock bajo
- ✅ Reducir stock (simulando venta)

## 🚀 Cómo probar

1. **Inicia sesión como admin@nexogo.com**
2. **Abre el módulo Inventario**
3. **Agrega un producto con imagen**
4. **Observa sincronización en tiempo real**
5. **Inicia sesión como assistant@nexogo.com**
6. **Verifica que solo puede ver, no editar**

## 🔧 Integración con otros módulos

### Con Config:
- Obtiene categorías dinámicamente
- Sincroniza cambios en categorías en tiempo real

### Con Ventas:
- Exporta función `reduceStock(productId, amount)`
- Actualiza stock automáticamente al vender
- Sincronización en tiempo real

### Con Perfil:
- Respeta permisos por rol
- Acceso restringido según usuario

## 📱 Características de UI

- **Jetpack Compose + Material 3**
- **Listado en grid responsive**
- **Filtros por categoría, nombre o stock bajo**
- **Imagen circular del producto**
- **Botones de acción contextuales**
- **Confirmación con diálogo modal**
- **Estadísticas del inventario**
- **Búsqueda en tiempo real**

## 🎯 Funcionalidades Avanzadas

- **Control de stock en tiempo real**
- **Alertas de stock bajo**
- **Categorización dinámica**
- **Subida de imágenes**
- **Filtros y búsqueda**
- **Estadísticas del inventario**
- **Integración con ventas**
- **Permisos por rol**

## 📈 Métricas y Estadísticas

- Total de productos
- Valor total del inventario
- Productos con stock bajo
- Productos sin stock
- Precio promedio
- Distribución por categoría

---

**Módulo desarrollado para NexoGo - Sistema de Gestión Veterinaria** 🐾

