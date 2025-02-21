package com.example.proyectocompose.usuario.buscarAfines

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectocompose.utils.Colecciones
import com.example.proyectocompose.utils.Constantes
import com.example.proyectocompose.model.Formulario
import com.example.proyectocompose.model.User
import com.example.proyectocompose.utils.Afinidad
import com.google.firebase.Firebase
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UsuariosAfinesViewModel : ViewModel() {


    val db = Firebase.firestore
    val storage = Firebase.storage
    var storageRef = storage.reference


    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _usuario = MutableStateFlow<User>(User())
    val usuario: StateFlow<User> get() = _usuario

//    private val _listaAmigos = MutableStateFlow<List<User>>(listOf())
//    val listaAmigos: StateFlow<List<User>> get() = _listaAmigos
//
//    private val _usuariosConLike = MutableStateFlow<List<User>>(listOf())
//    val usuariosConLike: StateFlow<List<User>> get() = _usuariosConLike

    private val _usrNoAmigos = MutableStateFlow<List<User>>(listOf())

    private val _candidatos = MutableStateFlow<List<User>>(listOf())
    val candidatos: StateFlow<List<User>> get() = _candidatos

    private val _candidatosDescartados = MutableStateFlow<List<User>>(listOf())
    val candidatosDescartados: StateFlow<List<User>> get() = _candidatosDescartados

    private val _hayCandidatos = MutableStateFlow<Boolean>(true)
    val hayCandidatos: MutableStateFlow<Boolean> get() = _hayCandidatos

    fun setUsuario(usuario: User) {
        _usuario.value = usuario
    }

    fun setHayCandidatos(value: Boolean) {
        _hayCandidatos.value = value
    }

    fun checkHayCandidatos(): Boolean{
        return _candidatos.value.isNotEmpty()
    }


    fun filtrarCandidatos() {
        viewModelScope.launch {
            _isLoading.value = true

            if (_candidatos.value.isNotEmpty()) {
                _candidatosDescartados.value = _candidatos.value + _candidatosDescartados.value
                _candidatos.value = emptyList()
            }

            getNoAmigos()
            val tempList = arrayListOf<User>()
            var comprobar: Boolean;

            for (usr in _usrNoAmigos.value) {
                Log.i(Constantes.TAG, "Usuario: ${_usuario.value}")
                Log.i(Constantes.TAG, "Candidato: $usr")
                comprobar = Afinidad.calcularAfinidad(usuario = _usuario.value, candidato = usr)

                if (comprobar) {
                    tempList.add(usr)
                }
                if (tempList.size > 9) {
                    _candidatos.value = tempList.toList()
                    break
                }
            }


            _isLoading.value = false
            _hayCandidatos.value = true
        }

    }


    // necesarias las copias de objetos para actualizar StateFlow
    fun like(correo: String) {

        val candidatoLikeado = _candidatos.value.find {candidato: User -> candidato.correo == correo }

        candidatoLikeado?.let { candidato ->
            val yaTieneLike = candidato.usuariosConLike.contains(_usuario.value.correo)

            if (yaTieneLike) {
                val nuevosAmigosCandidato = candidato.amigos + _usuario.value.correo
                val nuevosUsuariosConLikeCandidato = candidato.usuariosConLike - _usuario.value.correo

                val nuevoCandidato = candidato.copy(
                    amigos = nuevosAmigosCandidato,
                    usuariosConLike = nuevosUsuariosConLikeCandidato
                )

                val nuevosAmigosUsuario = _usuario.value.amigos + correo
                val nuevoUsuario = _usuario.value.copy(amigos = nuevosAmigosUsuario)

                _candidatos.value = _candidatos.value.map {
                    if (it.correo == correo) nuevoCandidato else it
                }

                _usuario.value = nuevoUsuario
            } else {
                val nuevosUsuariosConLikeUsuario = _usuario.value.usuariosConLike + correo
                _usuario.value = _usuario.value.copy(usuariosConLike = nuevosUsuariosConLikeUsuario)
            }
        }

        checkHayCandidatos()

    }


    fun descartar(correo: String){

        val candidato = _candidatos.value.find { candidato -> candidato.correo == correo }

        _candidatos.value -= candidato!!

        checkHayCandidatos()
    }

    private suspend fun getNoAmigos() {

        val results: QuerySnapshot?

        if(_usuario.value.amigos.isNotEmpty()){
            results = db.collection(Colecciones.usuarios)
                .whereNotIn("correo", _usuario.value.amigos)
                .whereEqualTo("activo", true)
                .get()
                .await()
        }else{
            results = db.collection(Colecciones.usuarios)
                .whereEqualTo("activo", true)
                .get()
                .await()
        }


        val todos = results.documents.mapNotNull { document ->

            val formField = document.get("formulario") as Map<*, *>

            val formulario = Formulario(
                sexo = formField["sexo"].toString(),
                arte = formField["arte"].toString().toInt(),
                deportes = formField["deportes"].toString().toInt(),
                politica = formField["politica"].toString().toInt(),
                tieneHijos = formField["tieneHijos"].toString().toBoolean(),
                quiereHijos = formField["quiereHijos"].toString().toBoolean(),
                interesSexual = formField["interesSexual"].toString(),
                relacionSeria = formField["relacionSeria"].toString().toBoolean(),
            )

            User(
                correo = document.getString("correo") ?: "",
                nombre = document.getString("nombre") ?: "",
                apellidos = document.getString("apellidos") ?: "",
                fecNac = document.getString("fecNac") ?: "",
                usuariosConLike = document.get("usuariosConLike") as? List<String> ?: emptyList(),
                formulario = formulario,
                amigos = document.get("amigos") as? List<String> ?: emptyList()
            )
        }

        _usrNoAmigos.value = todos
        Log.i(Constantes.TAG, "usrAfinesVW: Candidatos obtenidos")
    }


}