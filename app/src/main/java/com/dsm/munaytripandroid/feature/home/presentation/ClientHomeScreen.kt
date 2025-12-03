package com.dsm.munaytripandroid.feature.home.presentation

import android.Manifest
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.Card
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.dsm.munaytripandroid.core.location.LocationManager
import com.dsm.munaytripandroid.feature.client.data.repository.ClientRepository
import com.dsm.munaytripandroid.feature.client.domain.model.ClientLocation
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import androidx.lifecycle.viewmodel.compose.viewModel // Import necesario
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.storage
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException
import kotlin.math.*

private val MunayPrimary = Color(0xFF1A7FA6)
private val MunaySecondary = Color(0xFF4DB6E8)
private val MunayAccent = Color(0xFFFF6B6B)
private val MunaySuccess = Color(0xFF4ECDC4)

// Modelo temporal para ofertas (reemplazar con tu modelo real)
data class OfferPreview(
    val id: String,
    val title: String,
    val provider: String,
    val price: Double,
    val rating: Float,
    val distance: Double, // en km
    val category: String,
    val imageUrl: String?,
    val lat: Double,
    val lng: Double,
    val descuento: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientHomeScreen(
    homeViewModel: ClientHomeViewModel = viewModel(),
    auth: FirebaseAuth,
    onNavigateToProfile: () -> Unit,
    onNavigateToOffersList: () -> Unit,
    onNavigateToOfferDetail: (String) -> Unit,
    onNavigateToFoots: () -> Unit,
    onNavigateToBookings: () -> Unit,
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

    // Estados de búsqueda
    var searchQuery by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    val geocoder = remember { android.location.Geocoder(context) }
    var shouldAnimateMap by remember { mutableStateOf(false) }

    // Estados de filtros
    var selectedDistance by remember { mutableStateOf(5.0) } // km
    var selectedCategory by remember { mutableStateOf("Todas") }
    var showFilters by remember { mutableStateOf(false) }

    val allOffers by homeViewModel.promotions.collectAsState()
    val isLoadingOffers by homeViewModel.isLoading.collectAsState()
    // MOCK DATA - Reemplazar con datos reales de Firebase

    val filteredOffers = remember(allOffers, clientLocation, selectedDistance, selectedCategory) {

        // 2. SOLUCIÓN AL SMART CAST
        // Copiamos el valor a una variable local estable (val)
        val currentLocation = clientLocation

        // Usamos la variable local para el chequeo
        if (currentLocation == null) {
            emptyList() // No mostrar nada si no hay ubicación
        } else {
            // Usamos 'allOffers' como fuente
            allOffers.map { offer ->
                val distance = calculateDistance(
                    // Usamos la variable local (currentLocation) que SÍ es no-nula
                    lat1 = currentLocation.lat,
                    lon1 = currentLocation.lng,
                    lat2 = offer.lat,
                    lon2 = offer.lng
                )
                offer.copy(distance = distance) // Actualizamos la distancia
            }.filter { offer ->
// ... (resto del filtro)
// ... (resto de la lógica de filteredOffers)
                val distanceMatch = offer.distance <= selectedDistance
                val categoryMatch = selectedCategory == "Todas" || offer.category == selectedCategory
                distanceMatch && categoryMatch
            }.sortedBy { offer ->
                offer.distance // Ordenamos por la distancia calculada
            }
        }
    }

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
                    shouldAnimateMap = true
                }
                isLoadingLocation = false
            }
        }
    }

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

    LaunchedEffect(searchQuery) {
        if (searchQuery.length > 2) {
            delay(500)
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
                    .addOnFailureListener { suggestions = emptyList() }
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(MunayPrimary, MunaySecondary)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Explore,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                        Column {
                            Text(
                                text = "Munay Trip",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = clientLocation?.ciudad ?: "Ubicación desconocida",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
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
                    onClick = onNavigateToFoots,
                    icon = { Icon(Icons.Default.CardGiftcard, "Ruleta con premios") },
                    label = { Text("Premios") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MunayPrimary,
                        selectedTextColor = MunayPrimary,
                        indicatorColor = MunayPrimary.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToBookings,
                    icon = { Icon(Icons.Default.BookmarkBorder, "Mis Reservas") },
                    label = { Text("Reservas") }
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            // Barra de búsqueda mejorada
            item {
                SearchBarSection(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onClear = {
                        searchQuery = ""
                        suggestions = emptyList()
                    },
                    onSearch = {
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
                                        currentUser?.let {
                                            clientRepository.updateClientLocation(it.uid, clientLocation!!)
                                        }
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
                    }
                )
            }

            // Sugerencias de búsqueda
            if (suggestions.isNotEmpty()) {
                item {
                    SuggestionsCard(
                        suggestions = suggestions,
                        onSuggestionClick = { suggestion ->
                            searchQuery = suggestion
                            suggestions = emptyList()
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
                                            clientRepository.updateClientLocation(it.uid, clientLocation!!)
                                        }
                                        shouldAnimateMap = true
                                    }
                                } catch (e: IOException) {
                                    Log.e("Geocoder", "Error: ${e.message}")
                                } finally {
                                    isLoadingLocation = false
                                }
                            }
                        }
                    )
                }
            }

            // Welcome Card con stats
            item {
                WelcomeCard(
                    displayName = displayName,
                    nearbyOffersCount = filteredOffers.size
                )
            }

            // Mapa con ubicación
            item {
                LocationMapCard(
                    clientLocation = clientLocation,
                    isLoadingLocation = isLoadingLocation,
                    shouldAnimateMap = shouldAnimateMap,
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

            // Filtros de distancia y categoría
            item {
                FilterSection(
                    selectedDistance = selectedDistance,
                    onDistanceChange = { selectedDistance = it },
                    selectedCategory = selectedCategory,
                    onCategoryChange = { selectedCategory = it },
                    showFilters = showFilters,
                    onToggleFilters = { showFilters = !showFilters }
                )
            }

            // Promociones destacadas
            item {
                SectionHeader(
                    title = "🔥 Promociones Destacadas",
                    onSeeAll = onNavigateToOffersList
                )
            }

            item {
                PromotionsCarousel(
                    offers = filteredOffers.take(3),
                    onOfferClick = onNavigateToOfferDetail
                )
            }

            // Ofertas más cercanas
            item {
                SectionHeader(
                    title = "📍 Cerca de Ti",
                    subtitle = "${filteredOffers.size} ofertas en ${selectedDistance}km",
                    onSeeAll = onNavigateToOffersList
                )
            }

            // Lista de ofertas cercanas
            items(filteredOffers.take(5)) { offer ->
                OfferCard(
                    offer = offer,
                    userLocation = clientLocation,
                    onClick = { onNavigateToOfferDetail(offer.id) }
                )
            }

            // Categorías rápidas
            item {
                SectionHeader(
                    title = "🎯 Categorías",
                    subtitle = "Explora por tipo de experiencia"
                )
            }

            item {
                CategoriesGrid(
                    onCategoryClick = { category ->
                        selectedCategory = category
                        onNavigateToOffersList()
                    }
                )
            }

            // Espaciado final
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showLogoutDialog) {
        LogoutDialog(
            displayName = displayName,
            onDismiss = { showLogoutDialog = false },
            onConfirm = {
                showLogoutDialog = false
                onLogout()
            }
        )
    }
}

@Composable
fun SearchBarSection(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    onSearch: () -> Unit
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        placeholder = { Text("¿A dónde quieres ir?") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null, tint = MunayPrimary)
        },
        trailingIcon = {
            if (searchQuery.isNotEmpty()) {
                Row {
                    IconButton(onClick = onClear) {
                        Icon(Icons.Default.Clear, "Limpiar")
                    }
                    IconButton(onClick = onSearch) {
                        Icon(Icons.Default.Send, "Buscar", tint = MunayPrimary)
                    }
                }
            }
        },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = MunayPrimary,
            unfocusedBorderColor = Color.Transparent
        )
    )
}

@Composable
fun SuggestionsCard(
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            suggestions.take(5).forEachIndexed { index, suggestion ->
                TextButton(
                    onClick = { onSuggestionClick(suggestion) },
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
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = suggestion,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                if (index < suggestions.lastIndex) {
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun WelcomeCard(displayName: String, nearbyOffersCount: Int) {
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
                    brush = Brush.linearGradient(
                        colors = listOf(MunayPrimary, MunaySecondary)
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "¡Hola, $displayName! 👋",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Explora nuevas aventuras",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }

                Card(
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.2f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$nearbyOffersCount",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Ofertas",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LocationMapCard(
    clientLocation: ClientLocation?,
    isLoadingLocation: Boolean,
    shouldAnimateMap: Boolean,
    onAnimationComplete: () -> Unit,
    onRequestCurrentLocation: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoadingLocation -> {
                    CircularProgressIndicator(color = MunayPrimary)
                }
                clientLocation != null -> {
                    UserLocationMap(
                        lat = clientLocation.lat,
                        lng = clientLocation.lng,
                        city = clientLocation.ciudad,
                        country = clientLocation.pais,
                        shouldAnimate = shouldAnimateMap,
                        onAnimationComplete = onAnimationComplete,
                        onRequestCurrentLocation = onRequestCurrentLocation
                    )
                }
                else -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            Icons.Default.LocationOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Ubicación no disponible",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onRequestCurrentLocation,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MunayPrimary
                            )
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSection(
    selectedDistance: Double,
    onDistanceChange: (Double) -> Unit,
    selectedCategory: String,
    onCategoryChange: (String) -> Unit,
    showFilters: Boolean,
    onToggleFilters: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🎛️ Filtros",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onToggleFilters) {
                    Icon(
                        if (showFilters) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null
                    )
                }
            }

            AnimatedVisibility(visible = showFilters) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Filtro de distancia
                    Column {
                        Text(
                            text = "Distancia: ${selectedDistance.toInt()} km",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MunayPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Slider(
                            value = selectedDistance.toFloat(),
                            onValueChange = { onDistanceChange(it.toDouble()) },
                            valueRange = 1f..50f,
                            steps = 48,
                            colors = SliderDefaults.colors(
                                thumbColor = MunayPrimary,
                                activeTrackColor = MunayPrimary
                            )
                        )
                    }

                    // Filtro de categorías
                    Column {
                        Text(
                            text = "Categoría",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(listOf("Todas", "Tours", "Gastronomía", "Hospedaje", "Aventura")) { category ->
                                FilterChip(
                                    selected = selectedCategory == category,
                                    onClick = { onCategoryChange(category) },
                                    label = { Text(category) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MunayPrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    onSeeAll: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
        onSeeAll?.let {
            TextButton(onClick = it) {
                Text("Ver todo", color = MunayPrimary)
                Icon(Icons.Default.ChevronRight, null, tint = MunayPrimary)
            }
        }
    }
}

@Composable
fun PromotionsCarousel(
    offers: List<OfferPreview>,
    onOfferClick: (String) -> Unit
) {
    // Filtrar solo ofertas con descuento mayor a 0
    val offersWithDiscount = offers.filter { offer -> offer.descuento > 0 }

    if (offersWithDiscount.isEmpty()) {
        // Opcional: Mostrar un mensaje o no mostrar nada
        Text("No hay Actividades con descuento cerca de ti")
        return
    }
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(offersWithDiscount) { offer ->
            PromotionCard(offer = offer, onClick = { onOfferClick(offer.id) })
        }
    }
}
@Composable
fun PromotionCard(offer: OfferPreview, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(280.dp)
            .height(180.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ){

            // 🔹 Imagen de fondo desde Firebase Storage
            //    Usa offer.imageUrl, que ya fue preparado por el ViewModel
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(offer.imageUrl) // ¡Aquí se usa el link!
                    .crossfade(true)
                    .build(),
                contentDescription = offer.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // 🔹 Capa semitransparente (overlay)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                            startY = 100f
                        )
                    )
            )

            // Badge de descuento (arriba a la derecha)
            Card(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MunayAccent)
            ) {
                Text(
                    text = "-${offer.descuento}%",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Contenido principal
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Rating badge (arriba a la izquierda)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.9f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = offer.rating.toString(),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Información (abajo)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = offer.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MunaySuccess,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = offer.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "S/ ${offer.price}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${String.format("%.1f", offer.distance)} km",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OfferCard(
    offer: OfferPreview,
    userLocation: ClientLocation?,
    onClick: () -> Unit
) {
    val distance = userLocation?.let {
        calculateDistance(it.lat, it.lng, offer.lat, offer.lng)
    } ?: 0.0

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Imagen placeholder
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MunaySecondary.copy(alpha = 0.3f),
                                MunayPrimary.copy(alpha = 0.3f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // 🔹 Imagen de fondo desde Firebase Storage
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(offer.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = offer.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Información
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = offer.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MunayPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .background(
                                    MunayPrimary.copy(alpha = 0.1f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = offer.rating.toString(),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = offer.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = offer.provider,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "S/ ${offer.price}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MunayPrimary
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .background(
                                MunaySuccess.copy(alpha = 0.1f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MunaySuccess,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${String.format("%.1f", distance)} km",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MunaySuccess
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoriesGrid(onCategoryClick: (String) -> Unit) {
    val categories = listOf(
        "Tours" to Icons.Default.Explore,
        "Gastronomía" to Icons.Default.Restaurant,
        "Hospedaje" to Icons.Default.Hotel,
        "Aventura" to Icons.Default.Terrain,
        "Cultura" to Icons.Default.Museum,
        "Transporte" to Icons.Default.DirectionsCar
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(categories) { (category, icon) ->
            CategoryCard(
                category = category,
                icon = icon,
                onClick = { onCategoryClick(category) }
            )
        }
    }
}
@Composable
fun CategoryCard(
    category: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier  // Cambia esta línea
            .width(120.dp)
            .height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier  // Y esta línea también
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier  // Y esta
                    .size(48.dp)
                    .background(
                        MunayPrimary.copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MunayPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = category,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun LogoutDialog(
    displayName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Logout,
                contentDescription = null,
                tint = MunayAccent,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = "¿Cerrar sesión?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "Hasta pronto, $displayName. ¿Estás seguro de que quieres cerrar sesión?",
                style = MaterialTheme.typography.bodyLarge
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MunayAccent
                )
            ) {
                Text("Cerrar sesión")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar", color = MunayPrimary)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp)
    )
}

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
    val location = LatLng(lat, lng)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(location, 13f)
    }

    LaunchedEffect(shouldAnimate) {
        if (shouldAnimate) {
            cameraPositionState.animate(
                update = com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(location, 13f),
                durationMs = 1000
            )
            onAnimationComplete()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = com.google.maps.android.compose.MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false
            )
        ) {
            Marker(
                state = MarkerState(position = location),
                title = city,
                snippet = country
            )
        }

        // Botón de mi ubicación flotante
        FloatingActionButton(
            onClick = onRequestCurrentLocation,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .size(48.dp),
            containerColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
        ) {
            Icon(
                Icons.Default.MyLocation,
                contentDescription = "Mi ubicación",
                tint = MunayPrimary
            )
        }

        // Info card en la parte superior
        Card(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.95f)
            ),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MunayPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = city,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = country,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

// Función helper para calcular distancia entre dos puntos
fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371 // Radio de la Tierra en km
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return r * c
}