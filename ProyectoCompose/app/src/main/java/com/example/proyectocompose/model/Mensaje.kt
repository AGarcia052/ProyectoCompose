package com.example.proyectocompose.model

class Mensaje (
    var sender: String = "",
    var reciever: String = "",
    var mensaje: String = "",
    var leido: Boolean = false,
    var fechaYHora: Long = System.currentTimeMillis()
)
