package com.dsm.munaytripandroid.feature.analytics.domain.model

import com.google.firebase.Timestamp

// Representa el documento principal "int_001"
data class Interaction(
    val interactionId: String = "", // Se generará automáticamente o manualmente
    val userId: String = "",
    val offerId: String = "",
    val tipo: String = "view", // "view", "click", "booking_intent"
    val metadata: InteractionMetadata = InteractionMetadata(),
    val sessionId: String = "", // Identificador único de la sesión del usuario
    val timestamp: Timestamp = Timestamp.now()
)

// Representa el objeto "metadata" dentro del JSON
data class InteractionMetadata(
    val source: String = "direct", // De dónde vino: "search_results", "home", "shared_link"
    val searchTerm: String? = null, // Si vino del buscador, qué buscó
    val timeSpent: Long = 0, // En segundos (lo calcularemos después)
    val imagesViewed: List<String> = emptyList() // Lista de imágenes que vio
)