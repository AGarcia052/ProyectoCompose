package com.example.proyectocompose.administrador.quedadas.componentes

import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.ActivityCompat
import com.example.proyectocompose.administrador.quedadas.viewModels.MapsAdminQuedadaViewModel
import com.example.proyectocompose.administrador.quedadas.viewModels.QuedadasAdminViewModel
import com.example.proyectocompose.utils.toCustomString
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerInfoWindow
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun SeleccionarUbicacion(
    viewModel: MapsAdminQuedadaViewModel,
    quedadaViewModel: QuedadasAdminViewModel,
    isSeleccionar: Boolean = true,
    onDismissRequest: (String) -> Unit
) {
    val context = LocalContext.current
    val TAG = "AMIGOSAPP"
    val mark by viewModel.mark.collectAsState()
    val cameraPosition by viewModel.cameraPosition.collectAsState()
    val selectedCoordinates by viewModel.selectedCoordinates.collectAsState()
    val localicacion by quedadaViewModel.locNuevaQuedada.collectAsState()
    val locationPermissionGranted = remember { mutableStateOf(false) }
    val quedadaSelecc by quedadaViewModel.quedadaSelecc.collectAsState()
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    val location = remember { mutableStateOf<Location?>(null) }
    val cameraPositionState = rememberCameraPositionState { position = cameraPosition }
    val salir = remember { mutableStateOf(false) }
    val mapProperties = remember {
        MapProperties(
            mapType = MapType.HYBRID,
            isMyLocationEnabled = locationPermissionGranted.value
        )
    }

    LaunchedEffect(quedadaSelecc) {
        if(quedadaSelecc.ubicacion.isNotEmpty()){
            val locaz = quedadaSelecc.ubicacion.split(",")
            viewModel.addMarker(LatLng(locaz[0].toDouble(),locaz[1].toDouble()))
        }

    }




    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        locationPermissionGranted.value = granted
    }


    if(quedadaSelecc.ubicacion.isEmpty() && mark == null){
        LaunchedEffect(locationPermissionGranted.value) {
            if (!locationPermissionGranted.value) {
                locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
            } else {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                        location.value = loc
                        Log.d(TAG, "Location: ${loc?.latitude}, ${loc?.longitude}")
                    }
                }
            }
        }
    }

    LaunchedEffect(mark) {
        if (mark != null) {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newCameraPosition(
                    CameraPosition.Builder().target(mark!!.position).zoom(15f).tilt(45f).bearing(90f).build()
                ),
                durationMs = 400
            )
        }
    }


    LaunchedEffect(cameraPosition) {
        cameraPositionState.animate(
            update = CameraUpdateFactory.newCameraPosition(cameraPosition),
            durationMs = 400
        )
    }

    LaunchedEffect(location.value) {
        location.value?.let { loc ->
            cameraPositionState.position = CameraPosition(
                LatLng(loc.latitude, loc.longitude),
                15f,
                45f,
                90f
            )
        }
    }



    Dialog(
        onDismissRequest = { onDismissRequest("") },
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
                    properties = mapProperties,
                    uiSettings = MapUiSettings(
                        myLocationButtonEnabled = true,
                        tiltGesturesEnabled = true,
                        rotationGesturesEnabled = true
                    ),
                    onMapLongClick = { latLng ->
                        if (isSeleccionar) {
                            viewModel.addMarker(latLng)
                            viewModel.selectCoordinates(latLng)
                            viewModel.updateCoordinates(latLng)
                        }
                    },
                    onPOIClick = { poi ->
                        Toast.makeText(context, "POI: ${poi.name}", Toast.LENGTH_SHORT).show()
                        if (isSeleccionar) {
                            viewModel.selectCoordinates(poi.latLng)
                            viewModel.updateCoordinates(poi.latLng)
                        }
                    },
                    onMapClick = { latLng ->
                        if (isSeleccionar) {
                            viewModel.selectCoordinates(latLng)
                            viewModel.updateCoordinates(latLng)
                            viewModel.updateCameraPosition(
                                latLng,
                                cameraPositionState.position.zoom,
                                cameraPositionState.position.tilt,
                                cameraPositionState.position.bearing
                            )
                        }
                    },
                    onMyLocationButtonClick = {
                        viewModel.irAHome()
                        Toast.makeText(context, "Volviendo a casa", Toast.LENGTH_SHORT).show()
                        true
                    },
                    onMapLoaded = {
                        Log.d(TAG, "onMapLoaded")
                    },
                    onMyLocationClick = {
                        viewModel.updateCoordinates(LatLng(it.latitude, it.longitude))
                        Toast.makeText(context, "Estoy aquí", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    mark?.let {
                        MarkerInfoWindow(
                            state = MarkerState(position = it.position),
                            title = it.title,
                            snippet = it.snippet,
                            onInfoWindowClick = {
                                viewModel.removeMarker()
                                Toast.makeText(context, "Marcador eliminado", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    location.value?.let {
                        Circle(
                            center = LatLng(it.latitude, it.longitude),
                            radius = 70.0,
                            strokeColor = Color.Blue,
                            fillColor = Color.Blue.copy(alpha = 0.4f)
                        )
                    }
                }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        if (isSeleccionar) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedTextField(
                                value = selectedCoordinates?.latitude?.toString().orEmpty(),
                                onValueChange = {},
                                label = { Text("Latitude") },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                            )
                            OutlinedTextField(
                                value = selectedCoordinates?.longitude?.toString().orEmpty(),
                                onValueChange = {},
                                label = { Text("Longitude") },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Poner un marcador para aceptar")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = {
                                    mark?.let {
                                        quedadaViewModel.addLoc(it.position)
                                        Log.d(TAG, "Ubicación seleccionada: ${it.position}")
                                        salir.value = true
                                    } ?: Log.e(TAG, "No hay marcador seleccionado")
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                            ) {
                                Text(text = "Aceptar")
                            }
                            Button(
                                onClick = { onDismissRequest("") },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 8.dp)
                            ) {
                                Text(text = "Cancelar")
                            }
                        }
                    }else{
                        Row(modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center){
                            Button(onClick = { onDismissRequest("") }) { Text(text="Volver") }
                        }
                        }
                }
            }
        }
    }

    if (localicacion != null && salir.value) {
        viewModel.removeMarker()
        salir.value = false
        onDismissRequest(mark!!.position.toCustomString())
    }
}

