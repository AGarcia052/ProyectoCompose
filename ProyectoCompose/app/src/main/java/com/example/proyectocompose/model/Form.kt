package com.example.proyectocompose.model

data class Form (
    val relacionSeria: Boolean = false,
    val deportes: Int = 50,
    val arte: Int = 50,
    val politica: Int = 50,
    val tieneHijos: Boolean = false,
    val quiereHijos: Boolean = false,
    val interesSexual: List<String> = emptyList()

)