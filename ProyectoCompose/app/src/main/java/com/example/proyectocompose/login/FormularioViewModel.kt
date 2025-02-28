package com.example.proyectocompose.login

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.proyectocompose.utils.Colecciones
import com.google.firebase.Firebase
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

class FormularioViewModel: ViewModel(){

    val TAG = "AMIGOSAPP"
    val db = Firebase.firestore
    val storage = Firebase.storage
    var storageRef = storage.reference

    val terms = MutableStateFlow(false)
    //USUARIO
    val nombre = MutableStateFlow("")
    val apellidos = MutableStateFlow("")
    val fecNac = MutableStateFlow("")
    val correo = MutableStateFlow("")
    val sexo = MutableStateFlow("")
    val descripcion = MutableStateFlow("")
    //FORMULARIO
    val relacionSeria = MutableStateFlow(false)
    val deportes = MutableStateFlow(50)
    val arte = MutableStateFlow(50)
    val politica = MutableStateFlow(50)
    val tieneHijos = MutableStateFlow(false)
    val quiereHijos = MutableStateFlow(false)
    val interesSexual = MutableStateFlow("")

    private val _completado = MutableStateFlow(false)
    val completado: StateFlow<Boolean> get() = _completado

    private val _imageFile = MutableLiveData<File?>()
    val imageFile: LiveData<File?> get() = _imageFile

    private val _imageUri = MutableLiveData<Uri>(Uri.EMPTY)
    val imageUri: LiveData<Uri> get() = _imageUri

    private val _isUploading = MutableLiveData<Boolean>(false)
    val isUploading: LiveData<Boolean> get() = _isUploading

    private val _uploadSuccess = MutableLiveData<Boolean>()
    val uploadSuccess: LiveData<Boolean> get() = _uploadSuccess

    fun completarRegistro(context: Context){

        val form = mapOf(
            "sexo" to sexo.value,
            "relacionSeria" to relacionSeria.value,
            "deportes" to deportes.value,
            "arte" to arte.value,
            "politica" to politica.value,
            "tieneHijos" to tieneHijos.value,
            "quiereHijos" to quiereHijos.value,
            "interesSexual" to interesSexual.value
        )

        val user = mapOf(
            "nombre" to nombre.value,
            "apellidos" to apellidos.value,
            "fecNac" to fecNac.value,
            "descripcion" to descripcion.value,
            "correo" to correo.value,
            "formCompletado" to true,
            "rol" to "USUARIO",
            "activo" to false,
            "conectado" to false,
            "formulario" to form,
            "amigos" to listOf<String>(),
            "usuariosConLike" to listOf<String>()
        )

        db.collection(Colecciones.usuarios)
            .document(correo.value)
            .set(user, SetOptions.merge())
            .addOnSuccessListener {
                Log.e(TAG,"USUARIO MODIFICADO CORRECTAMENTE")
                _completado.value = true
            }
            .addOnFailureListener { e ->
                Log.e(TAG,"Error al actualizar el documento: ${e.message}")
            }

        uploadImage(context)


    }

    fun setRegistroCompletado(value: Boolean){
        _completado.value = value
    }

    fun updateImageUri(uri: Uri) {
        _imageUri.value = uri
    }

    fun setImageFile(file: File) {
        _imageFile.value = file
    }

    fun uploadImage(context: Context) {
        val file = _imageFile.value ?: return
        val fileUri = Uri.fromFile(file)
        val ref = storageRef.child("images/${correo.value}/perfil")

        _isUploading.value = true
        ref.putFile(fileUri)
            .addOnSuccessListener {
                _isUploading.value = false
                _uploadSuccess.value = true
                Log.d(TAG, "Imagen subida correctamente. URL de la misma: ${ref.downloadUrl}")
            }
            .addOnFailureListener { exception ->
                _isUploading.value = false
                _uploadSuccess.value = false
                Toast.makeText(context, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }





}