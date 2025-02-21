package com.example.proyectocompose.utils

import com.google.android.gms.maps.model.LatLng
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

fun LatLng.toCustomString(): String {
    return "${this.latitude},${this.longitude}"
}

fun calcularEdad(fechaNacimiento: String): Int {
    val formatter = DateTimeFormatter.ofPattern("d/M/yyyy")
    val fechaNac = LocalDate.parse(fechaNacimiento, formatter)
    val fechaActual = LocalDate.now()

    return Period.between(fechaNac, fechaActual).years
}
