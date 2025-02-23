package com.example.proyectocompose.model

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.proyectocompose.utils.Colecciones
import com.example.proyectocompose.utils.Constantes
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


/**
 *
 * Obtiene desde el intent de DashboardVW el correo del usuario.
 *
 * Cambia en todos los mensajes del usuario el atb. leido a true.
 *
 * **/
class SomeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        val databaseReference = FirebaseDatabase.getInstance().getReference("mensajes")

        val correoUsr = intent.getStringExtra("mensajes_leer")

        if (correoUsr != null) {
            databaseReference
                .orderByChild("reciever")
                .equalTo(correoUsr)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (messageSnapshot in snapshot.children) {
                            messageSnapshot.ref.child("leido").setValue(true)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e(Constantes.TAG, "SOMERECEIVER: Error al actualizar mensajes: ${error.message}")
                    }
                })
        } else {
            Log.e(Constantes.TAG, "SOMERECEIVER: El correo del usuario es nulo")
        }
    }
}