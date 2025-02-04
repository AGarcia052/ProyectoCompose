package com.example.proyectocompose.usuario.dashboard

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.proyectocompose.R
import com.example.proyectocompose.Rutas
import com.example.proyectocompose.login.LoginViewModel
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
    val isLoading by dashboardVM.isLoading.collectAsState()
    var mostrarMenuPuntos by remember { mutableStateOf(false) }
    val rol by dashboardVM.rol.collectAsState()
    val opciones = listOf("Perfil", "Opciones de administrador", "Cerrar Sesión")
    LaunchedEffect(rol) {
        if (rol == "") {
            dashboardVM.checkRol(loginVM.getCurrentEmail())
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
                        "Perfil" -> navController.navigate(Rutas.perfil)
                        "Opciones de administrador" -> navController.navigate(Rutas.adminPrincipal)
                        "Cerrar Sesión" -> navController.popBackStack(Rutas.login, inclusive = false)
                    }
                },
                onDismiss = { mostrarMenuPuntos = false },
                esAdministrador = { if (rol == "Administrador") true else false }
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
            onClick = { navController.navigate(Rutas.usuariosAfines) },
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
            onClick = { navController.navigate(Rutas.amigos) },
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
            onClick = { navController.navigate(Rutas.quedadasUsuario) },
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