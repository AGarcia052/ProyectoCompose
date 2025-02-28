package com.example.proyectocompose.administrador.listaUsuarios

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.proyectocompose.utils.Colecciones
import com.example.proyectocompose.model.User
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ListaUsuariosViewModel:ViewModel() {
    private val db = Firebase.firestore

    val storage = Firebase.storage
    var storageRef = storage.reference

    val TAG = "AMIGOSAPP"

    private val _usuarios = MutableStateFlow<List<User>>(emptyList())
    val usuarios: StateFlow<List<User>> get()=_usuarios

    private val _usuarioAEditar = MutableStateFlow<User?>(null)
    val usuarioAEditar: StateFlow<User?> get()=_usuarioAEditar

    fun seleccionarUsuario(usuario: User){
        _usuarioAEditar.value = usuario
        _rol.value = usuario.rol
        _activo.value = usuario.activo
    }
    fun desseleccionarUsuario(){
        _usuarioAEditar.value = null
    }

    private val _activo = MutableStateFlow<Boolean>(true)
    val activo: StateFlow<Boolean> get()=_activo

    private val _rol = MutableStateFlow<String>("")
    val rol: StateFlow<String> get()=_rol

    fun cambiarActivo(activo: Boolean){
        _activo.value = activo
    }

    fun cambiarRol(rol: String){
        _rol.value = rol
    }

    private val _imageUri = MutableStateFlow<Uri>(Uri.EMPTY)
    val imageUri: StateFlow<Uri> get() = _imageUri

    fun cargarImagen(email: String) {
        storageRef.child("images/$email/perfil").downloadUrl
            .addOnSuccessListener { uri ->
                Log.e(TAG, uri.toString())
                _imageUri.value = uri
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error al cargar la imagen: ${exception.message}")
            }
    }


    init {
        cargarUsuarios()
    }

    fun cargarUsuarios(){
        db.collection(Colecciones.usuarios)
            .get()
            .addOnSuccessListener { result ->
                val listaUsuarios = mutableListOf<User>()
                for (document in result) {
                    val usuario = document.toObject(User::class.java)
                    usuario.activo = document.getBoolean("activado") ?: false
                    listaUsuarios.add(usuario)
                }
                _usuarios.value = listaUsuarios
            }
            .addOnFailureListener { exception ->
                Log.e(TAG,"ERROR AL OBTENER LOS USUARIOS")
            }
    }

    fun cambiarEstadoUsuario(activo: Boolean){
        db.collection(Colecciones.usuarios).document(_usuarioAEditar.value!!.correo)
            .update("activado",activo)
            .addOnSuccessListener {
                _usuarioAEditar.value = _usuarioAEditar.value!!.copy(activo = activo)
                Log.d(TAG, "Estado del usuario actualizado")
            }
            .addOnFailureListener {
                Log.e(TAG, "ERROR AL ACTUALIZAR EL ESTADO DEL USUARIO")
            }
    }

    fun cambiarRolUsuario(rol: String){
        db.collection(Colecciones.usuarios).document(_usuarioAEditar.value!!.correo)
            .update("rol",rol)
            .addOnSuccessListener {
                _usuarioAEditar.value = _usuarioAEditar.value!!.copy(rol = rol)
                Log.d(TAG, "Rol del usuario actualizado")
            }
            .addOnFailureListener {
                Log.e(TAG, "ERROR AL ACTUALIZAR EL ROL DEL USUARIO")
            }
    }


}