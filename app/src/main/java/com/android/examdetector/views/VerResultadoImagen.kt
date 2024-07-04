package com.android.examdetector.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import android.util.Base64
import android.graphics.BitmapFactory
import com.android.examdetector.utils.ImageViewModel

@Composable
fun VerResultadoImagen(navController: NavController, imageViewModel: ImageViewModel) {
    val fotoBase64 = imageViewModel.image.value ?: ""
    val bytes = Base64.decode(fotoBase64, Base64.DEFAULT)
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Imagen seleccionada",
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
        )
    }
}
