package com.example.proyectocompose.usuario.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.proyectocompose.utils.Colecciones
import com.example.proyectocompose.model.Formulario
import com.example.proyectocompose.model.User
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DashboardViewModel:ViewModel() {
    val TAG = "AMIGOSAPP"


    private val db = Firebase.firestore
    init {
        cargarUsuariosConectados()
    }
    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> get()=_isLoading

    private val _numUsuariosConectados = MutableStateFlow<Int?>(null)
    val numUsuariosConectados: StateFlow<Int?> get()=_numUsuariosConectados

    private val _usuario = MutableStateFlow<User>(User())
    val usuario: StateFlow<User> get() = _usuario



    // todo(quizar usar count)
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

    fun cargarUsuario(email:String){
        _isLoading.value = true
        db.collection(Colecciones.usuarios)
            .document(email)
            .get()
            .addOnSuccessListener { result ->
                val datos = result.data
                datos?.let {
                    _usuario.value.nombre = datos["nombre"] as String
                    _usuario.value.apellidos = datos["apellidos"] as String
                    _usuario.value.rol = datos["rol"] as String
                    _usuario.value.fecNac = datos["fecNac"] as String
                    _usuario.value.correo = datos["correo"] as String
                    _usuario.value.conectado = datos["conectado"] as Boolean
                    _usuario.value.activo = datos["activo"] as Boolean
                    _usuario.value.formCompletado = datos["formCompletado"] as Boolean
                    val formulario = datos["formulario"] as Map<*, *>
                    formulario.let{
                        _usuario.value.formulario = Formulario(

                            relacionSeria = it["relacionSeria"] as Boolean,
                            deportes = (it["deportes"] as Long).toInt(),
                            arte = (it["arte"] as Long).toInt(),
                            politica = (it["politica"] as Long).toInt(),
                            tieneHijos = it["tieneHijos"] as Boolean,
                            quiereHijos = it["quiereHijos"] as Boolean,
                            interesSexual = it["interesSexual"] as String
                        )
                    }
                }
                _isLoading.value = false
                Log.e(TAG,"DATOS USUARIO"+_usuario.value.toString())
            }
            .addOnFailureListener {
                Log.e(TAG,"ERROR AL OBTENER EL USUARIO")
                _isLoading.value = false
            }
    }




}