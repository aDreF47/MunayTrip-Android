package com.dsm.munaytripandroid.feature.bookings.domain.model

import com.dsm.munaytripandroid.feature.offer.domain.model.Offer
import java.util.Date

data class Favorite(
    val id: String,
    val userId: String,
    val offerId: String,
    val offer: Offer,
    val addedAt: Date = Date()
){
    constructor() : this("", "", "", Offer(), Date())
}