package com.example.proyectocompose.model

data class User(
    val correo: String = "",
    val nombre: String = "",
    val apellidos: String = "",
    val fecNac: String = "",
    val formHecho: Boolean = false,
    val rol: String = "",
    val activo: Boolean = false,
    val conectado: Boolean = false

)