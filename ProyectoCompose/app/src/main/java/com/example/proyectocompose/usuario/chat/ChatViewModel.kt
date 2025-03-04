package com.example.proyectocompose.usuario.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.proyectocompose.utils.Colecciones
import com.example.proyectocompose.utils.Constantes.TAG
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

    private var mensajesListener: ValueEventListener? = null

    fun observeMessages(usuario1: String, usuario2: String) {
        mensajesListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val mensajesList = mutableListOf<Mensaje>()

                for (mensajeSnapshot in snapshot.children) {
                    val mensaje = mensajeSnapshot.getValue(Mensaje::class.java)
                    if (mensaje != null && ((mensaje.sender == usuario1 && mensaje.reciever == usuario2) ||
                                (mensaje.sender == usuario2 && mensaje.reciever == usuario1))) {
                        mensajesList.add(mensaje)
                        if (mensaje.sender == usuario1 && mensaje.reciever == usuario2 && !mensaje.leido) {
                            leerMensaje(mensajeSnapshot.key!!)
                        }
                    }
                }

                mensajesList.sortByDescending { it.fechaYHora }
                _mensajes.value = mensajesList.toList()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error al observar mensajes: ${error.message}")
            }
        }

        databaseReference.orderByChild("sender").addValueEventListener(mensajesListener!!)
    }

    fun detenerObservadorMensajes() {
        mensajesListener?.let {
            databaseReference.removeEventListener(it)
            mensajesListener = null
            Log.d(TAG, "Observer de mensajes eliminado")
        }
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

    private fun leerMensaje(mensajeId: String) {
        Log.d(TAG, "Leyendo mensaje con ID: $mensajeId")
        databaseReference.child(mensajeId).child("leido").setValue(true)

    }


}