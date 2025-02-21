package com.example.proyectocompose.model

data class User(
    var correo: String = "",
    var nombre: String = "",
    var apellidos: String = "",
    var fecNac: String = "",
    var formCompletado: Boolean = false,
    var activo: Boolean = false,
    var conectado: Boolean = false,
    var rol: String = "",
    var formulario: Formulario? = null, //cambiar a vacio
    var amigos: List<String> = emptyList(),
    var usuariosConLike: List<String> = emptyList()
)



