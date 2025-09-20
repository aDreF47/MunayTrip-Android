package com.dsm.munaytripandroid.data.models

import com.google.firebase.Timestamp

data class Review(
    val reviewId: String = "",
    val turistaId: String = "",
    val offerId: String = "",
    val bookingId: String = "",

    // 📝 Contenido - Sin imágenes por ahora
    val rating: Int = 0, // 1-5 estrellas
    val comentario: String = "",
    val aspectos: Map<String, Int> = mapOf(), // "limpieza": 5, "servicio": 4

    // 👍 Interacción
    val util: Int = 0, // likes
    val reportado: Boolean = false,

    // 💬 Respuesta del proveedor
    val respondido: Boolean = false,
    val respuestaProveedor: String = "",
    val fechaRespuesta: Timestamp? = null,

    val createdAt: Timestamp = Timestamp.now()
)