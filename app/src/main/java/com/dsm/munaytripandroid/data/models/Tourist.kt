package com.dsm.munaytripandroid.data.models

import com.google.firebase.Timestamp

data class Tourist(
    val uid: String = "",
    val nombre: String = "",
    val email: String = "",
    val preferencias: List<String> = emptyList(), // ["cultura", "gastronomia"]
    val idioma: String = "es",
    val ubicacionActual: Map<String, Double> = mapOf(), // lat, lng

    // 🎮 Gamificación
    val puntos: Int = 0,
    val nivel: Int = 1,
    val badges: List<String> = emptyList(),
    val racha: Int = 0, // días consecutivos activos

    // 📊 Estadísticas
    val totalReservas: Int = 0,
    val reviewsEscritas: Int = 0,

    val createdAt: Timestamp = Timestamp.now(),
    val lastActive: Timestamp = Timestamp.now()
)