package com.example.proyectocompose.usuario.dashboard.perfil

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.example.proyectocompose.utils.Colecciones
import com.example.proyectocompose.model.User
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

class PerfilViewModel: ViewModel() {

    val TAG = "AMIGOSAPP"

    private val db = Firebase.firestore
    private val storage = Firebase.storage
    val storageRef = storage.reference

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> get()=_isLoading


    private val _profileImg = MutableStateFlow<String>("")
    val profileImg: StateFlow<String> get()  = _profileImg

    private val _userImages = MutableStateFlow<List<String>>(emptyList())
    val userImages: StateFlow<List<String>> get() = _userImages

    private val _imageFile = MutableStateFlow<File?>(null)
    val imageFile: StateFlow<File?> get() = _imageFile

    private val _imageUri = MutableStateFlow<Uri>(Uri.EMPTY)
    val imageUri: StateFlow<Uri> get() = _imageUri

    private val _imageUploaded = MutableStateFlow(true)
    val imageUploaded: StateFlow<Boolean> get() = _imageUploaded

    private val _usuarioMod = MutableStateFlow(false)
    val usuarioMod: StateFlow<Boolean> get() = _usuarioMod

    fun cargarImagenes(usuario: User){
        _isLoading.value = true
        storageRef.child("images/${usuario.correo}/perfil").downloadUrl
            .addOnSuccessListener { uri ->
                Log.e(TAG, "Imagen de perfil: "+uri.toString())
                _profileImg.value = uri.toString()
                _isLoading.value = false

            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error al cargar la imagen: ${exception.message}")
                _isLoading.value = false
            }

        storageRef.child("images/${usuario.correo}/photos").listAll()
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

    fun uploadImage(context: Context, esPerfil: Boolean, numImg: Int, usuario: User) {
        _isLoading.value = true

        Log.e(TAG,"ENTRA EN UPLOAD IMAGE")

        val path = if(esPerfil){
            "images/${usuario.correo}/perfil"
        } else{
            "images/${usuario.correo}/photos/foto$numImg"
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
            .document(usuario.correo)
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
    fun borrarImagen(imagen: String, usuario: User){
        val ref = storage.getReferenceFromUrl(imagen)
        ref.delete()
            .addOnSuccessListener {
                _userImages.value = emptyList()
                cargarImagenes(usuario = usuario)
                // si no va descomentar: _imageUploaded.value = true
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error al eliminar archivo: ${exception.message}")
            }

    }
}