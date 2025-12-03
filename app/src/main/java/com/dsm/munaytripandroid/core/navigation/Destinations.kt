package com.dsm.munaytripandroid.core.navigation

import kotlinx.serialization.Serializable

// ==================== ONBOARDING & AUTH ====================
@Serializable
object Splash

@Serializable
object Initial

@Serializable
object Register

@Serializable
object RegisterMail

//@Serializable
//object RegisterPhone
//@Serializable
//object RegisterGoogle

@Serializable
object Login

// ==================== HOME (Dispatcher) ====================
@Serializable
object Home  // Este será el dispatcher que detecta userType

// ==================== CLIENT HOME ====================
@Serializable
object ClientHome

// ==================== PROVIDER HOME ====================
@Serializable
object ProviderHome

// ==================== OFFERS (Client View) ====================
@Serializable
object OffersList  // Lista de todas las ofertas (para clients)

@Serializable
data class OfferDetail(
    val offerId: String
)

// ==================== OFFERS (Provider View) ====================
@Serializable
object MyOffers  // Mis ofertas (solo providers)

@Serializable
object CreateOffer  // Crear nueva oferta (solo providers)

@Serializable
data class EditOffer(
    val offerId: String
)

// ==================== PROFILE ====================
@Serializable
object Profile

@Serializable
object Foot

// ==================== BOOKINGS ====================
@Serializable
object Booking

// ==================== FAVORITES ====================
@Serializable
object Favorite

// ==================== SEARCH (Futuro) ====================
//@Serializable
//object Search

// ==================== BOOKINGS (Futuro) ====================
//@Serializable
//object Bookings
//
//@Serializable
//data class BookingDetail(
//    val bookingId: String
//)
//


/**
 * RESUMEN DE DESTINOS:
 *
 * PÚBLICOS (sin auth):
 * - Splash, Initial, Login, Register, RegisterMail
 *
 * PRIVADOS (requieren auth):
 * - Home (dispatcher)
 * - ClientHome (viajeros)
 * - ProviderHome (proveedores)
 *
 * OFERTAS:
 * - OffersList (clients ven todas)
 * - OfferDetail (detalle individual)
 * - MyOffers (providers ven las suyas)
 * - CreateOffer (providers crean)
 * - EditOffer (providers editan)
 *
 * OTROS:
 * - Profile, Search, Bookings, Favorites
 */