package com.dsm.munaytripandroid.feature.home.presentation

import android.Manifest
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dsm.munaytripandroid.core.location.LocationManager
import com.dsm.munaytripandroid.feature.client.data.repository.ClientRepository
import com.dsm.munaytripandroid.feature.client.domain.model.ClientLocation
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException

private val MunayPrimary = Color(0xFF1A7FA6)
private val MunaySecondary = Color(0xFF4DB6E8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientHomeScreen(
    auth: FirebaseAuth,
    onNavigateToProfile: () -> Unit,
    onNavigateToOffersList: () -> Unit,
    onNavigateToOfferDetail: (String) -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val currentUser = auth.currentUser
    val displayName = currentUser?.displayName ?: "Viajero"

    var showLogoutDialog by remember { mutableStateOf(false) }
    var clientLocation by remember { mutableStateOf<ClientLocation?>(null) }
    var isLoadingLocation by remember { mutableStateOf(false) }

    val locationManager = remember { LocationManager(context) }
    val clientRepository = remember { ClientRepository() }
    val scope = rememberCoroutineScope()

    // Estado para búsqueda
    var searchQuery by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    val geocoder = remember { android.location.Geocoder(context) }

    // NUEVO: Variable para controlar si debemos mover el mapa
    var shouldAnimateMap by remember { mutableStateOf(false) }

    // Launcher para pedir permisos
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        if (granted && currentUser != null) {
            scope.launch {
                isLoadingLocation = true
                val location = locationManager.getCurrentLocation()
                if (location != null) {
                    clientRepository.updateClientLocation(currentUser.uid, location)
                    clientLocation = location
                    shouldAnimateMap = true // Activar animación
                }
                isLoadingLocation = false
            }
        }
    }

    // Cargar ubicación guardada al iniciar (SOLO UNA VEZ)
    LaunchedEffect(currentUser?.uid) {
        currentUser?.let { user ->
            val savedLocation = clientRepository.getClientLocation(user.uid)
            clientLocation = savedLocation

            if (savedLocation == null && locationManager.hasLocationPermission()) {
                isLoadingLocation = true
                val location = locationManager.getCurrentLocation()
                if (location != null) {
                    clientRepository.updateClientLocation(user.uid, location)
                    clientLocation = location
                    shouldAnimateMap = true
                }
                isLoadingLocation = false
            }
        }
    }

    // ✅ FIX 1: Autocomplete con debounce SIN interferir con el input
    LaunchedEffect(searchQuery) {
        if (searchQuery.length > 2) {
            delay(500) // Debounce de 500ms
            try {
                val client = Places.createClient(context)
                val request = FindAutocompletePredictionsRequest.builder()
                    .setQuery(searchQuery)
                    .build()
                client.findAutocompletePredictions(request)
                    .addOnSuccessListener { response ->
                        suggestions = response.autocompletePredictions
                            .map { it.getFullText(null).toString() }
                    }
                    .addOnFailureListener {
                        suggestions = emptyList()
                    }
            } catch (e: Exception) {
                Log.e("Autocomplete", "Error: ${e.message}")
                suggestions = emptyList()
            }
        } else {
            suggestions = emptyList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Munay Trip",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tu compañero de viajes",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Perfil",
                            tint = MunayPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.Home, "Inicio") },
                    label = { Text("Inicio") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MunayPrimary,
                        selectedTextColor = MunayPrimary,
                        indicatorColor = MunayPrimary.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToOffersList,
                    icon = { Icon(Icons.Default.Search, "Explorar") },
                    label = { Text("Explorar") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { },
                    icon = { Icon(Icons.Default.BookmarkBorder, "Guardados") },
                    label = { Text("Guardados") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToProfile,
                    icon = { Icon(Icons.Default.Person, "Perfil") },
                    label = { Text("Perfil") }
                )
            }
        },
        containerColor = Color(0xFFF5F9FB)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ✅ FIX 2: Campo de búsqueda sin animaciones que interfieran
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar ubicación...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            Row {
                                // Botón para limpiar
                                IconButton(onClick = {
                                    searchQuery = ""
                                    suggestions = emptyList()
                                }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                                }
                                // Botón para buscar
                                IconButton(onClick = {
                                    if (searchQuery.isNotBlank()) {
                                        scope.launch {
                                            try {
                                                isLoadingLocation = true
                                                val results = geocoder.getFromLocationName(searchQuery, 1)
                                                if (!results.isNullOrEmpty()) {
                                                    val location = results.first()
                                                    clientLocation = ClientLocation(
                                                        location.latitude,
                                                        location.longitude,
                                                        location.locality ?: searchQuery,
                                                        location.countryName ?: "Desconocido"
                                                    )

                                                    // Actualizar en Firebase
                                                    currentUser?.let {
                                                        clientRepository.updateClientLocation(
                                                            it.uid,
                                                            clientLocation!!
                                                        )
                                                    }

                                                    // ✅ ACTIVAR animación del mapa
                                                    shouldAnimateMap = true
                                                    suggestions = emptyList()
                                                }
                                            } catch (e: IOException) {
                                                Log.e("Geocoder", "Error: ${e.message}")
                                            } finally {
                                                isLoadingLocation = false
                                            }
                                        }
                                    }
                                }) {
                                    Icon(Icons.Default.Send, contentDescription = "Buscar")
                                }
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                )
            }

            // ✅ FIX 3: Dropdown de sugerencias sin interferir
            if (suggestions.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column {
                        suggestions.take(5).forEach { suggestion ->
                            TextButton(
                                onClick = {
                                    searchQuery = suggestion
                                    suggestions = emptyList()
                                    // Buscar automáticamente al seleccionar
                                    scope.launch {
                                        try {
                                            isLoadingLocation = true
                                            val results = geocoder.getFromLocationName(suggestion, 1)
                                            if (!results.isNullOrEmpty()) {
                                                val location = results.first()
                                                clientLocation = ClientLocation(
                                                    location.latitude,
                                                    location.longitude,
                                                    location.locality ?: suggestion,
                                                    location.countryName ?: "Desconocido"
                                                )
                                                currentUser?.let {
                                                    clientRepository.updateClientLocation(
                                                        it.uid,
                                                        clientLocation!!
                                                    )
                                                }
                                                shouldAnimateMap = true
                                            }
                                        } catch (e: IOException) {
                                            Log.e("Geocoder", "Error: ${e.message}")
                                        } finally {
                                            isLoadingLocation = false
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = MunayPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = suggestion,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                            if (suggestion != suggestions.last()) {
                                Divider()
                            }
                        }
                    }
                }
            }

            // Welcome Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(MunayPrimary, MunaySecondary)
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column {
                        Text(
                            text = "¡Hola, $displayName! 👋",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "¿Listo para tu próxima aventura?",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            // Mapa
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(
                            color = if (clientLocation != null) MunayPrimary.copy(alpha = 0.1f)
                            else Color(0xFFF5F5F5),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isLoadingLocation -> {
                            CircularProgressIndicator(color = MunayPrimary)
                        }
                        clientLocation != null -> {
                            // ✅ FIX 4: Pasar la bandera de animación
                            UserLocationMap(
                                lat = clientLocation!!.lat,
                                lng = clientLocation!!.lng,
                                city = clientLocation!!.ciudad,
                                country = clientLocation!!.pais,
                                shouldAnimate = shouldAnimateMap,
                                onAnimationComplete = { shouldAnimateMap = false },
                                onRequestCurrentLocation = {
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            )
                        }
                        else -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "📍",
                                    style = MaterialTheme.typography.displayMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Ubicación no disponible",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(
                                    onClick = {
                                        locationPermissionLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION
                                            )
                                        )
                                    }
                                ) {
                                    Icon(Icons.Default.MyLocation, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Activar ubicación")
                                }
                            }
                        }
                    }
                }
            }

            // Resto del código sin cambios...
            Button(
                onClick = onNavigateToOffersList,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MunayPrimary
                )
            ) {
                Icon(Icons.Default.Explore, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ver todas las ofertas", fontWeight = FontWeight.Bold)
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🎯 Rol: Cliente",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Explora ofertas turísticas, reserva experiencias y guarda tus favoritos.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "FASE 2 - Sistema de Ofertas Completo ✅",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }

    // Logout Dialog (sin cambios)
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = null,
                    tint = MunayPrimary,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = { Text("Cerrar sesión", fontWeight = FontWeight.Bold) },
            text = { Text("¿Estás seguro de que deseas cerrar sesión, $displayName?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MunayPrimary)
                ) {
                    Text("Cerrar sesión")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar", color = MunayPrimary)
                }
            }
        )
    }
}

// ✅ FIX 5: Componente de mapa corregido
@Composable
fun UserLocationMap(
    lat: Double,
    lng: Double,
    city: String,
    country: String,
    shouldAnimate: Boolean,
    onAnimationComplete: () -> Unit,
    onRequestCurrentLocation: () -> Unit
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(lat, lng), 13f)
    }

    // ✅ SOLO animar cuando shouldAnimate sea true
    LaunchedEffect(shouldAnimate) {
        if (shouldAnimate) {
            cameraPositionState.animate(
                update = com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(
                    LatLng(lat, lng),
                    15f
                ),
                durationMs = 1000
            )
            onAnimationComplete()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$city, $country",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(16.dp))
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                Marker(
                    state = MarkerState(position = LatLng(lat, lng)),
                    title = city,
                    snippet = country
                )
            }

            FloatingActionButton(
                onClick = onRequestCurrentLocation,
                containerColor = MunayPrimary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Mi ubicación", tint = Color.White)
            }
        }
    }
}