package com.example.alumni.ui.events

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.alumni.R
import com.example.alumni.data.model.Event
import com.google.android.material.chip.Chip
import java.text.SimpleDateFormat
import java.util.*

class EventsAdapter(
    private val onEventClick: (Event) -> Unit,
    private val onRegisterClick: (Event) -> Unit
) : ListAdapter<Event, EventsAdapter.EventViewHolder>(EventDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivEventImage: ImageView = itemView.findViewById(R.id.ivEventImage)
        private val tvEventTitle: TextView = itemView.findViewById(R.id.tvEventTitle)
        private val chipEventType: Chip = itemView.findViewById(R.id.chipEventType)
        private val tvEventDateTime: TextView = itemView.findViewById(R.id.tvEventDateTime)
        private val tvEventLocation: TextView = itemView.findViewById(R.id.tvEventLocation)
        private val tvAttendeesCount: TextView = itemView.findViewById(R.id.tvAttendeesCount)
        private val btnRegister: TextView = itemView.findViewById(R.id.btnRegister)

        fun bind(event: Event) {
            // Load event image
            Glide.with(itemView.context)
                .load(event.imageUrl)
                .placeholder(R.drawable.default_event_image)
                .error(R.drawable.default_event_image)
                .centerCrop()
                .into(ivEventImage)

            // Set event details
            tvEventTitle.text = event.title
            chipEventType.text = event.type
            tvEventDateTime.text = formatDateTime(event.date)
            tvEventLocation.text = event.location

            // Set attendees count
            val attendeesText = if (event.maxAttendees != null) {
                "${event.attendees.size}/${event.maxAttendees} attending"
            } else {
                "${event.attendees.size} attending"
            }
            tvAttendeesCount.text = attendeesText

            // Update register button state
            updateRegisterButton(event)

            // Set click listeners
            itemView.setOnClickListener { onEventClick(event) }
            btnRegister.setOnClickListener { onRegisterClick(event) }
        }

        private fun updateRegisterButton(event: Event) {
            btnRegister.apply {
                isEnabled = event.isActive() && event.hasAvailableSpots()
                text = when {
                    !event.isActive() -> "Event Ended"
                    !event.hasAvailableSpots() -> "Full"
                    else -> "Register"
                }
            }
        }

        private fun formatDateTime(dateString: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                val outputFormat = SimpleDateFormat("EEE, MMM dd • hh:mm a", Locale.getDefault())
                inputFormat.timeZone = TimeZone.getTimeZone("UTC")
                outputFormat.timeZone = TimeZone.getDefault()
                
                val date = inputFormat.parse(dateString)
                outputFormat.format(date!!)
            } catch (e: Exception) {
                dateString
            }
        }
    }

    class EventDiffCallback : DiffUtil.ItemCallback<Event>() {
        override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem == newItem
        }
    }
}
