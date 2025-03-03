package com.example.proyectocompose.administrador.quedadas.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectocompose.model.Llegada
import com.example.proyectocompose.model.MapMarker
import com.example.proyectocompose.model.Quedada
import com.example.proyectocompose.model.User
import com.example.proyectocompose.utils.Colecciones
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
class MapsAdminQuedadaViewModel: ViewModel() {
    val db = Firebase.firestore

    private val home = LatLng(40.416775, -3.703790)
    private val _mark = MutableStateFlow<MapMarker?>(null)
    val mark: StateFlow<MapMarker?> = _mark


    private val _cameraPosition = MutableStateFlow(
        CameraPosition.Builder()
            .target(home)
            .zoom(17f)
            .tilt(45f)
            .bearing(90f)
            .build()
    )
    val cameraPosition: StateFlow<CameraPosition> = _cameraPosition



    private val _selectedCoordinates = MutableStateFlow<LatLng?>(null)
    val selectedCoordinates: StateFlow<LatLng?> = _selectedCoordinates

    private val _longitude = MutableStateFlow("")
    val longitude: StateFlow<String> = _longitude

    private val _latitude = MutableStateFlow("")
    val latitude: StateFlow<String> = _latitude



    fun addMarker(latLng: LatLng, title: String = "Título del marcador", snippet: String = "Contenido del marcador") {
        viewModelScope.launch {
            _mark.value = MapMarker(position = latLng, title = title, snippet = snippet)
        }
    }

    fun getMarker(): LatLng {
        return _mark.value?.position ?: LatLng(0.0,0.0)
    }

    fun removeMarker() {
        _mark.value = null

    }


    fun irAHome() {
        val currentPosition = _cameraPosition.value
        _cameraPosition.value = CameraPosition.Builder()
            .target(home)
            .zoom(currentPosition.zoom)
            .tilt(currentPosition.tilt)
            .bearing(currentPosition.bearing)
            .build()
    }



    fun updateCameraPosition(latLng: LatLng, zoom: Float? = null, tilt: Float? = null, bearing: Float? = null) {
        viewModelScope.launch {
            val currentPosition = _cameraPosition.value
            _cameraPosition.value = CameraPosition.Builder()
                .target(latLng)
                .zoom(zoom ?: currentPosition.zoom)
                .tilt(tilt ?: currentPosition.tilt)
                .bearing(bearing ?: currentPosition.bearing)
                .build()
        }
    }


    fun updateCoordinates(latLng: LatLng) {
        viewModelScope.launch {
            _longitude.value = latLng.longitude.toString()
            _latitude.value = latLng.latitude.toString()
        }
    }

    fun selectCoordinates(latLng: LatLng) {
        viewModelScope.launch {
            _selectedCoordinates.value = latLng
        }
    }

    fun anunciarLlegada(ubicacion: String, usuario: User, horaLlegada: String, quedada: Quedada, volver :() -> Unit) {
        var llegadas = quedada.llegadas.toMutableList()
        var llegada = Llegada(usuario.correo, usuario.nombre, horaLlegada, ubicacion)
        llegadas.add(llegada)
        db.collection(Colecciones.quedadas).document(quedada.nombre).update("llegadas", llegadas)
            .addOnSuccessListener {
                volver()
                Log.i("Llegada", "Llegada registrada correctamente")
            }
            .addOnFailureListener { error ->
                Log.e("Llegada", "Error al registrar la llegada:\n$error")
            }
    }

}