package org.opencv.samples.recorder

import android.content.Context
import com.android.examdetector.utils.saveImageToGallery
import org.opencv.core.*
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.util.ArrayList

class ExtraerDNI {
    companion object {
        fun init(image: Mat, context: Context): Mat {

            // Convert the image to grayscale
            val gray = Mat()
            Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY)

            // Apply a smoothing filter to reduce noise
            Imgproc.GaussianBlur(gray, gray, Size(5.0, 5.0), 0.0)

            // Detect edges using the Canny edge detector
            val edges = Mat()
            Imgproc.Canny(gray, edges, 75.0, 200.0)


            // Find contours
            val contours: MutableList<MatOfPoint> = ArrayList()
            val hierarchy = Mat()
            Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)
            saveImageToGallery(hierarchy, context, "EdgesDni.png")

            // Calculate 50% of the total image area
            val halfTotalArea = 0.5 * image.rows() * image.cols()

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
                dni = Mat(image, dniRect)
                Sesion.instance.dniCoordinates = dniRect // Asignar el nuevo objeto Rect a dniCoordinates
                saveImageToGallery(dni, context, "Dni.png")

                println("DNI saved as Dni.png")
            } else {
                println("DNI not detected.")
            }

            return dni ?: throw RuntimeException("DNI not found.")
        }
    }
}