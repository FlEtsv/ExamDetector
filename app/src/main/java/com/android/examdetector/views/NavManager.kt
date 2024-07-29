package com.android.examdetector.views

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.android.examdetector.utils.ImageViewModel

@Composable
fun NavManager() {
    val navController = rememberNavController()
    val imageViewModel: ImageViewModel = viewModel()

    NavHost(navController = navController, startDestination = "CargadoView") {
        composable("CargadoView") {
            CargadoView(navController)
        }

        composable("Seleccionarimagen") {
            Seleccionarimagen(navController, imageViewModel)
        }
        composable("VerResultadoImagen/{checkedValue}/{guardarImagenBordes}/{guardarImagenFinal}") {backStackEntry ->
            val checkedValue = backStackEntry.arguments?.getString("checkedValue")?.toBoolean()
            val guardarImagenBordes = backStackEntry.arguments?.getString("guardarImagenBordes")?.toBoolean()
            val guardarImagenFinal = backStackEntry.arguments?.getString("guardarImagenFinal")?.toBoolean()
            if (checkedValue != null && guardarImagenBordes != null && guardarImagenFinal != null) {
                        VerResultadoImagen(navController, imageViewModel, context = LocalContext.current,checkedValue = checkedValue, guardarImagenBordes, guardarImagenFinal)


            }
        }
    }
}