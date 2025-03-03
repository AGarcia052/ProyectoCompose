package com.example.proyectocompose.usuario.quedadas

import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import com.example.proyectocompose.administrador.quedadas.viewModels.MapsAdminQuedadaViewModel
import com.example.proyectocompose.administrador.quedadas.viewModels.QuedadasAdminViewModel
import com.example.proyectocompose.login.LoginViewModel
import com.example.proyectocompose.usuario.dashboard.DashboardViewModel
import com.example.proyectocompose.utils.Rutas
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Composable
fun AnunciarLlegada(
    viewModel: MapsAdminQuedadaViewModel,
    quedadaViewModel: QuedadasAdminViewModel,
    onDismissRequest: () -> Unit,
    navController: NavController,
    dashboardViewModel: DashboardViewModel
) {
    val context = LocalContext.current
    val quedadaSelecc by quedadaViewModel.quedadaSelecc.collectAsState()
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    val location = remember { mutableStateOf<Location?>(null) }
    val cameraPositionState = rememberCameraPositionState()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                    location.value = loc
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
    }

    LaunchedEffect(location.value) {
        location.value?.let { loc ->
            cameraPositionState.position = CameraPosition(LatLng(loc.latitude, loc.longitude), 15f, 45f, 90f)
        }
    }

    Dialog(
        onDismissRequest = { onDismissRequest() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                GoogleMap(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(
                        mapType = MapType.HYBRID,
                        isMyLocationEnabled = true
                    ),
                    uiSettings = MapUiSettings(
                        myLocationButtonEnabled = true,
                        tiltGesturesEnabled = true,
                        rotationGesturesEnabled = true
                    )
                ) {


                    if (quedadaSelecc.ubicacion.isNotEmpty()) {
                        val locQuedada = quedadaSelecc.ubicacion.split(",")
                        val quedadaLatLng = LatLng(locQuedada[0].toDouble(), locQuedada[1].toDouble())

                        Marker(
                            state = MarkerState(position = quedadaLatLng),
                            title = "Ubicación de la quedada"
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(onClick = { onDismissRequest() }) {
                        Text(text = "Volver")
                    }

                    location.value?.let { loc ->
                        quedadaSelecc.ubicacion.takeIf { it.isNotEmpty() }?.split(",")?.let { locQuedada ->
                            val quedadaLatLng = LatLng(locQuedada[0].toDouble(), locQuedada[1].toDouble())
                            val distancia = FloatArray(1)
                            Location.distanceBetween(
                                loc.latitude, loc.longitude,
                                quedadaLatLng.latitude, quedadaLatLng.longitude,
                                distancia
                            )

                            if (distancia[0] < 20) {
                                Button(onClick = {
                                    val horaActual = LocalTime.now()
                                    val formato = DateTimeFormatter.ofPattern("HH:mm:ss")
                                    val horaFormateada = horaActual.format(formato)
                                    viewModel.anunciarLlegada(
                                        "${loc.latitude},${loc.longitude}",
                                        dashboardViewModel.getUsuario(),
                                        horaFormateada,
                                        quedadaSelecc
                                        ){
                                        onDismissRequest()
                                        quedadaViewModel.restart()
                                        navController.popBackStack(Rutas.quedadasUsuario, inclusive = false)
                                    }
                                }) {
                                    Text(text = "Anunciar llegada")
                                }
                            }else{
                                Text(text = "No estás cerca de la quedada")
                            }
                        }
                    }
                }
            }
        }
    }
}
