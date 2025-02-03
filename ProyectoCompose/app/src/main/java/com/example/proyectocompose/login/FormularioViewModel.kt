package com.example.proyectocompose.login

import androidx.lifecycle.ViewModel
import com.example.proyectocompose.model.Form
import com.example.proyectocompose.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FormularioViewModel: ViewModel(){

    val terms = MutableStateFlow(false)
    //USUARIO
    val nombre = MutableStateFlow("")
    val apellidos = MutableStateFlow("")
    val fecNac = MutableStateFlow("")
    val email = MutableStateFlow("")
    //FORMULARIO
    val relacionSeria = MutableStateFlow(false)
    val deportes = MutableStateFlow(50)
    val arte = MutableStateFlow(50)
    val politica = MutableStateFlow(50)
    val tieneHijos = MutableStateFlow(false)
    val quiereHijos = MutableStateFlow(false)
    val interesSexual = MutableStateFlow("")

    private val _formulario = MutableStateFlow<Form>(Form())
    val formulario: StateFlow<Form> get() = _formulario




}