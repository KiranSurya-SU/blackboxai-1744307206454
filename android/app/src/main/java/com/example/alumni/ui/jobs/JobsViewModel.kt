package com.example.alumni.ui.jobs

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.alumni.data.model.Job
import com.example.alumni.data.repository.JobRepository
import com.example.alumni.network.ApiResponse
import com.example.alumni.utils.SessionManager
import kotlinx.coroutines.launch

class JobsViewModel(application: Application) : AndroidViewModel(application) {

    private val jobRepository = JobRepository()
    private val sessionManager = SessionManager(application)

    private val _jobs = MutableLiveData<List<Job>>()
    val jobs: LiveData<List<Job>> = _jobs

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var currentSearchQuery: String = ""
    private var currentFilters: Set<String> = emptySet()

    init {
        loadJobs()
    }

    fun loadJobs() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when (val response = jobRepository.getJobs()) {
                is ApiResponse.Success -> {
                    _jobs.value = response.data
                }
                is ApiResponse.Error -> {
                    _error.value = response.exception.message
                }
            }

            _isLoading.value = false
        }
    }

    fun searchJobs(query: String) {
        currentSearchQuery = query
        applyFilters()
    }

    fun updateFilters(filters: Set<String>) {
        currentFilters = filters
        applyFilters()
    }

    private fun applyFilters() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val filteredJobs = jobRepository.getJobs(
                    query = currentSearchQuery,
                    filters = currentFilters
                )
                _jobs.value = when (filteredJobs) {
                    is ApiResponse.Success -> filteredJobs.data
                    is ApiResponse.Error -> {
                        _error.value = filteredJobs.exception.message
                        emptyList()
                    }
                }
            } catch (e: Exception) {
                _error.value = "Error filtering jobs"
            }

            _isLoading.value = false
        }
    }

    fun createJob(job: Job) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when (val response = jobRepository.createJob(job)) {
                is ApiResponse.Success -> {
                    // Refresh job list after creating new job
                    loadJobs()
                }
                is ApiResponse.Error -> {
                    _error.value = response.exception.message
                }
            }

            _isLoading.value = false
        }
    }

    fun isUserAlumni(): Boolean {
        return sessionManager.getUser()?.role == UserRole.ALUMNI
    }

    fun clearError() {
        _error.value = null
    }
}
