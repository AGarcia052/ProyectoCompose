package com.example.proyectocompose.usuario.buscarAfines

import android.content.ClipData.Item
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.proyectocompose.R
import com.example.proyectocompose.common.BodyText
import com.example.proyectocompose.common.Subtitle
import com.example.proyectocompose.model.User
import com.example.proyectocompose.utils.Rutas
import com.example.proyectocompose.usuario.dashboard.DashboardViewModel
import com.example.proyectocompose.utils.calcularEdad

@Composable
fun UsuariosAfines(viewModel: UsuariosAfinesViewModel, navController: NavController, dashboardViewModel: DashboardViewModel){

    val hayCandidatos by viewModel.hayCandidatos.collectAsState()
    val candidatos by viewModel.candidatos.collectAsState()
    val candidato = if(candidatos.isNotEmpty())candidatos.first()else User()
    LaunchedEffect(Unit) {
        viewModel.filtrarCandidatos()
    }
//    LaunchedEffect(!hayCandidatos) {
//        viewModel.filtrarCandidatos()
//        viewModel.setHayCandidatos(true)
//    }


    Scaffold(
        topBar = {TopAppBarUsrAfines(navController)}, bottomBar = {BottomAppBar {
            Row(){
                Button(onClick = {viewModel.like(candidato.correo)}) { Text(text = "Like") }
                Button(onClick = {viewModel.descartar(candidato.correo)}){ Text(text = "Descartar") }
            }
        }})
        { innerPadding ->

        Column(modifier = Modifier.padding(innerPadding)) {

            if(candidatos.isNotEmpty()){
                ItemCandidato(candidato)
            }else{
                Text("NO QUEDAN CANDIDATOS")
            }


        }

    }






}



@Composable
fun ItemCandidato(candidato: User){

    val expanded = remember { mutableStateOf(false)}


    Column(modifier = Modifier.fillMaxSize().padding(5.dp), horizontalAlignment = Alignment.CenterHorizontally){

//        AsyncImage(model = "imageUri",
//            contentDescription = "Imagen perfil",
//        )

        Image(
            painter = painterResource(R.drawable.ic_cuenta),
            contentDescription = "IMAGENES",
            modifier = Modifier.height(500.dp).size(300.dp)
        )



        Spacer(modifier = Modifier.height(10.dp))
        Text("${candidato.nombre}+${candidato.apellidos}", fontSize = 25.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(10.dp))
        BodyText(text = "${calcularEdad(candidato.fecNac)} Años")
        Spacer(modifier = Modifier.height(10.dp))
        BodyText(text = candidato.formulario!!.sexo)
        Spacer(modifier = Modifier.height(10.dp))
        BodyText(text = "Descripcion: AQUI DESC")
        Spacer(modifier = Modifier.height(10.dp))

        HorizontalDivider(modifier = Modifier.padding(horizontal = 30.dp), color = Color.Black)
        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .animateContentSize(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded.value = !expanded.value }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Más información")
                    Icon(
                        imageVector = if (expanded.value) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = if (expanded.value) "Colapsar" else "Expandir"
                    )
                }
                if (expanded.value) {
                    BodyText(text = "Quiere hijos: ${if (candidato.formulario!!.quiereHijos) "Si" else "No" }")
                    Spacer(modifier = Modifier.height(10.dp))
                    BodyText(text = "Tiene hijos: ${if (candidato.formulario!!.tieneHijos) "Si" else "No" }")
                    Spacer(modifier = Modifier.height(10.dp))
                    BodyText(text = "Interés en: ${candidato.formulario!!.interesSexual}")
                    Spacer(modifier = Modifier.height(10.dp))
                    BodyText(text = "Interés en política: ${interesIntaString(candidato.formulario!!.politica)}")
                    Spacer(modifier = Modifier.height(10.dp))
                    BodyText(text = "Interés en arte: ${interesIntaString(candidato.formulario!!.arte)}")
                    Spacer(modifier = Modifier.height(10.dp))
                    BodyText(text = "Interés en deporte: ${interesIntaString(candidato.formulario!!.deportes)}")
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }



    }

}

private fun interesIntaString(valor: Int): String{
    return when{
        valor > 75 -> "ALTO"
        valor > 40 -> "MEDIO"
        else -> "BAJO"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarUsrAfines(navController: NavController) {

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
                Text("Buscar afines")
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

