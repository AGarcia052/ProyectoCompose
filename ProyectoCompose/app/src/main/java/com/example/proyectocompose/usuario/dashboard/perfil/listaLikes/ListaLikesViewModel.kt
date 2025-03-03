package com.example.proyectocompose.usuario.dashboard.perfil.listaLikes

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.proyectocompose.model.User
import com.example.proyectocompose.model.UserQuedada
import com.example.proyectocompose.utils.Colecciones
import com.example.proyectocompose.utils.Constantes
import com.example.proyectocompose.utils.Constantes.TAG
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

class ListaLikesViewModel: ViewModel() {


    val db = Firebase.firestore
    val storage = Firebase.storage
    var storageRef = storage.reference


    private val _usuario = MutableStateFlow<User>(User())
    val usuario: StateFlow<User> get()=_usuario

    private val _usuariosLike = MutableStateFlow<MutableList<UserQuedada>>(mutableListOf())
    val usuariosLike: StateFlow<MutableList<UserQuedada>> get() = _usuariosLike


    private val _usuariosObtenidos = MutableStateFlow<Boolean>(false)
    val usuariosObtenidos: StateFlow<Boolean> get()=_usuariosObtenidos

    private var consultasPendientes = _usuariosLike.value.size

    fun setUsuario(usuario: User){

        _usuario.value = usuario

    }



    /**
     * Obtiene todos los usuarios en la lista de likes del usuario logueado.
     *
     * Al ser asíncronas las consultas a firebase, se usa verificarFinalizacion para que
     * _usuariosObtenidos sea true solo si se han intentado obtener todos los usuarios
     * **/
    fun getUsuariosLike(){
        _usuariosLike.value = mutableListOf()
        fun verificarFinalizacion() {
            consultasPendientes--
            if (consultasPendientes <= 0) {
                _usuariosObtenidos.value = true
            }
        }
        for(user in _usuario.value.usuariosConLike){

            storageRef.child("images/${user}/perfil").downloadUrl
                .addOnSuccessListener { uri ->
                    val img = uri.toString()
                    db.collection(Colecciones.usuarios)
                        .document(user)
                        .get()

                        .addOnSuccessListener { document ->

                                val nombre = document.getString("nombre") ?: ""
                                val correoUser = document.getString("correo") ?: ""
                                val fecNac = document.getString("fecNac") ?: ""
                                val usuarioQuedada = UserQuedada(nombre= nombre, correo = correoUser, fecNac = fecNac, imgUrl = img)
                                _usuariosLike.value.add(usuarioQuedada)
                                verificarFinalizacion()
                        }
                        .addOnFailureListener { error ->
                            Log.e(TAG, "Error al obtener usuario:\n$error")
                            verificarFinalizacion()
                        }
                }
                .addOnFailureListener { error ->
                    Log.e(TAG, "Error al cargar la imagen: \n$error")
                    verificarFinalizacion()
                }

        }


    }

    fun borrarLike(correo: String){

        _usuario.value.usuariosConLike -= correo

        db.collection(Colecciones.usuarios)
            .document(_usuario.value.correo)
            .update("usuariosConLike",_usuario.value.usuariosConLike)
            .addOnSuccessListener {
                _usuariosLike.value = _usuariosLike.value.filterNot { user -> user.correo == correo }.toMutableList()
                Log.i(TAG, "ListaLikesVW: Like borrado")

            }
            .addOnFailureListener {error->
                Log.e(TAG, "ListaLikesVW: Error al borrar el like: \n$error")

            }
    }







}