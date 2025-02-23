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

}