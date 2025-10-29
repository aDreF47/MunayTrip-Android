package com.dsm.munaytripandroid.feature.bookings.data.repository

import com.dsm.munaytripandroid.feature.bookings.domain.model.Booking
import com.dsm.munaytripandroid.feature.bookings.domain.model.BookingStatus
import com.dsm.munaytripandroid.feature.bookings.domain.repository.BookingRepository
import com.dsm.munaytripandroid.feature.offer.domain.model.Offer
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class BookingRepositoryImpl : BookingRepository {

    private val db = FirebaseFirestore.getInstance()
    private val bookingsCollection = db.collection("bookings")
    private val offersCollection = db.collection("offers")

    override suspend fun createBooking(
        userId: String,
        offerId: String,
        offer: Offer,
        status: String
    ): String {
        val booking = Booking(
            userId = userId,
            offerId = offerId,
            offer = offer,
            status = status
        )

        val document = bookingsCollection.document(booking.id)
        document.set(booking).await()

        decreaseOfferAvailability(offerId)

        return booking.id
    }

    override suspend fun getUserBookings(userId: String): List<Booking> {
        return try {
            val result = bookingsCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()

            result.documents.mapNotNull { document ->
                try {
                    document.toObject(Booking::class.java)
                } catch (e: Exception) {
                    null
                }
            }.sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getBookingStatus(userId: String): BookingStatus {
        val bookings = getUserBookings(userId)
        return BookingStatus(
            confirmed = bookings.count { it.status == "confirmed" },
            cancelled = bookings.count { it.status == "cancelled" },
            completed = bookings.count { it.status == "completed" }
        )
    }

    override suspend fun cancelBooking(bookingId: String) {
        val bookingDoc = bookingsCollection.document(bookingId).get().await()
        val booking = bookingDoc.toObject(Booking::class.java)

        if (booking != null) {
            increaseOfferAvailability(booking.offerId)

            bookingsCollection
                .document(bookingId)
                .update("status", "cancelled")
                .await()
        }
    }

    override suspend fun decreaseOfferAvailability(offerId: String) {
        try {
            offersCollection
                .document(offerId)
                .update("cuposDisponibles", FieldValue.increment(-1))
                .await()
        } catch (e: Exception) {
            throw Exception("Error al descontar cupo: ${e.message}")
        }
    }

    override suspend fun increaseOfferAvailability(offerId: String) {
        try {
            offersCollection
                .document(offerId)
                .update("cuposDisponibles", FieldValue.increment(1))
                .await()
        } catch (e: Exception) {
            throw Exception("Error al aumentar cupo: ${e.message}")
        }
    }
}