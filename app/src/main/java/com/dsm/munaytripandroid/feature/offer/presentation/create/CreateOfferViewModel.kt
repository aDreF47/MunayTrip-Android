package com.dsm.munaytripandroid.feature.offer.presentation.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dsm.munaytripandroid.core.util.Result
import com.dsm.munaytripandroid.feature.offer.data.remote.OfferFirestoreDataSource
import com.dsm.munaytripandroid.feature.offer.data.repository.OfferRepositoryImpl
import com.dsm.munaytripandroid.feature.offer.domain.model.*
import com.dsm.munaytripandroid.feature.offer.domain.repository.OfferRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CreateOfferViewModel : ViewModel() {

    private val offerRepository: OfferRepository = OfferRepositoryImpl(
        OfferFirestoreDataSource()
    )

    private val _uiState = MutableStateFlow(CreateOfferUiState())
    val uiState: StateFlow<CreateOfferUiState> = _uiState.asStateFlow()

    private val _createSuccess = MutableStateFlow<String?>(null)
    val createSuccess: StateFlow<String?> = _createSuccess.asStateFlow()

    // Actions
    fun onTituloChange(titulo: String) {
        _uiState.value = _uiState.value.copy(titulo = titulo, errorMessage = null)
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

    fun onPrecioChange(precio: String) {
        val parsedPrecio = precio.toDoubleOrNull() ?: 0.0
        _uiState.value = _uiState.value.copy(
            precio = parsedPrecio,
            esGratis = parsedPrecio == 0.0
        )
    }

    fun toggleEsGratis() {
        val newValue = !_uiState.value.esGratis
        _uiState.value = _uiState.value.copy(
            esGratis = newValue,
            precio = if (newValue) 0.0 else _uiState.value.precio
        )
    }

    fun onCapacidadMaximaChange(capacidad: String) {
        val parsed = capacidad.toIntOrNull() ?: 0
        _uiState.value = _uiState.value.copy(
            capacidadMaxima = parsed,
            cuposDisponibles = parsed
        )
    }

    fun onDuracionChange(duracion: String) {
        _uiState.value = _uiState.value.copy(duracion = duracion)
    }

    fun onUbicacionNombreChange(nombre: String) {
        val ubicacion = _uiState.value.ubicacion.copy(nombre = nombre)
        _uiState.value = _uiState.value.copy(ubicacion = ubicacion)
    }

    fun onUbicacionDireccionChange(direccion: String) {
        val ubicacion = _uiState.value.ubicacion.copy(direccion = direccion)
        _uiState.value = _uiState.value.copy(ubicacion = ubicacion)
    }

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
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null)

            val offer = Offer(
                titulo = state.titulo,
                descripcionCorta = state.descripcionCorta,
                descripcionLarga = state.descripcionLarga.ifBlank { state.descripcionCorta },
                categoria = state.categoria,
                tipoOferta = state.tipoOferta,
                precio = state.precio,
                esGratis = state.esGratis,
                capacidadMaxima = state.capacidadMaxima,
                cuposDisponibles = state.cuposDisponibles,
                ubicacion = state.ubicacion,
                duracion = state.duracion,
                etiquetas = state.etiquetas,
                incluye = emptyList(),  // TODO: Agregar después
                recomendaciones = emptyList(),  // TODO: Agregar después
                horarios = emptyList()  // TODO: Agregar después
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
    val capacidadMaxima: Int = 10,
    val cuposDisponibles: Int = 10,
    val duracion: String = "",
    val ubicacion: OfferLocation = OfferLocation(),
    val etiquetas: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)