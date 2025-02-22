package com.example.proyectocompose.administrador.quedadas.viewModels

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.proyectocompose.utils.Colecciones
import com.example.proyectocompose.utils.Constantes
import com.example.proyectocompose.model.Quedada
import com.example.proyectocompose.model.UserQuedada
import com.example.proyectocompose.utils.toCustomString
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class QuedadasAdminViewModel : ViewModel() {

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

    private val _quedadaModificada = MutableStateFlow<Boolean>(false)
    val quedadaModificada: StateFlow<Boolean> get() = _quedadaModificada

    private val _usuariosDisponibles = MutableStateFlow<List<UserQuedada>>(emptyList())
    val usuariosDisponibles: StateFlow<List<UserQuedada>> get() = _usuariosDisponibles

    private val _usuariosElegidos = MutableStateFlow<List<UserQuedada>>(emptyList())
    val usuariosElegidos: StateFlow<List<UserQuedada>> get() = _usuariosElegidos

    private val _usuariosObtenidos = MutableStateFlow<Boolean>(false)
    val usuariosObtenidos: StateFlow<Boolean> get() = _usuariosObtenidos

    fun restart() {
        _usuariosObtenidos.value = false
        _usuariosElegidos.value = emptyList()
        _usuariosDisponibles.value = emptyList()
        _quedadaModificada.value = false
        getQuedadas()

    }

    fun getUsuarios() {
        _isLoading.value = true
        var correo: String
        var nombre: String
        val usuariosEnQuedada = _quedadaSelecc.value.correosUsr
        var elegidos: ArrayList<UserQuedada> = arrayListOf()
        var disponibles: ArrayList<UserQuedada> = arrayListOf()
        var userQuedada: UserQuedada
        db.collection(Colecciones.usuarios)
            .whereEqualTo("activo", true)
            .get()
            .addOnSuccessListener { results ->
                results.documents.mapNotNull { document ->
                    try {
                        nombre = document.getString("nombre") ?: ""
                        correo = document.getString("correo") ?: ""
                        userQuedada = UserQuedada(nombre, correo)
                        if (correo in usuariosEnQuedada) {
                            elegidos.add(userQuedada)
                        } else {
                            disponibles.add(userQuedada)
                        }

                    } catch (error: Exception) {
                        Log.e(Constantes.TAG, "Error al guardar los usuarios\n$error")
                    }
                }

                _usuariosDisponibles.value = disponibles
                _usuariosElegidos.value = elegidos
                _usuariosObtenidos.value = true

                Log.i(
                    Constantes.TAG,
                    "Usuarios obtenidos correctamente\nDisponibles: ${disponibles}\nElegidos: ${elegidos}"
                )
            }
            .addOnFailureListener { error ->
                Log.e(Constantes.TAG, "Error al obtener los usuarios\n$error")

            }
    }

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
                        Log.e(
                            Constantes.TAG,
                            "Error al guardar las quedadas: \n${e.printStackTrace()}"
                        )
                        null
                    }
                }
                _quedadas.clear()
                _quedadas.addAll(todos)
                Log.i(Constantes.TAG, "Quedadas recuperadas con exito")
                _isLoading.value = false
            }
            .addOnFailureListener { error ->
                Log.e(Constantes.TAG, "Error al obtener las quedadas de firebase\n$error")
                _isLoading.value = false

            }
    }

    fun borrarQuedada(quedada: Quedada) {
        _isLoading.value = true
        db.collection(Colecciones.quedadas)
            .document(quedada.nombre)
            .delete()
            .addOnSuccessListener {
                _isLoading.value = false
                Log.i(Constantes.TAG, "quedadaAdminVW: QUEDADA ${quedada.nombre} BORRADA")
                getQuedadas()
            }
            .addOnFailureListener { error ->
                _isLoading.value = false
                Log.e(
                    Constantes.TAG,
                    "quedadaAdminVW: QUEDADA ${quedada.nombre}, ERROR AL BORRAR\n$error"
                )

            }
    }

    fun addLoc(loc: LatLng) {
        Log.i(Constantes.TAG, "AÑADIDA LOCALIZACION: ")
        _locNuevaQuedada.value = loc
    }


    fun crearQuedada(fecha: String, nombre: String, context: Context) {
        _isLoading.value = true

        val docRef = db.collection(Colecciones.quedadas).document(nombre)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {

                    Toast.makeText(
                        context,
                        "Ya existe un evento con ese nombre",
                        Toast.LENGTH_SHORT
                    ).show()

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
                            Log.i(Constantes.TAG, "Quedada creada correctamente")
                            _isLoading.value = false
                            Toast.makeText(
                                context,
                                "Quedada creada correctamente",
                                Toast.LENGTH_SHORT
                            ).show()

                        }

                        .addOnFailureListener {
                            Log.e(Constantes.TAG, "Error al crear la quedada")
                            _isLoading.value = false
                            Toast.makeText(context, "Error al crear la quedada", Toast.LENGTH_SHORT)
                                .show()

                        }
                }
            }
            .addOnFailureListener {
                Log.e(Constantes.TAG, "Error al obtener el documento")
            }


    }

    fun updateQuedada(nombreInicial: String, usuariosElegidos: List<UserQuedada>) {

        val correos = arrayListOf<String>()
        for (user in usuariosElegidos) {
            correos.add(user.correo)
        }

        _isLoading.value = true
        val nuevaQuedada = mapOf(
            "ubicacion" to _quedadaSelecc.value.ubicacion,
            "nombre" to _quedadaSelecc.value.nombre,
            "fechaEvento" to _quedadaSelecc.value.fecha,
            "inscripcionAbierta" to true,
            "usuarios" to correos.toList()
        )

        if (nombreInicial != _quedadaSelecc.value.nombre) {
            db.collection(Colecciones.quedadas)
                .document(nombreInicial)
                .delete()
                .addOnSuccessListener {
                    db.collection(Colecciones.quedadas)
                        .document(_quedadaSelecc.value.nombre)
                        .set(nuevaQuedada)
                        .addOnSuccessListener {
                            _quedadaModificada.value = true
                            Log.i(
                                Constantes.TAG,
                                "Quedada $nombreInicial modificada a ${_quedadaSelecc.value.nombre} con exito"
                            )
                            _isLoading.value = false
                        }
                        .addOnFailureListener { error ->
                            _isLoading.value = false
                            Log.e(
                                Constantes.TAG,
                                "Error al modificar quedada con nuevo nombre ${_quedadaSelecc.value.nombre}\n$error"
                            )
                        }
                }
                .addOnFailureListener { error ->
                    _isLoading.value = false
                    Log.e(Constantes.TAG, "Error al borrar la quedada $nombreInicial\n$error")
                }
        } else {
            db.collection(Colecciones.quedadas)
                .document(nombreInicial)
                .update(nuevaQuedada)
                .addOnSuccessListener {
                    _isLoading.value = false
                    _quedadaModificada.value = true
                    Log.i(Constantes.TAG, "Quedada $nombreInicial modificada con éxito")
                }
                .addOnFailureListener { error ->
                    _isLoading.value = false
                    Log.e(Constantes.TAG, "Error al modificar la quedada $nombreInicial\n$error")
                }
        }


    }

    fun setQuedadaCreada(value: Boolean) {
        _quedadaCreada.value = value
    }


    //SETTERS DE QUEDADA SELECCIONADA

    fun setQuedadaSelecc(quedada: Quedada) {
        _quedadaSelecc.value = quedada
    }

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