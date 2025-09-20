package com.dsm.munaytripandroid.data.models

import com.google.firebase.Timestamp

data class Booking(
    val bookingId: String = "",
    val turistaId: String = "",
    val offerId: String = "",
    val proveedorId: String = "",

    // 📋 Detalles de reserva
    val cantidadPersonas: Int = 1,
    val fechaReserva: Timestamp = Timestamp.now(),
    val fechaUso: Timestamp? = null,
    val notas: String = "",

    // 💰 Pago (simplificado sin gateway)
    val totalPagado: Double = 0.0,
    val metodoPago: String = "efectivo", // "efectivo", "transferencia", "gratuito"
    val estadoPago: PaymentStatus = PaymentStatus.PENDIENTE,

    // 📊 Estados
    val estado: BookingStatus = BookingStatus.PENDIENTE,

    // ⭐ Post-reserva
    val calificado: Boolean = false,
    val reviewId: String? = null,

    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

enum class BookingStatus {
    PENDIENTE, CONFIRMADO, COMPLETADO, CANCELADO, NO_SHOW
}

enum class PaymentStatus {
    PENDIENTE, PAGADO, REEMBOLSADO
}