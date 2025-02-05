package com.example.proyectocompose.usuario.perfil

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.proyectocompose.Rutas
import com.example.proyectocompose.login.LoginViewModel
import com.example.proyectocompose.usuario.dashboard.BodyDashboard
import com.example.proyectocompose.usuario.dashboard.DesplegarMenuPuntos
import com.example.proyectocompose.usuario.dashboard.TopBarDashboard

@Composable
fun Perfil(navController: NavController,loginViewModel: LoginViewModel){


    Scaffold(
        topBar = {
            TopBarProfile(navController = navController)
        }) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            //BodyProfile(loginViewModel = loginViewModel)
        }
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarProfile(navController: NavController){

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
                Text("Perfil")
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

@Preview(showBackground = true)
@Composable
fun BodyProfile(){

}

