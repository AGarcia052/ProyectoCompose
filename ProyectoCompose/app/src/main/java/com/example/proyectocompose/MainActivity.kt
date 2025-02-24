package com.example.proyectocompose

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.example.proyectocompose.login.Formulario
import com.example.proyectocompose.login.Login
import com.example.proyectocompose.login.LoginViewModel
import com.example.proyectocompose.login.UsuarioNoActivo
import com.example.proyectocompose.ui.theme.ProyectoComposeTheme
import com.example.proyectocompose.administrador.listaUsuarios.EditarUsuario
import com.example.proyectocompose.administrador.listaUsuarios.ListaUsuarios
import com.example.proyectocompose.administrador.listaUsuarios.ListaUsuariosViewModel
import com.example.proyectocompose.administrador.principal.AdminPrincipal
import com.example.proyectocompose.administrador.quedadas.AniadirQuedada
import com.example.proyectocompose.administrador.quedadas.EditarQuedada
import com.example.proyectocompose.administrador.quedadas.QuedadasAdmin
import com.example.proyectocompose.administrador.quedadas.viewModels.MapsAdminQuedadaViewModel
import com.example.proyectocompose.administrador.quedadas.viewModels.QuedadasAdminViewModel
import com.example.proyectocompose.usuario.amigos.ListaAmigos
import com.example.proyectocompose.usuario.amigos.ListaAmigosViewModel
import com.example.proyectocompose.usuario.buscarAfines.UsuariosAfines
import com.example.proyectocompose.usuario.buscarAfines.UsuariosAfinesViewModel
import com.example.proyectocompose.usuario.chat.Chat
import com.example.proyectocompose.usuario.chat.ChatViewModel
import com.example.proyectocompose.usuario.dashboard.Dashboard
import com.example.proyectocompose.usuario.dashboard.DashboardViewModel
import com.example.proyectocompose.usuario.dashboard.perfil.Perfil
import com.example.proyectocompose.usuario.dashboard.perfil.PerfilViewModel
import com.example.proyectocompose.usuario.dashboard.perfil.listaLikes.ListaLikes
import com.example.proyectocompose.usuario.dashboard.perfil.listaLikes.ListaLikesViewModel
import com.example.proyectocompose.utils.Rutas

class MainActivity : ComponentActivity() {
    val loginViewModel = LoginViewModel()
    val dashboardViewModel = DashboardViewModel()
    val listaUsuariosViewModel = ListaUsuariosViewModel()
    val listaAmigosViewModel = ListaAmigosViewModel()
    val chatViewModel = ChatViewModel()
    val perfilViewModel = PerfilViewModel()
    val quedadasAdminViewModel = QuedadasAdminViewModel()
    val mapsAdminQuedadaViewModel = MapsAdminQuedadaViewModel()
    val usuariosAfinesViewModel = UsuariosAfinesViewModel()
    val likesViewModel = ListaLikesViewModel()
    companion object {
        const val CHANNEL_ID = "sinLeerChannel"
    }
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProyectoComposeTheme {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            android.Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                            1
                        )
                    }
                }
                createNotificationChannel()


                val navController = rememberNavController()

                NavHost(navController, startDestination = Rutas.login){

                    composable(Rutas.login) {
                        Login(navController = navController, loginViewModel = loginViewModel)
                    }
                    composable(Rutas.formulario) {
                        Formulario(navController = navController,loginViewModel= loginViewModel)
                    }

                    composable(Rutas.usrNoActivo) {
                        UsuarioNoActivo(navController)
                    }
                    composable(Rutas.dashboard) {
                        Dashboard(navController, loginViewModel, dashboardViewModel)
                    }
                    composable(Rutas.adminPrincipal) {
                        AdminPrincipal(navController, loginViewModel)
                    }
                    composable(Rutas.usuariosAdmin) {
                        ListaUsuarios(navController, loginViewModel, listaUsuariosViewModel)
                    }
                    composable(Rutas.editarUsuario){
                        EditarUsuario(navController, listaUsuariosViewModel)

                    }
                    composable(Rutas.perfil){
                       Perfil(navController = navController, dashboardViewModel = dashboardViewModel, perfilViewModel = perfilViewModel)
                    }
                    composable(Rutas.quedadasAdmin){
                        QuedadasAdmin(navController = navController, viewModel = quedadasAdminViewModel, mapsViewModel = mapsAdminQuedadaViewModel)
                    }
                    composable(Rutas.addQuedada){
                        AniadirQuedada(navController = navController, viewModel = quedadasAdminViewModel)
                    }
                    composable(Rutas.editarQuedada){
                        EditarQuedada(navController = navController, viewModel = quedadasAdminViewModel, mapsViewModel = mapsAdminQuedadaViewModel)
                    }
                    composable(Rutas.amigos){
                        ListaAmigos(navController = navController, loginViewModel = loginViewModel, listaAmigosViewModel = listaAmigosViewModel)
                    }
                    composable(Rutas.chat) {
                        Chat(navController = navController,loginViewModel = loginViewModel,listaAmigosViewModel = listaAmigosViewModel,chatViewModel = chatViewModel)
                    }
                    composable(Rutas.usuariosAfines){
                        UsuariosAfines(
                            viewModel = usuariosAfinesViewModel, navController = navController,
                            dashboardViewModel = dashboardViewModel
                        )
                    }
                    composable(Rutas.likesUsuario){
                        ListaLikes(viewModel = likesViewModel, dashboardViewModel = dashboardViewModel, navController)
                    }
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        loginViewModel.cambiarConectado(false)
    }

    override fun onStop() {
        super.onStop()
        loginViewModel.cambiarConectado(false)
    }

    override fun onStart() {
        super.onStart()
        loginViewModel.cambiarConectado(true)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Notificaciones"
            val descriptionText = "Canal para notificaciones de la app"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(MainActivity.CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

