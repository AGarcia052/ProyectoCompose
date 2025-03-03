package com.example.proyectocompose.usuario.quedadas

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.proyectocompose.administrador.quedadas.componentes.SeleccionarUbicacion
import com.example.proyectocompose.administrador.quedadas.viewModels.MapsAdminQuedadaViewModel
import com.example.proyectocompose.administrador.quedadas.viewModels.QuedadasAdminViewModel
import com.example.proyectocompose.common.BodyText
import com.example.proyectocompose.login.LoginViewModel
import com.example.proyectocompose.model.Llegada
import com.example.proyectocompose.model.User
import com.example.proyectocompose.model.UserQuedada
import com.example.proyectocompose.usuario.dashboard.DashboardViewModel
import com.example.proyectocompose.utils.Rutas
import java.time.LocalDate
import java.time.format.DateTimeFormatter


@Composable
fun DatosQuedada(
    navController: NavController,
    viewModel: QuedadasAdminViewModel,
    loginViewModel: LoginViewModel,
    mapsViewModel: MapsAdminQuedadaViewModel,
    dashboardViewModel: DashboardViewModel
) {

    Scaffold(
        topBar = {
            TopBarDatosQuedada(navController = navController, quedadaViewModel = viewModel)
        }) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            DatosQuedadaBody(viewModel,navController, loginViewModel, mapsViewModel, dashboardViewModel)
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarDatosQuedada(navController: NavController, quedadaViewModel: QuedadasAdminViewModel) {
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
                Text(quedadaViewModel.getQuedadaSelecc().nombre)
            }
        },
        navigationIcon = {
            IconButton(
                onClick = {
                    quedadaViewModel.restart()
                    navController.popBackStack(Rutas.quedadasUsuario, inclusive = false)
                }
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
fun DatosQuedadaBody(
    viewModel: QuedadasAdminViewModel,
    navController: NavController,
    loginViewModel: LoginViewModel,
    mapsViewModel: MapsAdminQuedadaViewModel,
    dashboardViewModel: DashboardViewModel
) {
    val showSeleccUbicacion = remember { mutableStateOf(false) }
    val quedadaSelecc by viewModel.quedadaSelecc.collectAsState()
    val usuariosObtenidos by viewModel.usuariosObtenidos.collectAsState()
    val quedadaModificada by viewModel.quedadaModificada.collectAsState()
    val asistentes = remember { mutableStateListOf<UserQuedada>() }
    val formatter = DateTimeFormatter.ofPattern("d/M/yyyy")
    var quedadaFecha by remember { mutableStateOf(LocalDate.parse(quedadaSelecc.fecha, formatter))}
    val currentDate = LocalDate.now()


    LaunchedEffect(Unit) {
        viewModel.getUsuarios()
        quedadaFecha = LocalDate.parse(quedadaSelecc.fecha, formatter)
    }
    LaunchedEffect(usuariosObtenidos) {
        if (usuariosObtenidos && asistentes.isEmpty()) {
            asistentes.addAll(viewModel.usuariosElegidos.value)
        }
    }
    LaunchedEffect(quedadaModificada) {
        if (quedadaModificada) {
            viewModel.restart()
            navController.popBackStack(Rutas.quedadasUsuario, inclusive = false)
        }
    }


    val tabs = listOf("Asistentes", "Llegadas")
    var selectedTabIndex by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                if (currentDate.isBefore(quedadaFecha.minusDays(1)) &&
                    !quedadaSelecc.correosUsr.contains(loginViewModel.getCurrentEmail())) {
                    Button(
                        onClick = {
                            viewModel.inscribirse(loginViewModel.getCurrentEmail())
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        BodyText(text = "Inscribirse")
                    }
                }else if (currentDate.isBefore(quedadaFecha.minusDays(1)) &&
                    quedadaSelecc.correosUsr.contains(loginViewModel.getCurrentEmail())){
                    Button(
                        onClick = {
                            viewModel.desinscribirse(loginViewModel.getCurrentEmail())
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        BodyText(text = "Desinscribirse")
                    }
                }else if (currentDate.equals(quedadaFecha) &&
                    quedadaSelecc.correosUsr.contains(loginViewModel.getCurrentEmail())
                    && quedadaSelecc.llegadas.none { it.correo == loginViewModel.getCurrentEmail() }){
                    Button(
                        onClick = {
                            showSeleccUbicacion.value = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        BodyText(text = "Anunciar llegada")
                    }
                    if (showSeleccUbicacion.value) {
                        AnunciarLlegada(
                            viewModel = mapsViewModel,
                            quedadaViewModel = viewModel,
                            navController = navController,
                            dashboardViewModel = dashboardViewModel,
                            onDismissRequest = {showSeleccUbicacion.value = false}
                        )
                    }
                }

            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(text = title) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            when (selectedTabIndex) {
                0 -> {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Usuarios inscritos:",
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(asistentes) { usuario ->
                                ItemUsuario(usuario)
                            }
                        }
                    }
                }
                1 -> {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Usuarios que ya han llegado:",
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        if (quedadaSelecc.llegadas.isEmpty()) {
                            Text(
                                text = "Todavía no ha llegado nadie",
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(quedadaSelecc.llegadas) { usuario ->
                                    ItemLlegada(usuario)
                                }
                            }
                        }
                    }
                }

            }
        }
    }
}


@Composable
fun ItemUsuario(
    user: UserQuedada,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .border(1.dp, color = Color.Gray, RectangleShape)
    ) {
        BodyText(text = "Nombre: ${user.nombre}")
        Spacer(modifier= Modifier.height(10.dp))
        BodyText(text = "Correo: ${user.correo}")
    }
}

@Composable
fun ItemLlegada(
    llegada: Llegada,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .border(1.dp, color = Color.Gray, RectangleShape)
    ) {
        BodyText(text = "Nombre: ${llegada.nombre}")
        Spacer(modifier= Modifier.height(10.dp))
        BodyText(text = "Correo: ${llegada.correo}")
        Spacer(modifier= Modifier.height(10.dp))
        BodyText(text = "Hora: ${llegada.horaLlegada}")
        Spacer(modifier= Modifier.height(10.dp))
        BodyText(text = "Ubicacion: ${llegada.ubicacion}")
    }
}