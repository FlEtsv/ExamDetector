package org.opencv.samples.recorder

import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Rect
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
        var sumaIntensidades = 0
        var contadorPixeles = 0
        for (y in (centro.y - radio).toInt()..(centro.y + radio).toInt()) {
            for (x in (centro.x - radio).toInt()..(centro.x + radio).toInt()) {
                if ((x - centro.x).pow(2) + (y - centro.y).pow(2) <= radio.toDouble().pow(2)) {
                    if (y >= 0 && y < src.rows() && x >= 0 && x < src.cols()) {
                        val pixel = DoubleArray(1)
                        src.get(y, x, pixel)
                        val intensidad = pixel[0].toInt()
                        sumaIntensidades += intensidad
                        contadorPixeles++
                    }
                }
            }
        }
        val intensidadMedia = sumaIntensidades.toDouble() / contadorPixeles
        return intensidadMedia < 100
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