package com.example.alumni.data.model

import java.io.Serializable

data class Job(
    val id: String,
    val title: String,
    val company: String,
    val description: String,
    val requirements: List<String>,
    val location: String,
    val type: String,
    val salary: String?,
    val postedBy: User,
    val status: String,
    val applicationDeadline: String?,
    val experience: String?,
    val skills: List<String>,
    val applicationLink: String?,
    val contactEmail: String?,
    val createdAt: String,
    val updatedAt: String,
    var isSaved: Boolean = false
) : Serializable {

    companion object {
        const val TYPE_FULL_TIME = "full-time"
        const val TYPE_PART_TIME = "part-time"
        const val TYPE_INTERNSHIP = "internship"
        const val TYPE_CONTRACT = "contract"

        const val STATUS_ACTIVE = "active"
        const val STATUS_CLOSED = "closed"
    }

    fun isActive(): Boolean = status == STATUS_ACTIVE

    fun hasDeadlinePassed(): Boolean {
        return try {
            val deadline = applicationDeadline?.let { 
                java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                    .parse(it)?.time ?: 0 
            } ?: return false
            
            System.currentTimeMillis() > deadline
        } catch (e: Exception) {
            false
        }
    }

    fun getFormattedSalary(): String {
        return salary?.let { "Salary: $it" } ?: "Salary not specified"
    }

    fun getFormattedExperience(): String {
        return experience?.let { "Experience: $it" } ?: "Experience not specified"
    }

    fun getFormattedRequirements(): String {
        return requirements.joinToString("\n") { "• $it" }
    }

    fun getFormattedSkills(): String {
        return skills.joinToString(", ")
    }

    fun canApply(): Boolean {
        return isActive() && !hasDeadlinePassed()
    }

    fun getApplicationMethod(): ApplicationMethod {
        return when {
            !applicationLink.isNullOrBlank() -> ApplicationMethod.EXTERNAL_LINK
            !contactEmail.isNullOrBlank() -> ApplicationMethod.EMAIL
            else -> ApplicationMethod.IN_APP
        }
    }
}

enum class ApplicationMethod {
    IN_APP,
    EMAIL,
    EXTERNAL_LINK
}

// Request data classes for job operations
data class CreateJobRequest(
    val title: String,
    val company: String,
    val description: String,
    val requirements: List<String>,
    val location: String,
    val type: String,
    val salary: String?,
    val applicationDeadline: String?,
    val experience: String?,
    val skills: List<String>,
    val applicationLink: String?,
    val contactEmail: String?
)

data class UpdateJobRequest(
    val title: String?,
    val company: String?,
    val description: String?,
    val requirements: List<String>?,
    val location: String?,
    val type: String?,
    val salary: String?,
    val applicationDeadline: String?,
    val experience: String?,
    val skills: List<String>?,
    val applicationLink: String?,
    val contactEmail: String?,
    val status: String?
)

// Response data classes
data class JobResponse(
    val job: Job,
    val message: String
)

data class JobsResponse(
    val jobs: List<Job>,
    val total: Int,
    val page: Int,
    val totalPages: Int
)
