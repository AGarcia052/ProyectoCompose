package com.example.proyectocompose.utils

import com.google.android.gms.maps.model.LatLng

fun LatLng.toCustomString(): String {
    return "${this.latitude},${this.longitude}"
}