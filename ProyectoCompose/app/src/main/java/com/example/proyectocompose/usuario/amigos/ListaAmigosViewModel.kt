package com.example.proyectocompose.usuario.amigos

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.proyectocompose.Colecciones
import com.example.proyectocompose.Constantes.TAG
import com.example.proyectocompose.model.Amigo
import com.example.proyectocompose.model.User
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ListaAmigosViewModel: ViewModel() {
    private val database = FirebaseDatabase.getInstance().reference

    private val db = Firebase.firestore

    val storage = Firebase.storage
    var storageRef = storage.reference

    private val _usuarios = MutableStateFlow<List<Amigo>>(emptyList())
    val usuarios: StateFlow<List<Amigo>> = _usuarios

    private val _usuarioSeleccionado = MutableStateFlow<Amigo?>(null)
    val usuarioSeleccionado: StateFlow<Amigo?> = _usuarioSeleccionado

    private val _numAmigos = MutableStateFlow<Int>(0)
    val numAmigos: StateFlow<Int> = _numAmigos

    private val _numAmigosConectados = MutableStateFlow<Int>(0)
    val numAmigosConectados: StateFlow<Int> = _numAmigosConectados


    fun seleccionarUsuario(usuario: Amigo){
        _usuarioSeleccionado.value = usuario
    }

    fun cargarUsuarios(currentUser: String) {
        db.collection(Colecciones.usuarios).document(currentUser)
            .get()
            .addOnSuccessListener { result ->
                val listaUsuarios = mutableListOf<Amigo>()
                var usuariosConectados = 0
                val tareasPendientes = mutableListOf<Task<*>>()

                if (result["amigos"] != null) {
                    for (correoAmigo in result["amigos"] as List<String>) {
                        val tareaUsuario = db.collection(Colecciones.usuarios).document(correoAmigo)
                            .get()
                            .continueWithTask { task ->
                                if (!task.isSuccessful || task.result == null) {
                                    throw task.exception ?: Exception("Error al obtener usuario")
                                }
                                val document = task.result!!

                                val usuario = Amigo(
                                    document["correo"].toString(),
                                    document["nombre"].toString(),
                                    document["apellidos"].toString(),
                                    document["conectado"] as Boolean
                                )
                                if (usuario.conectado) {
                                    usuariosConectados++
                                }

                                val tareaImagen =
                                    storageRef.child("images/${usuario.correo}/perfil")
                                        .downloadUrl
                                        .addOnSuccessListener { uri ->
                                            usuario.foto = uri.toString()
                                        }
                                        .addOnFailureListener {
                                            Log.e(TAG,"Error al cargar la imagen: ${it.message}")
                                        }

                                val tareaMensajes = database.child(Colecciones.mensajes)
                                    .orderByChild("sender").equalTo(usuario.correo)
                                    .orderByChild("reciever").equalTo(currentUser)
                                    .orderByChild("leido").equalTo(false)
                                    .get()
                                    .addOnSuccessListener { snapshot ->
                                        usuario.mensajesSinLeer = snapshot.children.count()
                                    }
                                    .addOnFailureListener {
                                        Log.e(TAG,"Error al cargar los mensajes: ${it.message}")
                                    }

                                Tasks.whenAllComplete(tareaImagen, tareaMensajes)
                                    .addOnSuccessListener {
                                        listaUsuarios.add(usuario)
                                    }
                            }

                        tareasPendientes.add(tareaUsuario)
                    }
                }
                Tasks.whenAllComplete(tareasPendientes).addOnSuccessListener {
                    _usuarios.value = listaUsuarios
                    _numAmigos.value = listaUsuarios.size
                    _numAmigosConectados.value = usuariosConectados
                }
            }
            .addOnFailureListener { Log.e(TAG, "Error al cargar el usuario actual: ${it.message}") }
    }
}