package com.example.proyectocompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
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
import com.example.proyectocompose.usuario.dashboard.Dashboard
import com.example.proyectocompose.usuario.dashboard.DashboardViewModel
import com.example.proyectocompose.usuario.dashboard.perfil.Perfil
import com.example.proyectocompose.usuario.dashboard.perfil.PerfilViewModel

class MainActivity : ComponentActivity() {
    val loginViewModel = LoginViewModel()
    val dashboardViewModel = DashboardViewModel()
    val listaUsuariosViewModel = ListaUsuariosViewModel()
    val perfilViewModel = PerfilViewModel()
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProyectoComposeTheme {
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
}

