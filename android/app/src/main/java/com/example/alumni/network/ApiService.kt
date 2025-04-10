package com.example.alumni.network

import com.example.alumni.data.model.User
import retrofit2.http.*

interface ApiService {
    // Auth endpoints
    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): LoginResponse

    @POST("auth/register")
    suspend fun register(@Body registerRequest: RegisterRequest): RegisterResponse

    // User endpoints
    @GET("users/profile")
    suspend fun getCurrentUser(): User

    @PUT("users/profile")
    suspend fun updateProfile(@Body user: User): User

    // Jobs endpoints
    @GET("jobs")
    suspend fun getJobs(): List<Job>

    @POST("jobs")
    suspend fun createJob(@Body job: Job): Job

    // Events endpoints
    @GET("events")
    suspend fun getEvents(): List<Event>

    @POST("events")
    suspend fun createEvent(@Body event: Event): Event

    @POST("events/{id}/register")
    suspend fun registerForEvent(@Path("id") eventId: String): Event

    // Donations endpoints
    @GET("donations/history")
    suspend fun getDonationHistory(): List<Donation>

    @POST("donations")
    suspend fun makeDonation(@Body donation: DonationRequest): Donation

    // Chat endpoints
    @GET("chat")
    suspend fun getChats(): List<Chat>

    @GET("chat/{id}")
    suspend fun getChatById(@Path("id") chatId: String): Chat

    @POST("chat/{id}/message")
    suspend fun sendMessage(@Path("id") chatId: String, @Body message: Message): Message
}

// Request/Response data classes
data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val user: User
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val role: String,
    val graduationYear: String,
    val department: String
)

data class RegisterResponse(
    val token: String,
    val user: User
)

data class Job(
    val id: String,
    val title: String,
    val company: String,
    val description: String,
    val requirements: List<String>,
    val location: String,
    val type: String,
    val postedBy: User,
    val createdAt: String
)

data class Event(
    val id: String,
    val title: String,
    val description: String,
    val date: String,
    val location: String,
    val type: String,
    val organizer: User,
    val attendees: List<User>
)

data class Donation(
    val id: String,
    val amount: Double,
    val purpose: String,
    val donor: User,
    val date: String,
    val status: String
)

data class DonationRequest(
    val amount: Double,
    val purpose: String,
    val paymentMethod: String
)

data class Chat(
    val id: String,
    val participants: List<User>,
    val messages: List<Message>,
    val type: String,
    val lastMessage: Message?
)

data class Message(
    val id: String,
    val sender: User,
    val content: String,
    val timestamp: String,
    val readBy: List<User>
)
