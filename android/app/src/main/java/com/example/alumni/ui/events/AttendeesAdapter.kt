package com.example.alumni.ui.events

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.alumni.R
import com.example.alumni.data.model.User
import com.google.android.material.imageview.ShapeableImageView

class AttendeesAdapter(
    private val onMessageClick: (User) -> Unit
) : ListAdapter<User, AttendeesAdapter.AttendeeViewHolder>(AttendeeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendeeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_attendee, parent, false)
        return AttendeeViewHolder(view)
    }

    override fun onBindViewHolder(holder: AttendeeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AttendeeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivProfileImage: ShapeableImageView = itemView.findViewById(R.id.ivProfileImage)
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvRole: TextView = itemView.findViewById(R.id.tvRole)
        private val btnMessage: ImageButton = itemView.findViewById(R.id.btnMessage)

        fun bind(user: User) {
            // Load profile image
            Glide.with(itemView.context)
                .load(user.profileImage)
                .placeholder(R.drawable.default_profile_image)
                .error(R.drawable.default_profile_image)
                .circleCrop()
                .into(ivProfileImage)

            // Set user details
            tvName.text = user.name
            tvRole.text = buildUserRole(user)

            // Set click listener for message button
            btnMessage.setOnClickListener { onMessageClick(user) }
        }

        private fun buildUserRole(user: User): String {
            return when {
                user.role.isNotEmpty() && user.company.isNotEmpty() -> 
                    "${user.role} at ${user.company}"
                user.role.isNotEmpty() -> user.role
                user.company.isNotEmpty() -> "Works at ${user.company}"
                else -> "Alumni"
            }
        }
    }

    class AttendeeDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }
}
