package com.dsm.munaytripandroid.feature.offer.presentation.create

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dsm.munaytripandroid.feature.offer.domain.model.OfferCategory
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
            SectionHeader("💰 Precio y Capacidad")

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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = if (uiState.capacidadMaxima == 0) "" else uiState.capacidadMaxima.toString(),
                    onValueChange = viewModel::onCapacidadMaximaChange,
                    label = { Text("Capacidad *") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = !uiState.isLoading,
                    isError = uiState.errorMessage?.contains("capacidad", ignoreCase = true) == true
                )

                OutlinedTextField(
                    value = uiState.duracion,
                    onValueChange = viewModel::onDuracionChange,
                    label = { Text("Duración") },
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isLoading,
                    placeholder = { Text("Ej: 2 horas") }
                )
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
                enabled = !uiState.isLoading,
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


/**
 * MEJORAS EN FASE 2:
 * ✅ UI mejorada con secciones claras
 * ✅ Headers de sección con emojis
 * ✅ Validaciones visuales en campos
 * ✅ Contador de caracteres
 * ✅ Mensajes de error mejorados
 * ✅ Card de error destacado
 * ✅ Placeholder texts en campos
 * ✅ Grid de categorías más limpio
 * ✅ Etiquetas en card con mejor UX
 * ✅ Loading state en botón
 * ✅ Íconos en botones
 */