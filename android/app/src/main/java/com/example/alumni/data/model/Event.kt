package com.example.alumni.data.model

import java.util.*

data class Event(
    val id: String,
    val title: String,
    val description: String,
    val date: String,
    val location: String,
    val type: String,
    val imageUrl: String?,
    val organizer: User,
    val attendees: List<User>,
    val maxAttendees: Int?,
    val createdAt: String,
    val updatedAt: String
) {
    fun isActive(): Boolean {
        return try {
            val eventDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.parse(date)
            
            eventDate?.after(Date()) ?: false
        } catch (e: Exception) {
            false
        }
    }

    fun hasAvailableSpots(): Boolean {
        return maxAttendees == null || attendees.size < maxAttendees
    }

    fun isOrganizer(userId: String): Boolean {
        return organizer.id == userId
    }

    fun isAttending(userId: String): Boolean {
        return attendees.any { it.id == userId }
    }

    companion object {
        const val TYPE_NETWORKING = "Networking"
        const val TYPE_WORKSHOP = "Workshop"
        const val TYPE_SEMINAR = "Seminar"
        const val TYPE_SOCIAL = "Social"
        const val TYPE_CAREER_FAIR = "Career Fair"
        const val TYPE_OTHER = "Other"

        val EVENT_TYPES = listOf(
            TYPE_NETWORKING,
            TYPE_WORKSHOP,
            TYPE_SEMINAR,
            TYPE_SOCIAL,
            TYPE_CAREER_FAIR,
            TYPE_OTHER
        )
    }
}

data class CreateEventRequest(
    val title: String,
    val description: String,
    val date: String,
    val location: String,
    val type: String,
    val imageUrl: String? = null,
    val maxAttendees: Int? = null
)

data class UpdateEventRequest(
    val title: String? = null,
    val description: String? = null,
    val date: String? = null,
    val location: String? = null,
    val type: String? = null,
    val imageUrl: String? = null,
    val maxAttendees: Int? = null
)
