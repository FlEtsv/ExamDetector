package org.opencv.samples.recorder

import org.opencv.core.*
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.util.ArrayList

class ExtraerDNI {
    companion object {
        fun init(image: String): Mat {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME)

            // Load the image
            val src = Imgcodecs.imread(image)
            if (src.empty()) {
                println("Image could not be opened.")
                return src
            }

            // Convert the image to grayscale
            val gray = Mat()
            Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY)

            // Apply a smoothing filter to reduce noise
            Imgproc.GaussianBlur(gray, gray, Size(5.0, 5.0), 0.0)

            // Detect edges using the Canny edge detector
            val edges = Mat()
            Imgproc.Canny(gray, edges, 75.0, 200.0)

            // Find contours
            val contours: MutableList<MatOfPoint> = ArrayList()
            val hierarchy = Mat()
            Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE)

            // Calculate 50% of the total image area
            val halfTotalArea = 0.5 * src.rows() * src.cols()

            // Select the DNI contour
            var dniRect: Rect? = null
            var maxArea = 0.0
            for (contour in contours) {
                val rect = Imgproc.boundingRect(contour)
                val area = rect.area()
                if (area > maxArea && area < halfTotalArea) {
                    maxArea = area
                    dniRect = rect
                }
            }

            var dni: Mat? = null
            if (dniRect != null) {
                // Crop and save the DNI region
                dni = Mat(src, dniRect)
                Sesion.instance.dniCoordinates = dniRect // Asignar el nuevo objeto Rect a dniCoordinates

                Imgcodecs.imwrite("img/Dni.png", dni)
                println("DNI detected and saved as Dni.png")
            } else {
                println("DNI not detected.")
            }

            return dni ?: throw RuntimeException("DNI not found.")
        }
    }
}