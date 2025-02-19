package com.example.proyectocompose.usuario.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.proyectocompose.Colecciones
import com.example.proyectocompose.Constantes.TAG
import com.example.proyectocompose.model.Amigo
import com.example.proyectocompose.model.Mensaje
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ChatViewModel: ViewModel() {
    private val _mensajes = MutableStateFlow<List<Mensaje>>(emptyList())
    val mensajes: StateFlow<List<Mensaje>> = _mensajes

    private val databaseReference = FirebaseDatabase.getInstance().getReference(Colecciones.mensajes)

    fun observeMessages(usuario1: String, usuario2: String) {
        val mensajesList = mutableListOf<Mensaje>()
        var consultasCompletadas = 0

        databaseReference.orderByChild("sender").equalTo(usuario1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (mensajeSnapshot in snapshot.children) {
                        val mensaje = mensajeSnapshot.getValue(Mensaje::class.java)
                        mensaje?.leido = true
                        if (mensaje?.reciever == usuario2) {
                            mensajesList.add(mensaje)
                        }
                    }
                    consultasCompletadas++
                    if (consultasCompletadas == 2) {
                        mensajesList.sortBy { it.fechaYHora }
                        _mensajes.value = mensajesList
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Error en consulta 1: ${error.message}")
                }
            })
        databaseReference.orderByChild("sender").equalTo(usuario2)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (mensajeSnapshot in snapshot.children) {
                        val mensaje = mensajeSnapshot.getValue(Mensaje::class.java)
                        if (mensaje?.reciever == usuario1) {
                            mensajesList.add(mensaje)
                        }
                    }
                    consultasCompletadas++
                    if (consultasCompletadas == 2) {
                        mensajesList.sortBy { it.fechaYHora }
                        _mensajes.value = mensajesList
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Error en consulta 2: ${error.message}")
                }
            })
    }

    fun sendMessage(sender: String, receiver: String, text: String) {
        val newMessageId = databaseReference.push().key
        if (newMessageId != null) {
            val mens = Mensaje(sender = sender, reciever = receiver, mensaje = text)
            databaseReference.child(newMessageId).setValue(mens)
                .addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.e(TAG, "Error al enviar el mensaje", task.exception)
                    }
                }
        }
    }

}