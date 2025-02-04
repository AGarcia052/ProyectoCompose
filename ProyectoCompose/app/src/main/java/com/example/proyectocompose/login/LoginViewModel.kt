package com.example.proyectocompose.login

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.proyectocompose.Colecciones
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
    val TAG = "AMIGOSAPP"
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

    //val errorMessage = MutableStateFlow<String?>(null)


    val isUserLoggedIn: Boolean
        get() = auth.currentUser != null

    fun loginWithEmail(email: String, password: String) {
        _isLoading.value = true
       // errorMessage.value = null
        _loginSuccess.value = false

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                _isLoading.value = false
                if (task.isSuccessful) {
                    //_loginSuccess.value = true
                    _currentEmail.value = auth.currentUser?.email!!
                    checkForm()
                } else {
                    //_errorMessage.value = task.exception?.message ?: "Error desconocido"
                }
            }
    }

    fun registerWithEmail(email: String, password: String) {
        _isLoading.value = true
        //errorMessage.value = null
        _registerSuccess.value = false

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _currentEmail.value = auth.currentUser?.email!!
                    createBasicUser()
                } else {
                    //errorMessage.value = task.exception?.message ?: "Error desconocido"
                }
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
                    val newUser = task.result?.additionalUserInfo?.isNewUser ?: false
                    _currentEmail.value = auth.currentUser?.email!!
                    if (newUser) {
                        createBasicUser()
                    } else {
                        checkForm()
                    }



                } else {
                    //errorMessage.value = task.exception?.message ?: "Error desconocido"
                }
            }
    }

    fun signOut(context: Context) {
        Log.d(TAG, "signOut() llamado ${_loginSuccess.value}")
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
                    Log.d(TAG, "Acceso revocado correctamente")
                    auth.signOut()
                    cambiarConectado(false)
                    Log.d(TAG, "Sesión cerrada correctamente")
                } else {
                    Log.e(TAG, "Error al revocar el acceso")
                }
            }
        } else {
            cambiarConectado(false)
            auth.signOut()
            Log.d(TAG, "Sesión cerrada para usuario no Google")
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
                Log.e(TAG,"USUARIO CREADO")
                _registerSuccess.value = true
                _isLoading.value = false
            }
            .addOnFailureListener {
                Log.e(TAG,"ERROR AL CREAR EL USUARIO")
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
                    Log.e(TAG,"FORM COMPLETADO: "+formCompletado)
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
                Log.e(TAG,"ERROR AL OBTENER EL USUARIO")
            }

    }

     fun cambiarConectado(valor: Boolean){
         if (_currentEmail.value.isNotEmpty()){
             db.collection(Colecciones.usuarios)
                 .document(_currentEmail.value)
                 .update("conectado",valor)
                 .addOnSuccessListener {
                     Log.i(TAG,"USUARIO CONECTADO")
                 }
                 .addOnFailureListener {
                     Log.e(TAG,"fallo en update usuario CONECTADO")

                 }
         }
    }

    fun restart(){
        _loginSuccess.value = false
        _registerSuccess.value = false
    }

}