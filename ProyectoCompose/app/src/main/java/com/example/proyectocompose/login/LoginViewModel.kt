package com.example.proyectocompose.login

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.proyectocompose.Colecciones
import com.example.proyectocompose.Constantes
import com.example.proyectocompose.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow



class LoginViewModel: ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore


    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> get()=_isLoading

    private val _loginSuccess = MutableStateFlow<Boolean>(false)
    val loginSuccess: StateFlow<Boolean> get()=_loginSuccess

    private val _registerSuccess = MutableStateFlow<Boolean>(false)
    val registerSuccess: StateFlow<Boolean> get()=_registerSuccess

    private val _currentEmail = MutableStateFlow<String>("")
    val currentEmail: StateFlow<String> get()=_currentEmail

    private val _userActivo = MutableStateFlow<Boolean>(true)
    val userActivo: StateFlow<Boolean> get()=_userActivo

    private val _errorMessage = MutableStateFlow<String>("")
    val errorMessage: StateFlow<String> get()=_errorMessage


    val isUserLoggedIn: Boolean
        get() = auth.currentUser != null

    fun loginWithEmail(email: String, password: String) {
        _isLoading.value = true
        _loginSuccess.value = false

        auth.signInWithEmailAndPassword(email, password)

            .addOnCompleteListener { task ->
                _isLoading.value = false

                if (task.isSuccessful) {
                    _loginSuccess.value = true
                    _currentEmail.value = auth.currentUser?.email!!
                    checkForm()
                    Log.i(Constantes.TAG,"LOGINVW: Login con email correcto")

                } else {
                    Log.i(Constantes.TAG,"LOGINVW: Login con email fallido")
                    _errorMessage.value = "Usuario o Contraseña incorrecto"
                }
            }

            .addOnFailureListener { error->
                _isLoading.value = false
                Log.e(Constantes.TAG,"LOGINVW: (ONFAILURE) Login con email fallido\n$error")
                _errorMessage.value = "Usuario o Contraseña incorrectos"
            }
    }

    fun registerWithEmail(email: String, password: String) {
        _isLoading.value = true
        _registerSuccess.value = false

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _isLoading.value = false
                    _currentEmail.value = auth.currentUser?.email!!
                    createBasicUser()

                } else {
                    _isLoading.value = false
                    Log.e(Constantes.TAG,"LOGINVW: Registro con email fallido")
                    _errorMessage.value = "Registro fallido, revise los campos"
                }
            }
            .addOnFailureListener {error ->
                Log.e(Constantes.TAG,"LOGINVW: (ONFAILURE) Registro con email fallido\n$error")
                _isLoading.value = false
                _errorMessage.value = "Registro fallido, revise los campos"
            }
    }

    fun loginWithGoogle(idToken: String) {
        _isLoading.value = true
        _loginSuccess.value = false
        _registerSuccess.value = false

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _isLoading.value = false
                    val newUser = task.result?.additionalUserInfo?.isNewUser ?: false
                    _currentEmail.value = auth.currentUser?.email!!
                    Log.e(Constantes.TAG,"LOGINVW:  Login con gmail correcto")
                    if (newUser) {
                        createBasicUser()
                    } else {
                        checkForm()
                    }



                } else {
                    Log.e(Constantes.TAG,"LOGINVW: Login con gmail fallido")
                    _errorMessage.value = "Ha ocurrido un error, inténtelo de nuevo más tarde"
                    _isLoading.value = false
                }
            }
            .addOnFailureListener { error ->
                Log.e(Constantes.TAG,"LOGINVW: (ONFAILURE) Login con gmail fallido\n$error")
                _isLoading.value = false
            }
    }

    fun signOut(context: Context) {
        Log.d(Constantes.TAG, "LOGINVW: signOut() llamado ${_loginSuccess.value}")
        if (_loginSuccess.value) {
            val googleSignInClient = GoogleSignIn.getClient(
                context,
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(context.getString(R.string.client_id))
                    .requestEmail()
                    .build()
            )

            googleSignInClient.revokeAccess().addOnCompleteListener { revokeTask ->
                if (revokeTask.isSuccessful) {
                    Log.d(Constantes.TAG, "LOGINVW: Acceso revocado correctamente")
                    auth.signOut()
                    cambiarConectado(false)
                    Log.d(Constantes.TAG, "LOGINVW: Sesión cerrada correctamente")
                } else {
                    Log.e(Constantes.TAG, "LOGINVW: Error al revocar el acceso")
                }
            }
        } else {
            cambiarConectado(false)
            auth.signOut()
            Log.d(Constantes.TAG, "LOGINVW: Sesión cerrada para usuario no Google")
        }

        _loginSuccess.value = false
    }

    fun getCurrentEmail(): String{
        return auth.currentUser?.email ?:""
    }

    private fun createBasicUser(){
        val user = hashMapOf(
            "correo" to _currentEmail.value,
            "formCompletado" to false
        )

        db.collection(Colecciones.usuarios)
            .document(_currentEmail.value)
            .set(user)
            .addOnSuccessListener {
                Log.e(Constantes.TAG,"LOGINVW: USUARIO CREADO")
                _registerSuccess.value = true
                _isLoading.value = false
            }
            .addOnFailureListener {error ->
                _isLoading.value = false
                _errorMessage.value = "Error al crear la cuenta"
                Log.e(Constantes.TAG,"LOGINVW: ERROR AL CREAR EL USUARIO\n$error")
            }
    }

    private fun checkForm(){

        var formCompletado:Boolean
        var activo:Boolean?
        db.collection(Colecciones.usuarios)
            .document(currentEmail.value)
            .get()
            .addOnSuccessListener { result ->
                val datos = result.data
                datos?.let {
                    formCompletado = datos["formCompletado"] as Boolean
                    activo = datos["activo"] as Boolean?
                    Log.e(Constantes.TAG, "LOGINVW: FORM COMPLETADO: $formCompletado")
                    if (formCompletado){
                        if(activo != null){
                            if(!activo!!){
                                _userActivo.value = false
                            }
                            else{
                                cambiarConectado(true)
                            }
                        }
                        _loginSuccess.value = true
                    }
                    else{
                        _registerSuccess.value = true
                    }
                }
                _isLoading.value = false

            }
            .addOnFailureListener {
                _isLoading.value = false
                Log.e(Constantes.TAG,"LOGINVW: ERROR AL OBTENER EL USUARIO")
                _errorMessage.value ="Error al iniciar sesión"
            }

    }

     fun cambiarConectado(valor: Boolean){
         if (_currentEmail.value.isNotEmpty()){
             db.collection(Colecciones.usuarios)
                 .document(_currentEmail.value)
                 .update("conectado",valor)
                 .addOnSuccessListener {
                     Log.i(Constantes.TAG,"LOGINVW: USUARIO CONECTADO")
                 }
                 .addOnFailureListener {error ->
                     Log.e(Constantes.TAG,"LOGINVW: fallo en update usuario CONECTADO\n$error")
                 }
         }
    }

    fun restart(){
        _loginSuccess.value = false
        _registerSuccess.value = false
        _errorMessage.value = ""
        _isLoading.value = false
    }

    fun resetErrorMessage(){
        _errorMessage.value = ""
    }

}