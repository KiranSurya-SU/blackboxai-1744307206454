package com.example.alumni.ui.events

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alumni.data.model.Event
import com.example.alumni.data.model.CreateEventRequest
import com.example.alumni.data.model.UpdateEventRequest
import com.example.alumni.data.repository.EventRepository
import com.example.alumni.data.repository.UserRepository
import com.example.alumni.network.ApiResponse
import kotlinx.coroutines.launch

class EventsViewModel : ViewModel() {
    private val eventRepository = EventRepository.getInstance()
    private val userRepository = UserRepository.getInstance()

    private val _events = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>> = _events

    private val _event = MutableLiveData<Event>()
    val event: LiveData<Event> = _event

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _searchResults = MutableLiveData<List<Event>>()
    val searchResults: LiveData<List<Event>> = _searchResults

    fun loadUpcomingEvents() {
        viewModelScope.launch {
            _loading.value = true
            when (val response = eventRepository.getUpcomingEvents()) {
                is ApiResponse.Success -> {
                    _events.value = response.data
                    _error.value = null
                }
                is ApiResponse.Error -> {
                    _error.value = response.exception.message
                }
            }
            _loading.value = false
        }
    }

    fun loadPastEvents() {
        viewModelScope.launch {
            _loading.value = true
            when (val response = eventRepository.getPastEvents()) {
                is ApiResponse.Success -> {
                    _events.value = response.data
                    _error.value = null
                }
                is ApiResponse.Error -> {
                    _error.value = response.exception.message
                }
            }
            _loading.value = false
        }
    }

    fun loadMyEvents() {
        viewModelScope.launch {
            _loading.value = true
            when (val response = eventRepository.getMyEvents()) {
                is ApiResponse.Success -> {
                    _events.value = response.data
                    _error.value = null
                }
                is ApiResponse.Error -> {
                    _error.value = response.exception.message
                }
            }
            _loading.value = false
        }
    }

    fun getEventById(eventId: String): LiveData<Event> {
        viewModelScope.launch {
            _loading.value = true
            when (val response = eventRepository.getEventById(eventId)) {
                is ApiResponse.Success -> {
                    _event.value = response.data
                    _error.value = null
                }
                is ApiResponse.Error -> {
                    _error.value = response.exception.message
                }
            }
            _loading.value = false
        }
        return event
    }

    fun createEvent(request: CreateEventRequest) {
        viewModelScope.launch {
            _loading.value = true
            when (val response = eventRepository.createEvent(request)) {
                is ApiResponse.Success -> {
                    _event.value = response.data
                    loadUpcomingEvents() // Refresh events list
                    _error.value = null
                }
                is ApiResponse.Error -> {
                    _error.value = response.exception.message
                }
            }
            _loading.value = false
        }
    }

    fun updateEvent(eventId: String, request: UpdateEventRequest) {
        viewModelScope.launch {
            _loading.value = true
            when (val response = eventRepository.updateEvent(eventId, request)) {
                is ApiResponse.Success -> {
                    _event.value = response.data
                    loadUpcomingEvents() // Refresh events list
                    _error.value = null
                }
                is ApiResponse.Error -> {
                    _error.value = response.exception.message
                }
            }
            _loading.value = false
        }
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            _loading.value = true
            when (val response = eventRepository.deleteEvent(eventId)) {
                is ApiResponse.Success -> {
                    loadUpcomingEvents() // Refresh events list
                    _error.value = null
                }
                is ApiResponse.Error -> {
                    _error.value = response.exception.message
                }
            }
            _loading.value = false
        }
    }

    fun registerForEvent(eventId: String) {
        viewModelScope.launch {
            _loading.value = true
            when (val response = eventRepository.registerForEvent(eventId)) {
                is ApiResponse.Success -> {
                    _event.value = response.data
                    loadUpcomingEvents() // Refresh events list
                    _error.value = null
                }
                is ApiResponse.Error -> {
                    _error.value = response.exception.message
                }
            }
            _loading.value = false
        }
    }

    fun unregisterFromEvent(eventId: String) {
        viewModelScope.launch {
            _loading.value = true
            when (val response = eventRepository.unregisterFromEvent(eventId)) {
                is ApiResponse.Success -> {
                    _event.value = response.data
                    loadUpcomingEvents() // Refresh events list
                    _error.value = null
                }
                is ApiResponse.Error -> {
                    _error.value = response.exception.message
                }
            }
            _loading.value = false
        }
    }

    fun searchEvents(query: String) {
        viewModelScope.launch {
            _loading.value = true
            when (val response = eventRepository.searchEvents(query)) {
                is ApiResponse.Success -> {
                    _searchResults.value = response.data
                    _error.value = null
                }
                is ApiResponse.Error -> {
                    _error.value = response.exception.message
                }
            }
            _loading.value = false
        }
    }

    fun isCurrentUserOrganizer(event: Event): Boolean {
        val currentUser = userRepository.getCurrentUser()
        return currentUser?.id == event.organizer.id
    }

    fun isUserRegistered(eventId: String): Boolean {
        val currentUser = userRepository.getCurrentUser()
        return _event.value?.isAttending(currentUser?.id ?: "") ?: false
    }

    fun clearError() {
        _error.value = null
    }
}
