package com.android.examdetector.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.android.examdetector.utils.ImageViewModel
import com.android.examdetector.utils.bitmapToMat
import com.android.examdetector.utils.matToBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.opencv.core.Mat
import org.opencv.core.Rect
import org.opencv.samples.recorder.ExtraerColumnas
import org.opencv.samples.recorder.ExtraerDNI
import org.opencv.samples.recorder.ProcesadorImagen.detectarColumnas
import org.opencv.samples.recorder.Sesion

@Composable
fun VerResultadoImagen(
    navController: NavController,
    imageViewModel: ImageViewModel,
    context: Context
) {
    val fotoBase64 = imageViewModel.image.value ?: ""
    val bytes = Base64.decode(fotoBase64, Base64.DEFAULT)
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    var isImagenProcesed by remember { mutableStateOf(false) }
    var processedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val coroutineScope = rememberCoroutineScope()

    var isProcessCompleted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!isImagenProcesed && !isProcessCompleted) {
            coroutineScope.launch(Dispatchers.Default) {
                val imagenOriginal = bitmapToMat(bitmap)
                val imagenProcesada = procesarImagen(imagenOriginal, context)
                processedBitmap = matToBitmap(imagenProcesada)
                isImagenProcesed = true
                isProcessCompleted = true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "Resultados de la Imagen",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.CenterHorizontally)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Imagen Original",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Imagen seleccionada",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(16.dp)
                    )
                }
            }
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Imagen Procesada",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    if (processedBitmap != null) {
                        Image(
                            bitmap = processedBitmap!!.asImageBitmap(),
                            contentDescription = "Imagen procesada",
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(16.dp)
                        )
                    } else {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(50.dp)
                                .padding(32.dp)
                        )
                    }
                }
            }
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Datos extraidos de la imagen",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row {
                        val respuestas = Sesion.instance.examAnswers
                        Text(
                            text = "Respuestas: $respuestas",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    Row {
                        val codigos = Sesion.instance.examCode
                        Text(
                            text = "Codigos: $codigos",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    Row {
                        val dni = Sesion.instance.dniNie
                        Text(
                            text = "DNI/NIE: $dni",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )



                    }
                    Row {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}



fun procesarImagen(imagenOriginal: Mat, context: Context): Mat {
    //vamos a preparar la api sesion para que se pueda usar en el procesador de imagen
    //borrando los datos anteriores
    Sesion.instance.clearSession()
    Log.d("ExamDetector", "Iniciando procesamiento de imagen...")
    runBlocking {
        val dni = withContext(Dispatchers.Default) { ExtraerDNI.init(imagenOriginal, context) }
        val columnas: List<Mat> = withContext(Dispatchers.Default) { ExtraerColumnas.init(imagenOriginal, context) }

        // Procesar la imagen
        // Procesar cada columna y el DNI
        Log.d("ExamDetector", "Columnas extraidas: ${columnas.size}")
        for (i in columnas.indices) {
            val coordenadasColumnas: List<Rect> = Sesion.instance.columnCoordinates
            Log.d(
                "ExamDetector",
                "Procesando columna ${i + 1}... con coordenadas ${coordenadasColumnas[i]}"
            )
            detectarColumnas(columnas[i], 0, i, imagenOriginal, coordenadasColumnas[i], context)
        }
        detectarColumnas(dni, 1, columnas.size, imagenOriginal, Sesion.instance.dniCoordinates, context)
    }
    Log.d("ExamDetector", "Procesamiento de imagen finalizado.")
    return imagenOriginal
}



