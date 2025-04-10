package com.example.alumni.data.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val role: UserRole,
    val graduationYear: String,
    val department: String,
    val currentCompany: String? = null,
    val designation: String? = null,
    val location: String? = null,
    val skills: List<String> = emptyList(),
    val profileImage: String? = null
)

enum class UserRole {
    STUDENT,
    ALUMNI
}
