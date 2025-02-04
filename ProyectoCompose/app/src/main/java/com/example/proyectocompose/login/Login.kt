package com.example.proyectocompose.login

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.proyectocompose.R
import com.example.proyectocompose.Rutas
import com.example.proyectocompose.common.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions


@Composable
fun Login(loginViewModel: LoginViewModel, navController: NavController) {

    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var passwd by remember { mutableStateOf("") }
    val loginSuccess by loginViewModel.loginSuccess.collectAsState()
    val registerSuccess by loginViewModel.registerSuccess.collectAsState()
    val loading by loginViewModel.isLoading.collectAsState()
    var isRegistering by remember { mutableStateOf(false) }
    val activo by loginViewModel.userActivo.collectAsState()

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.result
            val idToken = account?.idToken
            if (idToken != null) {
                loginViewModel.loginWithGoogle(idToken)
            } else {
                Toast.makeText(context, "Error obteniendo token de Google", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    fun launchGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.client_id))
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(context, gso)
        googleSignInLauncher.launch(googleSignInClient.signInIntent)
    }

    LaunchedEffect(loginSuccess) {
        val destino = if(activo) Rutas.dashboard else Rutas.usrNoActivo
        if (loginSuccess) {
            loginViewModel.restart()
            navController.navigate(destino) {
                popUpTo(Rutas.login) { inclusive = true }
            }
        }
    }

    LaunchedEffect(registerSuccess) {
        if (registerSuccess) {
            loginViewModel.restart()
            navController.navigate(Rutas.formulario) {
                popUpTo(Rutas.formulario) { inclusive = true }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

        Column(
            modifier = Modifier
                .padding(vertical = 10.dp, horizontal = 15.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            TitleText(if (isRegistering) "Regístrate" else "Iniciar Sesión")

            Spacer(modifier = Modifier.height(90.dp))

            TFBasic(value = email, label = "Correo: ") { email = it }

            Spacer(modifier = Modifier.height(90.dp))

            TFPasswd(passwd = passwd, label = "Contraseña: ") { passwd = it }

            Spacer(modifier = Modifier.height(70.dp))

            Button(
                onClick = {
                    if (email.isEmpty() || passwd.isEmpty()){
                        Toast.makeText(context,"Completa todos los campos para continuar",Toast.LENGTH_SHORT).show()
                    }
                    else{
                        if (isRegistering) {
                            loginViewModel.registerWithEmail(email, passwd)
                        } else {
                            loginViewModel.loginWithEmail(email, passwd)
                        }
                    }

                },
                elevation = ButtonDefaults.elevatedButtonElevation(),
                modifier = Modifier.width(300.dp)
            ) {

                Text(if (isRegistering) "Regístrate" else "Iniciar Sesión")
            }

            Spacer(modifier = Modifier.height(10.dp))

            BodyText(text = "O")

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    if (email.isEmpty() || passwd.isEmpty()){
                        Toast.makeText(context,"Completa todos los campos para continuar",Toast.LENGTH_SHORT).show()
                    }
                    else{
                        launchGoogleSignIn()
                    }
                },
                elevation = ButtonDefaults.elevatedButtonElevation(),
                modifier = Modifier.width(300.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_google),
                        contentDescription = "Imagen Google",
                        modifier = Modifier.requiredSize(24.dp)
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Text("Iniciar Sesión con Google")
                }

            }

            Spacer(modifier = Modifier.height(25.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isRegistering){

                    BodyText(text = "¿Ya tienes cuenta?")

                    Spacer(modifier = Modifier.width(5.dp))

                    ClickableText(text = "Iniciar sesión") { isRegistering = !isRegistering }
                }
                else{
                    BodyText(text = "¿Aún no tienes cuenta?")

                    Spacer(modifier = Modifier.width(5.dp))

                    ClickableText(text = "Registrate") { isRegistering = !isRegistering }
                }

            }


        }

        if (loading) {
            CircularProgressIndicator()
        }

    }

}

//TODO(CAMBIAR POR ALERTDIALOG)
@Composable
fun UsuarioNoActivo(navController: NavController){
    Column (verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(100.dp))
        Text(text = "TU USARIO NO HA SIDO ACTIVADO.\nContacta con un administrador para activar tu cuenta")
        Spacer(modifier = Modifier.height(100.dp))
        Button(onClick = {

            navController.navigate(Rutas.login){
                popUpTo(Rutas.login) { inclusive = true }
            }

        }) {
            Text(text="Volver")
        }
    }
}