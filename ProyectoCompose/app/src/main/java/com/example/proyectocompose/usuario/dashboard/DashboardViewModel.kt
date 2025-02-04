package com.example.proyectocompose.usuario.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.proyectocompose.Colecciones
import com.example.proyectocompose.model.User
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DashboardViewModel:ViewModel() {
    val TAG = "AMIGOSAPP"

    private val _rol = MutableStateFlow<String>("")
    val rol: StateFlow<String> get()=_rol

    private val db = Firebase.firestore

    init {
        cargarUsuariosConectados()
    }
    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> get()=_isLoading

    private val _numUsuariosConectados = MutableStateFlow<Int?>(null)
    val numUsuariosConectados: StateFlow<Int?> get()=_numUsuariosConectados

    fun cargarUsuariosConectados(){
        db.collection(Colecciones.usuarios)
            .whereEqualTo("conectado", true)
            .addSnapshotListener { snapshot,_ ->
                var numUsuarios = 0
                if (snapshot != null) {
                    numUsuarios = snapshot.size()
                }

                _numUsuariosConectados.value = numUsuarios
                Log.d(TAG, "Usuarios conectados: $_numUsuariosConectados")
            }
    }

    fun checkRol(email:String){
        _isLoading.value = true

        db.collection(Colecciones.usuarios)
            .document(email)
            .get()
            .addOnSuccessListener { result ->
                val datos = result.data
                datos?.let {
                    _rol.value = datos["rol"] as String
                }
                _isLoading.value = false

            }
            .addOnFailureListener {
                Log.e(TAG,"ERROR AL OBTENER EL ROL")
            }

    }
}