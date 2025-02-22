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
import com.google.firebase.firestore.FieldValue
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
                _candidatos.value = emptyList()
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

                _candidatos.value = tempList.filterNot { it in _candidatosDescartados.value }
            }


            _isLoading.value = false
            _profileImg.value = ""
            obtenerPerfilImg()
        }

    }


    // necesarias las copias de objetos para actualizar StateFlow
    fun like(correo: String) {

        val candidatoLikeado = _candidatos.value.find {candidato: User -> candidato.correo == correo }

        val yaTieneLike = candidatoLikeado!!.usuariosConLike.contains(_usuario.value.correo)

        if(yaTieneLike){

            //CANDIDATO
            val nuevosAmigosCandidato = candidatoLikeado.amigos + _usuario.value.correo
            val nuevosUsuariosConLikeCandidato = candidatoLikeado.usuariosConLike - _usuario.value.correo

            _candidatos.value = _candidatos.value.filterNot { it.correo == correo }
            _profileImg.value = ""

            db.collection(Colecciones.usuarios)
                .document(correo)
                .update("amigos",nuevosAmigosCandidato)
                .addOnSuccessListener {  }
                .addOnFailureListener {  }

            db.collection(Colecciones.usuarios)
                .document(correo)
                .update("usuariosConLike",nuevosUsuariosConLikeCandidato)
                .addOnSuccessListener {  }
                .addOnFailureListener {  }

            //USUARIO

            val nuevosAmigosUsuario = _usuario.value.amigos + correo
            val nuevosLikesUsuario = _usuario.value.usuariosConLike.filterNot { it == correo }
            val nuevoUsuario = _usuario.value.copy(amigos = nuevosAmigosUsuario, usuariosConLike = nuevosLikesUsuario)
            _usuario.value = nuevoUsuario

            db.collection(Colecciones.usuarios)
                .document(_usuario.value.correo)
                .update("amigos",FieldValue.arrayUnion(correo))
                .addOnSuccessListener {  }
                .addOnFailureListener {  }
            db.collection(Colecciones.usuarios)
                .document(_usuario.value.correo)
                .update("usuariosConLike",nuevosLikesUsuario)
                .addOnSuccessListener {  }
                .addOnFailureListener {  }

        }else {

            val nuevosUsuariosConLikeUsuario = _usuario.value.usuariosConLike + correo

            _usuario.value = _usuario.value.copy(usuariosConLike = nuevosUsuariosConLikeUsuario)

            db.collection(Colecciones.usuarios)
                .document(_usuario.value.correo)
                .update("usuariosConLike", FieldValue.arrayUnion(correo))
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

        checkHayCandidatos()
    }


    fun descartar(correo: String){

        val candidato = _candidatos.value.find { candidato -> candidato.correo == correo }

        _candidatos.value -= candidato!!
        _profileImg.value = ""

        checkHayCandidatos()
        obtenerPerfilImg()
    }

    private suspend fun getNoAmigos() {

        val results: QuerySnapshot = db.collection(Colecciones.usuarios)
                .whereEqualTo("activo", true)
                .get()
                .await()

        val amigosMasLikes = (_usuario.value.amigos + _usuario.value.usuariosConLike).filter { it != _usuario.value.correo }


        //EVITAR USO DE INDICES

//        results = if(_usuario.value.amigos.isNotEmpty() || _usuario.value.usuariosConLike.isNotEmpty()){
//            db.collection(Colecciones.usuarios)
//                .whereNotIn("correo", amigosMasLikes)
//                .whereEqualTo("activo", true)
//                .get()
//                .await()
//
//        }else{
//            db.collection(Colecciones.usuarios)
//                .whereEqualTo("activo", true)
//                .get()
//                .await()
//        }


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

        val quitar = todos.filterNot { user -> user.correo == _usuario.value.correo }

        if(_usuario.value.amigos.isNotEmpty() || _usuario.value.usuariosConLike.isNotEmpty()){
            val temp = arrayListOf<User>()
            for(candidato in quitar){

                if ((amigosMasLikes.find {correo -> correo == candidato.correo } == null)){

                    temp.add(candidato)
                }
            }
            _usrNoAmigos.value = temp.toList()

        }else{
            _usrNoAmigos.value = quitar
        }



        Log.i(Constantes.TAG, "usrAfinesVW: Candidatos obtenidos")
    }


}