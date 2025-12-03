package com.dsm.munaytripandroid.feature.offer.domain.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

/**
 * Modelo de Oferta
 * Puede ser: evento, hospedaje, tour, experiencia, etc.
 */
data class Offer(
    val offerId: String = "",
    val providerId: String = "",
    val titulo: String = "",
    val descripcionCorta: String = "",
    val descripcionLarga: String = "",
    val categoria: OfferCategory = OfferCategory.CULTURAL,
    val etiquetas: List<String> = emptyList(),
    val ubicacion: OfferLocation = OfferLocation(),
    val duracion: String = "",
    val horarios: List<OfferHorario> = emptyList(),
    val precio: Double = 0.0,
    val esGratis: Boolean = false,
    val capacidadMaxima: Int = 0,
    val cuposDisponibles: Int = 0,
    val incluye: List<String> = emptyList(),
    val recomendaciones: List<String> = emptyList(),
    val descuento: Int = 0,
    val imageUrls: List<String> = emptyList(),
    val thumbnailUrls: List<String> = emptyList(),
    val tipoOferta: OfferType = OfferType.EVENT,
    val requiereConfirmacion: Boolean = false,
    val instruccionesPago: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val estado: String = "activo",  // activo, pausado, finalizado
    val pointsReward: Int = 50
){
    constructor() : this(
        "", "", "", "", "", OfferCategory.CULTURAL, emptyList(),
        OfferLocation(), "", emptyList(), 0.0, false, 0, 0,
        emptyList(), emptyList(), 0, emptyList(), emptyList(),
        OfferType.EVENT, false, "", null, null, "activo",50
    )
}

data class PointsToken(
    val id: String = "",
    val providerId: String = "",
    val offerId: String = "",
    val pointsAmount: Int = 0,
    val status: String = "PENDING", // Valores: "PENDING", "REDEEMED"
    val createdAt: Any? = null
)
data class OfferLocation(
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val nombre: String = "",
    val direccion: String = ""
)

data class OfferHorario(
    val dia: String = "",  // "lunes", "sabado", "todos"
    val horaInicio: String = "",  // "10:00"
    val horaFin: String = ""  // "18:00"
)

enum class OfferCategory(val displayName: String) {
    CULTURAL("Cultural"),
    HOSPEDAJE("Hospedaje"),
    GASTRONOMIA("Gastronomía"),
    AVENTURA("Aventura"),
    TOUR("Tour Guiado"),
    NATURALEZA("Naturaleza"),
    ARTE("Arte"),
    MUSICA("Música"),
    DEPORTE("Deporte"),
    FAMILIAR("Familiar")
}

enum class OfferType(val displayName: String) {
    EVENT("Evento"),  // Eventos, festividades
    RESERVATION("Reserva"),  // Hoteles, restaurantes
    TOUR("Tour"),  // Tours guiados
    EXPERIENCE("Experiencia"),  // Talleres, actividades
    SERVICE("Servicio")  // Servicios generales
}