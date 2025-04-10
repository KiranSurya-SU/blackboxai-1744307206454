package com.example.alumni.data.repository

import com.example.alumni.data.model.Event
import com.example.alumni.data.model.CreateEventRequest
import com.example.alumni.data.model.UpdateEventRequest
import com.example.alumni.network.ApiResponse
import com.example.alumni.network.ApiService
import com.example.alumni.network.NetworkModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EventRepository {
    private val apiService: ApiService = NetworkModule.provideApiService()

    suspend fun getEvents(): ApiResponse<List<Event>> = withContext(Dispatchers.IO) {
        return@withContext safeApiCall { apiService.getEvents() }
    }

    suspend fun getEventById(eventId: String): ApiResponse<Event> = withContext(Dispatchers.IO) {
        return@withContext safeApiCall { apiService.getEventById(eventId) }
    }

    suspend fun createEvent(eventRequest: CreateEventRequest): ApiResponse<Event> = withContext(Dispatchers.IO) {
        return@withContext safeApiCall { apiService.createEvent(eventRequest) }
    }

    suspend fun updateEvent(eventId: String, updateRequest: UpdateEventRequest): ApiResponse<Event> = withContext(Dispatchers.IO) {
        return@withContext safeApiCall { apiService.updateEvent(eventId, updateRequest) }
    }

    suspend fun deleteEvent(eventId: String): ApiResponse<Unit> = withContext(Dispatchers.IO) {
        return@withContext safeApiCall { apiService.deleteEvent(eventId) }
    }

    suspend fun registerForEvent(eventId: String): ApiResponse<Event> = withContext(Dispatchers.IO) {
        return@withContext safeApiCall { apiService.registerForEvent(eventId) }
    }

    suspend fun unregisterFromEvent(eventId: String): ApiResponse<Event> = withContext(Dispatchers.IO) {
        return@withContext safeApiCall { apiService.unregisterFromEvent(eventId) }
    }

    suspend fun getMyEvents(): ApiResponse<List<Event>> = withContext(Dispatchers.IO) {
        return@withContext safeApiCall { apiService.getMyEvents() }
    }

    suspend fun getRegisteredEvents(): ApiResponse<List<Event>> = withContext(Dispatchers.IO) {
        return@withContext safeApiCall { apiService.getRegisteredEvents() }
    }

    suspend fun searchEvents(query: String): ApiResponse<List<Event>> = withContext(Dispatchers.IO) {
        return@withContext safeApiCall { apiService.searchEvents(query) }
    }

    suspend fun getUpcomingEvents(): ApiResponse<List<Event>> = withContext(Dispatchers.IO) {
        return@withContext safeApiCall { apiService.getUpcomingEvents() }
    }

    suspend fun getPastEvents(): ApiResponse<List<Event>> = withContext(Dispatchers.IO) {
        return@withContext safeApiCall { apiService.getPastEvents() }
    }

    companion object {
        private var instance: EventRepository? = null

        fun getInstance(): EventRepository {
            return instance ?: synchronized(this) {
                instance ?: EventRepository().also { instance = it }
            }
        }
    }
}
