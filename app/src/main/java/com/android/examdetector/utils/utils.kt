package com.android.examdetector.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import java.io.ByteArrayOutputStream
import java.io.OutputStream

fun bitmapToBase64(bitmap: Bitmap): String {
    val byteArrayOutputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
    val byteArray = byteArrayOutputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.DEFAULT)
}

fun bitmapToMat(bitmap: Bitmap): Mat {
    val mat = Mat()
    Utils.bitmapToMat(bitmap, mat)
    return mat
}
fun convertirProfundidad(mat: Mat, depth: Int): Mat {
    val matConvertida = Mat()
    mat.convertTo(matConvertida, depth)
    return matConvertida
}
fun matToBitmap(mat: Mat): Bitmap {
    // Convertir a una profundidad compatible
    val matConvertida = convertirProfundidad(mat, CvType.CV_8U)

    // Verificar y convertir el tipo si es necesario
    var matFinal = Mat()
    if (matConvertida.type() != CvType.CV_8UC1 && matConvertida.type() != CvType.CV_8UC3 && matConvertida.type() != CvType.CV_8UC4) {
        Imgproc.cvtColor(matConvertida, matFinal, Imgproc.COLOR_RGBA2BGRA)
    } else {
        matFinal = matConvertida
    }

    // Convertir a Bitmap
    val bitmap = Bitmap.createBitmap(matFinal.cols(), matFinal.rows(), Bitmap.Config.ARGB_8888)
    Utils.matToBitmap(matFinal, bitmap)
    return bitmap
}



fun escalarImagen(src: Mat): Mat {
    // Crear un objeto Size con las dimensiones deseadas
    val size = Size(3024.0, 4032.0)

    // Crear un objeto Mat para almacenar la imagen escalada
    val dst = Mat()

    // Escalar la imagen
    Imgproc.resize(src, dst, size)

    return dst
}
fun saveImageToGallery(mat: Mat, context: Context, filename: String) {
    // Convertir a una profundidad compatible
    val matConvertida = convertirProfundidad(mat, CvType.CV_8U)

    // Verificar y convertir el tipo si es necesario
    var matFinal = Mat()
    if (matConvertida.type() != CvType.CV_8UC1 && matConvertida.type() != CvType.CV_8UC3 && matConvertida.type() != CvType.CV_8UC4) {
        Imgproc.cvtColor(matConvertida, matFinal, Imgproc.COLOR_RGBA2BGRA)
    } else {
        matFinal = matConvertida
    }

    // Convertir Mat a Bitmap
    val bmp = Bitmap.createBitmap(matFinal.cols(), matFinal.rows(), Bitmap.Config.ARGB_8888)
    Utils.matToBitmap(matFinal, bmp)

    // Guardar la imagen en la galería
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }
    }

    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    uri?.let {
        val outputStream: OutputStream? = resolver.openOutputStream(it)
        outputStream?.use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}

