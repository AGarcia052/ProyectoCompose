package com.example.proyectocompose.login

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.proyectocompose.R
import com.example.proyectocompose.common.BodyText
import com.example.proyectocompose.common.ClickableText
import com.example.proyectocompose.common.Subtitle
import com.example.proyectocompose.common.TFBasic
import java.util.Calendar
import java.util.Date

@Composable
fun Formulario(navController: NavController, loginViewModel: LoginViewModel) {

    val viewModel = remember { FormularioViewModel() }
    val email by loginViewModel.currentEmail.collectAsState()
    var page by remember { mutableIntStateOf(1) }
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        when (page) {
            1 -> Info(viewModel) { page = 2 }
            2 -> Datos(viewModel, back = { page = 1 }) { page = 3 }
            3 -> Preferencias(viewModel, back = { page = 2 })
            {

            }
        }
    }

}

@Composable
fun Info(viewModel: FormularioViewModel, next: () -> Unit) {

    val checked by viewModel.terms.collectAsState()
    val openAlertDialog = remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Subtitle(text = "¡Bienvenido a la Aplicación!")
            Spacer(modifier = Modifier.height(10.dp))

            BodyText(
                text = "A continuación, tendrás que completar la creación de tu cuenta" +
                        " y rellenar un formulario con tus intereses para recomendarte usuarios con gustos parecidos a los tuyos"
            )
            Spacer(modifier = Modifier.height(10.dp))

            BodyText(text = "Para continuar, acepte los términos y condiciones de la aplicación.")
            Spacer(modifier = Modifier.height(20.dp))

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
fun Datos(viewModel: FormularioViewModel, back: () -> Unit, next: () -> Unit) {

    val nombre by viewModel.nombre.collectAsState()
    val apellidos by viewModel.apellidos.collectAsState()
    val fecNac by viewModel.fecNac.collectAsState()

    // TODO(AÑADIR IMAGEN PERFIL)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 70.dp, horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        ) {

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            TFBasic(
                value = nombre,
                label = "Nombre: "
            ) {
                viewModel.nombre.value = it
            }
            Spacer(modifier = Modifier.height(30.dp))

            TFBasic(
                value = apellidos,
                label = "Apellidos: "
            ) {
                viewModel.apellidos.value = it
            }
            Spacer(modifier = Modifier.height(10.dp))

            DatePickerEdad { viewModel.fecNac.value = it }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = back) {
                Text("Atrás")
            }
            Button(onClick = next) {
                Text("Siguiente")
            }
        }
    }
}

@Composable
fun Preferencias(viewModel: FormularioViewModel, back: () -> Unit, next: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = back) {
                Text("Atrás")
            }
            Button(onClick = next) {
                Text("Siguiente")
            }
        }
    }
}

@Composable
fun DatePickerEdad(onDateSelected: (String) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    var fechaSelecc by remember { mutableStateOf("") }

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