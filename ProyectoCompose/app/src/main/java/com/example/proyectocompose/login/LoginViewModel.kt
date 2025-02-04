package com.example.proyectocompose.login

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.proyectocompose.Colecciones
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LoginViewModel: ViewModel() {

    private val _currentEmail = MutableStateFlow<String>("")
    val currentEmail: StateFlow<String> get()=_currentEmail

    fun getCurrentEmail(): String{
        return currentEmail.value
    }
}