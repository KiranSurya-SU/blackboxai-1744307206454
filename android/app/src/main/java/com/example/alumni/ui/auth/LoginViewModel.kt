package com.example.alumni.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.alumni.data.repository.UserRepository
import com.example.alumni.network.ApiResponse
import com.example.alumni.network.LoginRequest
import com.example.alumni.network.LoginResponse
import com.example.alumni.network.safeApiCall
import com.example.alumni.utils.SessionManager
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository = UserRepository()
    private val sessionManager = SessionManager(application)

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun login(email: String, password: String) {
        if (!validateInput(email, password)) {
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            
            when (val response = safeApiCall { 
                userRepository.login(LoginRequest(email, password))
            }) {
                is ApiResponse.Success -> {
                    handleSuccessfulLogin(response.data)
                }
                is ApiResponse.Error -> {
                    handleLoginError(response.exception)
                }
            }
            
            _isLoading.value = false
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _loginResult.value = LoginResult.Error("Please enter a valid email address")
            return false
        }

        if (password.isEmpty() || password.length < 6) {
            _loginResult.value = LoginResult.Error("Password must be at least 6 characters")
            return false
        }

        return true
    }

    private fun handleSuccessfulLogin(loginResponse: LoginResponse) {
        sessionManager.apply {
            saveAuthToken(loginResponse.token)
            saveUser(loginResponse.user)
        }
        _loginResult.value = LoginResult.Success
    }

    private fun handleLoginError(exception: Exception) {
        val errorMessage = when (exception) {
            is NetworkException -> "Network error. Please check your connection."
            is AuthenticationException -> "Invalid email or password"
            else -> "An unexpected error occurred"
        }
        _loginResult.value = LoginResult.Error(errorMessage)
    }

    fun isUserLoggedIn(): Boolean {
        return sessionManager.isLoggedIn()
    }
}

sealed class LoginResult {
    object Success : LoginResult()
    data class Error(val message: String) : LoginResult()
}
