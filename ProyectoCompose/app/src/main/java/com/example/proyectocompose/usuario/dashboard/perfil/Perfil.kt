package com.example.proyectocompose.usuario.dashboard.perfil

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.proyectocompose.BuildConfig
import com.example.proyectocompose.R
import com.example.proyectocompose.Rutas
import com.example.proyectocompose.common.ComboBox
import com.example.proyectocompose.login.DatePickerEdad
import com.example.proyectocompose.login.SliderPreference
import com.example.proyectocompose.model.Formulario
import com.example.proyectocompose.model.User
import com.example.proyectocompose.usuario.dashboard.DashboardViewModel
import java.io.File


@Composable
fun Perfil(navController: NavController, dashboardViewModel: DashboardViewModel) {


    Scaffold(
        topBar = {
            TopBarProfile(navController = navController)
        }) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            BodyProfile(viewModel = dashboardViewModel, navController = navController)
        }
    }


}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarProfile(navController: NavController) {

    TopAppBar(
        colors = topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("Perfil")
            }
        },
        navigationIcon = {
            IconButton(
                onClick = { navController.popBackStack(Rutas.dashboard, inclusive = false) }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver atrás"
                )
            }
        }
    )
}

@Composable
fun BodyProfile(viewModel: DashboardViewModel, navController: NavController) {
    val usuario = viewModel.usuario.collectAsState()
    val nombre = remember { mutableStateOf(usuario.value.nombre) }
    val apellidos = remember { mutableStateOf(usuario.value.apellidos) }
    val fecnac = remember { mutableStateOf(usuario.value.fecNac) }
    val relacionSeria =
        remember { mutableStateOf(usuario.value.formulario?.relacionSeria ?: false) }
    val deportes = remember { mutableIntStateOf(usuario.value.formulario?.deportes ?: 50) }
    val arte = remember { mutableIntStateOf(usuario.value.formulario?.arte ?: 50) }
    val politica = remember { mutableIntStateOf(usuario.value.formulario?.politica ?: 50) }
    val tieneHijos = remember { mutableStateOf(usuario.value.formulario?.tieneHijos ?: false) }
    val quiereHijos = remember { mutableStateOf(usuario.value.formulario?.quiereHijos ?: false) }
    val interesSexual =
        remember { mutableStateOf(usuario.value.formulario?.interesSexual ?: "") }
    val textBoxActivos = remember { mutableStateOf(false) }
    //
    val listaimagenes by viewModel.userImages.collectAsState()
    val profileImage by viewModel.profileImg.collectAsState()
    val addPhoto = remember { mutableStateOf(false) }
    val imageFile by viewModel.imageFile.collectAsState()
    val esPerfil = remember { mutableStateOf(false) }
    val contexto = LocalContext.current
    val imageUploaded by viewModel.imageUploaded.collectAsState()

    val usuarioMod by viewModel.usuarioMod.collectAsState()
    val usuarioInicial = remember(usuario.value) { usuario.value.copy() }
    val isLoading by viewModel.isLoading.collectAsState()
    val haModificado = remember {
        derivedStateOf {
            usuarioInicial.nombre != nombre.value ||
                    usuarioInicial.apellidos != apellidos.value ||
                    usuarioInicial.fecNac != fecnac.value ||
                    usuarioInicial.formulario?.relacionSeria != relacionSeria.value ||
                    usuarioInicial.formulario?.deportes != deportes.intValue ||
                    usuarioInicial.formulario?.arte != arte.intValue ||
                    usuarioInicial.formulario?.politica != politica.intValue ||
                    usuarioInicial.formulario?.tieneHijos != tieneHijos.value ||
                    usuarioInicial.formulario?.quiereHijos != quiereHijos.value ||
                    usuarioInicial.formulario?.interesSexual != interesSexual.value
        }
    }



    LaunchedEffect(imageUploaded) {
        if (imageUploaded) {
            viewModel.cargarImagenes()
            viewModel.setUploaded(false)
        }
    }


    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                imageFile?.let { file ->
                    viewModel.updateImageUri(Uri.fromFile(file))
                    viewModel.uploadImage(contexto, esPerfil.value, listaimagenes.size + 1)
                }
            } else {
                viewModel.updateImageUri(Uri.EMPTY)
            }
        }


    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                val file = File.createTempFile("CAM_", ".jpg", contexto.cacheDir)
                viewModel.setImageFile(file)
                cameraLauncher.launch(
                    FileProvider.getUriForFile(
                        contexto,
                        BuildConfig.APPLICATION_ID + ".provider",
                        file
                    )
                )
            }
        }
    val galleryLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val inputStream = contexto.contentResolver.openInputStream(it)
                val tempFile = File.createTempFile("GAL_", ".jpg", contexto.cacheDir)
                tempFile.outputStream().use { outputStream ->
                    inputStream?.copyTo(outputStream)
                }
                viewModel.updateImageUri(it)
                viewModel.setImageFile(tempFile)
                viewModel.uploadImage(contexto, esPerfil.value, listaimagenes.size + 1)
            }
        }



    Box(
        modifier = Modifier
            .fillMaxSize()
            .blur(if (isLoading) 8.dp else 0.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally
        ) {

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (profileImage != "") {
                            AsyncImage(
                                model = profileImage,
                                contentDescription = "Imagen perfil",
                                modifier = Modifier
                                    .size(100.dp)
                                    .border(1.dp, Color.Black)
                            )
                        } else {
                            Image(
                                painter = painterResource(R.drawable.ic_no_image),
                                contentDescription = "Imagen perfil",
                                modifier = Modifier
                                    .size(100.dp)
                                    .border(1.dp, Color.Black)
                            )
                        }

                        Spacer(modifier = Modifier.width(20.dp))

                        Column {
                            TextField(
                                value = nombre.value,
                                onValueChange = { nombre.value = it },
                                label = { Text("Nombre:") },
                                enabled = textBoxActivos.value,
                                modifier = Modifier.width(230.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                )
                            )
                            TextField(
                                value = apellidos.value,
                                onValueChange = { apellidos.value = it },
                                label = { Text("Apellidos:") },
                                modifier = Modifier.width(230.dp),
                                enabled = textBoxActivos.value,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    //unfocusedIndicatorColor = if (textBoxActivos.value) Color.Green.copy(alpha = 0.6f) else Color.Gray,
                                    //focusedIndicatorColor = if (textBoxActivos.value) Color.Green else Color.Blue

                                )
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))
                        IconButton(
                            onClick = { textBoxActivos.value = !textBoxActivos.value },
                            modifier = Modifier.border((if(textBoxActivos.value)2 else 0).dp, color = Color.Green,
                                CircleShape)
                        ) {
                            Icon(imageVector = Icons.Filled.Edit, contentDescription = "Editar")
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(top = 15.dp), color = Color.Black)
                }

                item {
                    DatePickerEdad(fecnac.value) { fecnac.value = it }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                item {
                    Text("Imágenes:", fontSize = 25.sp)
                    Spacer(modifier = Modifier.height(20.dp))
                    LazyRow(verticalAlignment = Alignment.CenterVertically) {
                        items(listaimagenes) { image ->
                            AsyncImage(
                                model = image,
                                contentDescription = "Imagen del usuario",
                                modifier = Modifier
                                    .width(200.dp)
                                    .height(200.dp)
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.width(10.dp))
                            if (listaimagenes.size < 5) {
                                IconButton(onClick = { addPhoto.value = true }) {
                                    Icon(
                                        imageVector = Icons.Filled.AddCircle,
                                        contentDescription = "Añadir imagen"
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }


                //      PREFERENCIAS

                item {
                    Text("Preferencias:", fontSize = 30.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Relación seria:", modifier = Modifier.weight(0.5f))
                        Switch(
                            checked = relacionSeria.value,
                            onCheckedChange = { relacionSeria.value = it },
                            modifier = Modifier.weight(0.5f)
                        )
                    }
                    Spacer(modifier = Modifier.height(15.dp))
                }

                item { SliderPreference("Deportes", deportes.value) { deportes.value = it } }
                item { SliderPreference("Arte", arte.value) { arte.value = it } }
                item { SliderPreference("Política", politica.value) { politica.value = it } }

                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("¿Tienes hijos?: ")
                        ComboBox(
                            listOf("No", "Sí"),
                            default = (if (tieneHijos.value) "Si" else "No")
                        ) { tieneHijos.value = it == "Sí" }
                    }
                    Spacer(modifier = Modifier.height(15.dp))
                }

                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("¿Quieres tener hijos?: ")
                        ComboBox(
                            listOf("No", "Sí"),
                            default = (if (tieneHijos.value) "Si" else "No")
                        ) { quiereHijos.value = it == "Sí" }
                    }
                    Spacer(modifier = Modifier.height(15.dp))
                }

                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Interesado en: ")
                        ComboBox(
                            listOf("Mujer", "Hombre", "Ambos"),
                            default = interesSexual.value
                        ) {
                            interesSexual.value = it
                        }
                    }
                    Spacer(modifier = Modifier.height(35.dp))
                }
            }
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {

                Button(onClick = {
                    viewModel.actualizarUsuario(
                        User(
                            nombre = nombre.value,
                            apellidos = apellidos.value,
                            activo = true,
                            conectado = true,
                            correo = usuario.value.correo,
                            fecNac = fecnac.value,
                            formCompletado = true,
                            rol = usuario.value.rol,
                            formulario = Formulario(
                                relacionSeria = relacionSeria.value,
                                deportes = deportes.value,
                                interesSexual = interesSexual.value,
                                politica = politica.value,
                                quiereHijos = quiereHijos.value,
                                tieneHijos = tieneHijos.value,
                                arte = arte.value
                            )
                        )
                    )
                }, enabled = haModificado.value) {
                    Text(text = "Guardar cambios")
                }
            }


            if (addPhoto.value) {
                OpcionFoto(
                    onDismissRequest = {
                        addPhoto.value = false
                    },
                    onConfirmation = { num ->
                        addPhoto.value = false
                        esPerfil.value = false
                        if (num == 1) {
                            galleryLauncher.launch("image/*")
                        } else {
                            permissionLauncher.launch(android.Manifest.permission.CAMERA)
                        }
                    }
                )
            }
            if (usuarioMod) {
                navController.navigate(Rutas.dashboard) {
                    popUpTo(Rutas.dashboard) { inclusive = false }
                }
                viewModel.setUsuarioMod(false)
            }

        }
        if (isLoading) {
            CircularProgressIndicator()
        }
    }
}


@Composable
fun OpcionFoto(
    onDismissRequest: () -> Unit,
    onConfirmation: (Int) -> Unit
) {
    AlertDialog(
        icon = {
            Icon(painterResource(R.drawable.ic_documento), contentDescription = "Icono Términos")
        },
        title = {
            Text(text = "Añadir foto")
        },
        text = {

        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            Button(onClick = { onConfirmation(1) }) { Text(text = "Galería") }
            Button(onClick = { onConfirmation(2) }) { Text(text = "Cámara") }
        }
    )
}



