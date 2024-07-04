package com.android.examdetector.views

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.android.examdetector.utils.ImageViewModel

@Composable
fun NavManager() {
    val navController = rememberNavController()
    val imageViewModel: ImageViewModel = viewModel()

    NavHost(navController = navController, startDestination = "Seleccionarimagen") {

        composable("Seleccionarimagen") {
            Seleccionarimagen(navController, imageViewModel)
        }
        composable("VerResultadoImagen") {
            VerResultadoImagen(navController, imageViewModel)
        }
    }
}