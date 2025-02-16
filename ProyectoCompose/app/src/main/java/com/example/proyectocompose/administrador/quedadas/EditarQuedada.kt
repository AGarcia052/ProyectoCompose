package com.example.proyectocompose.administrador.quedadas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.proyectocompose.Rutas
import com.example.proyectocompose.administrador.quedadas.componentes.SeleccionarFecha
import com.example.proyectocompose.administrador.quedadas.componentes.SeleccionarUbicacion
import com.example.proyectocompose.administrador.quedadas.viewModels.MapsAdminQuedadaViewModel
import com.example.proyectocompose.administrador.quedadas.viewModels.QuedadasAdminViewModel
import com.example.proyectocompose.common.BodyText
import com.example.proyectocompose.model.Quedada
import com.example.proyectocompose.utils.toCustomString


@Composable
fun EditarQuedada(navController: NavController, viewModel: QuedadasAdminViewModel, mapsViewModel: MapsAdminQuedadaViewModel){

    Scaffold(
        topBar = {
            TopBarEditarQuedada(navController = navController)
        }) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            EditarQuedadaBody(viewModel,mapsViewModel)
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarEditarQuedada(navController: NavController){
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
                Text("Editar Quedada")
            }
        },
        navigationIcon = {
            IconButton(
                onClick = { navController.popBackStack(Rutas.quedadasAdmin, inclusive = false) }
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
fun EditarQuedadaBody(viewModel: QuedadasAdminViewModel, mapsViewModel: MapsAdminQuedadaViewModel){

    val quedadaSelecc by viewModel.quedadaSelecc.collectAsState()
    val showSeleccUbicacion = remember { mutableStateOf(false) }
    val quedadaInicial = remember { mutableStateOf(quedadaSelecc.copy()) }
    val haModificado = remember {
        derivedStateOf {
            quedadaSelecc != quedadaInicial.value
        }
    }
    Box(){
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            TextField(
                value = quedadaSelecc.nombre,
                onValueChange = { viewModel.setQuedadaSeleccNombre(it) },
                placeholder = { Text("Nombre:") }
            )

            Spacer(modifier = Modifier.height(30.dp))

            SeleccionarFecha(quedadaSelecc.fecha) { viewModel.setQuedadaSeleccFecha(it) }

            Spacer(modifier = Modifier.height(30.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                if (quedadaSelecc.ubicacion.isNotEmpty()) {
                    BodyText(text = "Localizacion actual: ${quedadaSelecc.ubicacion}")
                    Spacer(modifier = Modifier.height(15.dp))

                }
            }

            Button(onClick = { showSeleccUbicacion.value = true }) {
                Text(text = "Seleccionar ubicacion")
            }

            if (showSeleccUbicacion.value) {
                SeleccionarUbicacion(
                    viewModel = mapsViewModel,
                    quedadaViewModel = viewModel
                ) { showSeleccUbicacion.value = false }
            }
        }
    }



}