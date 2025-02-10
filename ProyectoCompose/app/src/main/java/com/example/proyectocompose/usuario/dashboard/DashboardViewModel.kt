package com.example.proyectocompose.usuario.dashboard

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.proyectocompose.Colecciones
import com.example.proyectocompose.model.Formulario
import com.example.proyectocompose.model.User
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

class DashboardViewModel:ViewModel() {
    val TAG = "AMIGOSAPP"

    /*
    private val _rol = MutableStateFlow<String>("")
    val rol: StateFlow<String> get()=_rol*/

    private val db = Firebase.firestore
    private val storage = Firebase.storage
    val storageRef = storage.reference
    init {
        cargarUsuariosConectados()
    }
    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> get()=_isLoading

    private val _numUsuariosConectados = MutableStateFlow<Int?>(null)
    val numUsuariosConectados: StateFlow<Int?> get()=_numUsuariosConectados

    private val _usuario = MutableStateFlow<User>(User())
    val usuario: StateFlow<User> get() = _usuario

    private val _profileImg = MutableStateFlow<String>("")
    val profileImg: StateFlow<String> get()  = _profileImg

    private val _userImages = MutableStateFlow<List<String>>(listOf())
    val userImages: StateFlow<List<String>> get() = _userImages

    private val _imageFile = MutableStateFlow<File?>(null)
    val imageFile: StateFlow<File?> get() = _imageFile

    private val _imageUri = MutableStateFlow<Uri>(Uri.EMPTY)
    val imageUri: StateFlow<Uri> get() = _imageUri

    private val _imageUploaded = MutableStateFlow(true)
    val imageUploaded: StateFlow<Boolean> get() = _imageUploaded

    private val _usuarioMod = MutableStateFlow(false)
    val usuarioMod: StateFlow<Boolean> get() = _usuarioMod

    // QUIZAS USAR .COUNT()
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

    /*
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

    }*/

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

    fun cargarImagenes(){
        _isLoading.value = true
        storageRef.child("images/${_usuario.value.correo}/perfil").downloadUrl
            .addOnSuccessListener { uri ->
                Log.e(TAG, "Imagen de perfil: "+uri.toString())
                _profileImg.value = uri.toString()
                _isLoading.value = false

            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error al cargar la imagen: ${exception.message}")
                _isLoading.value = false
            }

        storageRef.child("images/${_usuario.value.correo}/photos").listAll()
            .addOnSuccessListener { result ->
                val urls = mutableListOf<String>()
                val tasks = result.items.map { ref ->
                    ref.downloadUrl.addOnSuccessListener { uri ->
                        urls.add(uri.toString())
                        if (urls.size == result.items.size){
                            _userImages.value = urls.toList()
                            _isLoading.value = false
                        }
                    }
                        .addOnFailureListener {
                            Log.e(TAG, "Error al cargar la lista: ${it.message}")
                            _isLoading.value = false
                        }

                }
            }
    }

    fun updateImageUri(uri: Uri) {
        _imageUri.value = uri
    }

    fun setImageFile(file: File) {
        _imageFile.value = file
    }

    fun uploadImage(context: Context,esPerfil: Boolean,numImg: Int) {
        _isLoading.value = true

        Log.e(TAG,"ENTRA EN UPLOAD IMAGE")

        val path = if(esPerfil){
            "images/${_usuario.value.correo}/perfil"
        } else{
            "images/${_usuario.value.correo}/photos/foto$numImg"
        }

        val file = _imageFile.value ?: return
        val fileUri = Uri.fromFile(file)
        val ref = storageRef.child(path)

        ref.putFile(fileUri)
            .addOnSuccessListener {
                Log.d(TAG, "Imagen subida correctamente. URL de la misma: ${ref.downloadUrl}")
                _isLoading.value = false
                _imageUploaded.value = true
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                _isLoading.value = false

            }

    }

    fun setUploaded(value: Boolean){
        _imageUploaded.value = value
    }
    fun setUsuarioMod(value: Boolean){
        _usuarioMod.value = value
    }

    fun actualizarUsuario(usuario: User){
        _isLoading.value = true

        db.collection(Colecciones.usuarios)
            .document(_usuario.value.correo)
            .set(usuario)
            .addOnSuccessListener {
                Log.e(TAG,"PERFIL: USUARIO ACTUALIZADO CORRECTAMENTE")
                _isLoading.value = false
                _usuarioMod.value = true
            }
            .addOnFailureListener {
                Log.e(TAG,"PERFIL: ERROR AL ACTUALIZAR AL USUSARIO\n${it.message}")
                _isLoading.value = false

            }


    }


}