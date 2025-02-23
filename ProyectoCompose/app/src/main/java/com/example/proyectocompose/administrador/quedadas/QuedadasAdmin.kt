package com.example.proyectocompose.administrador.quedadas

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.proyectocompose.R
import com.example.proyectocompose.utils.Rutas
import com.example.proyectocompose.administrador.quedadas.componentes.SeleccionarFecha
import com.example.proyectocompose.administrador.quedadas.componentes.SeleccionarUbicacion
import com.example.proyectocompose.administrador.quedadas.viewModels.MapsAdminQuedadaViewModel
import com.example.proyectocompose.administrador.quedadas.viewModels.QuedadasAdminViewModel
import com.example.proyectocompose.common.BodyText
import com.example.proyectocompose.common.Subtitle
import com.example.proyectocompose.common.TitleText
import com.example.proyectocompose.model.Quedada
import com.example.proyectocompose.utils.toCustomString
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun QuedadasAdmin(navController: NavController, viewModel: QuedadasAdminViewModel) {

    Scaffold(
        topBar = {
            TopBarQuedadasAdmin(navController = navController)
        }) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            BodyQuedadasBody(viewModel, navController)
        }
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarQuedadasAdmin(navController: NavController) {

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
                Text("Quedadas")
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
fun BodyQuedadasBody(viewModel: QuedadasAdminViewModel, navController: NavController) {

    val context = LocalContext.current
    val quedadas = viewModel.quedadas
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.restart()
    }

    LaunchedEffect(quedadas) {
        viewModel.getQuedadas()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .blur(if (isLoading) 8.dp else 0.dp),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {

            items(quedadas) { quedada ->
                ItemQuedada(quedada, navController, viewModel)
            }
        }

        FloatingActionButton(
            onClick = {

                navController.navigate(Rutas.addQuedada)
                      },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_add),
                contentDescription = "Boton añadir"
            )
        }
        if (isLoading) {
            CircularProgressIndicator()
        }

    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItemQuedada(quedada: Quedada, navController: NavController, viewModel: QuedadasAdminViewModel) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val contexto = LocalContext.current
    val formatter = DateTimeFormatter.ofPattern("d/M/yyyy")
    val quedadaFecha = LocalDate.parse(quedada.fecha, formatter)
    val currentDate = LocalDate.now()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .border(1.dp, color = Color.Gray, RectangleShape)
            .combinedClickable(
                onClick = {

                    if(quedadaFecha.isBefore(currentDate) || quedadaFecha.isEqual(currentDate)){

                        Toast.makeText(contexto,"La quedada ya ha cerrado, no se puede modificar",Toast.LENGTH_SHORT).show()

                    }else{
                        viewModel.setQuedadaSelecc(quedada)
                        navController.navigate(Rutas.editarQuedada)
                    }


                },
                onLongClick = {
                    showDeleteDialog = true
                }
            )
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Subtitle(text = quedada.nombre)
            Spacer(modifier = Modifier.height(4.dp))
            BodyText(text = "Número de asistentes: ${quedada.correosUsr.size}")
            Spacer(modifier = Modifier.height(4.dp))


            BodyText(text = "Ubicación: ${quedada.ubicacion}")
            Spacer(modifier = Modifier.height(4.dp))
            BodyText(text = "Fecha: ${quedada.fecha}")

        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Quedada") },
            text = { Text("¿Estás seguro de que quieres eliminar esta quedada?") },
            confirmButton = {
                Button(onClick = {
                    viewModel.borrarQuedada(quedada)
                    showDeleteDialog = false
                }) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}


//todo(isloading)
@Composable
fun AniadirQuedada(navController: NavController, viewModel: QuedadasAdminViewModel) {

    val showFechaDialog = remember { mutableStateOf(false) }
    val showSeleccUbicacion = remember { mutableStateOf(false) }
    val fecha = remember { mutableStateOf("") }
    val mapsViewModel = remember { MapsAdminQuedadaViewModel() }
    val localicacion by viewModel.locNuevaQuedada.collectAsState()
    val isBtnActivo = remember { mutableStateOf(false) }
    val nombre = remember { mutableStateOf("") }
    val quedadaCreada by viewModel.quedadaCreada.collectAsState()
    val context = LocalContext.current



    Scaffold(
        topBar = {
            TopBarQuedadasAdmin(navController = navController)
        }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding), horizontalAlignment = Alignment.CenterHorizontally
        ) {

            TitleText(text = "Nueva Quedada")

            Spacer(modifier = Modifier.height(30.dp))

            TextField(
                value = nombre.value,
                onValueChange = { nombre.value = it },
                placeholder = { Text("Nombre:") }
            )

            Spacer(modifier = Modifier.height(30.dp))

            SeleccionarFecha(fecha.value) { fecha.value = it }

            Spacer(modifier = Modifier.height(30.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                if (localicacion != null) {
                    BodyText(text = "Localizacion actual: ${localicacion!!.toCustomString()}")
                    Spacer(modifier = Modifier.height(15.dp))

                }
            }

            Button(onClick = { showSeleccUbicacion.value = true }) {
                Text(text = "Seleccionar ubicacion")
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.Center) {
                Button(onClick = {
                    viewModel.crearQuedada(
                        fecha = fecha.value,
                        nombre = nombre.value,
                        context = context
                    )
                }, enabled = isBtnActivo.value) {
                    BodyText(text = "Crear")
                }
            }


        }



        if (showSeleccUbicacion.value) {
            SeleccionarUbicacion(
                viewModel = mapsViewModel,
                quedadaViewModel = viewModel
            ) { showSeleccUbicacion.value = false }
        }

        if (localicacion != null && nombre.value.trim().length > 5 && fecha.value.isNotEmpty()) {
            isBtnActivo.value = true
        } else {
            isBtnActivo.value = false

        }

        if (quedadaCreada) {
            navController.navigate(Rutas.quedadasAdmin)
            viewModel.setQuedadaCreada(false)
        }
    }


}


    
