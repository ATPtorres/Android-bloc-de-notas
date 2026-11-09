# Publicación en GitHub

Repositorio: https://github.com/ATPtorres/Android-bloc-de-notas

El repositorio está configurado para generar una APK de depuración automáticamente con GitHub Actions al recibir cambios en \`main\`, o manualmente con **Actions → Compilar APK Android → Run workflow**.

## Descargar la APK

1. Abre https://github.com/ATPtorres/Android-bloc-de-notas/actions/workflows/build-apk.yml
2. Selecciona una ejecución con el símbolo verde de éxito.
3. En **Artifacts**, descarga **BlocNotas-Android-APK** (ZIP).
4. Extrae \`BlocNotas-Android-v1.0-debug.apk\` de ese ZIP.
5. Abre la APK en Android y permite la instalación desde el explorador de archivos si se solicita.

La APK resultante es una compilación **debug**, apta para pruebas y uso personal. No está firmada con una clave de publicación. Las APK compiladas en diferentes ejecuciones pueden tener firmas de depuración distintas; antes de desinstalar, **exporta tus notas**.

La compilación debe terminar correctamente para que el artefacto esté disponible. En caso de fallo, revisa los mensajes del paso fallido dentro de GitHub Actions.
