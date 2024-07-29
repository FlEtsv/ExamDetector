# ExamDetector
Un detector de Respuestas y datos basado en OpenCv, desarrollado para Android enteramente en kotlin.

## Descripción
Este proyecto fue desarrollado con el fin de detectar respuestas de examenes y datos en una hoja de papel, para ello se utilizo la libreria de OpenCv en su version 4.5.1, la cual nos permite trabajar con imagenes y videos en tiempo real, ademas de la libreria de Tesseract en su version 4.1.1, la cual nos permite realizar la lectura de texto en imagenes.
Se implementa el uso de la camara o galeria del dispositivo para capturar la imagen, la cual sera procesada para detectar las respuestas y datos en la hoja de papel, ademas de la lectura de texto en la imagen.
Se puede seleccionar si se quieren guardar en galeria las imagenes que openCv procesa, ademas de la opcion de guardar en un archivo de texto la lectura de texto en la imagen.

## Funcionalidades
- Captura de imagen desde la camara o galeria del dispositivo.
- Procesamiento de la imagen para detectar respuestas y datos en la hoja de papel.
- Lectura de texto en la imagen.
- Guardar imagenes procesadas en la galeria del dispositivo.
- Guardar lectura de texto en un archivo de texto.

## Requisitos
- Android Studio
- OpenCv 4.8.0
- java 8 minimo

## Instalación
1. Clonar el repositorio con el comando: 
```bash
git clone
```
2. Abrir el proyecto en Android Studio.
3. Descargar la libreria de OpenCv en su version 4.8.0 desde el siguiente enlace: [OpenCv](https://opencv.org/releases/)
4. Descomprimir el archivo descargado y copiar la carpeta que se dese en la raiz del proyecto o en otro sitio.
5. Damos new -> Import Module y seleccionamos la carpeta que copiamos.
6. Nos metemos en el archivo build.gradle del modulo OpenCv a traves de la vista Project de android estudio y le añadimos:
```bash
    namespace "org.opencv"
```
7. Tambien debemos añadir en el archivo build.gradle del modulo OpenCv:
```bash
    buildFeatures {
        aidl true
        buildConfig true
    }
```
8. seleccionamos File -> Project Structure -> Dependencies -> App -> + -> Module Dependency -> OpenCv
9. Añadimos la siguiente linea en el archivo build.gradle del modulo app:
```bash
    implementation project(':opencv')
```
10. Sincronizamos el proyecto.
11. Tutorial mas detallado en: [OpenCv en Android Studio](https://medium.com/@ankitsachan/android-studio-kotlin-open-cv-16d75f8d9969)
12. Es posible que en diferentes versiones la forma de proceder para integrar esta libreria cambie, por lo que se recomienda buscar informacion actualizad o utilizar la misma version que se expone.
## Uso
1. Abrir el proyecto en Android Studio.
2. Ejecutar el proyecto en un dispositivo o emulador.
3. Seleccionar la opcion de camara o galeria para capturar la imagen.
4. Una vez selecciones imagen y las opciones dispoibles, la imagen se procesara automaticamente.
5. Se mostrara la imagen procesada con las respuestas y datos detectados, ademas de la lectura de texto en la imagen.
