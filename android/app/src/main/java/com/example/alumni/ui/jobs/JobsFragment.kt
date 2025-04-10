package com.example.alumni.ui.jobs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.alumni.R
import com.example.alumni.data.model.Job
import com.example.alumni.ui.auth.LoginActivity
import com.example.alumni.utils.SessionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class JobsFragment : Fragment() {

    private lateinit var jobsViewModel: JobsViewModel
    private lateinit var rvJobs: RecyclerView
    private lateinit var etSearch: TextInputEditText
    private lateinit var btnPostJob: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_jobs, container, false)

        // Initialize ViewModel
        jobsViewModel = ViewModelProvider(this).get(JobsViewModel::class.java)

        // Initialize views
        rvJobs = root.findViewById(R.id.rvJobs)
        etSearch = root.findViewById(R.id.etSearch)
        btnPostJob = root.findViewById(R.id.fabPostJob)

        // Setup RecyclerView
        rvJobs.layoutManager = LinearLayoutManager(context)
        val adapter = JobsAdapter()
        rvJobs.adapter = adapter

        // Setup click listener for post job button
        btnPostJob.setOnClickListener {
            // Navigate to post job screen
            // Implement navigation logic here
        }

        // Observe ViewModel data
        observeViewModelData(adapter)

        return root
    }

    private fun observeViewModelData(adapter: JobsAdapter) {
        // Observe job listings
        jobsViewModel.jobs.observe(viewLifecycleOwner) { jobs ->
            adapter.submitList(jobs)
        }

        // Observe loading state
        jobsViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Show/hide loading indicator if needed
        }

        // Observe error state
        jobsViewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
