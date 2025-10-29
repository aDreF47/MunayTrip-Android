package com.dsm.munaytripandroid.feature.bookings.domain.model

import com.dsm.munaytripandroid.feature.offer.domain.model.Offer
import java.util.*

data class Booking(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val offerId: String,
    val offer: Offer,
    val status: String, // "confirmed", "cancelled", "completed"
    val bookingDate: Date = Date(),
    val createdAt: Date = Date()
){
    constructor() : this("", "", "", Offer(), "confirmed", Date(), Date())
}

data class BookingStatus(
    val confirmed: Int = 0,
    val cancelled: Int = 0,
    val completed: Int = 0
){
    constructor() : this(0, 0, 0)
}