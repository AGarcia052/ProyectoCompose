package com.example.proyectocompose.usuario.dashboard

import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.proyectocompose.MainActivity
import com.example.proyectocompose.R
import com.example.proyectocompose.utils.Rutas
import com.example.proyectocompose.login.LoginViewModel
import com.example.proyectocompose.utils.Constantes

@Composable
fun Dashboard(navController: NavController,loginVM: LoginViewModel, dashboardVM: DashboardViewModel){



    Scaffold(
        topBar = {
            TopBarDashboard(navController, loginVM, dashboardVM)
        }) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            BodyDashboard(navController, dashboardVM)
        }
    }



}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarDashboard(navController: NavController, loginVM: LoginViewModel, dashboardVM: DashboardViewModel){
    val contexto = LocalContext.current
    val isLoading by dashboardVM.isLoading.collectAsState()
    var mostrarMenuPuntos by remember { mutableStateOf(false) }
    val usuario by dashboardVM.usuario.collectAsState()
    val opciones = listOf("Perfil", "Opciones de administrador", "Cerrar Sesión")
    LaunchedEffect(usuario) {
        if (usuario.rol.isEmpty()){
            dashboardVM.cargarUsuario(loginVM.getCurrentEmail())
        }
    }

    TopAppBar(
        colors = topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = {
            Text("Dashboard")
        },
        actions = {
            IconButton(
                onClick = {
                    dashboardVM.cargarUsuariosConectados()
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Localized description"
                )
            }
            if (isLoading){
                CircularProgressIndicator()
            }else{
                IconButton(onClick = { mostrarMenuPuntos = true }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "Localized description"
                    )
                }
            }

            DesplegarMenuPuntos(
                expanded = mostrarMenuPuntos,
                opciones = opciones,
                onItemClick = { opcion ->
                    when (opcion) {
                        "Perfil" -> navController.navigate(Rutas.perfil){
                            popUpTo(Rutas.dashboard) { inclusive = false }
                        }
                        "Opciones de administrador" -> navController.navigate(Rutas.adminPrincipal){
                            popUpTo(Rutas.dashboard) { inclusive = false }
                        }
                        "Cerrar Sesión" -> {
                            loginVM.signOut(contexto)
                            navController.popBackStack(Rutas.login, inclusive = false)
                        }
                    }
                },
                onDismiss = { mostrarMenuPuntos = false },
                esAdministrador = { if (usuario!!.rol == "Administrador") true else false }
            )
        }
    )
}

@Composable
fun DesplegarMenuPuntos(expanded: Boolean, opciones: List<String>, onItemClick: (String) -> Unit,  onDismiss: () -> Unit, esAdministrador: () -> Boolean) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        opciones.forEach { option ->
            if (option != "Opciones de administrador" || esAdministrador()){
                DropdownMenuItem(
                    onClick = {
                        onItemClick(option)
                        onDismiss()
                    },
                    text = { Text(text = option) },
                    leadingIcon = {
                        if (option == "Perfil"){
                            Icon(
                                painter = painterResource(id = R.drawable.ic_cuenta),
                                contentDescription = null,
                            )
                        }else if(option == "Opciones de administrador"){
                            Icon(
                                painter = painterResource(id = R.drawable.ic_admin),
                                contentDescription = null,
                            )
                        }
                        else if (option == "Cerrar Sesión"){
                            Icon(
                                painter = painterResource(id = R.drawable.ic_cerrar_sesion),
                                contentDescription = null,
                            )
                        }

                    }
                )
            }

        }
    }
}

@Composable
fun BodyDashboard(navController: NavController, dashboardVM: DashboardViewModel){
    val isLoading by dashboardVM.isLoading.collectAsState()
    val numUsuariosConectados by dashboardVM.numUsuariosConectados.collectAsState()
    val msgObtenidos by dashboardVM.msgObtenidos.collectAsState()
    val contexto = LocalContext.current
    val notificacionEnviada by dashboardVM.notificacionEnviada.collectAsState()
    LaunchedEffect(Unit) {
        Log.i(Constantes.TAG,"Obteniendo mensajes no leídos...")

        dashboardVM.obtenerMensajesNoLeidos()
    }

    LaunchedEffect(msgObtenidos) {
        if (msgObtenidos && !notificacionEnviada) {
            Log.i(Constantes.TAG, "Enviando notificación...")
            dashboardVM.sendNotification(contexto)
        }
    }

    LaunchedEffect(notificacionEnviada) {
        dashboardVM.setMsgObtenidos(false)
    }

    Column (modifier = Modifier.fillMaxWidth().padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center){
        if (isLoading){
            Text(text = "Cargando usuarios conectados...")
        }else{
            Text(text = "Usuarios conectados: "+numUsuariosConectados)
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = { navController.navigate(Rutas.usuariosAfines){
                popUpTo(Rutas.dashboard) { inclusive = false }
            } },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .animateContentSize()
        ) {
            Text("Usuarios Afines", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.navigate(Rutas.amigos) {
                popUpTo(Rutas.dashboard) { inclusive = false }
            }},
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .animateContentSize()
        ) {
            Text("Amigos", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.navigate(Rutas.quedadasUsuario){
                popUpTo(Rutas.dashboard) { inclusive = false }
            } },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .animateContentSize()
        ) {
            Text("Quedadas", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}