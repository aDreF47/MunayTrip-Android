package com.dsm.munaytripandroid.feature.offer.presentation.create

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.dsm.munaytripandroid.feature.offer.domain.model.OfferCategory
import com.dsm.munaytripandroid.feature.offer.domain.model.OfferHorario
import com.dsm.munaytripandroid.feature.offer.domain.model.OfferType

private val MunayPrimary = Color(0xFF1A7FA6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOfferScreen(
    providerId: String,
    viewModel: CreateOfferViewModel = viewModel(),
    onOfferCreated: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val createSuccess by viewModel.createSuccess.collectAsState()

    var showEtiquetaDialog by remember { mutableStateOf(false) }
    var showHorarioDialog by remember { mutableStateOf(false) }
    var showIncluyeDialog by remember { mutableStateOf(false) }
    var showRecomendacionDialog by remember { mutableStateOf(false) }

    // Launcher para seleccionar imágenes
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.onImagesSelected(uris)
        }
    }

    // Observar éxito
    LaunchedEffect(createSuccess) {
        createSuccess?.let { offerId ->
            onOfferCreated(offerId)
            viewModel.resetCreateSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Oferta") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MunayPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {

            // Sección: Imágenes
            SectionHeader("📷 Imágenes de la Oferta")

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF5F9FB)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Agrega hasta 5 imágenes de tu oferta",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Mostrar imágenes seleccionadas
                    if (uiState.selectedImageUris.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            uiState.selectedImageUris.forEachIndexed { index, uri ->
                                Box(
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(
                                            width = 2.dp,
                                            color = MunayPrimary.copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                ) {
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = "Imagen ${index + 1}",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )

                                    // Botón eliminar
                                    IconButton(
                                        onClick = { viewModel.removeImage(index) },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(32.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = MaterialTheme.colorScheme.error
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Eliminar",
                                                modifier = Modifier
                                                    .padding(4.dp)
                                                    .size(16.dp),
                                                tint = Color.White
                                            )
                                        }
                                    }

                                    // Indicador de imagen principal
                                    if (index == 0) {
                                        Surface(
                                            modifier = Modifier
                                                .align(Alignment.BottomStart)
                                                .padding(4.dp),
                                            color = MunayPrimary,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "Principal",
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Botón para agregar imágenes
                    OutlinedButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading && uiState.selectedImageUris.size < 5
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (uiState.selectedImageUris.isEmpty())
                                "Seleccionar imágenes (Máx. 5)"
                            else
                                "Agregar más imágenes (${uiState.selectedImageUris.size}/5)"
                        )
                    }

                    // Mostrar estado de carga de imágenes
                    if (uiState.isUploadingImages) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Subiendo imágenes... ${uiState.uploadProgress}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MunayPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sección: Información Básica
            SectionHeader("📝 Información Básica")

            OutlinedTextField(
                value = uiState.titulo,
                onValueChange = viewModel::onTituloChange,
                label = { Text("Título de la oferta *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !uiState.isLoading,
                isError = uiState.errorMessage?.contains("título", ignoreCase = true) == true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MunayPrimary,
                    focusedLabelColor = MunayPrimary
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.descripcionCorta,
                onValueChange = viewModel::onDescripcionCortaChange,
                label = { Text("Descripción corta *") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                enabled = !uiState.isLoading,
                isError = uiState.errorMessage?.contains("descripción", ignoreCase = true) == true,
                supportingText = { Text("${uiState.descripcionCorta.length}/200 caracteres") }
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.descripcionLarga,
                onValueChange = viewModel::onDescripcionLargaChange,
                label = { Text("Descripción detallada (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                maxLines = 8,
                enabled = !uiState.isLoading,
                supportingText = { Text("Describe todos los detalles de tu oferta") }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Sección: Tipo y Categoría
            SectionHeader("🏷️ Tipo y Categoría")

            Text(
                text = "Tipo de oferta *",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OfferType.entries.forEach { type ->
                    FilterChip(
                        selected = uiState.tipoOferta == type,
                        onClick = { viewModel.onTipoOfertaChange(type) },
                        label = { Text(type.displayName) },
                        enabled = !uiState.isLoading,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Categoría *",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Grid de categorías
            OfferCategory.entries.chunked(2).forEach { rowCategories ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowCategories.forEach { category ->
                        FilterChip(
                            selected = uiState.categoria == category,
                            onClick = { viewModel.onCategoriaChange(category) },
                            label = { Text(category.displayName) },
                            modifier = Modifier.weight(1f),
                            enabled = !uiState.isLoading
                        )
                    }
                    if (rowCategories.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sección: Precio y Capacidad
            SectionHeader("💰 Precio, Descuento y Capacidad")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = if (uiState.precio == 0.0) "" else uiState.precio.toString(),
                    onValueChange = viewModel::onPrecioChange,
                    label = { Text("Precio (S/)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    enabled = !uiState.esGratis && !uiState.isLoading,
                    prefix = { Text("S/ ") }
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = uiState.esGratis,
                        onCheckedChange = { viewModel.toggleEsGratis() },
                        enabled = !uiState.isLoading
                    )
                    Text("Gratis", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = if (uiState.descuento == 0) "" else uiState.descuento.toString(),
                onValueChange = viewModel::onDescuentoChange,
                label = { Text("Descuento (%)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                enabled = !uiState.isLoading && !uiState.esGratis,
                suffix = { Text("%") },
                supportingText = { Text("Opcional: Aplica un porcentaje de descuento") }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = if (uiState.capacidadMaxima == 0) "" else uiState.capacidadMaxima.toString(),
                    onValueChange = viewModel::onCapacidadMaximaChange,
                    label = { Text("Capacidad máxima *") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = !uiState.isLoading,
                    isError = uiState.errorMessage?.contains("capacidad", ignoreCase = true) == true
                )

                OutlinedTextField(
                    value = if (uiState.cuposDisponibles == 0) "" else uiState.cuposDisponibles.toString(),
                    onValueChange = viewModel::onCuposDisponiblesChange,
                    label = { Text("Cupos disponibles *") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = !uiState.isLoading
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.duracion,
                onValueChange = viewModel::onDuracionChange,
                label = { Text("Duración") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
                placeholder = { Text("Ej: 2 horas, 3 días") }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Spacer(modifier = Modifier.height(24.dp))

            // Sección: Horarios
            SectionHeader("🕐 Horarios (Opcional)")

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF5F9FB)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    if (uiState.horarios.isEmpty()) {
                        Text(
                            text = "Sin horarios definidos. Agrega horarios de atención si aplica.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    } else {
                        uiState.horarios.forEach { horario ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = horario.dia.capitalize(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${horario.horaInicio} - ${horario.horaFin}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                                IconButton(onClick = { viewModel.removeHorario(horario) }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Eliminar",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                            if (horario != uiState.horarios.last()) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { showHorarioDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Agregar horario")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sección: Ubicación
            SectionHeader("📍 Ubicación")

            OutlinedTextField(
                value = uiState.ubicacion.nombre,
                onValueChange = viewModel::onUbicacionNombreChange,
                label = { Text("Nombre del lugar *") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
                isError = uiState.errorMessage?.contains("ubicación", ignoreCase = true) == true,
                placeholder = { Text("Ej: Plaza de Armas") }
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.ubicacion.direccion,
                onValueChange = viewModel::onUbicacionDireccionChange,
                label = { Text("Dirección completa") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
                placeholder = { Text("Ej: Av. El Sol 123, Cusco") }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = uiState.latitudInput,
                    onValueChange = viewModel::onUbicacionLatitud,
                    label = { Text("Latitud *") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = !uiState.isLoading,
                    placeholder = { Text("-10.0980896") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = uiState.longitudInput,
                    onValueChange = viewModel::onUbicacionLongitud,
                    label = { Text("Longitud *") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = !uiState.isLoading,
                    placeholder = { Text("-75.0980896") },
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sección: Incluye
            SectionHeader("✅ ¿Qué incluye? (Opcional)")

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF5F9FB)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    if (uiState.incluye.isEmpty()) {
                        Text(
                            text = "Lista vacía. Agrega lo que incluye tu oferta.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    } else {
                        uiState.incluye.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "• $item",
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                IconButton(onClick = { viewModel.removeIncluye(item) }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Eliminar",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { showIncluyeDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Agregar ítem")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sección: Recomendaciones
            SectionHeader("💡 Recomendaciones (Opcional)")

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF5F9FB)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    if (uiState.recomendaciones.isEmpty()) {
                        Text(
                            text = "Sin recomendaciones. Agrega consejos para los usuarios.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    } else {
                        uiState.recomendaciones.forEach { recomendacion ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "• $recomendacion",
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                IconButton(onClick = { viewModel.removeRecomendacion(recomendacion) }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Eliminar",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { showRecomendacionDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Agregar recomendación")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sección: Etiquetas
            SectionHeader("🏷️ Etiquetas")

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF5F9FB)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    if (uiState.etiquetas.isEmpty()) {
                        Text(
                            text = "Sin etiquetas. Agrega palabras clave para ayudar a los usuarios a encontrar tu oferta.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            uiState.etiquetas.forEach { etiqueta ->
                                AssistChip(
                                    onClick = { viewModel.removeEtiqueta(etiqueta) },
                                    label = { Text(etiqueta) },
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Eliminar",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { showEtiquetaDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Agregar etiqueta")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sección: Instrucciones de Pago
            SectionHeader("💳 Instrucciones de Pago")

            OutlinedTextField(
                value = uiState.instruccionesPago,
                onValueChange = viewModel::onInstruccionesPagoChange,
                label = { Text("Instrucciones de pago") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 6,
                enabled = !uiState.isLoading,
                placeholder = { Text("Ej: El pago se realiza en efectivo al llegar al lugar. También aceptamos Yape y Plin.") },
                supportingText = { Text("Indica cómo y cuándo deben realizar el pago") }
            )

            // Mensaje de error general
            if (uiState.errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = uiState.errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Botón crear
            Button(
                onClick = { viewModel.createOffer(providerId) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !uiState.isLoading && !uiState.isUploadingImages,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MunayPrimary
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Creando...", fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Crear Oferta", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Diálogo agregar etiqueta
    if (showEtiquetaDialog) {
        var newEtiqueta by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showEtiquetaDialog = false },
            title = { Text("Agregar etiqueta") },
            text = {
                OutlinedTextField(
                    value = newEtiqueta,
                    onValueChange = { newEtiqueta = it },
                    label = { Text("Etiqueta") },
                    singleLine = true,
                    placeholder = { Text("Ej: familia, aventura") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newEtiqueta.isNotBlank()) {
                            viewModel.addEtiqueta(newEtiqueta)
                            showEtiquetaDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MunayPrimary
                    )
                ) {
                    Text("Agregar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEtiquetaDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo agregar horario
    if (showHorarioDialog) {
        var dia by remember { mutableStateOf("") }
        var horaInicio by remember { mutableStateOf("") }
        var horaFin by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showHorarioDialog = false },
            title = { Text("Agregar horario") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = dia,
                        onValueChange = { dia = it },
                        label = { Text("Día") },
                        placeholder = { Text("Ej: Lunes, Sábado, Todos") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = horaInicio,
                            onValueChange = { horaInicio = it },
                            label = { Text("Hora inicio") },
                            placeholder = { Text("09:00") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = horaFin,
                            onValueChange = { horaFin = it },
                            label = { Text("Hora fin") },
                            placeholder = { Text("18:00") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (dia.isNotBlank() && horaInicio.isNotBlank() && horaFin.isNotBlank()) {
                            viewModel.addHorario(dia, horaInicio, horaFin)
                            showHorarioDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MunayPrimary)
                ) {
                    Text("Agregar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showHorarioDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo agregar incluye
    if (showIncluyeDialog) {
        var newItem by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showIncluyeDialog = false },
            title = { Text("¿Qué incluye?") },
            text = {
                OutlinedTextField(
                    value = newItem,
                    onValueChange = { newItem = it },
                    label = { Text("Ítem") },
                    placeholder = { Text("Ej: Guía turístico, Transporte") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newItem.isNotBlank()) {
                            viewModel.addIncluye(newItem)
                            showIncluyeDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MunayPrimary)
                ) {
                    Text("Agregar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showIncluyeDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo agregar recomendación
    if (showRecomendacionDialog) {
        var newRecomendacion by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showRecomendacionDialog = false },
            title = { Text("Agregar recomendación") },
            text = {
                OutlinedTextField(
                    value = newRecomendacion,
                    onValueChange = { newRecomendacion = it },
                    label = { Text("Recomendación") },
                    placeholder = { Text("Ej: Llevar protector solar") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newRecomendacion.isNotBlank()) {
                            viewModel.addRecomendacion(newRecomendacion)
                            showRecomendacionDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MunayPrimary)
                ) {
                    Text("Agregar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRecomendacionDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}


@Composable
fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MunayPrimary,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}