package com.example.proyectocompose.model

data class User(
    val correo: String = "",
    val nombre: String = "",
    val apellidos: String = "",
    val fecNac: String = "",
    val formCompletado: Boolean = false,
    val activado: Boolean = false
)
