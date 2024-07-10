package org.opencv.samples.recorder

import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfDouble
import org.opencv.core.Point
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import kotlin.math.pow
import kotlin.math.sqrt

object verificacionFormas {

    fun esRectangulo(rect: Rect, img: Mat): Boolean {
        val minWidthPercent = 0.1
        val minAreaPercent = 5.5

        val totalWidth = img.cols().toDouble()
        val totalArea = (img.rows() * img.cols()).toDouble()

        val rectWidthPercent = (rect.width / totalWidth) * 100
        val rectAreaPercent = (rect.area() / totalArea) * 100

        return rect.width > rect.height * 3 && rectWidthPercent > minWidthPercent && rectAreaPercent > minAreaPercent
    }

    fun esRectanguloTorcido(rect: Rect, img: Mat): Boolean {
        val minWidthPercent = 0.1
        val minAreaPercent = 3.0

        val totalWidth = img.cols().toDouble()
        val totalArea = (img.rows() * img.cols()).toDouble()

        val rectWidthPercent = (rect.width / totalWidth) * 100
        val rectAreaPercent = (rect.area() / totalArea) * 100

        val aspectRatio = rect.width.toDouble() / rect.height
        val lowerBound = 2.5
        val upperBound = 3.5

        return aspectRatio > lowerBound && aspectRatio < upperBound && rectWidthPercent > minWidthPercent && rectAreaPercent > minAreaPercent
    }

    fun verificarRelleno(src: Mat, centro: Point, radio: Int): Boolean {
        // Convertir a escala de grises si la imagen no está en escala de grises
        val gray = if (src.channels() > 1) {
            val grayMat = Mat()
            Imgproc.cvtColor(src, grayMat, Imgproc.COLOR_BGR2GRAY)
            grayMat
        } else {
            src
        }

        // Ecualizar el histograma para normalizar la iluminación
        val equalizedGray = Mat()
        Imgproc.equalizeHist(gray, equalizedGray)

        var sumaIntensidades = 0.0
        var contadorPixeles = 0

        for (y in (centro.y - radio).toInt()..(centro.y + radio).toInt()) {
            for (x in (centro.x - radio).toInt()..(centro.x + radio).toInt()) {
                if ((x - centro.x).pow(2) + (y - centro.y).pow(2) <= radio.toDouble().pow(2)) {
                    if (y >= 0 && y < equalizedGray.rows() && x >= 0 && x < equalizedGray.cols()) {
                        val intensidad = equalizedGray.get(y, x)[0]
                        sumaIntensidades += intensidad
                        contadorPixeles++
                    }
                }
            }
        }

        val intensidadMedia = sumaIntensidades / contadorPixeles
        // Definir un umbral para decidir si el círculo está relleno
        return intensidadMedia < 80
    }


    fun asignarPosicionAlfabetica(indiceCirculo: Int): Char {
        return 'A' + indiceCirculo
    }

    fun esCasiCuadrado(rect: Rect, img: Mat): Boolean {
        val minHeightPercent = 3.0
        val minAreaPercent = 16.0

        val totalHeight = img.rows().toDouble()
        val totalArea = (img.rows() * img.cols()).toDouble()

        val rectHeightPercent = (rect.height / totalHeight) * 100
        val rectAreaPercent = (rect.area() / totalArea) * 100

        return rect.height > rect.width * 1.18 && rectHeightPercent > minHeightPercent && rectAreaPercent > minAreaPercent
    }
}