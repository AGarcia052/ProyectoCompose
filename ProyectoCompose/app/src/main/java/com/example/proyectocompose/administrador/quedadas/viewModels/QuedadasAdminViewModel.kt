package com.example.proyectocompose.administrador.quedadas.viewModels

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.proyectocompose.Colecciones
import com.example.proyectocompose.model.Quedada
import com.example.proyectocompose.utils.toCustomString
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class QuedadasAdminViewModel : ViewModel() {

    val TAG = "AMIGOSAPP"
    val db = Firebase.firestore


    private val _quedadas = mutableStateListOf<Quedada>()
    val quedadas: List<Quedada> get() = _quedadas

    private val _quedadaSelecc = MutableStateFlow<Quedada>(Quedada())
    val quedadaSelecc: MutableStateFlow<Quedada> get() = _quedadaSelecc

    private val _locNuevaQuedada = MutableStateFlow<LatLng?>(null)
    val locNuevaQuedada: StateFlow<LatLng?> get() = _locNuevaQuedada

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _quedadaCreada = MutableStateFlow<Boolean>(false)
    val quedadaCreada: StateFlow<Boolean> get() = _quedadaCreada


    fun getQuedadas() {
        _isLoading.value = true
        db.collection(Colecciones.quedadas)
            .get()
            .addOnSuccessListener { results ->
                val todos = results.documents.mapNotNull { document ->
                    try {
                        val ubicacion = document.getString("ubicacion") ?: ""
                        val inscripcionAbierta = document.getBoolean("inscripcionAbierta") ?: false
                        val fechaEvento = document.getString("fechaEvento") ?: ""
                        val correosUsr = document.get("usuarios") as? List<String> ?: emptyList()
                        val nombre = document.getString("nombre") as String ?: ""
                        Quedada(
                            nombre = nombre,
                            correosUsr = correosUsr,
                            fecha = fechaEvento,
                            ubicacion = ubicacion,
                            inscripcion = inscripcionAbierta
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error al guardar las quedadas: \n${e.printStackTrace()}")
                        null
                    }
                }
                _quedadas.clear()
                _quedadas.addAll(todos)
                Log.i(TAG, "Quedadas recuperadas: ${_quedadas.toList()}")
                _isLoading.value = false
            }
            .addOnFailureListener {
                Log.e(TAG, "Error al obtener las quedadas de firebase")
                _isLoading.value = false

            }
    }

    fun addLoc(loc: LatLng) {
        Log.i(TAG,"AÑADIDA LOCALIZACION: ")
        _locNuevaQuedada.value = loc
    }


    fun crearQuedada(fecha: String, nombre: String, context: Context) {
        _isLoading.value = true

        val docRef = db.collection(Colecciones.quedadas).document(nombre)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {

                    Toast.makeText(context,"Ya existe un evento con ese nombre",Toast.LENGTH_SHORT).show()

                } else {
                    val quedada = mapOf(
                        "ubicacion" to _locNuevaQuedada.value!!.toCustomString(),
                        "nombre" to nombre,
                        "fechaEvento" to fecha,
                        "inscripcionAbierta" to true,
                        "usuarios" to emptyList<String>()
                    )
                    db.collection(Colecciones.quedadas)
                        .document(nombre)
                        .set(quedada)
                        .addOnSuccessListener {
                            _quedadaCreada.value = true
                            Log.i(TAG, "Quedada creada correctamente")
                            _isLoading.value = false
                            Toast.makeText(context,"Quedada creada correctamente",Toast.LENGTH_SHORT).show()

                        }

                        .addOnFailureListener {
                            Log.e(TAG, "Error al crear la quedada")
                            _isLoading.value = false
                            Toast.makeText(context,"Error al crear la quedada",Toast.LENGTH_SHORT).show()

                        }
                }
            }
            .addOnFailureListener {
                Log.e(TAG, "Error al obtener el documento")
            }




    }

    fun setQuedadaCreada(value: Boolean){
        _quedadaCreada.value = value
    }

    fun setQuedadaSelecc(quedada: Quedada){
        _quedadaSelecc.value = quedada
    }

    //SETTER QUEDADA SELECCIONADA

    fun setQuedadaSeleccNombre(nombre: String) {
        _quedadaSelecc.value = _quedadaSelecc.value.copy(nombre = nombre)
    }

    fun setQuedadaSeleccCorreosUsr(correos: List<String>) {
        _quedadaSelecc.value = _quedadaSelecc.value.copy(correosUsr = correos)
    }

    fun setQuedadaSeleccFecha(fecha: String) {
        _quedadaSelecc.value = _quedadaSelecc.value.copy(fecha = fecha)
    }

    fun setQuedadaSeleccUbicacion(ubicacion: String) {
        _quedadaSelecc.value = _quedadaSelecc.value.copy(ubicacion = ubicacion)
    }

    fun setQuedadaSeleccInscripcion(inscripcion: Boolean) {
        _quedadaSelecc.value = _quedadaSelecc.value.copy(inscripcion = inscripcion)
    }


}