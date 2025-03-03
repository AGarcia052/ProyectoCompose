package com.example.proyectocompose.administrador.principal

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
import androidx.navigation.NavController
import com.example.proyectocompose.R
import com.example.proyectocompose.utils.Rutas
import com.example.proyectocompose.login.LoginViewModel


@Composable
fun AdminPrincipal(navController: NavController, loginViewModel: LoginViewModel){
    Scaffold(
        topBar = {
            TopBarAdminPrincipal(navController, loginViewModel)
        }) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            BodyAdminPrincipal(navController)
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarAdminPrincipal(navController: NavController, loginViewModel: LoginViewModel){
    val contexto = LocalContext.current
    var mostrarMenuPuntos by remember { mutableStateOf(false) }
    val opciones = listOf("Perfil", "Volver al Dashboard", "Cerrar Sesión")

    TopAppBar(
        colors = topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = {
            Text("Opciones de Administrador")
        },
        actions = {
            IconButton(onClick = { mostrarMenuPuntos = true }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "Localized description"
                )
            }
            DesplegarMenuPuntos(
                expanded = mostrarMenuPuntos,
                opciones = opciones,
                onItemClick = { opcion ->
                    when (opcion) {
                        "Perfil" -> navController.navigate(Rutas.perfil){
                            popUpTo(Rutas.adminPrincipal) { inclusive = false }
                        }
                        "Volver al Dashboard" -> navController.popBackStack(Rutas.dashboard, inclusive = false)
                        "Cerrar Sesión" -> {
                            loginViewModel.signOut(contexto)
                            navController.popBackStack(Rutas.login, inclusive = false)
                        }
                    }
                },
                onDismiss = { mostrarMenuPuntos = false }
            )
        }
    )
}

@Composable
fun DesplegarMenuPuntos(expanded: Boolean, opciones: List<String>, onItemClick: (String) -> Unit,  onDismiss: () -> Unit) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        opciones.forEach { option ->
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
                    }else if(option == "Volver al Dashboard"){
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

@Composable
fun BodyAdminPrincipal(navController: NavController){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = { navController.navigate(Rutas.usuariosAdmin){
                popUpTo(Rutas.adminPrincipal) { inclusive = false }
            } },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .animateContentSize()
        ) {
            Text("Usuarios", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.navigate(Rutas.quedadasAdmin){
                popUpTo(Rutas.adminPrincipal) { inclusive = false }
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