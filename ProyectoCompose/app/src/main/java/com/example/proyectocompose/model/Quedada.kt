package com.example.proyectocompose.model

data class Quedada(
    val nombre: String = "",
    val correosUsr: List<String> = emptyList(),
    val fecha: String = "",
    val ubicacion: String = "",
    val inscripcion: Boolean = false
)
