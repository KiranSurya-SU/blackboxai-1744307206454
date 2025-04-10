package com.example.alumni.data.repository

import com.example.alumni.data.model.Job
import com.example.alumni.network.ApiService
import com.example.alumni.network.ApiResponse
import com.example.alumni.network.CreateJobRequest
import com.example.alumni.network.JobsResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class JobRepository {

    private val apiService: ApiService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://your-backend-url:3000/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)
    }

    suspend fun getJobs(): ApiResponse<List<Job>> {
        return safeApiCall { apiService.getJobs() }
    }

    suspend fun createJob(job: CreateJobRequest): ApiResponse<Job> {
        return safeApiCall { apiService.createJob(job) }
    }

    // Additional job-related methods can be added here
}
