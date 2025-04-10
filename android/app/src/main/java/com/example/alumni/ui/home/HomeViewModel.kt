package com.example.alumni.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alumni.data.model.User
import com.example.alumni.data.repository.UserRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HomeViewModel : ViewModel() {

    private val userRepository = UserRepository()

    private val _userData = MutableLiveData<User>()
    val userData: LiveData<User> = _userData

    private val _recentActivities = MutableLiveData<List<RecentActivity>>()
    val recentActivities: LiveData<List<RecentActivity>> = _recentActivities

    init {
        loadUserData()
        loadRecentActivities()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            try {
                val user = userRepository.getCurrentUser()
                _userData.value = user
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private fun loadRecentActivities() {
        viewModelScope.launch {
            try {
                // This would typically come from a repository
                val activities = listOf(
                    RecentActivity(
                        id = "1",
                        title = "New Job Posted",
                        description = "Software Engineer position at Tech Corp",
                        timestamp = formatDate(Date()),
                        type = ActivityType.JOB_POSTED
                    ),
                    RecentActivity(
                        id = "2",
                        title = "Upcoming Event",
                        description = "Annual Alumni Meet 2024",
                        timestamp = formatDate(Date()),
                        type = ActivityType.EVENT_CREATED
                    ),
                    RecentActivity(
                        id = "3",
                        title = "Donation Received",
                        description = "Thank you for your contribution to the scholarship fund",
                        timestamp = formatDate(Date()),
                        type = ActivityType.DONATION_MADE
                    )
                )
                _recentActivities.value = activities
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private fun formatDate(date: Date): String {
        val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return formatter.format(date)
    }

    fun refreshData() {
        loadUserData()
        loadRecentActivities()
    }
}
