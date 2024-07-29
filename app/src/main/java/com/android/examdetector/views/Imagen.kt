package com.android.examdetector.views

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.android.examdetector.utils.ImageViewModel
import com.android.examdetector.utils.bitmapToBase64

@Composable
fun Seleccionarimagen(navController: NavController, imageViewModel: ImageViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var checked by remember { mutableStateOf(false)}
    var checkedImageFinal by remember { mutableStateOf(false)}
    var checkedImageBorder by remember { mutableStateOf(false)}
    var hasCameraPermission by remember { mutableStateOf(false) }
    var hasGalleryPermission by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val photo: Bitmap = result.data?.extras?.get("data") as Bitmap
            val photoBase64 = bitmapToBase64(photo)
            imageViewModel.image.value = photoBase64
            navController.navigate("VerResultadoImagen/${checked.toString()}/${checkedImageBorder.toString()}/${checkedImageFinal.toString()}")
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri = result.data?.data
            val bitmap = if (imageUri != null) {
                MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
            } else {
                null
            }
            if (bitmap != null) {
                val photoBase64 = bitmapToBase64(bitmap)
                imageViewModel.image.value = photoBase64
                navController.navigate("VerResultadoImagen/${checked.toString()}/${checkedImageBorder.toString()}/${checkedImageFinal.toString()}")
            }
        }
    }

    val requestCameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(context, "Permiso de cámara concedido", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

    val requestGalleryPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(context, "Permiso de galería concedido", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Permiso de galería denegado", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        hasCameraPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        hasGalleryPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasCameraPermission = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED

                hasGalleryPermission = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box ( modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row {
                Spacer(modifier = Modifier.height(70.dp))
            }
            Row {


                Text(
                    text = "Seleccionar Imagen",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

            }

            Row {
                Spacer(modifier = Modifier.height(25.dp))
            }
            Row {
                Text(
                    text = "Puede seleccionar la imagen desde la cámara o la galería. La imagen seleccionada será procesada y se mostrará el resultado junto con la imagen original.",
                    fontSize = 20.sp,
                    color = Color.Gray
                )
            }
            Row {
                Spacer(modifier = Modifier.height(25.dp))
            }
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Button(onClick = {
                    if (hasCameraPermission) {
                        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        cameraLauncher.launch(cameraIntent)
                    } else {
                        requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }, modifier = Modifier.size(150.dp, 100.dp)) {
                    Text(text = "Abrir Cámara")
                }
                Spacer(modifier = Modifier.width(24.dp)) // Use width for horizontal spacing
                Button(onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val galleryIntent =
                            Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        galleryLauncher.launch(galleryIntent)
                    }
                    if (hasGalleryPermission) {
                        val galleryIntent =
                            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        galleryLauncher.launch(galleryIntent)
                    } else {
                        requestGalleryPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                }, modifier = Modifier.size(150.dp, 100.dp)) {
                    Text(text = "Abrir Galería")
                }

            }
            Row {
                Spacer(modifier = Modifier.height(25.dp))
            }
            Row(modifier = Modifier
                .padding(8.dp)
                .fillMaxSize())
            {
                Spacer(modifier = Modifier.height(5.dp))
                Text(text = "¿Quieres guardar la imagen Procesada en la galería?", fontSize = 20.sp)
                Checkbox(checked = checked,
                    onCheckedChange = { checked = it },
                    colors = CheckboxDefaults.colors(
                        checkmarkColor = Color.White,
                        checkedColor = Color.Blue,
                        uncheckedColor = Color.Gray
                    )
                )

            }
            Row {
                Spacer(modifier = Modifier.height(25.dp))

            }
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxSize()
            ) {
                Text(text ="¿Quieres guardar la imagen que OpenCv reconoce?", fontSize = 20.sp)
                Checkbox(checked = checkedImageBorder,
                    onCheckedChange = { checkedImageBorder = it },
                    colors = CheckboxDefaults.colors(
                        checkmarkColor = Color.White,
                        checkedColor = Color.Blue,
                        uncheckedColor = Color.Gray
                    )
                )
            }
            Row {
                Spacer(modifier = Modifier.height(25.dp))

            }
            Row (
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxSize()
            ){
                Text(text ="¿Quieres guardar la imagen que OpenCv recortará?", fontSize = 20.sp)
                Checkbox(checked = checkedImageFinal,
                    onCheckedChange = { checkedImageFinal = it },
                    colors = CheckboxDefaults.colors(
                        checkmarkColor = Color.White,
                        checkedColor = Color.Blue,
                        uncheckedColor = Color.Gray
                    )
                )
            }

        }
    }

}

