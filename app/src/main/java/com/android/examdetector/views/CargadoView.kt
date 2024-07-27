package com.android.examdetector.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.android.examdetector.R

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CargadoView(navController: NavController) {
    var showLoading by remember { mutableStateOf(true) }

    LaunchedEffect(key1 = true) {
        delay(3500)
        showLoading = false
    }

    if (showLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                Spacer(modifier = Modifier.padding(24.dp))
                Text(
                    text = "Cargando...",
                    fontSize = 24.sp,
                    color = colorResource(id = R.color.black),

                    )
                Spacer(modifier = Modifier.padding(8.dp))
                Text(
                    text = "Por favor espere",
                    fontSize = 18.sp,
                    color = colorResource(id = R.color.black),

                    )
                Spacer(modifier = Modifier.padding(8.dp))
                CirculoCarga()
            }
        }
    } else {
        navController.navigate("Seleccionarimagen") {
            popUpTo("CargadoView") {
                inclusive = true
            }
        }
    }
}
@Composable
fun CirculoCarga() {

    CircularProgressIndicator(
        color = colorResource(id = R.color.purple_200)
    )
}

