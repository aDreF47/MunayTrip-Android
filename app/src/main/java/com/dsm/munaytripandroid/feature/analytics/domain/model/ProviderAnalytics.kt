package com.dsm.munaytripandroid.feature.analytics.domain.model

import com.google.firebase.Timestamp

data class ProviderAnalytics(
    val analyticsId: String = "",
    val providerId: String = "",
    val offerId: String = "",
    val totalViews: Int = 0,
    val totalClicks: Int = 0,
    val totalBookings: Int = 0,
    val totalAttendance: Int = 0,
    val totalInterest: Int = 0,
    val conversionRate: Double = 0.0,
    val imageEngagement: ImageEngagement = ImageEngagement(),
    val topSearchTerms: List<String> = emptyList(),
    val peakHours: List<Int> = emptyList(),
    val period: String = "monthly", // daily, weekly, monthly, yearly
    val lastUpdated: Timestamp = Timestamp.now()
)

data class ImageEngagement(
    val totalImageViews: Int = 0,
    val avgTimePerImage: Double = 0.0,
    val mostViewedImage: String = ""
)