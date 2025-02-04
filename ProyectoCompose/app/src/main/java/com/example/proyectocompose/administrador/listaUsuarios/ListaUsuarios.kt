package com.example.proyectocompose.administrador.listaUsuarios

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.proyectocompose.Rutas
import com.example.proyectocompose.login.LoginViewModel
import com.example.proyectocompose.model.User

@Composable
fun ListaUsuarios(navController: NavController, loginViewModel: LoginViewModel, listaUsuariosViewModel: ListaUsuariosViewModel){
    Scaffold(
        topBar = {
            TopBarListaUsuarios(navController)
        }) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            BodyListaUsuarios(navController, loginViewModel, listaUsuariosViewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarListaUsuarios(navController: NavController){
    TopAppBar(
        colors = topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = {
            Text("Usuarios")
        },
        navigationIcon = {
            IconButton(
                onClick = {
                    navController.popBackStack(Rutas.adminPrincipal, inclusive = false)
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Localized description"
                )
            }
        })
}


@Composable
fun BodyListaUsuarios(navController: NavController, loginViewModel: LoginViewModel, listaUsuariosViewModel: ListaUsuariosViewModel){
    val usuarios by listaUsuariosViewModel.usuarios.collectAsState()

    Column (modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center){
        LazyColumn {
            items(usuarios) { usuario ->
                if (usuario.correo != loginViewModel.getCurrentEmail()){
                    ItemUsuario(usuario){
                        listaUsuariosViewModel.seleccionarUsuario(it)
                        navController.navigate(Rutas.editarUsuario)
                    }
                }

            }

        }
    }
}

@Composable
fun ItemUsuario(usuario: User, editar: (User) -> Unit){
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Nombre: "+usuario.nombre)
            Text(text = "Apellidos: "+usuario.apellidos)
            Text(text = "Correo: "+usuario.correo)
            Text(text = "Fecha de Nacimiento: ${usuario.fecNac}")
            Text(text = "Rol: ${usuario.rol}")

            Spacer(modifier = Modifier.height(8.dp))

            if (usuario.activado){
                Text(text = "Activo", color = Color.Green)
            }else{
                Text(text = "Inactivo", color = Color.Red)
            }

            if (usuario.conectado){
                Text(text = "Conectado", color = Color.Green)
            }else{
                Text(text = "Desconectado", color = Color.Red)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(onClick = { editar(usuario) }) {
                Text(text = "Editar")
            }
        }
    }
}