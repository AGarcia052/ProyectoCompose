package com.example.proyectocompose.usuario.buscarAfines

import android.widget.RadioGroup
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.proyectocompose.R
import com.example.proyectocompose.common.BodyText
import com.example.proyectocompose.model.User
import com.example.proyectocompose.utils.Rutas
import com.example.proyectocompose.usuario.dashboard.DashboardViewModel
import com.example.proyectocompose.utils.calcularEdad

@Composable
fun UsuariosAfines(viewModel: UsuariosAfinesViewModel, navController: NavController, dashboardViewModel: DashboardViewModel){

    val candidatos by viewModel.candidatos.collectAsState()
    val candidato = if(candidatos.isNotEmpty())candidatos.first()else User()
    val usuarioCargado by viewModel.usuarioCargado.collectAsState()
    val mostrarDialog = remember { mutableStateOf(false) }
    val btnActivos = candidatos.isNotEmpty()

    if(usuarioCargado){
        viewModel.filtrarCandidatos()
        viewModel.setUsuarioCargado(false)
    }

    LaunchedEffect(Unit) {
        viewModel.setUsuario(dashboardViewModel.usuario.value)
    }
//    LaunchedEffect(!hayCandidatos) {
//        viewModel.filtrarCandidatos()
//        viewModel.setHayCandidatos(true)
//    }


    Scaffold(
        topBar = {TopAppBarUsrAfines(navController)}, bottomBar = {BottomAppBar {
            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()){
                Button(onClick = {
                    viewModel.like(candidato.correo)
                    dashboardViewModel.setUsuarioLike(candidato.correo)
                                 }, enabled = btnActivos) { Text(text = "Like") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {viewModel.descartar(candidato.correo)},enabled = btnActivos){ Text(text = "Descartar") }
            }
        }})
        { innerPadding ->

        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {

            if(candidatos.isNotEmpty()){
                ItemCandidato(candidato,viewModel)
            }else{
                Column(modifier= Modifier.fillMaxSize(),horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center){
                    Text("No quedan candidatos afines")
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center){
                        Button(onClick = { navController.navigate(Rutas.dashboard) }) { Text(text="Volver") }
                        Spacer(modifier=Modifier.width(8.dp))
                        Button(onClick = { mostrarDialog.value = true }) { Text(text = "Mostrar no afines") }
                    }
                }

            }
            if(mostrarDialog.value){
                MostrarTodosDialog(onDismissRequest = { mostrarDialog.value = false }) {
                    viewModel.filtrarCandidatos(filtrar=false)
                    mostrarDialog.value = false
                }

            }

        }

    }






}

@Composable
fun MostrarTodosDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit
) {
    AlertDialog(
        icon = {
            Icon(Icons.Filled.Refresh, contentDescription = "Icono Términos")
        },
        title = {
            Text(text = "Mostrar todos")
        },
        text = {
            Text(text = "¿Quieres mostrar todos los usuarios?\nEsto incluye los candidatos rechazados y los candidatos no afines")
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
                Text("Mostrar")
            }
        },
        dismissButton = {TextButton(onClick = { onDismissRequest() }) {
            Text(text="Cancelar")}
        }
    )
}



@Composable
fun ItemCandidato(candidato: User,viewModel: UsuariosAfinesViewModel) {
    val expanded = remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val cambiarScroll = remember { mutableStateOf(false)}
    val profileImage by viewModel.profileImg.collectAsState()
    val imagenesCandidato by viewModel.candidatoImgs.collectAsState()
    val imgSeleccionada = remember { mutableStateOf(0) }
    val imgLoding by viewModel.imgLoading.collectAsState()
    LaunchedEffect(cambiarScroll.value) {
        if (cambiarScroll.value) {
            scrollState.animateScrollTo(scrollState.maxValue)
            cambiarScroll.value = false
        }
    }
    LaunchedEffect(candidato.correo) {
        imgSeleccionada.value = 0
        viewModel.obtenerPerfilImg()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
        ,
        horizontalAlignment = Alignment.CenterHorizontally

    ) {
        Row(modifier = Modifier
            .height(200.dp)
            .fillMaxWidth()
            , horizontalArrangement = Arrangement.Center
        ){
            Box(
                modifier = Modifier
                    .height(200.dp)
                    .width(200.dp)
                    .border(1.dp, Color.Gray, shape = MaterialTheme.shapes.medium)
                , contentAlignment = Alignment.Center
            ) {
                when {
                    imgLoding ->
                        CircularProgressIndicator(Modifier.align(Alignment.Center))

                    imgSeleccionada.value == 0 ->
                        AsyncImage(
                            model = profileImage,
                            contentDescription = "Imagen perfil",
                            modifier = Modifier.fillMaxSize()
                        )

                    else -> {
                        val imagenIndice = imgSeleccionada.value - 1
                        if (imagenIndice < imagenesCandidato.size) {
                            AsyncImage(
                                model = imagenesCandidato[imagenIndice],
                                contentDescription = "Imagen usuario",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                RadioButton(
                    selected = imgSeleccionada.value == 0,
                    onClick = { imgSeleccionada.value = 0 }
                )

                imagenesCandidato.forEachIndexed { index, _ ->
                    RadioButton(
                        selected = imgSeleccionada.value == index + 1,
                        onClick = { imgSeleccionada.value = index + 1 }
                    )
                }
            }
        }



        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "${candidato.nombre} ${candidato.apellidos}",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))
        BodyText(text = "${calcularEdad(candidato.fecNac)} Años")
        Spacer(modifier = Modifier.height(10.dp))
        BodyText(text = candidato.formulario.sexo)
        Spacer(modifier = Modifier.height(10.dp))
        BodyText(text = "Descripción: AQUÍ DESC")
        Spacer(modifier = Modifier.height(10.dp))

        HorizontalDivider(modifier = Modifier.padding(horizontal = 30.dp), color = Color.Black)
        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .animateContentSize(
                    finishedListener = { _: IntSize, _: IntSize ->
                        if(expanded.value){
                            cambiarScroll.value = true
                        }
                    }
                )
            ,
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded.value = !expanded.value }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Más información")
                    Icon(
                        imageVector = if (expanded.value) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = if (expanded.value) "Colapsar" else "Expandir"
                    )
                }
                if (expanded.value) {

                    BodyText(text = "Quiere hijos: ${if (candidato.formulario.quiereHijos) "Sí" else "No"}")
                    Spacer(modifier = Modifier.height(10.dp))
                    BodyText(text = "Tiene hijos: ${if (candidato.formulario.tieneHijos) "Sí" else "No"}")
                    Spacer(modifier = Modifier.height(10.dp))
                    BodyText(text = "Interés en: ${candidato.formulario.interesSexual}")
                    Spacer(modifier = Modifier.height(10.dp))
                    BodyText(text = "Interés en política: ${interesIntAString(candidato.formulario.politica)}")
                    Spacer(modifier = Modifier.height(10.dp))
                    BodyText(text = "Interés en arte: ${interesIntAString(candidato.formulario.arte)}")
                    Spacer(modifier = Modifier.height(10.dp))
                    BodyText(text = "Interés en deporte: ${interesIntAString(candidato.formulario.deportes)}")
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}


private fun interesIntAString(valor: Int): String{
    return when{
        valor > 75 -> "ALTO"
        valor > 40 -> "MEDIO"
        else -> "BAJO"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarUsrAfines(navController: NavController) {

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
                Text("Buscar afines")
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

