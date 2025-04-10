package com.example.alumni.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.alumni.data.model.UserRole
import com.example.alumni.data.repository.UserRepository
import com.example.alumni.network.ApiResponse
import com.example.alumni.network.RegisterRequest
import com.example.alumni.network.RegisterResponse
import com.example.alumni.network.safeApiCall
import com.example.alumni.utils.SessionManager
import kotlinx.coroutines.launch

class RegisterViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository = UserRepository()
    private val sessionManager = SessionManager(application)

    private val _registrationResult = MutableLiveData<RegistrationResult>()
    val registrationResult: LiveData<RegistrationResult> = _registrationResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun register(
        name: String,
        email: String,
        password: String,
        role: UserRole,
        graduationYear: String,
        department: String
    ) {
        if (!validateInput(name, email, password, graduationYear, department)) {
            return
        }

        viewModelScope.launch {
            _isLoading.value = true

            val registerRequest = RegisterRequest(
                name = name,
                email = email,
                password = password,
                role = role.toString(),
                graduationYear = graduationYear,
                department = department
            )

            when (val response = safeApiCall { 
                userRepository.register(registerRequest)
            }) {
                is ApiResponse.Success -> {
                    handleSuccessfulRegistration(response.data)
                }
                is ApiResponse.Error -> {
                    handleRegistrationError(response.exception)
                }
            }

            _isLoading.value = false
        }
    }

    private fun validateInput(
        name: String,
        email: String,
        password: String,
        graduationYear: String,
        department: String
    ): Boolean {
        if (name.isEmpty()) {
            _registrationResult.value = RegistrationResult.Error("Please enter your name")
            return false
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _registrationResult.value = RegistrationResult.Error("Please enter a valid email address")
            return false
        }

        if (password.isEmpty() || password.length < 6) {
            _registrationResult.value = RegistrationResult.Error("Password must be at least 6 characters")
            return false
        }

        if (graduationYear.isEmpty()) {
            _registrationResult.value = RegistrationResult.Error("Please enter your graduation year")
            return false
        }

        if (department.isEmpty()) {
            _registrationResult.value = RegistrationResult.Error("Please enter your department")
            return false
        }

        return true
    }

    private fun handleSuccessfulRegistration(registerResponse: RegisterResponse) {
        sessionManager.apply {
            saveAuthToken(registerResponse.token)
            saveUser(registerResponse.user)
        }
        _registrationResult.value = RegistrationResult.Success
    }

    private fun handleRegistrationError(exception: Exception) {
        val errorMessage = when (exception) {
            is NetworkException -> "Network error. Please check your connection."
            is ValidationException -> "Registration failed: ${exception.message}"
            else -> "An unexpected error occurred during registration"
        }
        _registrationResult.value = RegistrationResult.Error(errorMessage)
    }
}

sealed class RegistrationResult {
    object Success : RegistrationResult()
    data class Error(val message: String) : RegistrationResult()
}
