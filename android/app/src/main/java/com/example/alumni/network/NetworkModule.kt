package com.example.alumni.network

import android.content.Context
import com.example.alumni.utils.SessionManager
import okhttp3.OkHttpClient
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {
    private const val BASE_URL = "http://your-backend-url:3000/api/"
    private const val TIMEOUT = 30L

    fun provideApiService(context: Context): ApiService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(provideOkHttpClient(context))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    private fun provideOkHttpClient(context: Context): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(provideAuthInterceptor(context))
            .addInterceptor(provideLoggingInterceptor())
            .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
            .build()
    }

    private fun provideAuthInterceptor(context: Context): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()
            val sessionManager = SessionManager(context)
            
            // Skip authentication for login and register endpoints
            if (originalRequest.url.encodedPath.contains("/auth/login") ||
                originalRequest.url.encodedPath.contains("/auth/register")) {
                return@Interceptor chain.proceed(originalRequest)
            }

            // Add authentication header for other requests
            val token = sessionManager.getAuthToken()
            if (token != null) {
                val authenticatedRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
                return@Interceptor chain.proceed(authenticatedRequest)
            }

            chain.proceed(originalRequest)
        }
    }

    private fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }
}

// Custom exception classes
class NetworkException(message: String) : Exception(message)
class AuthenticationException(message: String) : Exception(message)
class ValidationException(message: String) : Exception(message)

// API response wrapper
sealed class ApiResponse<out T> {
    data class Success<out T>(val data: T) : ApiResponse<T>()
    data class Error(val exception: Exception) : ApiResponse<Nothing>()
}

// Extension function to handle API responses
suspend fun <T> safeApiCall(apiCall: suspend () -> T): ApiResponse<T> {
    return try {
        ApiResponse.Success(apiCall.invoke())
    } catch (throwable: Throwable) {
        when (throwable) {
            is retrofit2.HttpException -> {
                when (throwable.code()) {
                    401 -> ApiResponse.Error(AuthenticationException("Authentication failed"))
                    422 -> ApiResponse.Error(ValidationException("Validation failed"))
                    else -> ApiResponse.Error(NetworkException("Network error occurred"))
                }
            }
            else -> ApiResponse.Error(NetworkException(throwable.message ?: "Unknown error occurred"))
        }
    }
}
