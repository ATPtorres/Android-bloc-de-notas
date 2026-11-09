# Bloc de notas para Android

Aplicación Android nativa en **Java**, sin Internet ni dependencias de terceros en tiempo de ejecución.

## Funciones
- Crear, editar y eliminar notas guardadas localmente.
- Buscar por título o contenido.
- Importar y exportar archivos de texto desde el selector de Android.
- Tema claro y oscuro con preferencia persistente.
- Sin cuentas, anuncios ni permisos de Internet.

> **Antes de desinstalar la aplicación, exporta tus notas.** Los datos se guardan dentro del almacenamiento privado de la app.

## Obtener la APK

1. Abre [GitHub Actions](https://github.com/ATPtorres/Android-bloc-de-notas/actions/workflows/build-apk.yml).
2. Selecciona la ejecución más reciente de **Compilar APK Android** con estado correcto (verde).
3. En **Artifacts**, descarga **BlocNotas-Android-APK**.
4. Descomprime el ZIP y abre `BlocNotas-Android-v1.0-debug.apk` en tu teléfono.
5. Si Android lo solicita, permite la instalación desde tu administrador de archivos.

Si no se ha iniciado la compilación, selecciona **Run workflow** desde la pestaña Actions.

**Compatibilidad:** Android 6.0+ (API 23). **Tecnología:** Java 17, Android SDK 35, Android Gradle Plugin 8.6.1, Gradle 8.7.

### Importante sobre las versiones
La APK generada es una versión de **depuración** para uso personal. Cada ejecución puede firmarla con una clave distinta; podría ser necesario desinstalar la anterior para instalar la siguiente. Exporta tus notas antes de desinstalar.

## Desarrollo local

Requiere Java 17, Android SDK 35 con Build Tools 35.0.0 y Gradle 8.7.

```bash
gradle assembleDebug
```

La salida local está en `app/build/outputs/apk/debug/app-debug.apk`.

## Privacidad
La app no requiere acceso a Internet ni sincroniza notas; `android:allowBackup="false"` evita la copia de seguridad automática.

## Fuente
Proyecto preparado desde `BlocNotasAndroid_compilacion_nube.zip` con compilación de APK mediante GitHub Actions.
