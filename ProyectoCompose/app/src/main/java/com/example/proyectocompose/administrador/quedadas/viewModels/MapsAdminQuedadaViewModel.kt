package com.example.proyectocompose.administrador.quedadas.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectocompose.model.MapMarker
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
class MapsAdminQuedadaViewModel: ViewModel() {

    val home = LatLng(38.693245786259595, -4.108508457997148)
    private val _mark = MutableStateFlow<MapMarker?>(null)
    val mark: StateFlow<MapMarker?> = _mark

    //Si comentamos o descomentamos esto: opción a / opción b; vemos que se respeta el zoom y demás comportamientos actuales o se refresca todo a un zoom determinado.
    //Opcion a)
//    private val _cameraPosition = MutableStateFlow(CameraPosition.fromLatLngZoom(home, 17f)) // Posición inicial con zoom 17.
//    val cameraPosition: StateFlow<CameraPosition> = _cameraPosition
    //Opcion b)
    private val _cameraPosition = MutableStateFlow(
        CameraPosition.Builder()
            .target(home) //Coordenadas de la posición inicial
            .zoom(17f)    //Nivel de zoom inicial
            .tilt(45f)    //Inclinación inicial (en grados)
            .bearing(90f) //Orientación inicial (en grados, 0=norte, 90=este, etc.)
            .build()
    )
    val cameraPosition: StateFlow<CameraPosition> = _cameraPosition

    private val _selectedCoordinates = MutableStateFlow<LatLng?>(null)
    val selectedCoordinates: StateFlow<LatLng?> = _selectedCoordinates

    private val _longitude = MutableStateFlow("")
    val longitude: StateFlow<String> = _longitude

    private val _latitude = MutableStateFlow("")
    val latitude: StateFlow<String> = _latitude



    //Añade un amrcador.
    fun addMarker(latLng: LatLng, title: String = "Título del marcador", snippet: String = "Contenido del marcador") {
        viewModelScope.launch {
            _mark.value = MapMarker(position = latLng, title = title, snippet = snippet)
        }
    }

    //Borra un marcador.
    fun removeMarker() {
//        viewModelScope.launch { //Esto no lo pongo con corrutinas porque no sigo hasta que no se borre. Hace un efecto no deseado en caso contrario.
        _mark.value = null
//        }
    }

    /**
     * Va a home pero siempre a la misma distancia de zoomm: 17f.
     */
//    fun irAHome() {
//        _cameraPosition.value = CameraPosition.fromLatLngZoom(home, 17f)
//    }

    /**
     * Va a home pero mantiene la posición de la cámara sin cambiar el zoom / tilt? / bearing? actuales.
     */
    fun irAHome() {
        val currentPosition = _cameraPosition.value //Obtenemos el zoom actual.
//        _cameraPosition.value = CameraPosition.fromLatLngZoom(home, currentPosition.zoom) //Lo mantenemos en la ubicaciçon.
        _cameraPosition.value = CameraPosition.Builder()
            .target(home)
            .zoom(currentPosition.zoom)
            .tilt(currentPosition.tilt)
            .bearing(currentPosition.bearing)
            .build()
    }


    /**
     * Mantiene en zoom a 15 de distancia la posición de la cámara.
     */
//    fun updateCameraPosition(latLng: LatLng, zoom: Float = 15f) {
//        viewModelScope.launch {
//            _cameraPosition.value = CameraPosition.fromLatLngZoom(latLng, zoom)
//        }
//    }

    /**
     * Actualiza la posición de la cámara sin cambiar el zoom / tilt? / bearing?.
     */
    fun updateCameraPosition(latLng: LatLng, zoom: Float? = null, tilt: Float? = null, bearing: Float? = null) {
        viewModelScope.launch {
            val currentPosition = _cameraPosition.value
//            _cameraPosition.value = CameraPosition.fromLatLngZoom(latLng, zoom ?: currentPosition.zoom) //Mantenemos el zoom actual.
            _cameraPosition.value = CameraPosition.Builder()
                .target(latLng)
                .zoom(zoom ?: currentPosition.zoom)
                .tilt(tilt ?: currentPosition.tilt)
                .bearing(bearing ?: currentPosition.bearing)
                .build()
        }
    }


    //Este método actualiza dos flujos separados: _longitude y _latitude.
    fun updateCoordinates(latLng: LatLng) {
        viewModelScope.launch {
            _longitude.value = latLng.longitude.toString()
            _latitude.value = latLng.latitude.toString()
        }
    }

    //Este método actualiza el flujo _selectedCoordinates con un objeto LatLng completo
    fun selectCoordinates(latLng: LatLng) {
        viewModelScope.launch {
            _selectedCoordinates.value = latLng
        }
    }

}