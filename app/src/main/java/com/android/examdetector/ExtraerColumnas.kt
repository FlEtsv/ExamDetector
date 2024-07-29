package org.opencv.samples.recorder

import android.content.Context
import com.android.examdetector.utils.saveImageToGallery
import org.opencv.core.*
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import kotlin.math.pow
import kotlin.math.sqrt

class ExtraerColumnas {
    companion object {
        fun init(image: Mat, context: Context, guardarDniBordes :Boolean, guardarDniFinal :Boolean): List<Mat> {
            val columns = extractColumns(image,context, guardarDniBordes)

            // Process each column to find things within them
            if(guardarDniFinal) {
                var columnNumber = 1
                for (column in columns) {
                    saveImageToGallery(column, context, "column_$columnNumber.png")
                    println("Column $columnNumber saved as column_$columnNumber.png")
                    columnNumber++
                }
            }
            return columns
        }

        fun extractColumns(imagePath: Mat, context : Context, guardarDniBordes: Boolean): List<Mat> {


            // Convert the image to grayscale
            val gray = Mat()
            Imgproc.cvtColor(imagePath, gray, Imgproc.COLOR_BGR2GRAY)

            // Apply a smoothing filter to reduce noise
            Imgproc.GaussianBlur(gray, gray, Size(5.0, 5.0), 0.0)

            // Detect edges using the Canny edge detector
            val edges = Mat()
            Imgproc.Canny(gray, edges, 75.0, 200.0)

            // Find contours
            val contours: MutableList<MatOfPoint> = ArrayList()
            val hierarchy = Mat()
            Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)
            if(guardarDniBordes) {
                saveImageToGallery(edges, context, "bordesImgDni.png")
            }

            // Sort contours by their position on the X axis (from left to right)
            contours.sortBy { Imgproc.boundingRect(it).x }

            // Calculate the total area of the image
            val totalArea = imagePath.rows() * imagePath.cols()
            val minArea = totalArea * 0.024 // 5% of the total area

            // Select the 5 largest columns that meet the aspect ratio, minimum area and minimum distance between centers
            val columns = mutableListOf<Rect>()
            val minDistance = 50 // minimum distance in pixels between the centers of the columns
            for (contour in contours) {
                val rect = Imgproc.boundingRect(contour)
                val aspectRatio = rect.height.toDouble() / rect.width
                if (aspectRatio >= 2.5 && rect.area() >= minArea) {
                    var overlapped = false
                    val centerRect = Point(rect.x + rect.width / 2.0, rect.y + rect.height / 2.0)
                    for (existingColumn in columns) {
                        val existingCenter = Point(existingColumn.x + existingColumn.width / 2.0, existingColumn.y + existingColumn.height / 2.0)
                        val distance = sqrt((centerRect.x - existingCenter.x).pow(2) + (centerRect.y - existingCenter.y).pow(2))
                        if (distance < minDistance) {
                            overlapped = true
                            break
                        }
                    }
                    if (!overlapped) {
                        columns.add(rect)
                        Sesion.instance.columnCoordinates.add(rect)
                        if (columns.size == 5) break
                    }
                }
            }

            // Crop and save each column as a new image
            val columnImages = mutableListOf<Mat>()
            for (column in columns) {
                val columnImg = Mat(imagePath, column)
                columnImages.add(columnImg)
            }

            return columnImages
        }
    }
}