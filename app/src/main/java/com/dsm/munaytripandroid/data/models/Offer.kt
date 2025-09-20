package com.dsm.munaytripandroid.data.models

import com.google.firebase.Timestamp

data class Offer(
    val offerId: String = "",
    val titulo: String = "",
    val descripcionCorta: String = "",
    val descripcionLarga: String = "",

    // 🏢 Proveedor
    val proveedorId: String = "",
    val nombreProveedor: String = "",
    val providerType: String = "", // Para ícono automático

    // 🎨 Imágenes - Sin Storage
    val categoria: OfferCategory = OfferCategory.GENERAL,
    val imageResources: List<Int> = emptyList(), // Recursos predefinidos
    val customImageUrls: List<String> = emptyList(), // URLs opcionales

    // 📍 Ubicación y tiempo
    val ubicacion: Map<String, Any> = mapOf(),
    val fechaInicio: Timestamp? = null,
    val fechaFin: Timestamp? = null,
    val horarios: List<Map<String, String>> = emptyList(),

    // 💰 Precio
    val precio: Double = 0.0,
    val moneda: String = "PEN",
    val esGratis: Boolean = false,

    // 📊 Capacidad
    val capacidadMaxima: Int = 0,
    val cuposDisponibles: Int = 0,

    // ⭐ Reviews
    val ratingPromedio: Double = 0.0,
    val totalReseñas: Int = 0,

    // 📈 Métricas
    val vecesVisto: Int = 0,
    val vecesReservado: Int = 0,
    val destacado: Boolean = false,

    val estado: OfferStatus = OfferStatus.ACTIVO,
    val createdAt: Timestamp = Timestamp.now()
)

enum class OfferCategory {
    CULTURAL, AVENTURA, GASTRONOMIA, HOSPEDAJE, TRANSPORTE, GENERAL
}

enum class OfferStatus {
    ACTIVO, INACTIVO, AGOTADO, EXPIRADO
}