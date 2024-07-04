package org.opencv.samples.recorder

import android.os.Build
import org.opencv.core.*
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import kotlin.math.roundToInt

object ProcesadorImagen {
    /**
     * Detecta columnas en la imagen especificada.
     *
     * @param imagen La imagen a procesar
     * @param tipo   El tipo de procesamiento (0 para columnas, 1 para DNI)
     * @param contador El contador de imágenes procesadas
     */
    fun detectarColumnas(imagen: Mat, tipo: Int, contador: Int, originalImagen: Mat, coordenadas: Rect) {
        if (imagen.empty()) {
            println("No se pudo abrir la imagen.")
            return
        }

        val imagenProcesada = auxiliar.preprocesarImagen(imagen)

        // Guardar la imagen preprocesada
        Imgcodecs.imwrite("src/img/preprocesada$contador.png", imagenProcesada)

        val rect = Rect(0, 0, imagen.cols(), imagen.rows())

        if (tipo == 0) {
            detectarRectangulos(imagenProcesada, imagen, rect, contador, originalImagen, coordenadas)
        } else {
            detectarRectangulosDNI(imagenProcesada, imagen, rect, contador, originalImagen)
        }

        // Guardar la imagen resultante
        Imgcodecs.imwrite("src/img/result$contador.png", imagen)
    }


    private fun detectarRectangulosDNI(bordes: Mat, src: Mat, rect: Rect, contador: Int, originalImagen: Mat) {
        val recuadroDniRecorteImagenOriginal = Sesion.instance.dniCoordinates
        var contadorRectangulos = 0
        val imagenColumna = Mat(bordes, rect)
        val contornosInternos = ArrayList<MatOfPoint>()
        Imgproc.findContours(imagenColumna, contornosInternos, Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)

        val rectangulosDetectadosCasiCuadrados = ArrayList<Rect>()

        for (contornoInterno in contornosInternos) {
            val rectInterno = Imgproc.boundingRect(contornoInterno)
            if (verificacionFormas.esCasiCuadrado(rectInterno, imagenColumna)) {
                // Ajustar las coordenadas del rectángulo
                rectInterno.x += rect.x
                rectInterno.y += rect.y

                // Verificar si el rectángulo está duplicado
                var duplicado = false
                for (i in rectangulosDetectadosCasiCuadrados.indices) {
                    val rectExistente = rectangulosDetectadosCasiCuadrados[i]
                    if (Math.abs(rectExistente.x - rectInterno.x) < 10 && Math.abs(rectExistente.y - rectInterno.y) < 10 &&
                        Math.abs(rectExistente.width - rectInterno.width) < 10 && Math.abs(rectExistente.height - rectInterno.height) < 10) {
                        val aspectRatioInterno = rectInterno.width / rectInterno.height.toDouble()
                        val aspectRatioExistente = rectExistente.width / rectExistente.height.toDouble()

                        // Si el nuevo rectángulo es más cuadrado, reemplaza el rectángulo existente
                        if (Math.abs(aspectRatioInterno - 1) < Math.abs(aspectRatioExistente - 1)) {
                            rectangulosDetectadosCasiCuadrados[i] = rectInterno
                        }
                        duplicado = true
                        break
                    }
                }

                if (!duplicado) {
                    rectangulosDetectadosCasiCuadrados.add(rectInterno)
                }
            }
        }

        // Ordenar los rectángulos casi cuadrados de izquierda a derecha
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            rectangulosDetectadosCasiCuadrados.sortWith(Comparator.comparingInt { it.x })
        }

        for (rectInterno in rectangulosDetectadosCasiCuadrados) {
            // Dibujar en la imagen procesada (como ya estás haciendo)
            Imgproc.rectangle(src, rectInterno, Scalar(0.0, 0.0, 255.0), 2)
            Imgproc.putText(src, "Rect $contador.$contadorRectangulos", Point(rectInterno.x.toDouble(), rectInterno.y - 10.0), Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, Scalar(0.0, 0.0, 255.0), 2)

            // Dibujar en la imagen original
            // Asegúrate de que las coordenadas del rectángulo sean relativas a la imagen original
            val rectOriginal = Rect(rectInterno.x + recuadroDniRecorteImagenOriginal.x, rectInterno.y + recuadroDniRecorteImagenOriginal.y, rectInterno.width, rectInterno.height)
            Imgproc.rectangle(originalImagen, rectOriginal, Scalar(0.0, 0.0, 255.0), 2)
            Imgproc.putText(originalImagen, "Rect $contador.$contadorRectangulos", Point(rectOriginal.x.toDouble(), rectOriginal.y - 10.0), Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, Scalar(0.0, 0.0, 255.0), 2)

            detectarCirculosDniletra(bordes, src, rectInterno, contadorRectangulos, originalImagen)
            contadorRectangulos++
        }
        println("Cantidad de rectángulos DNI ${rectangulosDetectadosCasiCuadrados.size}")
    }

    private fun detectarRectangulos(bordes: Mat, src: Mat, rect: Rect, contador: Int, originalImagen: Mat, coordenadasOriginales: Rect) {
        var contadorRectangulos = 0

        val imagenColumna = Mat(bordes, rect)
        val contornosInternos = ArrayList<MatOfPoint>()
        Imgproc.findContours(imagenColumna, contornosInternos, Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE)
        val rectangulosNumeroExamen = ArrayList<Rect>()
        val rectangulosDetectados = ArrayList<Rect>()

        for (contornoInterno in contornosInternos) {
            val rectInterno = Imgproc.boundingRect(contornoInterno)
            if (verificacionFormas.esRectangulo(rectInterno, imagenColumna) || verificacionFormas.esRectanguloTorcido(rectInterno, imagenColumna)) {
                // Ajustar las coordenadas del rectángulo
                rectInterno.x += rect.x
                rectInterno.y += rect.y

                // Verificar si el rectángulo está duplicado
                var duplicado = false
                for (rectExistente in rectangulosDetectados) {
                    if (Math.abs(rectExistente.x - rectInterno.x) < 5 && Math.abs(rectExistente.y - rectInterno.y) < 5 &&
                        Math.abs(rectExistente.width - rectInterno.width) < 5 && Math.abs(rectExistente.height - rectInterno.height) < 5) {
                        duplicado = true
                        break
                    }
                }

                if (!duplicado) {
                    rectangulosDetectados.add(rectInterno)
                }
            }
            // Columna de códigos de examen
            if (contador == 4 && verificacionFormas.esCasiCuadrado(rectInterno, imagenColumna)) {
                rectangulosNumeroExamen.add(rectInterno)
            }
        }
        // La número 4 en la lista de columnas
        if (contador == 4) {
            val rectInternoNExamen = rectangulosNumeroExamen.first()
            Imgproc.rectangle(src, rectInternoNExamen, Scalar(0.0, 0.0, 255.0), 2)
            Imgproc.putText(src, "Rect $contador.$contadorRectangulos", Point(rectInternoNExamen.x.toDouble(), rectInternoNExamen.y - 10.0), Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, Scalar(0.0, 0.0, 255.0), 2)
            detectarCirculosCodExamen(bordes, src, rectInternoNExamen, originalImagen, coordenadasOriginales)
            val adjustedRect = Rect(rectInternoNExamen.x + coordenadasOriginales.x, rectInternoNExamen.y + coordenadasOriginales.y, rectInternoNExamen.width, rectInternoNExamen.height)
            Imgproc.rectangle(originalImagen, adjustedRect, Scalar(0.0, 0.0, 255.0), 2)
            Imgproc.putText(originalImagen, "Rect $contador.$contadorRectangulos", Point(rectInternoNExamen.x + coordenadasOriginales.x.toDouble(), rectInternoNExamen.y - 10.0 + coordenadasOriginales.y), Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, Scalar(0.0, 0.0, 255.0), 2)
        } else {
            // Ordenar los rectángulos de arriba a abajo
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                rectangulosDetectados.sortWith(Comparator.comparingInt { it.y })
            }
            for (rectInterno in rectangulosDetectados) {
                contadorRectangulos++
                Imgproc.rectangle(src, rectInterno, Scalar(0.0, 0.0, 255.0), 2)
                Imgproc.putText(src, "Rect $contador.$contadorRectangulos", Point(rectInterno.x.toDouble(), rectInterno.y - 10.0), Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, Scalar(0.0, 0.0, 255.0), 2)
                detectarCirculosRespuestas(bordes, src, rectInterno, originalImagen, coordenadasOriginales)
                val adjustedRect = Rect(rectInterno.x + coordenadasOriginales.x, rectInterno.y + coordenadasOriginales.y, rectInterno.width, rectInterno.height)
                Imgproc.rectangle(originalImagen, adjustedRect, Scalar(0.0, 0.0, 255.0), 2)
                Imgproc.putText(originalImagen, "Rect $contador.$contadorRectangulos", Point(rectInterno.x + coordenadasOriginales.x.toDouble(), rectInterno.y - 10.0 + coordenadasOriginales.y), Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, Scalar(0.0, 0.0, 255.0), 2)
            }
        }

        println("Cantidad de rectángulos columna $contador: ${rectangulosDetectados.size}")
    }

    private fun detectarCirculosRespuestas(bordes: Mat, src: Mat, rectInterno: Rect, originalImagen: Mat, coordenadasOriginales: Rect) {
        val rect = Rect(rectInterno.x, rectInterno.y, rectInterno.width, rectInterno.height)

        // Asegurarse de que los índices están dentro de los límites
        rect.x = Math.max(rect.x, 0)
        rect.y = Math.max(rect.y, 0)
        rect.width = Math.min(rect.width, bordes.cols() - rect.x)
        rect.height = Math.min(rect.height, bordes.rows() - rect.y)

        // Asegurarse de que el tamaño del rectángulo es positivo
        if (rect.width <= 0 || rect.height <= 0) {
            return
        }

        val imagenRectangulo = Mat(bordes, rect)

        // Escalar la imagen si es necesario
        val scale = 1.0 // Ajustar este valor según sea necesario
        val size = Size(imagenRectangulo.cols() * scale, imagenRectangulo.rows() * scale)
        Imgproc.resize(imagenRectangulo, imagenRectangulo, size)

        val circulos = Mat()
        var minDist = Math.min(imagenRectangulo.rows(), imagenRectangulo.cols()) / 8
        if (minDist < 1) {
            minDist = 1
        }

        // Ajustar los parámetros según sea necesario 100, 30, 13, 21 valores clave
        Imgproc.HoughCircles(imagenRectangulo, circulos, Imgproc.CV_HOUGH_GRADIENT, 1.0, minDist.toDouble(), 100.0, 30.0, 21, 37)

        val centrosCirculos = ArrayList<Point>()
        val radiosCirculos = ArrayList<Int>()
        for (i in 0 until circulos.cols()) {
            val datosCirculo = circulos.get(0, i) ?: continue
            val centro = Point(datosCirculo[0] + rectInterno.x, datosCirculo[1] + rectInterno.y)
            val radio = datosCirculo[2].roundToInt()

            var solapado = false
            for (j in centrosCirculos.indices) {
                if (distancia(centro, centrosCirculos[j]) < radio + radiosCirculos[j]) {
                    solapado = true
                    break
                }
            }

            if (!solapado) {
                centrosCirculos.add(centro)
                radiosCirculos.add(radio)
            }
        }

        // Ordenar los círculos de izquierda a derecha
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            centrosCirculos.sortWith(Comparator.comparingDouble { it.x })
        }
        var respuestasEncontradas = 0
        var posicionAlfabetica = 'N' // Inicializamos con 'N' por defecto
        for (i in centrosCirculos.indices) {
            val centro = centrosCirculos[i]
            val radio = radiosCirculos[i]
            // Dibujar en la imagen original y en la imagen procesada
            val centroOriginal = Point(centro.x + coordenadasOriginales.x, centro.y + coordenadasOriginales.y)
            // Si el círculo no está relleno, pintarlo con el borde de color amarillo
            Imgproc.circle(originalImagen, centroOriginal, radio, Scalar(0.0, 255.0, 255.0), 2)
            Imgproc.circle(src, centro, radio, Scalar(0.0, 255.0, 255.0), 2)
            if (verificacionFormas.verificarRelleno(src, centro, radio)) {
                // Si el círculo está relleno, pintarlo de verde
                Imgproc.circle(originalImagen, centroOriginal, radio, Scalar(0.0, 255.0, 0.0), -1) // -1 indica relleno completo
                Imgproc.circle(src, centro, radio, Scalar(0.0, 255.0, 0.0), -1) // -1 indica relleno completo
                posicionAlfabetica = verificacionFormas.asignarPosicionAlfabetica(i)
                respuestasEncontradas++
            }
        }

        // Si no se encontró exactamente una respuesta, marcamos como 'N'
        if (respuestasEncontradas != 1) {
            posicionAlfabetica = 'N'
        }

        println("Respuesta $posicionAlfabetica")
        Sesion.instance.examAnswers.add(posicionAlfabetica.toString())
    }

    private fun detectarCirculosDniletra(bordes: Mat, src: Mat, rectInterno: Rect, caso: Int, originalImagen: Mat) {
        val imagenOriginal = Sesion.instance.dniCoordinates
        var radio = 0
        var contadorCirculos = 0
        var contadorCirculosDNI: Int
        val respuestas = ArrayList<String>()
        val respuestasDni = ArrayList<String>()
        val rect = Rect(rectInterno.x, rectInterno.y, rectInterno.width, rectInterno.height)

        // Asegurarse de que los índices están dentro de los límites
        rect.x = Math.max(rect.x, 0)
        rect.y = Math.max(rect.y, 0)
        rect.width = Math.min(rect.width, bordes.cols() - rect.x)
        rect.height = Math.min(rect.height, bordes.rows() - rect.y)

        // Asegurarse de que el tamaño del rectángulo es positivo
        if (rect.width <= 0 || rect.height <= 0) {
            return
        }

        val imagenRectangulo = Mat(bordes, rect)

        val circulos = Mat()
        var minDist = Math.min(imagenRectangulo.rows(), imagenRectangulo.cols()) / 8
        if (minDist < 1) {
            minDist = 1
        }

        // Ajustar los parámetros según sea necesario 100, 30, 13, 21 valores clave
        Imgproc.HoughCircles(imagenRectangulo, circulos, Imgproc.CV_HOUGH_GRADIENT, 1.0, 2.0, 50.0, 20.0, 20, 35)

        val centrosCirculos = ArrayList<Point>()
        val radiosCirculos = ArrayList<Int>()
        for (i in 0 until circulos.cols()) {
            val datosCirculo = circulos.get(0, i) ?: continue

            // Crear el centro del círculo y el radio con los datos recuperados
            val centro = Point(datosCirculo[0] + rectInterno.x, datosCirculo[1] + rectInterno.y)
            radio = datosCirculo[2].roundToInt()

            var solapado = false
            for (j in centrosCirculos.indices) {
                if (distancia(centro, centrosCirculos[j]) < radio + radiosCirculos[j]) {
                    solapado = true
                    break
                }
            }

            if (!solapado) {
                centrosCirculos.add(centro)
                radiosCirculos.add(radio)
            }
        }

        if (caso == 0 || caso == 2) {
            // Ordenar los círculos de izquierda a derecha y pasar a la línea de abajo si se llega al final
            val filasCirculos = ArrayList<ArrayList<Point>>()
            for (centro in centrosCirculos) {
                var agregado = false
                for (fila in filasCirculos) {
                    if (Math.abs(fila[0].y - centro.y) < radio) {
                        fila.add(centro)
                        agregado = true
                        break
                    }
                }
                if (!agregado) {
                    val nuevaFila = ArrayList<Point>()
                    nuevaFila.add(centro)
                    filasCirculos.add(nuevaFila)
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                filasCirculos.sortWith(Comparator.comparingDouble { it[0].y })
            }
            for (fila in filasCirculos) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    fila.sortWith(Comparator.comparingDouble { it.x })
                }
                for (centro in fila) {
                    val radioCirculosOrdenados = radiosCirculos[centrosCirculos.indexOf(centro)]

                    // Dibujar círculos en la imagen procesada
                    Imgproc.circle(src, centro, radioCirculosOrdenados, Scalar(0.0, 255.0, 255.0), 2)

                    // Ajustar las coordenadas del centro para la imagen original
                    val centroOriginal = Point(centro.x + imagenOriginal.x, centro.y + imagenOriginal.y)

                    // Dibujar círculos en la imagen original
                    Imgproc.circle(originalImagen, centroOriginal, radio, Scalar(0.0, 255.0, 255.0), 2)

                    // Verificar si el círculo está relleno
                    if (verificacionFormas.verificarRelleno(src, centro, radioCirculosOrdenados)) {
                        Imgproc.circle(src, centro, radioCirculosOrdenados, Scalar(0.0, 255.0, 0.0), 2)
                        // Dibujar círculos en la imagen original
                        Imgproc.circle(originalImagen, centroOriginal, radio, Scalar(0.0, 255.0, 0.0), -1)
                        // Asignar una posición alfabética al círculo
                        val posicionAlfabetica = verificacionFormas.asignarPosicionAlfabetica(contadorCirculos)
                        println("Letra $posicionAlfabetica")

                        // Agregar la posición alfabética a la sesión
                        Sesion.instance.addDniNie(posicionAlfabetica.toString())
                    }
                    contadorCirculos++
                }
            }
        } else {
            // Ordenar los círculos de arriba a abajo
            val columnasCirculos = ArrayList<ArrayList<Point>>()
            for (centro in centrosCirculos) {
                var agregado = false
                for (columna in columnasCirculos) {
                    if (Math.abs(columna[0].x - centro.x) < radio) {
                        columna.add(centro)
                        agregado = true
                        break
                    }
                }
                if (!agregado) {
                    val nuevaColumna = ArrayList<Point>()
                    nuevaColumna.add(centro)
                    columnasCirculos.add(nuevaColumna)
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                columnasCirculos.sortWith(Comparator.comparingDouble { it[0].x })
            }
            for (columna in columnasCirculos) {
                contadorCirculosDNI = 0
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    columna.sortWith(Comparator.comparingDouble { it.y })
                }
                for (centro in columna) {
                    val radioCirculosOrdenados = radiosCirculos[centrosCirculos.indexOf(centro)]

                    // Dibujar círculos en la imagen procesada
                    Imgproc.circle(src, centro, radioCirculosOrdenados, Scalar(0.0, 255.0, 255.0), 2)

                    // Ajustar las coordenadas del centro para la imagen original
                    val centroOriginal = Point(centro.x + imagenOriginal.x, centro.y + imagenOriginal.y)

                    // Dibujar círculos en la imagen original
                    Imgproc.circle(originalImagen, centroOriginal, radio, Scalar(0.0, 255.0, 255.0), 2)

                    // Verificar si el círculo está relleno
                    if (verificacionFormas.verificarRelleno(src, centro, radioCirculosOrdenados)) {
                        Imgproc.circle(src, centro, radioCirculosOrdenados, Scalar(0.0, 255.0, 0.0), 2)
                        // Dibujar círculos en la imagen original
                        Imgproc.circle(originalImagen, centroOriginal, radio, Scalar(0.0, 255.0, 0.0), -1)
                        // Asignar un número DNI al círculo
                        println("Numero Dni $contadorCirculosDNI")
                        Sesion.instance.addDniNie(contadorCirculosDNI.toString())
                        contadorCirculosDNI = 0
                    }
                    contadorCirculosDNI++
                }
            }
        }
    }

    private fun detectarCirculosCodExamen(bordes: Mat, src: Mat, rectInterno: Rect, originalImagen: Mat, coordenadasOriginales: Rect) {
        var radio = 0
        var contadorCirculosDNI: Int
        val respuestas = ArrayList<String>()
        val rect = Rect(rectInterno.x, rectInterno.y, rectInterno.width, rectInterno.height)

        // Asegurarse de que los índices están dentro de los límites
        rect.x = Math.max(rect.x, 0)
        rect.y = Math.max(rect.y, 0)
        rect.width = Math.min(rect.width, bordes.cols() - rect.x)
        rect.height = Math.min(rect.height, bordes.rows() - rect.y)

        // Asegurarse de que el tamaño del rectángulo es positivo
        if (rect.width <= 0 || rect.height <= 0) {
            return
        }

        val imagenRectangulo = Mat(bordes, rect)

        // Escalar la imagen si es necesario añadir casos de resoluciones
        val scale = 1.0
        val size = Size(imagenRectangulo.cols() * scale, imagenRectangulo.rows() * scale)
        Imgproc.resize(imagenRectangulo, imagenRectangulo, size)

        val circulos = Mat()
        var minDist = Math.min(imagenRectangulo.rows(), imagenRectangulo.cols()) / 8
        if (minDist < 1) {
            minDist = 1
        }

        // Ajustar los parámetros según sea necesario 100, 30, 13, 21 valores clave
        Imgproc.HoughCircles(imagenRectangulo, circulos, Imgproc.CV_HOUGH_GRADIENT, 1.0, 2.0, 50.0, 20.0, 20, 35)

        val centrosCirculos = ArrayList<Point>()
        val radiosCirculos = ArrayList<Int>()
        for (i in 0 until circulos.cols()) {
            val datosCirculo = circulos.get(0, i) ?: continue
            // Crear el centro del círculo y el radio con los datos recuperados
            val centro = Point(datosCirculo[0] + rectInterno.x, datosCirculo[1] + rectInterno.y)
            radio = datosCirculo[2].roundToInt()

            var solapado = false
            for (j in centrosCirculos.indices) {
                if (distancia(centro, centrosCirculos[j]) < radio + radiosCirculos[j]) {
                    solapado = true
                    break
                }
            }

            if (!solapado) {
                centrosCirculos.add(centro)
                radiosCirculos.add(radio)
            }
        }
        // Ordenar los círculos de arriba a abajo
        val columnasCirculos = ArrayList<ArrayList<Point>>()
        for (centro in centrosCirculos) {
            var agregado = false
            for (columna in columnasCirculos) {
                if (Math.abs(columna[0].x - centro.x) < radio) {
                    columna.add(centro)
                    agregado = true
                    break
                }
            }
            if (!agregado) {
                val nuevaColumna = ArrayList<Point>()
                nuevaColumna.add(centro)
                columnasCirculos.add(nuevaColumna)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            columnasCirculos.sortWith(Comparator.comparingDouble { it[0].x })
        }
        for (columna in columnasCirculos) {
            contadorCirculosDNI = 0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                columna.sortWith(Comparator.comparingDouble { it.y })
            }
            for (centroCirculosColumnas in columna) {
                val radioCirculosOrdenados = radiosCirculos[centrosCirculos.indexOf(centroCirculosColumnas)]

                // Dibujar círculos en la imagen procesada
                Imgproc.circle(src, centroCirculosColumnas, radioCirculosOrdenados, Scalar(0.0, 255.0, 255.0), 2)

                // Ajustar las coordenadas del centro para la imagen original
                val centroOriginal = Point(centroCirculosColumnas.x + coordenadasOriginales.x, centroCirculosColumnas.y + coordenadasOriginales.y)

                // Dibujar círculos en la imagen original
                Imgproc.circle(originalImagen, centroOriginal, radio, Scalar(0.0, 255.0, 255.0), 2)

                // Verificar si el círculo está relleno
                if (verificacionFormas.verificarRelleno(src, centroCirculosColumnas, radioCirculosOrdenados)) {
                    // Dibujar círculos en la imagen procesada
                    Imgproc.circle(src, centroCirculosColumnas, radioCirculosOrdenados, Scalar(0.0, 255.0, 0.0), 2)
                    // Dibujar círculos en la imagen original
                    Imgproc.circle(originalImagen, centroOriginal, radio, Scalar(0.0, 255.0, 0.0), -1)
                    // Asignar un número de examen al círculo
                    println("Numero Examen $contadorCirculosDNI")
                    Sesion.instance.examCode.add(contadorCirculosDNI)
                    contadorCirculosDNI = 0
                }
                contadorCirculosDNI++
            }
        }
    }

    private fun distancia(p1: Point, p2: Point): Double {
        return Math.sqrt(Math.pow(p1.x - p2.x, 2.0) + Math.pow(p1.y - p2.y, 2.0))
    }
}
