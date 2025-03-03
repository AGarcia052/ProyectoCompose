package com.example.proyectocompose.usuario.quedadas

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
import com.example.proyectocompose.administrador.quedadas.BodyQuedadasBody
import com.example.proyectocompose.administrador.quedadas.componentes.SeleccionarUbicacion
import com.example.proyectocompose.administrador.quedadas.viewModels.MapsAdminQuedadaViewModel
import com.example.proyectocompose.administrador.quedadas.viewModels.QuedadasAdminViewModel
import com.example.proyectocompose.common.BodyText
import com.example.proyectocompose.common.Subtitle
import com.example.proyectocompose.model.Quedada
import com.example.proyectocompose.utils.Rutas
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun QuedadasUsuario(navController: NavController, viewModel: QuedadasAdminViewModel, mapsViewModel: MapsAdminQuedadaViewModel) {

    Scaffold(
        topBar = {
            TopBarQuedadasUsuario(navController = navController)
        }) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            BodyQuedadasUsuario(viewModel, navController,mapsViewModel)
        }
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarQuedadasUsuario(navController: NavController) {

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
fun BodyQuedadasUsuario(viewModel: QuedadasAdminViewModel, navController: NavController, mapsViewModel: MapsAdminQuedadaViewModel) {
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
                ItemQuedadaUsuario(quedada, navController, viewModel, mapsViewModel)
            }
        }


        if (isLoading) {
            CircularProgressIndicator()
        }

    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItemQuedadaUsuario(quedada: Quedada, navController: NavController, viewModel: QuedadasAdminViewModel, mapsAdminQuedadaViewModel: MapsAdminQuedadaViewModel) {
    val showMapa = remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .border(1.dp, color = Color.Gray, RectangleShape)
            .combinedClickable(
                onClick = {
                    viewModel.setQuedadaSelecc(quedada)
                    navController.navigate(Rutas.datosQuedada)

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

            Row(modifier = Modifier.fillMaxWidth()){
                BodyText(text = "Ubicación: ")
                Button(onClick = {
                    viewModel.setQuedadaSelecc(quedada)
                    showMapa.value = true
                }){
                    Text(text="Ver ubicación")
                }

            }
            Spacer(modifier = Modifier.height(4.dp))
            BodyText(text = "Fecha: ${quedada.fecha}")

        }
    }


    if(showMapa.value){
        SeleccionarUbicacion(isSeleccionar = false, quedadaViewModel = viewModel, viewModel = mapsAdminQuedadaViewModel) {
            showMapa.value = false
        }
    }

}