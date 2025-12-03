package com.dsm.munaytripandroid.feature.bookings.domain.repository

import com.dsm.munaytripandroid.feature.bookings.domain.model.Booking
import com.dsm.munaytripandroid.feature.bookings.domain.model.BookingStatus


interface BookingRepository {
    suspend fun createBooking(
        userId: String,
        offerId: String,
        offer: com.dsm.munaytripandroid.feature.offer.domain.model.Offer,
        status: String
    ): String

    suspend fun getUserBookings(userId: String): List<Booking>
    suspend fun getBookingStatus(userId: String): BookingStatus
    suspend fun cancelBooking(bookingId: String)

    suspend fun decreaseOfferAvailability(offerId: String)
    suspend fun increaseOfferAvailability(offerId: String)
}