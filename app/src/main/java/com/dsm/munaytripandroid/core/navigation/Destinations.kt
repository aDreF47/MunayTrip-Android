package com.dsm.munaytripandroid.core.navigation

import kotlinx.serialization.Serializable

/**
 * Según Google 2025: Usa @Serializable para crear rutas type-safe
 * Esto previene errores en tiempo de ejecución y proporciona autocompletado
 */

// Destinos sin argumentos (Objects)
@Serializable
object Splash

@Serializable
object Initial

@Serializable
object Login

@Serializable
object Register

@Serializable
object Home
//
//@Serializable
//object Search
//
//@Serializable
//object Bookings
//
//@Serializable
//object Profile
//
//@Serializable
//object CreateOffer
//
//
//// Destinos con argumentos (Data Classes)
//@Serializable
//data class TripDetail(
//    val tripId: String,
//    val tripName: String? = null  // Opcional
//)
//
//@Serializable
//data class OfferDetail(
//    val offerId: String
//)
//
//@Serializable
//data class EditProfile(
//    val userId: String
//)
//
//@Serializable
//data class BookingDetail(
//    val bookingId: String,
//    val isEditable: Boolean = false  // Argumento opcional con valor default
//)