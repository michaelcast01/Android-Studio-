@echo off
echo Configurando emulador Android para mejor rendimiento...

echo 1. Configurando para graphics emulation (software)
echo    Para solucionar problemas de GPU con x86_64

echo 2. Configuraciones recomendadas para el emulador:
echo    - OpenGL ES API Level: Desktop OpenGL
echo    - OpenGL ES Renderer: swiftshader_indirect
echo    - GPU emulation: software - GLES 2.0

echo 3. Si el emulador sigue con pantalla negra:
echo    - Reinicia el emulador desde Android Studio
echo    - Ve a Tools > AVD Manager
echo    - Haz clic en el icono de editar (lápiz) de tu AVD
echo    - Ve a "Show Advanced Settings" 
echo    - Cambia "Graphics" a "Software - GLES 2.0"
echo    - Asegúrate de que "Use Host GPU" esté DESHABILITADO

echo 4. Para mejorar el rendimiento:
echo    - RAM: 2048 MB o más
echo    - VM Heap: 512 MB
echo    - Internal Storage: 6000 MB
echo    - SD Card: 1000 MB

echo =====================================
echo Configuración completada.
echo Ahora puedes iniciar tu emulador.
echo =====================================

pause