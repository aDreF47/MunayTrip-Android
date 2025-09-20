package com.dsm.munaytripandroid.data.models

import com.google.firebase.Timestamp

data class Provider(
    val uid: String = "",
    val nombre: String = "",
    val email: String = "",
    val tipo: ProviderType = ProviderType.OTRO,
    val categoria: String = "",
    val descripcion: String = "",

    // 🎨 Sin Storage - Solo íconos predefinidos
    val iconType: String = "", // Mapea a drawable resource
    val logoUrl: String? = null, // URL opcional personalizada

    // 📍 Ubicación
    val ubicacion: Map<String, Any> = mapOf(
        "lat" to 0.0,
        "lng" to 0.0,
        "direccion" to "",
        "ciudad" to "",
        "region" to ""
    ),

    // 📞 Contacto
    val telefono: String = "",
    val whatsapp: String = "",
    val sitioWeb: String = "",
    val horarioAtencion: Map<String, String> = mapOf(),

    // ✅ Verificación
    val verificado: Boolean = false,
    val estado: ProviderStatus = ProviderStatus.ACTIVO,

    // 📊 Estadísticas básicas
    val ratingPromedio: Double = 0.0,
    val totalReseñas: Int = 0,
    val totalOfertas: Int = 0,

    val createdAt: Timestamp = Timestamp.now()
)

enum class ProviderType {
    MUNICIPALIDAD, HOTEL, RESTAURANT, GUIA, TRANSPORTE, COMUNIDAD, OTRO
}

enum class ProviderStatus {
    ACTIVO, INACTIVO, SUSPENDIDO, PENDIENTE
}