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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UsuariosAfinesViewModel : ViewModel() {


    val db = Firebase.firestore
    val storage = Firebase.storage
    var storageRef = storage.reference


    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _imgLoading = MutableStateFlow<Boolean>(false)
    val imgLoading: StateFlow<Boolean> get() = _imgLoading

    private val _usuario = MutableStateFlow<User>(User())
    val usuario: StateFlow<User> get() = _usuario

//    private val _listaAmigos = MutableStateFlow<List<User>>(listOf())
//    val listaAmigos: StateFlow<List<User>> get() = _listaAmigos
//
//    private val _usuariosConLike = MutableStateFlow<List<User>>(listOf())
//    val usuariosConLike: StateFlow<List<User>> get() = _usuariosConLike

    private val _usrNoAmigos = MutableStateFlow<List<User>>(listOf())

    private val _usuarioCargado = MutableStateFlow<Boolean>(false)
    val usuarioCargado: StateFlow<Boolean> get()=_usuarioCargado

    private val _candidatos = MutableStateFlow<List<User>>(listOf())
    val candidatos: StateFlow<List<User>> get() = _candidatos

    private val _candidatosDescartados = MutableStateFlow<List<User>>(listOf())
    val candidatosDescartados: StateFlow<List<User>> get() = _candidatosDescartados

    private val _profileImg = MutableStateFlow<String>("")
    val profileImg: StateFlow<String> get()  = _profileImg

    private val _candidatoImgs = MutableStateFlow<List<String>>(emptyList())
    val candidatoImgs: StateFlow<List<String>> get() = _candidatoImgs

    fun setUsuario(usuario: User) {
        _usuario.value = usuario
        _usuarioCargado.value = true
    }
    fun setUsuarioCargado(value: Boolean){
        _usuarioCargado.value = value
    }

    fun checkHayCandidatos(): Boolean{
        return _candidatos.value.isNotEmpty()
    }


    fun obtenerPerfilImg(){
        _imgLoading.value = true
        _candidatoImgs.value = emptyList()
        if(_candidatos.value.isNotEmpty()){
            val candidato =_candidatos.value.first().correo
            storageRef.child("images/$candidato/perfil").downloadUrl
                .addOnSuccessListener { uri ->
                    Log.e(Constantes.TAG, "USRafinesVW: Imagen de perfil: "+uri.toString())
                    _profileImg.value = uri.toString()
                    _imgLoading.value = false

                }
                .addOnFailureListener { error ->
                    Log.e(Constantes.TAG, "USRafinesVW: Error al cargar la imagen de perfil:\n$error}")
                    _imgLoading.value = false
                }

            storageRef.child("images/$candidato/photos").listAll()
                .addOnSuccessListener { result ->
                    val urls = mutableListOf<String>()
                    val tasks = result.items.map { ref ->
                        ref.downloadUrl.addOnSuccessListener { uri ->
                            urls.add(uri.toString())
                            if (urls.size == result.items.size){
                                _candidatoImgs.value = urls.toList()
                                _isLoading.value = false
                            }
                        }
                            .addOnFailureListener {error->
                                Log.e(Constantes.TAG, "USRafinesVW: Error al cargar las imagenes:\n$error")
                                _isLoading.value = false
                            }

                    }
                }
                .addOnFailureListener {error->
                    Log.e(Constantes.TAG, "USRafinesVW: candidato sin imagenes u otro error:\n$error")
                    _candidatoImgs.value = emptyList()
                }

        }

    }


    fun filtrarCandidatos(filtrar: Boolean = true) {
        viewModelScope.launch {
            _isLoading.value = true

            _profileImg.value = ""
            _candidatoImgs.value = emptyList()

            if (_candidatos.value.isNotEmpty()) {
                _candidatosDescartados.value = _candidatos.value + _candidatosDescartados.value
                _candidatos.value = emptyList()
            }

            if(!filtrar){
                _candidatosDescartados.value = emptyList()
            }

            getNoAmigos()
            val tempList = arrayListOf<User>()
            var comprobar: Boolean;
            Log.i(Constantes.TAG, "Usuario: ${_usuario.value}")

            for (usr in _usrNoAmigos.value) {
                Log.i(Constantes.TAG, "Candidato: $usr")
                comprobar = if(filtrar){
                    Afinidad.calcularAfinidad(usuario = _usuario.value, candidato = usr)
                }else{
                    true
                }

                if (comprobar) {
                    tempList.add(usr)
                }

                _candidatos.value = tempList.toList()

            }


            _isLoading.value = false
            _profileImg.value = ""
            obtenerPerfilImg()
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

                _candidatos.value = _candidatos.value.filterNot { it.correo == correo }


                _usuario.value = nuevoUsuario
            } else {

                val nuevosUsuariosConLikeUsuario = _usuario.value.usuariosConLike + correo

                _usuario.value = _usuario.value.copy(usuariosConLike = nuevosUsuariosConLikeUsuario)

                db.collection(Colecciones.usuarios)
                    .document(_usuario.value.correo)
                    .update("usuariosConLike",_usuario.value.usuariosConLike)
                    .addOnSuccessListener {

                        Log.i(Constantes.TAG,"Candidato ${correo} añadido a likeados")
                        _candidatos.value = _candidatos.value.filterNot { it.correo == correo }
                        _profileImg.value = ""
                        obtenerPerfilImg()


                    }
                    .addOnFailureListener {error ->
                        Log.e(Constantes.TAG,"Error al añadir candidato a likeados\n$error")

                    }
            }
        }




        checkHayCandidatos()
    }


    fun descartar(correo: String){

        val candidato = _candidatos.value.find { candidato -> candidato.correo == correo }

        _candidatos.value -= candidato!!

        checkHayCandidatos()
        _profileImg.value = ""
        obtenerPerfilImg()
    }

    private suspend fun getNoAmigos() {

        val results: QuerySnapshot?

        if(_usuario.value.amigos.isNotEmpty() && _usuario.value.usuariosConLike.isNotEmpty()){
            results = db.collection(Colecciones.usuarios)
                .whereNotIn("correo", _usuario.value.amigos)
                .whereNotIn("correo",_usuario.value.usuariosConLike)
                .whereEqualTo("activo", true)
                //.whereNotEqualTo("correo",_usuario.value.correo)
                .get()
                .await()
        }else if(_usuario.value.usuariosConLike.isNotEmpty()){
            results = db.collection(Colecciones.usuarios)
                .whereEqualTo("activo", true)
                .whereNotEqualTo("correo",_usuario.value.correo)
                .whereNotIn("correo",_usuario.value.usuariosConLike)
                .get()
                .await()
        }else if(_usuario.value.amigos.isNotEmpty()){
            results = db.collection(Colecciones.usuarios)
                .whereEqualTo("activo", true)
                //.whereNotEqualTo("correo",_usuario.value.correo)
                .whereNotIn("correo",_usuario.value.amigos)
                .get()
                .await()
        }else{
            results = db.collection(Colecciones.usuarios)
                .whereEqualTo("activo", true)
                //.whereNotEqualTo("correo",_usuario.value.correo)
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

        val quitar = todos.find { user -> user.correo == _usuario.value.correo }!!

        _usrNoAmigos.value = todos - quitar
        _usrNoAmigos.value -= _candidatosDescartados.value
        Log.i(Constantes.TAG, "usrAfinesVW: Candidatos obtenidos")
    }


}