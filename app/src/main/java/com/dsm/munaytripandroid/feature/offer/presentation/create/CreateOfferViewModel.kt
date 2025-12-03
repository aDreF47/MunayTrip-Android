package com.dsm.munaytripandroid.feature.offer.presentation.create

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dsm.munaytripandroid.core.util.Result
import com.dsm.munaytripandroid.feature.offer.data.remote.OfferFirestoreDataSource
import com.dsm.munaytripandroid.feature.offer.data.repository.OfferRepositoryImpl
import com.dsm.munaytripandroid.feature.offer.domain.model.*
import com.dsm.munaytripandroid.feature.offer.domain.repository.OfferRepository
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class CreateOfferViewModel : ViewModel() {

    private val offerRepository: OfferRepository = OfferRepositoryImpl(
        OfferFirestoreDataSource()
    )

    private val storage = Firebase.storage

    private val _uiState = MutableStateFlow(CreateOfferUiState())
    val uiState: StateFlow<CreateOfferUiState> = _uiState.asStateFlow()

    private val _createSuccess = MutableStateFlow<String?>(null)
    val createSuccess: StateFlow<String?> = _createSuccess.asStateFlow()

    // Manejo de imágenes
    fun onImagesSelected(uris: List<Uri>) {
        val currentImages = _uiState.value.selectedImageUris
        val newImages = (currentImages + uris).take(5) // Máximo 5 imágenes
        _uiState.value = _uiState.value.copy(selectedImageUris = newImages)
    }

    fun removeImage(index: Int) {
        val images = _uiState.value.selectedImageUris.toMutableList()
        images.removeAt(index)
        _uiState.value = _uiState.value.copy(selectedImageUris = images)
    }

    private suspend fun uploadImages(uris: List<Uri>): Pair<List<String>, List<String>> {
        val imageUrls = mutableListOf<String>()
        val thumbnailUrls = mutableListOf<String>()

        _uiState.value = _uiState.value.copy(isUploadingImages = true, uploadProgress = 0)

        uris.forEachIndexed { index, uri ->
            try {
                val filename = "offers/${UUID.randomUUID()}.jpg"
                val thumbnailFilename = "thumbnails/$filename"

                // Subir imagen original
                val imageRef = storage.reference.child(filename)
                imageRef.putFile(uri).await()
                val imageUrl = imageRef.downloadUrl.await().toString()
                imageUrls.add(imageUrl)

                // Subir thumbnail (en producción, deberías redimensionar la imagen primero)
                val thumbnailRef = storage.reference.child(thumbnailFilename)
                thumbnailRef.putFile(uri).await()
                val thumbnailUrl = thumbnailRef.downloadUrl.await().toString()
                thumbnailUrls.add(thumbnailUrl)

                // Actualizar progreso
                val progress = ((index + 1) * 100) / uris.size
                _uiState.value = _uiState.value.copy(uploadProgress = progress)

            } catch (e: Exception) {
                e.printStackTrace()
                // Continuar con las demás imágenes aunque falle una
            }
        }

        _uiState.value = _uiState.value.copy(isUploadingImages = false, uploadProgress = 0)
        return Pair(imageUrls, thumbnailUrls)
    }

    // Actions - Información básica
    fun onTituloChange(titulo: String) {
        _uiState.value = _uiState.value.copy(titulo = titulo)
    }

    fun onDescripcionCortaChange(descripcion: String) {
        _uiState.value = _uiState.value.copy(descripcionCorta = descripcion)
    }

    fun onDescripcionLargaChange(descripcion: String) {
        _uiState.value = _uiState.value.copy(descripcionLarga = descripcion)
    }

    fun onCategoriaChange(categoria: OfferCategory) {
        _uiState.value = _uiState.value.copy(categoria = categoria)
    }

    fun onTipoOfertaChange(tipo: OfferType) {
        _uiState.value = _uiState.value.copy(tipoOferta = tipo)
    }

    // Actions - Precio y capacidad
    fun onPrecioChange(precio: String) {
        val parsedPrecio = precio.toDoubleOrNull() ?: 0.0
        _uiState.value = _uiState.value.copy(
            precio = parsedPrecio,
            esGratis = parsedPrecio == 0.0,
        )
    }

    fun toggleEsGratis() {
        val newValue = !_uiState.value.esGratis
        _uiState.value = _uiState.value.copy(
            precio = if (newValue) 0.0 else _uiState.value.precio,
            esGratis = newValue,
        )
    }

    fun onDescuentoChange(descuento: String) {
        val parsed = descuento.toIntOrNull() ?: 0
        _uiState.value = _uiState.value.copy(descuento = parsed.coerceIn(0, 100))
    }

    fun onCapacidadMaximaChange(capacidad: String) {
        val parsed = capacidad.toIntOrNull() ?: 0
        _uiState.value = _uiState.value.copy(
            capacidadMaxima = parsed,
            cuposDisponibles = if (_uiState.value.cuposDisponibles == _uiState.value.capacidadMaxima || _uiState.value.cuposDisponibles == 0) {
                parsed
            } else {
                _uiState.value.cuposDisponibles
            }
        )
    }

    fun onCuposDisponiblesChange(cupos: String) {
        val parsed = cupos.toIntOrNull() ?: 0
        _uiState.value = _uiState.value.copy(
            cuposDisponibles = parsed.coerceIn(0, _uiState.value.capacidadMaxima)
        )
    }

    fun onDuracionChange(duracion: String) {
        _uiState.value = _uiState.value.copy(duracion = duracion)
    }

    // Actions - Ubicación
    fun onUbicacionNombreChange(nombre: String) {
        val ubicacion = _uiState.value.ubicacion.copy(nombre = nombre)
        _uiState.value = _uiState.value.copy(ubicacion = ubicacion)
    }

    fun onUbicacionLatitud(latitudTexto: String) {
        if (latitudTexto.count { it == '.' } > 1) return
        if (latitudTexto.contains("-") && latitudTexto.indexOf("-") > 0) return

        val latDouble = latitudTexto.toDoubleOrNull() ?: 0.0
        val nuevaUbicacion = _uiState.value.ubicacion.copy(lat = latDouble)

        _uiState.value = _uiState.value.copy(
            latitudInput = latitudTexto,
            ubicacion = nuevaUbicacion
        )
    }

    fun onUbicacionLongitud(longitudTexto: String) {
        if (longitudTexto.count { it == '.' } > 1) return

        val lngDouble = longitudTexto.toDoubleOrNull() ?: 0.0
        val nuevaUbicacion = _uiState.value.ubicacion.copy(lng = lngDouble)

        _uiState.value = _uiState.value.copy(
            longitudInput = longitudTexto,
            ubicacion = nuevaUbicacion
        )
    }

    fun onUbicacionDireccionChange(direccion: String) {
        val ubicacion = _uiState.value.ubicacion.copy(direccion = direccion)
        _uiState.value = _uiState.value.copy(ubicacion = ubicacion)
    }

    // Actions - Horarios
    fun addHorario(dia: String, horaInicio: String, horaFin: String) {
        val horarios = _uiState.value.horarios.toMutableList()
        horarios.add(OfferHorario(dia = dia, horaInicio = horaInicio, horaFin = horaFin))
        _uiState.value = _uiState.value.copy(horarios = horarios)
    }

    fun removeHorario(horario: OfferHorario) {
        val horarios = _uiState.value.horarios.toMutableList()
        horarios.remove(horario)
        _uiState.value = _uiState.value.copy(horarios = horarios)
    }

    // Actions - Incluye
    fun addIncluye(item: String) {
        if (item.isNotBlank()) {
            val incluye = _uiState.value.incluye.toMutableList()
            incluye.add(item.trim())
            _uiState.value = _uiState.value.copy(incluye = incluye)
        }
    }

    fun removeIncluye(item: String) {
        val incluye = _uiState.value.incluye.toMutableList()
        incluye.remove(item)
        _uiState.value = _uiState.value.copy(incluye = incluye)
    }

    // Actions - Recomendaciones
    fun addRecomendacion(recomendacion: String) {
        if (recomendacion.isNotBlank()) {
            val recomendaciones = _uiState.value.recomendaciones.toMutableList()
            recomendaciones.add(recomendacion.trim())
            _uiState.value = _uiState.value.copy(recomendaciones = recomendaciones)
        }
    }

    fun removeRecomendacion(recomendacion: String) {
        val recomendaciones = _uiState.value.recomendaciones.toMutableList()
        recomendaciones.remove(recomendacion)
        _uiState.value = _uiState.value.copy(recomendaciones = recomendaciones)
    }

    // Actions - Etiquetas
    fun addEtiqueta(etiqueta: String) {
        if (etiqueta.isNotBlank()) {
            val etiquetas = _uiState.value.etiquetas.toMutableList()
            etiquetas.add(etiqueta.trim())
            _uiState.value = _uiState.value.copy(etiquetas = etiquetas)
        }
    }

    fun removeEtiqueta(etiqueta: String) {
        val etiquetas = _uiState.value.etiquetas.toMutableList()
        etiquetas.remove(etiqueta)
        _uiState.value = _uiState.value.copy(etiquetas = etiquetas)
    }

    // Actions - Instrucciones de pago
    fun onInstruccionesPagoChange(instrucciones: String) {
        _uiState.value = _uiState.value.copy(instruccionesPago = instrucciones)
    }

    // Crear oferta
    fun createOffer(providerId: String) {
        val state = _uiState.value

        // Validaciones
        when {
            state.titulo.isBlank() -> {
                _uiState.value = state.copy(errorMessage = "El título es obligatorio")
                return
            }
            state.descripcionCorta.isBlank() -> {
                _uiState.value = state.copy(errorMessage = "La descripción corta es obligatoria")
                return
            }
            state.ubicacion.nombre.isBlank() -> {
                _uiState.value = state.copy(errorMessage = "La ubicación es obligatoria")
                return
            }
            state.capacidadMaxima <= 0 -> {
                _uiState.value = state.copy(errorMessage = "La capacidad debe ser mayor a 0")
                return
            }
            state.cuposDisponibles > state.capacidadMaxima -> {
                _uiState.value = state.copy(errorMessage = "Los cupos disponibles no pueden ser mayores a la capacidad máxima")
                return
            }
            state.selectedImageUris.isEmpty() -> {
                _uiState.value = state.copy(errorMessage = "Debes agregar al menos una imagen")
                return
            }
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true)

            try {
                // Subir imágenes primero
                val (imageUrls, thumbnailUrls) = uploadImages(state.selectedImageUris)

                if (imageUrls.isEmpty()) {
                    _uiState.value = state.copy(
                        isLoading = false,
                        errorMessage = "Error al subir las imágenes. Intenta de nuevo."
                    )
                    return@launch
                }

                // Crear la oferta con las URLs de las imágenes
                val offer = Offer(
                    titulo = state.titulo,
                    descripcionCorta = state.descripcionCorta,
                    descripcionLarga = state.descripcionLarga.ifBlank { state.descripcionCorta },
                    categoria = state.categoria,
                    tipoOferta = state.tipoOferta,
                    precio = state.precio,
                    esGratis = state.esGratis,
                    descuento = state.descuento,
                    capacidadMaxima = state.capacidadMaxima,
                    cuposDisponibles = state.cuposDisponibles,
                    ubicacion = state.ubicacion,
                    duracion = state.duracion,
                    horarios = state.horarios,
                    etiquetas = state.etiquetas,
                    incluye = state.incluye,
                    recomendaciones = state.recomendaciones,
                    instruccionesPago = state.instruccionesPago,
                    imageUrls = imageUrls,
                    thumbnailUrls = thumbnailUrls
                )

                when (val result = offerRepository.createOffer(offer, providerId)) {
                    is Result.Success -> {
                        _uiState.value = state.copy(isLoading = false)
                        _createSuccess.value = result.data
                    }
                    is Result.Error -> {
                        _uiState.value = state.copy(
                            isLoading = false,
                            errorMessage = result.exception.message ?: "Error al crear oferta"
                        )
                    }
                    is Result.Loading -> {}
                }
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    isLoading = false,
                    errorMessage = "Error inesperado: ${e.message}"
                )
            }
        }
    }

    fun resetCreateSuccess() {
        _createSuccess.value = null
    }
}

data class CreateOfferUiState(
    val titulo: String = "",
    val descripcionCorta: String = "",
    val descripcionLarga: String = "",
    val categoria: OfferCategory = OfferCategory.CULTURAL,
    val tipoOferta: OfferType = OfferType.EVENT,
    val precio: Double = 0.0,
    val esGratis: Boolean = true,
    val descuento: Int = 0,
    val capacidadMaxima: Int = 10,
    val cuposDisponibles: Int = 10,
    val duracion: String = "",
    val ubicacion: OfferLocation = OfferLocation(),
    val horarios: List<OfferHorario> = emptyList(),
    val etiquetas: List<String> = emptyList(),
    val incluye: List<String> = emptyList(),
    val recomendaciones: List<String> = emptyList(),
    val instruccionesPago: String = "",
    val selectedImageUris: List<Uri> = emptyList(),
    val isLoading: Boolean = false,
    val isUploadingImages: Boolean = false,
    val uploadProgress: Int = 0,
    val errorMessage: String? = null,
    val latitudInput: String = "",
    val longitudInput: String = ""
)