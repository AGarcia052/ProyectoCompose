package com.example.proyectocompose.usuario.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.proyectocompose.utils.Rutas
import com.example.proyectocompose.login.LoginViewModel
import com.example.proyectocompose.model.Mensaje
import com.example.proyectocompose.usuario.amigos.ListaAmigosViewModel

@Composable
fun Chat(navController: NavController, loginViewModel: LoginViewModel, listaAmigosViewModel: ListaAmigosViewModel, chatViewModel: ChatViewModel){
    Scaffold(
        topBar = {
            TopBarChat(navController,listaAmigosViewModel)
        }) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            BodyChat(loginViewModel, listaAmigosViewModel, chatViewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarChat(navController: NavController, listaAmigosViewModel: ListaAmigosViewModel){
    val usuarioSeleccionado by listaAmigosViewModel.usuarioSeleccionado.collectAsState()
    TopAppBar(
        colors = topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = {
            if (usuarioSeleccionado != null){
                Text(usuarioSeleccionado!!.nombre)
            }
            else{
                Text("Chat")
            }
        },
        navigationIcon = {
            IconButton(
                onClick = {
                    navController.popBackStack(Rutas.amigos, inclusive = false)
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Localized description"
                )
            }
        }
    )
}

@Composable
fun BodyChat(loginViewModel: LoginViewModel, listaAmigosViewModel: ListaAmigosViewModel, chatViewModel: ChatViewModel){
    val mensajesUI by chatViewModel.mensajes.collectAsState()
    var inputMessage by remember { mutableStateOf("") }

    val listState = rememberLazyListState()
    LaunchedEffect (mensajesUI){
        if (mensajesUI.isEmpty()){
            chatViewModel.observeMessages(listaAmigosViewModel.usuarioSeleccionado.value!!.correo, loginViewModel.getCurrentEmail())
        }
    }
    Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
        LaunchedEffect(mensajesUI) {
            if (mensajesUI.isNotEmpty()) {
                listState.animateScrollToItem(0)
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(8.dp),
            reverseLayout = true,
            state = listState
        ) {
            items(mensajesUI) { mens ->
                ChatMessageItem(mens, loginViewModel)
            }
        }

        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = inputMessage,
                onValueChange = { inputMessage = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Escribe tu mensaje") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                if (inputMessage.isNotBlank()) {
                    chatViewModel.sendMessage(loginViewModel.getCurrentEmail(), listaAmigosViewModel.usuarioSeleccionado.value!!.correo, inputMessage)
                    inputMessage = ""
                }
            }) {
                Text("Enviar")
            }
        }


    }
}

@Composable
fun ChatMessageItem(mens: Mensaje, loginViewModel: LoginViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        horizontalArrangement = if (mens.sender == loginViewModel.getCurrentEmail()) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .background(
                    if (mens.sender == loginViewModel.getCurrentEmail()) MaterialTheme.colorScheme.surfaceContainerLow else MaterialTheme.colorScheme.surfaceContainerHighest,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(8.dp)
        ) {
            Text(
                text = mens.sender,
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = mens.mensaje,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}