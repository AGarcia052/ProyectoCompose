package com.example.proyectocompose.usuario.amigos

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.proyectocompose.R
import com.example.proyectocompose.utils.Rutas
import com.example.proyectocompose.login.LoginViewModel
import com.example.proyectocompose.model.Amigo

@Composable
fun ListaAmigos(navController: NavController, loginViewModel: LoginViewModel, listaAmigosViewModel: ListaAmigosViewModel){
    Scaffold(
        topBar = {
            TopBarListaAmigos(navController, loginViewModel,listaAmigosViewModel)
        }) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            BodyListaAmigos(navController, loginViewModel, listaAmigosViewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarListaAmigos(navController: NavController, loginViewModel: LoginViewModel, listaAmigosViewModel: ListaAmigosViewModel){
    TopAppBar(
        colors = topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = {
            Text("Amigos")
        },
        navigationIcon = {
            IconButton(
                onClick = {
                    navController.popBackStack(Rutas.dashboard, inclusive = false)
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Localized description"
                )
            }
        },
        actions =
        {
            IconButton(
                onClick = {
                    listaAmigosViewModel.cargarUsuarios(loginViewModel.getCurrentEmail())
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Localized description"
                )
            }
        }
    )
}


@Composable
fun BodyListaAmigos(navController: NavController,loginViewModel: LoginViewModel, listaAmigosViewModel: ListaAmigosViewModel){
    val usuarios by listaAmigosViewModel.usuarios.collectAsState()
    LaunchedEffect (usuarios){
        if (usuarios.isEmpty()){
            listaAmigosViewModel.cargarUsuarios(loginViewModel.getCurrentEmail())
        }
    }
    val numAmigos by listaAmigosViewModel.numAmigos.collectAsState()
    val numAmigosConectados by listaAmigosViewModel.numAmigosConectados.collectAsState()

    Column (modifier = Modifier.fillMaxWidth().padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center){
        Text(text = "Amigos: "+numAmigos+ " - Conectados: "+numAmigosConectados)
    }
    Column (modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center){
        LazyColumn {
            items(usuarios) { usuario ->
                ItemUsuario(usuario){
                    listaAmigosViewModel.seleccionarUsuario(it)
                    navController.navigate(Rutas.chat){
                        popUpTo(Rutas.amigos) { inclusive = false }
                    }
                }
            }
        }
    }
}

@Composable
fun ItemUsuario(usuario: Amigo, chat: (Amigo) -> Unit){
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (usuario.foto.isNotEmpty()) {
                AsyncImage(
                    model = usuario.foto,
                    contentDescription = "Imagen perfil",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color.Black, CircleShape)
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.ic_no_image),
                    contentDescription = "Imagen perfil",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color.Black, CircleShape)
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(text = usuario.nombre + " " + usuario.apellidos, fontWeight = FontWeight.Bold)
                Text(text = usuario.correo, fontSize = 14.sp, color = Color.Gray)
            }

            Button(onClick = { chat(usuario) }) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp) // Espaciado entre elementos
                ) {
                    Text(text = "Chat")

                    BadgedBox(badge = {
                        if (usuario.mensajesSinLeer > 0) { // Mostrar solo si hay mensajes no leídos
                            Badge {
                                Text(text = usuario.mensajesSinLeer.toString())
                            }
                        }
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_chat),
                            contentDescription = "Número de mensajes no leídos"
                        )
                    }
                }
            }
        }
    }

}