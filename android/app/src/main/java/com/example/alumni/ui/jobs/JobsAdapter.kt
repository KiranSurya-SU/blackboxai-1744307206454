package com.example.alumni.ui.jobs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.alumni.R
import com.example.alumni.data.model.Job
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*

class JobsAdapter(
    private val onJobClick: (Job) -> Unit,
    private val onApplyClick: (Job) -> Unit,
    private val onSaveClick: (Job) -> Unit
) : ListAdapter<Job, JobsAdapter.JobViewHolder>(JobDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_job, parent, false)
        return JobViewHolder(view)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class JobViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvJobTitle: TextView = itemView.findViewById(R.id.tvJobTitle)
        private val tvCompany: TextView = itemView.findViewById(R.id.tvCompany)
        private val tvLocation: TextView = itemView.findViewById(R.id.tvLocation)
        private val tvJobType: TextView = itemView.findViewById(R.id.tvJobType)
        private val tvRequirements: TextView = itemView.findViewById(R.id.tvRequirements)
        private val tvPostedBy: TextView = itemView.findViewById(R.id.tvPostedBy)
        private val tvPostedDate: TextView = itemView.findViewById(R.id.tvPostedDate)
        private val btnApply: MaterialButton = itemView.findViewById(R.id.btnApply)
        private val btnSave: MaterialButton = itemView.findViewById(R.id.btnSave)

        fun bind(job: Job) {
            tvJobTitle.text = job.title
            tvCompany.text = job.company
            tvLocation.text = job.location
            tvJobType.text = job.type
            tvRequirements.text = "Requirements: ${job.requirements.joinToString(", ")}"
            tvPostedBy.text = "Posted by ${job.postedBy.name}"
            tvPostedDate.text = formatDate(job.createdAt)

            // Set click listeners
            itemView.setOnClickListener { onJobClick(job) }
            btnApply.setOnClickListener { onApplyClick(job) }
            btnSave.setOnClickListener { onSaveClick(job) }

            // Update save button icon based on saved status
            btnSave.icon = itemView.context.getDrawable(
                if (job.isSaved) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark_border
            )
        }

        private fun formatDate(dateString: String): String {
            try {
                val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                format.timeZone = TimeZone.getTimeZone("UTC")
                val date = format.parse(dateString)
                
                val now = Date()
                val diff = now.time - (date?.time ?: now.time)
                val days = diff / (24 * 60 * 60 * 1000)

                return when {
                    days == 0L -> "Today"
                    days == 1L -> "Yesterday"
                    days < 7 -> "$days days ago"
                    days < 30 -> "${days / 7} weeks ago"
                    else -> SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)
                }
            } catch (e: Exception) {
                return dateString
            }
        }
    }

    class JobDiffCallback : DiffUtil.ItemCallback<Job>() {
        override fun areItemsTheSame(oldItem: Job, newItem: Job): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Job, newItem: Job): Boolean {
            return oldItem == newItem
        }
    }
}

// Extension property for saved status (implement in Job data class)
private val Job.isSaved: Boolean
    get() = false // Implement actual saved status logic
