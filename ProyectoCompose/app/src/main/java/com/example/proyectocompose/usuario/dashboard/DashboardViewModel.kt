package com.example.proyectocompose.usuario.dashboard

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModel
import com.example.proyectocompose.MainActivity
import com.example.proyectocompose.R
import com.example.proyectocompose.utils.Colecciones
import com.example.proyectocompose.model.Formulario
import com.example.proyectocompose.model.Mensaje
import com.example.proyectocompose.model.SomeReceiver
import com.example.proyectocompose.model.User
import com.example.proyectocompose.utils.Constantes
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.sin

class DashboardViewModel:ViewModel() {
    val TAG = "AMIGOSAPP"


    private val db = Firebase.firestore
    private val databaseReference = FirebaseDatabase.getInstance().getReference(Colecciones.mensajes)

    init {
        cargarUsuariosConectados()
    }
    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> get()=_isLoading

    private val _numUsuariosConectados = MutableStateFlow<Int?>(null)
    val numUsuariosConectados: StateFlow<Int?> get()=_numUsuariosConectados

    private val _usuario = MutableStateFlow<User>(User())
    val usuario: StateFlow<User> get() = _usuario

    fun getUsuario():User{
        return _usuario.value
    }

    private val _msgObtenidos = MutableStateFlow<Boolean>(false)
    val msgObtenidos: StateFlow<Boolean> get()=_msgObtenidos

    private val _notificacionEnviada = MutableStateFlow<Boolean>(false)
    val notificacionEnviada: StateFlow<Boolean> get()= _notificacionEnviada

    var sendersYCant = Pair(0,0)

    /**
     * Realiza un count de cuantos usuarios tienen el atb. conectado a true en firestore.
     **/

    // todo(quizar usar count)
    fun cargarUsuariosConectados(){
        db.collection(Colecciones.usuarios)
            .whereEqualTo("conectado", true)
            .addSnapshotListener { snapshot,_ ->
                var numUsuarios = 0
                if (snapshot != null) {
                    numUsuarios = snapshot.size()
                }

                _numUsuariosConectados.value = numUsuarios
                Log.d(TAG, "Usuarios conectados: $_numUsuariosConectados")
            }
    }

    /**
     * Carga todos los datos del usuario, que se pasarán al resto de ventanas del usuario.
     * **/

    fun cargarUsuario(email:String){
        _isLoading.value = true
        db.collection(Colecciones.usuarios)
            .document(email)
            .get()
            .addOnSuccessListener { result ->
                val datos = result.data
                datos?.let {
                    _usuario.value.nombre = datos["nombre"] as String
                    _usuario.value.apellidos = datos["apellidos"] as String
                    _usuario.value.rol = datos["rol"] as String
                    _usuario.value.fecNac = datos["fecNac"] as String
                    _usuario.value.correo = datos["correo"] as String
                    _usuario.value.conectado = datos["conectado"] as Boolean
                    _usuario.value.activo = datos["activo"] as Boolean
                    _usuario.value.formCompletado = datos["formCompletado"] as Boolean
                    _usuario.value.usuariosConLike = datos["usuariosConLike"] as List<String>
                    _usuario.value.amigos = datos["amigos"] as List<String>
                    _usuario.value.descripcion = datos["descripcion"] as String
                    val formulario = datos["formulario"] as Map<*, *>
                    formulario.let{
                        _usuario.value.formulario = Formulario(

                            relacionSeria = it["relacionSeria"] as Boolean,
                            deportes = (it["deportes"] as Long).toInt(),
                            arte = (it["arte"] as Long).toInt(),
                            politica = (it["politica"] as Long).toInt(),
                            tieneHijos = it["tieneHijos"] as Boolean,
                            quiereHijos = it["quiereHijos"] as Boolean,
                            interesSexual = it["interesSexual"] as String
                        )
                    }
                }
                _isLoading.value = false
                Log.e(TAG,"DATOS USUARIO"+_usuario.value.toString())
            }
            .addOnFailureListener {
                Log.e(TAG,"ERROR AL OBTENER EL USUARIO")
                _isLoading.value = false
            }
    }

    /**
     *
     * Obtiene los mensajes no leídos del usuario.
     *
     * Hace un count de todos los sender distintos y del total de mensajes y los guarda en un Pair
     *
     * **/

    fun obtenerMensajesNoLeidos() {
        databaseReference
            .orderByChild("sender")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val sinLeer = mutableListOf<Mensaje>()

                    for (mensajeSnapshot in snapshot.children) {
                        val mensaje = mensajeSnapshot.getValue(Mensaje::class.java)

                        if ((mensaje != null) && (mensaje.reciever == _usuario.value.correo) && !mensaje.leido) {
                            sinLeer.add(mensaje)
                        }
                    }

                    val distintosSender = mutableListOf<String>()
                    val cantidad = sinLeer.count()
                    for (mensaje in sinLeer){

                        if (mensaje.sender !in distintosSender){
                            distintosSender.add(mensaje.sender)
                        }


                    }



                    sendersYCant = Pair(distintosSender.count(),cantidad)
                    Log.d(Constantes.TAG,"Sender: ${sendersYCant.first}, Total mensajes: ${sendersYCant.second}")

                    if(sendersYCant.second != 0){
                        _msgObtenidos.value = true
                        Log.i(Constantes.TAG,"_MSGObtenidos cambiado a TRUE")
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(Constantes.TAG, "Error al observar mensajes: ${error.message}")
                }
            })
    }


    /**
     * Envía una notificación infromando de los mensajes sin leer y marca como leído todos los mensajes
     * si se pulsa el icono de "Marcar como leído"
     *
     * **/

    fun sendNotification(context: Context) {
        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val mainPendingIntent = PendingIntent.getActivity(
            context,
            0,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val actionIntent = Intent(context, SomeReceiver::class.java).apply {
            putExtra("mensajes_leer", _usuario.value.correo)
        }
        val actionPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            actionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        val notification = NotificationCompat.Builder(context, MainActivity.CHANNEL_ID)
            .setContentTitle("AMIGOS APP")
            .setContentText("Tienes ${sendersYCant.second} mensajes sin leer de ${sendersYCant.first} chats")
            .setSmallIcon(R.drawable.ic_chat)
            //.setContentIntent(mainPendingIntent) // esto abre la app, problema si estas en dashboard
            .addAction(
                R.drawable.ic_double_check,
                "Marcar como leído",
                actionPendingIntent
            )
            .setAutoCancel(true)
            .build()

        notificationManager.notify("AMIGOS APP".hashCode(), notification)
        _notificacionEnviada.value = true
    }


    fun setMsgObtenidos(value: Boolean){
        _msgObtenidos.value = value
    }
    fun setNotificacionEnviada(value:Boolean){
        _notificacionEnviada.value = value
    }

    fun setUsuarioLike(correo:String){
        _usuario.value.usuariosConLike += correo
    }

    fun setUsuario(usuario:User){
        _usuario.value = usuario
    }


}