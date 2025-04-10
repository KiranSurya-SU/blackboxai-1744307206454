package com.example.alumni.ui.events

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.example.alumni.R
import com.example.alumni.data.model.Event
import com.example.alumni.ui.events.EventsViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView

class EventDetailsFragment : Fragment() {

    private lateinit var eventsViewModel: EventsViewModel
    private val args: EventDetailsFragmentArgs by navArgs()

    private lateinit var tvEventTitle: MaterialTextView
    private lateinit var tvEventDateTime: MaterialTextView
    private lateinit var tvEventLocation: MaterialTextView
    private lateinit var tvEventDescription: MaterialTextView
    private lateinit var btnRegister: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_event_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        eventsViewModel = ViewModelProvider(requireActivity()).get(EventsViewModel::class.java)

        // Initialize views
        initializeViews(view)

        // Load event details
        loadEventDetails(args.eventId)
    }

    private fun initializeViews(view: View) {
        tvEventTitle = view.findViewById(R.id.tvEventTitle)
        tvEventDateTime = view.findViewById(R.id.tvEventDateTime)
        tvEventLocation = view.findViewById(R.id.tvEventLocation)
        tvEventDescription = view.findViewById(R.id.tvEventDescription)
        btnRegister = view.findViewById(R.id.btnRegister)

        btnRegister.setOnClickListener {
            // Handle registration logic
            eventsViewModel.registerForEvent(args.eventId)
        }
    }

    private fun loadEventDetails(eventId: String) {
        // Fetch event details from ViewModel
        eventsViewModel.getEventById(eventId).observe(viewLifecycleOwner) { event ->
            if (event != null) {
                displayEventDetails(event)
            } else {
                Toast.makeText(context, "Event not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayEventDetails(event: Event) {
        tvEventTitle.text = event.title
        tvEventDateTime.text = event.date
        tvEventLocation.text = event.location
        tvEventDescription.text = event.description
    }
}
