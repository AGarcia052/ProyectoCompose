package com.example.proyectocompose.model
data class Amigo(
    var correo: String = "",
    var nombre: String = "",
    var apellidos: String = "",
    var conectado: Boolean = false,
    var foto: String = "",
    var mensajesSinLeer: Int = 0
)