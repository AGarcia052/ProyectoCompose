package com.example.proyectocompose.model

import com.google.android.gms.maps.model.LatLng

data class MapMarker(
    val position: LatLng,
    val title: String,
    val snippet: String? = null
)