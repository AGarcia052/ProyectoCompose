package com.example.proyectocompose.login

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.proyectocompose.Colecciones
import com.example.proyectocompose.model.Form
import com.google.firebase.Firebase
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FormularioViewModel: ViewModel(){

    val TAG = "AMIGOSAPP"
    val db = Firebase.firestore

    val terms = MutableStateFlow(false)
    //USUARIO
    val nombre = MutableStateFlow("")
    val apellidos = MutableStateFlow("")
    val fecNac = MutableStateFlow("")
    val correo = MutableStateFlow("")
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

    fun completarRegistro(){

        var form = mapOf(
            "relacionSeria" to relacionSeria.value,
            "deportes" to deportes.value,
            "arte" to arte.value,
            "politica" to politica.value,
            "tieneHijos" to tieneHijos.value,
            "quiereHijos" to quiereHijos.value,
            "interesSexual" to interesSexual.value
        )

        var user = mapOf(
            "nombre" to nombre.value,
            "apellidos" to apellidos.value,
            "fecNac" to fecNac.value,
            "correo" to correo.value,
            "formCompletado" to true,
            "formulario" to form
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


    }

    fun setRegistroCompletado(value: Boolean){
        _completado.value = value
    }



}