package com.example.proyectocompose.login

import android.app.DatePickerDialog
import android.net.Uri
import android.util.Log
import android.widget.DatePicker
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.proyectocompose.BuildConfig
import com.example.proyectocompose.R
import com.example.proyectocompose.Rutas
import com.example.proyectocompose.common.BodyText
import com.example.proyectocompose.common.ClickableText
import com.example.proyectocompose.common.Subtitle
import java.io.File
import java.util.Calendar
import java.util.Date

@Composable
fun Formulario(navController: NavController, loginViewModel: LoginViewModel) {

    val viewModel = remember { FormularioViewModel() }
    var page by remember { mutableIntStateOf(1) }
    val registroCompletado by viewModel.completado.collectAsState()
    val contexto = LocalContext.current
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        when (page) {
            1 -> Info(viewModel) { page = 2 }
            2 -> DatosPreferencias(viewModel, back = { page = 1 })
            {
                viewModel.correo.value = loginViewModel.currentEmail.value
                viewModel.completarRegistro(context = contexto)
            }
        }
    }
    if(registroCompletado){
        navController.navigate(Rutas.usrNoActivo) {
            popUpTo(Rutas.login) { inclusive = false }
        }
        viewModel.setRegistroCompletado(false)
    }

}

@Composable
fun Info(viewModel: FormularioViewModel, next: () -> Unit) {

    val checked by viewModel.terms.collectAsState()
    val openAlertDialog = remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 70.dp, horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Subtitle(text = "¡Bienvenido a la Aplicación!")
            Spacer(modifier = Modifier.height(50.dp))

            BodyText(
                text = "A continuación, tendrás que completar la creación de tu cuenta" +
                        " y rellenar un formulario con tus intereses para recomendarte usuarios con gustos parecidos a los tuyos"
            )
            Spacer(modifier = Modifier.height(30.dp))

            BodyText(text = "Para continuar, acepte los términos y condiciones de la aplicación.")
            Spacer(modifier = Modifier.height(40.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = checked, onCheckedChange = { viewModel.terms.value = !checked })
                Spacer(modifier = Modifier.width(8.dp))
                ClickableText(text = "Aceptar términos y condiciones") {
                    openAlertDialog.value = true
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(onClick = next, enabled = checked) {
                Text("Siguiente")
            }
        }

        if (openAlertDialog.value) {
            Terminos({
                openAlertDialog.value = false
            }) {
                openAlertDialog.value = false
            }
        }

    }
}

@Composable
fun DatosPreferencias(viewModel: FormularioViewModel, back: () -> Unit, next: () -> Unit) {
    val nombre by viewModel.nombre.collectAsState()
    val contexto = LocalContext.current
    val apellidos by viewModel.apellidos.collectAsState()
    val fecNac by viewModel.fecNac.collectAsState()
    val relacionSeria by viewModel.relacionSeria.collectAsState()
    val deportes by viewModel.deportes.collectAsState()
    val arte by viewModel.arte.collectAsState()
    val politica by viewModel.politica.collectAsState()
    val interesSexual by viewModel.interesSexual.collectAsState()
    val imageUri by viewModel.imageUri.observeAsState(Uri.EMPTY)
    val imageFile by viewModel.imageFile.observeAsState(null)
    val uploadSuccess by viewModel.uploadSuccess.observeAsState()
    var btnEnabled by remember {
        mutableStateOf(false)
    }
    btnEnabled = nombre.trim().isNotEmpty()  && apellidos.trim().isNotEmpty() && fecNac.isNotEmpty() && interesSexual.trim().isNotEmpty()

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            imageFile?.let { file ->
                viewModel.updateImageUri(Uri.fromFile(file))
            }
        } else {
            viewModel.updateImageUri(Uri.EMPTY)
        }
    }


    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            val file = File.createTempFile("CAM_", ".jpg", contexto.cacheDir)
            viewModel.setImageFile(file)
            cameraLauncher.launch(FileProvider.getUriForFile(contexto, BuildConfig.APPLICATION_ID + ".provider", file))
        }
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val inputStream = contexto.contentResolver.openInputStream(it)
            val tempFile = File.createTempFile("GAL_", ".jpg", contexto.cacheDir)
            tempFile.outputStream().use { outputStream ->
                inputStream?.copyTo(outputStream)
            }
            viewModel.updateImageUri(it)
            viewModel.setImageFile(tempFile)
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 70.dp, horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item{
                Column(horizontalAlignment = Alignment.CenterHorizontally){
                    Text(text="Datos Personales", fontSize = 30.sp, lineHeight = 35.sp)
                    Spacer(modifier=Modifier.padding(bottom=20.dp))
                    HorizontalDivider()
                }
            }

            item{
                TextField(value =nombre, onValueChange = {viewModel.nombre.value = it}, placeholder = { Text(text="Nombre: ")})
                Spacer(modifier = Modifier.height(30.dp))
            }

            item{
                TextField(value =apellidos, onValueChange = {viewModel.apellidos.value = it}, placeholder = { Text(text="Apellidos: ")})
                Spacer(modifier = Modifier.height(10.dp))
            }

            item{
                DatePickerEdad(fecNac) { viewModel.fecNac.value = it }
                Spacer(modifier = Modifier.height(40.dp))
            }
            item{
                Text(text="Imagen de perfil: ", modifier = Modifier.padding(bottom=10.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Start, modifier = Modifier.fillMaxWidth()){
                    if (imageUri != Uri.EMPTY) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "Imagen perfil",
                            modifier = Modifier
                                .size(200.dp).border(1.dp,Color.Black)
                        )
                    } else {
                        Image(
                            painter = painterResource(R.drawable.ic_no_image),
                            contentDescription ="Imagen perfil",
                            modifier = Modifier
                                .size(200.dp).border(1.dp,Color.Black)
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        IconButton(onClick = {galleryLauncher.launch("image/*")}, modifier = Modifier
                            .padding(8.dp)
                            .size(48.dp)) {
                            Icon(
                                painter = painterResource(R.drawable.ic_gallery),
                                contentDescription = "Galeria",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        IconButton(onClick = {permissionLauncher.launch(android.Manifest.permission.CAMERA)}, modifier = Modifier
                            .padding(8.dp)
                            .size(48.dp)) {
                            Icon(
                                painter = painterResource(R.drawable.ic_camera),
                                contentDescription = "Cámara",
                                modifier = Modifier.size(24.dp),

                                )
                        }
                    }

                }

                Spacer(modifier = Modifier.height(45.dp))
            }

            item{
                Column(horizontalAlignment = Alignment.CenterHorizontally){
                    Text(text="Preferencias:", fontSize = 30.sp, lineHeight = 35.sp)
                    Spacer(modifier=Modifier.padding(bottom=20.dp))
                    HorizontalDivider()
                }
            }

            item{
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Relación seria:",
                        modifier = Modifier
                            .weight(0.5f)
                            .padding(end = 10.dp)
                    )
                    Switch(
                        checked = relacionSeria,
                        onCheckedChange = { viewModel.relacionSeria.value = it },
                        modifier = Modifier.weight(0.5f)
                    )
                }
                Spacer(modifier = Modifier.height(15.dp))
            }

            item{
                SliderPreference(
                    title = "deportes",
                    value = deportes,
                    onValueChange = { viewModel.deportes.value = it })
                Spacer(modifier = Modifier.height(25.dp))
            }

            item{
                SliderPreference(
                    title = "arte",
                    value = arte,
                    onValueChange = { viewModel.arte.value = it })
                Spacer(modifier = Modifier.height(25.dp))
            }

            item{
                SliderPreference(
                    title = "política",
                    value = politica,
                    onValueChange = { viewModel.politica.value = it })
                Spacer(modifier = Modifier.height(25.dp))
            }

            item{
                Row(verticalAlignment = Alignment.CenterVertically){
                    Text("¿Tienes hijos?: ", modifier = Modifier.padding(end=10.dp))
                    ComboBox (listOf("No","Si")) { viewModel.tieneHijos.value = it as Boolean }
                }
                Spacer(modifier = Modifier.height(15.dp))
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically){
                    Text("¿Quieres tener hijos?: ", modifier = Modifier.padding(end=10.dp))
                    ComboBox (listOf("No","Si")) { viewModel.quiereHijos.value = it as Boolean }
                }
                Spacer(modifier = Modifier.height(15.dp))
            }

            item{
                Row(verticalAlignment = Alignment.CenterVertically){
                    Text("Interesado en: ", modifier = Modifier.padding(end=10.dp))
                    ComboBox (listOf("","Mujer","Hombre","Ambos")) { viewModel.interesSexual.value = it as String }
                }

                Spacer(modifier = Modifier.height(35.dp))
            }
            
        }

        Row(
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(onClick = back) {
                Text("Atrás")
            }
            Button(onClick = next, enabled = btnEnabled) {
                Text("Finalizar")
            }
        }
    }



}

@Composable
fun SliderPreference(title: String, value: Int, onValueChange: (Int) -> Unit) {
    Column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Interés en $title:",
            modifier = Modifier.padding(bottom = 8.dp)
        )


        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {

            Slider(
                value = value.toFloat(),
                onValueChange = { onValueChange(it.toInt()) },
                valueRange = 0f..100f,
                steps = 99,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    activeTickColor = Color.Transparent,
                    inactiveTickColor = Color.Transparent
                )
            )

            Text(
                text = value.toString(),
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComboBox(opciones: List<String>,onOptionSelected: (Any) -> Unit) {

    var selectedOption by remember { mutableStateOf(opciones.first()) }
    var isExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = { isExpanded = it }
        ) {
            TextField(
                value = selectedOption,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { TrailingIcon(expanded = isExpanded) },
                modifier = Modifier.menuAnchor().width(150.dp).height(50.dp)

            )

            ExposedDropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { isExpanded = false }
            ) {
                opciones.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(text = option) },
                        onClick = {
                            selectedOption = option
                            when(selectedOption){
                                "No" ->onOptionSelected(false)
                                "Si" -> onOptionSelected(true)
                                "" -> ""
                                else -> onOptionSelected(selectedOption)
                            }

                            isExpanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
    }
}

@Composable
fun DatePickerEdad(fechaElegida: String, onDateSelected: (String) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    var fechaSelecc by remember { mutableStateOf(fechaElegida) }

    calendar.add(Calendar.YEAR, -18)
    val fecMinima = calendar.timeInMillis

    calendar.time = Date()

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            val fecha = "$dayOfMonth/${month + 1}/$year"
            fechaSelecc = fecha
            onDateSelected(fecha)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).apply {
        datePicker.maxDate = fecMinima
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (fechaSelecc.isEmpty()) "Fecha de nacimiento:" else "Fecha de nacimiento: $fechaSelecc",
            modifier = Modifier
                .clickable { datePickerDialog.show() }
                .padding(16.dp)
        )

        Button(onClick = { datePickerDialog.show() }) {
            Text("Elegir fecha")
        }
    }
}

@Composable
fun Terminos(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit
) {
    AlertDialog(
        icon = {
            Icon(painterResource(R.drawable.ic_documento), contentDescription = "Icono Términos")
        },
        title = {
            Text(text = "Términos y condiciones")
        },
        text = {
            Text(text = "El usuario debe de ser mayor de edad.\nVamos a guardar tus datos")
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text("Cerrar")
            }
        }
    )
}


