# Guía para Solucionar Pantalla Negra del Emulador Android

## Problemas Solucionados:

### ✅ 1. Error del archivo .env
- **Problema**: `Error al cargar archivo .env: .env`
- **Solución**: Creado archivo `.env` en `app/src/main/assets/.env`

### ✅ 2. Configuración de SDK 
- **Problema**: Target SDK 36 no compatible con emulador
- **Solución**: Actualizado a compileSdk 36, targetSdk 34 para mejor compatibilidad

### ✅ 3. Recursos drawable corruptos
- **Problema**: Archivos PNG corruptos causando fallos de compilación
- **Solución**: Reemplazados con archivos vectoriales XML

### ✅ 4. Build exitoso
- **Resultado**: Aplicación compilada e instalada correctamente

## Configuración Recomendada del Emulador:

### En Android Studio AVD Manager:
1. **Graphics**: Software - GLES 2.0 (NO Hardware)
2. **RAM**: 2048 MB mínimo
3. **VM Heap**: 512 MB
4. **Use Host GPU**: DESHABILITADO
5. **Snapshot**: Habilitado para arranque rápido

### Si persiste la pantalla negra:

#### Método 1: Reiniciar completamente
```bash
# En terminal dentro de Android Studio:
1. Tools > AVD Manager
2. Click en "Cold Boot Now" en lugar de "Play"
3. Espera 2-3 minutos para el arranque completo
```

#### Método 2: Recrear AVD
```bash
1. Tools > AVD Manager
2. Delete el AVD actual
3. Create Virtual Device
4. Selecciona Pixel 4 o Pixel 5
5. Descarga System Image API 34 (recomendado)
6. En Advanced Settings:
   - Graphics: Software - GLES 2.0
   - RAM: 2048 MB
   - VM Heap: 512 MB
```

#### Método 3: Verificar sistema backend
Si usas Windows con Hyper-V:
```bash
# Deshabilitar Hyper-V temporalmente
1. Windows + R
2. Escribe: optionalfeatures
3. Desmarca "Hyper-V"
4. Reinicia el PC
```

## Para ejecutar la app:

### Opción 1: Desde Android Studio
```bash
1. Abre el proyecto en Android Studio
2. Click en "Run" (▶️)
3. Selecciona el emulador
```

### Opción 2: Desde línea de comandos
```powershell
# Construir e instalar
cd "c:\Users\micha\Downloads\TiendaSuplementacion"
.\gradlew assembleDebug
& "C:\Users\micha\AppData\Local\Android\Sdk\platform-tools\adb.exe" install app\build\outputs\apk\debug\app-debug.apk

# Abrir la app
& "C:\Users\micha\AppData\Local\Android\Sdk\platform-tools\adb.exe" shell am start -n com.example.tiendasuplementacion/.MainActivity
```

## Notas importantes:

### Rendimiento del Emulador:
- **Frames perdidos**: Normal en emuladores x86_64 en hosts x64
- **Tiempo de carga**: Primer arranque puede tomar 3-5 minutos
- **Memoria**: La app está optimizada con lazy loading para imágenes

### Logs de la aplicación:
- Los warnings sobre "CPU variant" y "Frame time in future" son normales
- La app está cargando datos correctamente del servidor local (10.0.2.2:8080)
- Sistema de autenticación funcionando (usuario Orlando restaurado)

### Backend Status:
✅ Servidor funcionando en puerto 8080
✅ Endpoints respondiendo correctamente:
- `/api/payments` (200 OK)
- `/api/products` (200 OK)  
- `/api/categories` (200 OK)
- `/api/categories-products` (200 OK)

La aplicación debería funcionar correctamente ahora. Si el emulador sigue mostrando pantalla negra, usa Cold Boot y espera unos minutos para el arranque completo.