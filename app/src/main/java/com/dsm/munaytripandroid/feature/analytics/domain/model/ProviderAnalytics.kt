package com.dsm.munaytripandroid.feature.analytics.domain.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class ProviderAnalytics(
    @get:PropertyName("analytics_id")
    @set:PropertyName("analytics_id")
    var analyticsId: String = "",

    @get:PropertyName("provider_id")
    @set:PropertyName("provider_id")
    var providerId: String = "",

    @get:PropertyName("offer_id")
    @set:PropertyName("offer_id")
    var offerId: String = "",

    @get:PropertyName("total_views")
    @set:PropertyName("total_views")
    var totalViews: Int = 0,

    @get:PropertyName("total_clicks")
    @set:PropertyName("total_clicks")
    var totalClicks: Int = 0,

    @get:PropertyName("total_bookings")
    @set:PropertyName("total_bookings")
    var totalBookings: Int = 0,

    @get:PropertyName("total_attendance")
    @set:PropertyName("total_attendance")
    var totalAttendance: Int = 0,

    @get:PropertyName("total_interest")
    @set:PropertyName("total_interest")
    var totalInterest: Int = 0,

    @get:PropertyName("conversion_rate")
    @set:PropertyName("conversion_rate")
    var conversionRate: Double = 0.0,

    @get:PropertyName("image_engagement")
    @set:PropertyName("image_engagement")
    var imageEngagement: ImageEngagement = ImageEngagement(),

    @get:PropertyName("top_search_terms")
    @set:PropertyName("top_search_terms")
    var topSearchTerms: List<String> = emptyList(),

    @get:PropertyName("peak_hours")
    @set:PropertyName("peak_hours")
    var peakHours: List<Int> = emptyList(),

    @get:PropertyName("period")
    @set:PropertyName("period")
    var period: String = "monthly",

    @get:PropertyName("last_updated")
    @set:PropertyName("last_updated")
    var lastUpdated: Timestamp = Timestamp.now()
) {
    // Constructor sin argumentos requerido por Firestore
    constructor() : this(
        analyticsId = "",
        providerId = "",
        offerId = "",
        totalViews = 0,
        totalClicks = 0,
        totalBookings = 0,
        totalAttendance = 0,
        totalInterest = 0,
        conversionRate = 0.0,
        imageEngagement = ImageEngagement(),
        topSearchTerms = emptyList(),
        peakHours = emptyList(),
        period = "monthly",
        lastUpdated = Timestamp.now()
    )
}

data class ImageEngagement(
    @get:PropertyName("total_image_views")
    @set:PropertyName("total_image_views")
    var totalImageViews: Int = 0,

    @get:PropertyName("avg_time_per_image")
    @set:PropertyName("avg_time_per_image")
    var avgTimePerImage: Double = 0.0,

    @get:PropertyName("most_viewed_image")
    @set:PropertyName("most_viewed_image")
    var mostViewedImage: String = ""
) {
    // Constructor sin argumentos requerido por Firestore
    constructor() : this(
        totalImageViews = 0,
        avgTimePerImage = 0.0,
        mostViewedImage = ""
    )
}
