package com.example.proyectocompose.administrador.listaUsuarios

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.proyectocompose.R
import com.example.proyectocompose.Rutas

@Composable
fun EditarUsuario(navController: NavController, listaUsuariosViewModel: ListaUsuariosViewModel){
    Scaffold(
        topBar = {
            TopBarEditarUsuario(navController)
        }) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            BodyEditarUsuario(navController, listaUsuariosViewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarEditarUsuario(navController: NavController){
    TopAppBar(
        colors = topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = {
            Text("Editar usuario")
        },
        navigationIcon = {
            IconButton(
                onClick = {
                    navController.popBackStack(Rutas.usuariosAdmin, inclusive = false)
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
fun BodyEditarUsuario(navController: NavController, listaUsuariosViewModel: ListaUsuariosViewModel){
    val usuarioAEditar by listaUsuariosViewModel.usuarioAEditar.collectAsState()
    if (usuarioAEditar != null){

        Column (modifier = Modifier.padding(16.dp).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(text = "Nombre: " + usuarioAEditar!!.nombre)
            Text(text = "Apellidos: " + usuarioAEditar!!.apellidos)
            Text(text = "Correo: " + usuarioAEditar!!.correo)
            Text(text = "Fecha de Nacimiento: ${usuarioAEditar!!.fecNac}")
            FotoDePerfil(listaUsuariosViewModel)
            Row (horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically){
                Text(text = "Rol: ")
                Rol(listaUsuariosViewModel)
            }
            Row (horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically){
                Text(text = "Estado: ")
                Activo(listaUsuariosViewModel)
            }

            if (usuarioAEditar!!.conectado) {
                Text(text = "Conectado", color = Color.Green)
            } else {
                Text(text = "Desconectado", color = Color.Red)
            }
            Button(onClick = {
                listaUsuariosViewModel.desseleccionarUsuario()
                navController.popBackStack(Rutas.usuariosAdmin, inclusive = false)
            }) {
                Text(text = "Cancelar")
            }
            Button(onClick = {
                if (usuarioAEditar!!.activo != listaUsuariosViewModel.activo.value){
                    listaUsuariosViewModel.cambiarEstadoUsuario(listaUsuariosViewModel.activo.value)
                }
                if (usuarioAEditar!!.rol != listaUsuariosViewModel.rol.value){
                    listaUsuariosViewModel.cambiarRolUsuario(listaUsuariosViewModel.rol.value)
                }
                //listaUsuariosViewModel.desseleccionarUsuario()
                listaUsuariosViewModel.cargarUsuarios()
                navController.popBackStack(Rutas.usuariosAdmin, inclusive = false)
            }) {
                Text(text = "Aceptar Cambios")
            }
        }
    }else {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun FotoDePerfil(listaUsuariosViewModel: ListaUsuariosViewModel){
    val imageUri by listaUsuariosViewModel.imageUri.collectAsState()
    LaunchedEffect(imageUri) {
        listaUsuariosViewModel.cargarImagen(listaUsuariosViewModel.usuarioAEditar.value!!.correo)
    }
    Text(text = "Foto de perfil: ")
    if (imageUri != Uri.EMPTY) {
        AsyncImage(
            model = imageUri,
            contentDescription = "Foto de perfil",
            modifier = Modifier
                .fillMaxWidth()
                .size(200.dp)
        )
    } else {
        Image(
            painter = painterResource(R.drawable.pfp_default),
            contentDescription = "Foto de perfil",
            modifier = Modifier
                .fillMaxWidth()
                .size(200.dp),
            alignment = Alignment.Center
        )
    }
}

@Composable
fun Activo(listaUsuariosViewModel: ListaUsuariosViewModel){
    var expanded by remember { mutableStateOf(false) }
    val activo by listaUsuariosViewModel.activo.collectAsState()
    val listaActivo = listOf("Activo", "Inactivo")
    Column(modifier = Modifier.padding(10.dp)) {
        OutlinedTextField(
            value = if (activo) "Activo" else "Inactivo",
            onValueChange = { listaUsuariosViewModel.cambiarActivo(it == "Activo")},
            enabled = false,
            readOnly = true,
            modifier = Modifier
                .clickable {
                    expanded = true
                }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false })
        {
            listaActivo.forEach {
                DropdownMenuItem(text = { Text(text = it) }, onClick = {
                    expanded = false
                    listaUsuariosViewModel.cambiarActivo(it == "Activo")
                })
            }
        }
    }
}

@Composable
fun Rol(listaUsuariosViewModel: ListaUsuariosViewModel){
    var expanded by remember { mutableStateOf(false) }
    val rol by listaUsuariosViewModel.rol.collectAsState()

    val listaRol = listOf("Administrador", "Usuario")
    Column(modifier = Modifier.padding(10.dp)) {
        OutlinedTextField(
            value = rol,
            onValueChange = { listaUsuariosViewModel.cambiarRol(it) },
            enabled = false,
            readOnly = true,
            modifier = Modifier
                .clickable {
                    expanded = true
                }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            listaRol.forEach {
                DropdownMenuItem(text = { Text(text = it) }, onClick = {
                    expanded = false
                    listaUsuariosViewModel.cambiarRol(it)
                })
            }

        }
    }
}