package com.example.proyectocompose.usuario.dashboard.perfil.listaLikes

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.proyectocompose.model.UserQuedada
import com.example.proyectocompose.usuario.dashboard.DashboardViewModel
import com.example.proyectocompose.utils.Rutas
import com.example.proyectocompose.utils.calcularEdad

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaLikes(
    viewModel: ListaLikesViewModel,
    dashboardViewModel: DashboardViewModel,
    navController: NavController
) {
    val usuario by viewModel.usuario.collectAsState()
    val userCargado = remember { mutableStateOf(false) }
    val usuariosObtenidos by viewModel.usuariosObtenidos.collectAsState()
    val usuariosLike by viewModel.usuariosLike.collectAsState()
    if (!userCargado.value && usuario.correo.isNotEmpty()) {

        viewModel.getUsuariosLike()
        userCargado.value = true

    }

    LaunchedEffect(Unit) {

        viewModel.setUsuario(dashboardViewModel.usuario.value)

    }
    Scaffold(topBar = {

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
                    Text("Likes")
                }
            },
            navigationIcon = {
                IconButton(
                    onClick = { navController.popBackStack(Rutas.perfil, inclusive = false) }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver atrás"
                    )
                }
            }
        )

    }) { innerPadding ->
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (usuario.usuariosConLike.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No tienes a nadie en la lista de likes")
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(onClick = { navController.navigate(Rutas.usuariosAfines) }) {
                        Text(text="Buscar afines")
                    }
                }
            } else if (usuariosObtenidos) {
                LazyColumn(verticalArrangement = Arrangement.SpaceBetween) {
                    items(usuariosLike) {
                        LikeItem(it, viewModel)
                    }
                }
            } else {
                CircularProgressIndicator()
            }
        }
    }

}


@Composable
fun LikeItem(usuario: UserQuedada, viewModel: ListaLikesViewModel) {

    val showDeleteDialog = remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .border(2.dp, color = Color.Black),
        elevation = CardDefaults.cardElevation(5.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = usuario.imgUrl,
                    contentDescription = "Imagen perfil",
                    modifier = Modifier
                        .size(100.dp)
                        .border(1.dp, color = Color.Gray)
                )
                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(text = "Nombre: ${usuario.nombre}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Edad: ${calcularEdad(usuario.fecNac)}")
                }
            }

            Button(onClick = { showDeleteDialog.value = true }) {
                Text(text = "Borrar Like")
            }
        }
    }
    if (showDeleteDialog.value) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog.value = false },
            title = { Text("Quitar like") },
            text = { Text("¿Estás seguro de quitar el like? Esta acción es irreversible") },
            confirmButton = {
                Button(onClick = {
                    viewModel.borrarLike(usuario.correo)
                    showDeleteDialog.value = false
                }) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog.value = false }) {
                    Text("Cancelar")
                }
            }
        )
    }


}