package com.example.proyectocompose.administrador.quedadas

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.TextField
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
import com.example.proyectocompose.utils.Rutas
import com.example.proyectocompose.administrador.quedadas.componentes.SeleccionarFecha
import com.example.proyectocompose.administrador.quedadas.componentes.SeleccionarUbicacion
import com.example.proyectocompose.administrador.quedadas.viewModels.MapsAdminQuedadaViewModel
import com.example.proyectocompose.administrador.quedadas.viewModels.QuedadasAdminViewModel
import com.example.proyectocompose.common.BodyText
import com.example.proyectocompose.model.UserQuedada
import com.example.proyectocompose.utils.toCustomString


@Composable
fun EditarQuedada(
    navController: NavController,
    viewModel: QuedadasAdminViewModel,
    mapsViewModel: MapsAdminQuedadaViewModel
) {

    Scaffold(
        topBar = {
            TopBarEditarQuedada(navController = navController, mapsViewModel = mapsViewModel, quedadaViewModel = viewModel)
        }) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            EditarQuedadaBody(viewModel, mapsViewModel,navController)
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarEditarQuedada(navController: NavController, mapsViewModel: MapsAdminQuedadaViewModel, quedadaViewModel: QuedadasAdminViewModel) {
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
                onClick = {

                    mapsViewModel.removeMarker()
                    navController.popBackStack(Rutas.quedadasAdmin, inclusive = false)
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
fun EditarQuedadaBody(
    viewModel: QuedadasAdminViewModel,
    mapsViewModel: MapsAdminQuedadaViewModel,
    navController: NavController
) {
    val quedadaSelecc by viewModel.quedadaSelecc.collectAsState()
    val showSeleccUbicacion = remember { mutableStateOf(false) }
    val quedadaInicial = remember { mutableStateOf(quedadaSelecc.copy()) }
    val usuariosObtenidos by viewModel.usuariosObtenidos.collectAsState()
    val usuariosDisponibles = remember { mutableStateListOf<UserQuedada>() }
    val usuariosElegidos = remember { mutableStateListOf<UserQuedada>() }
    val quedadaModificada by viewModel.quedadaModificada.collectAsState()

    val cantUsuariosDisponiblesInicial = remember { mutableStateOf(0) }
    val haModificado = remember {
        derivedStateOf {
            (quedadaSelecc != quedadaInicial.value) ||
                    (usuariosDisponibles.size != cantUsuariosDisponiblesInicial.value)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.getUsuarios()
    }
    LaunchedEffect(usuariosObtenidos) {
        if (usuariosObtenidos && usuariosDisponibles.isEmpty() && usuariosElegidos.isEmpty()) {
            usuariosDisponibles.addAll(viewModel.usuariosDisponibles.value)
            usuariosElegidos.addAll(viewModel.usuariosElegidos.value)
            cantUsuariosDisponiblesInicial.value = usuariosDisponibles.size
        }
    }
    LaunchedEffect(quedadaModificada) {
        if (quedadaModificada) {
            viewModel.restart()
            navController.navigate(Rutas.quedadasAdmin)
        }
    }

    val tabs = listOf("Datos", "Usuarios")
    var selectedTabIndex by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = {

                        viewModel.updateQuedada(quedadaInicial.value.nombre,usuariosElegidos.toList())
                        mapsViewModel.removeMarker()
                              },
                    enabled = haModificado.value,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    BodyText(text = "Guardar cambios")
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
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TextField(
                            value = quedadaSelecc.nombre,
                            onValueChange = { viewModel.setQuedadaSeleccNombre(it) },
                            placeholder = { Text("Nombre:") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(30.dp))
                        SeleccionarFecha(quedadaSelecc.fecha) { viewModel.setQuedadaSeleccFecha(it) }
                        Spacer(modifier = Modifier.height(30.dp))
                        if (quedadaSelecc.ubicacion.isNotEmpty()) {
                            BodyText(text = "Localización actual: ${quedadaSelecc.ubicacion}")
                            Spacer(modifier = Modifier.height(15.dp))
                        }
                        Button(
                            onClick = { showSeleccUbicacion.value = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Seleccionar ubicación")
                        }
                        if (showSeleccUbicacion.value) {
                            SeleccionarUbicacion(
                                viewModel = mapsViewModel,
                                quedadaViewModel = viewModel
                            ) { loc ->
                                showSeleccUbicacion.value = false
                                viewModel.setQuedadaSeleccUbicacion(loc)
                            }
                        }
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
                1 -> {
                    Row(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Usuarios disponibles:",
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            LazyColumn(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(usuariosDisponibles) { usuario ->
                                    ItemUsuario(
                                        user = usuario,
                                        isDisponible = true,
                                        onMoverUsuario = { user, disponible ->
                                            if (disponible) {
                                                usuariosDisponibles.remove(user)
                                                usuariosElegidos.add(user)
                                            } else {
                                                usuariosElegidos.remove(user)
                                                usuariosDisponibles.add(user)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Usuarios seleccionados:",
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            LazyColumn(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(usuariosElegidos) { usuario ->
                                    ItemUsuario(
                                        user = usuario,
                                        isDisponible = false,
                                        onMoverUsuario = { user, disponible ->
                                            if (disponible) {
                                                usuariosDisponibles.remove(user)
                                                usuariosElegidos.add(user)
                                            } else {
                                                usuariosElegidos.remove(user)
                                                usuariosDisponibles.add(user)
                                            }
                                        }
                                    )
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
    isDisponible: Boolean,
    onMoverUsuario: (UserQuedada, Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .border(1.dp, color = Color.Gray, RectangleShape)
            .clickable {
                onMoverUsuario(user, isDisponible)
            }
    ) {
        BodyText(text = "Nombre: ${user.nombre}")
        Spacer(modifier=Modifier.height(10.dp))
        BodyText(text = "Correo: ${user.correo}")
    }
}