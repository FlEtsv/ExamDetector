package org.opencv.samples.recorder

import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import kotlin.math.pow
import kotlin.math.sqrt

object auxiliar {
    fun distancia(p1: Point, p2: Point): Double {
        return sqrt((p1.x - p2.x).pow(2.0) + (p1.y - p2.y).pow(2.0))
    }

    fun preprocesarImagen(src: Mat): Mat {
        val gris = Mat()
        Imgproc.cvtColor(src, gris, Imgproc.COLOR_BGR2GRAY)

        val suavizado = Mat()
        Imgproc.GaussianBlur(gris, suavizado, Size(5.0, 5.0), 0.0)

        val bilateral = Mat()
        Imgproc.bilateralFilter(suavizado, bilateral, 9, 75.0, 75.0)

        val binaria = Mat()
        Imgproc.adaptiveThreshold(bilateral, binaria, 255.0, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11,
            1.0
        )
        Core.bitwise_not(binaria, binaria)

        Imgproc.dilate(binaria, binaria, Mat(), Point(-1.0, -1.0), 1)
        Imgproc.erode(binaria, binaria, Mat(), Point(-1.0, -1.0), 1)

        val bordes = Mat()
        Imgproc.Canny(binaria, bordes, 50.0, 150.0)

        return bordes
    }

    private fun aplicarCorreccionGamma(src: Mat, gamma: Double): Mat {
        val lut = Mat(1, 256, CvType.CV_8U)
        lut.setTo(Scalar(0.0))
        for (i in 0 until 256) {
            lut.put(0, i, (i / 255.0).pow(gamma) * 255.0)
        }
        val corregida = Mat()
        Core.LUT(src, lut, corregida)
        return corregida
    }

    fun detectarBordes(src: Mat): Mat {
        val bordes = Mat()
        Imgproc.Canny(src, bordes, 100.0, 150.0)
        return bordes
    }

    fun estaSolapando(rect1: Rect, rectangulos: List<Rect>): Boolean {
        val margen = 1.0
        for (rect2 in rectangulos) {
            if (rect1.x < rect2.x + rect2.width + margen &&
                rect1.x + rect1.width + margen > rect2.x &&
                rect1.y < rect2.y + rect2.height + margen &&
                rect1.y + rect1.height + margen > rect2.y) {
                return true
            }
        }
        return false
    }

    private fun superponen(r1: Rect, r2: Rect): Boolean {
        return r1.x < r2.x + r2.width &&
                r1.x + r1.width > r2.x &&
                r1.y < r2.y + r2.height &&
                r1.y + r1.height > r2.y
    }
}