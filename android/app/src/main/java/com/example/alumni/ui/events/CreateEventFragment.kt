package com.example.alumni.ui.events

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.alumni.R
import com.example.alumni.data.model.CreateEventRequest
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*

class CreateEventFragment : Fragment() {

    private val viewModel: EventsViewModel by viewModels()
    private lateinit var toolbar: MaterialToolbar
    private lateinit var cardEventImage: MaterialCardView
    private lateinit var ivEventImage: ImageView
    private lateinit var etEventTitle: TextInputEditText
    private lateinit var spinnerEventType: AutoCompleteTextView
    private lateinit var etEventDate: TextInputEditText
    private lateinit var etEventTime: TextInputEditText
    private lateinit var etEventLocation: TextInputEditText
    private lateinit var etMaxAttendees: TextInputEditText
    private lateinit var etEventDescription: TextInputEditText
    private lateinit var btnSave: MaterialButton

    private var selectedImageUri: String? = null
    private var selectedDate: Calendar = Calendar.getInstance()

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = uri.toString()
            Glide.with(requireContext())
                .load(uri)
                .placeholder(R.drawable.default_event_image)
                .error(R.drawable.default_event_image)
                .centerCrop()
                .into(ivEventImage)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupToolbar()
        setupEventTypeSpinner()
        setupClickListeners()
        setupObservers()
    }

    private fun initViews(view: View) {
        toolbar = view.findViewById(R.id.toolbar)
        cardEventImage = view.findViewById(R.id.cardEventImage)
        ivEventImage = view.findViewById(R.id.ivEventImage)
        etEventTitle = view.findViewById(R.id.etEventTitle)
        spinnerEventType = view.findViewById(R.id.spinnerEventType)
        etEventDate = view.findViewById(R.id.etEventDate)
        etEventTime = view.findViewById(R.id.etEventTime)
        etEventLocation = view.findViewById(R.id.etEventLocation)
        etMaxAttendees = view.findViewById(R.id.etMaxAttendees)
        etEventDescription = view.findViewById(R.id.etEventDescription)
        btnSave = view.findViewById(R.id.btnSave)
    }

    private fun setupToolbar() {
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupEventTypeSpinner() {
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.dropdown_item,
            Event.EVENT_TYPES
        )
        spinnerEventType.setAdapter(adapter)
    }

    private fun setupClickListeners() {
        cardEventImage.setOnClickListener {
            getContent.launch("image/*")
        }

        etEventDate.setOnClickListener {
            showDatePicker()
        }

        etEventTime.setOnClickListener {
            showTimePicker()
        }

        btnSave.setOnClickListener {
            if (validateInputs()) {
                createEvent()
            }
        }
    }

    private fun setupObservers() {
        viewModel.event.observe(viewLifecycleOwner) { event ->
            event?.let {
                // Navigate to event details
                val action = CreateEventFragmentDirections
                    .actionCreateEventToEventDetails(event.id)
                findNavController().navigate(action)
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                selectedDate.set(Calendar.YEAR, year)
                selectedDate.set(Calendar.MONTH, month)
                selectedDate.set(Calendar.DAY_OF_MONTH, day)
                updateDateTimeFields()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            requireContext(),
            { _, hour, minute ->
                selectedDate.set(Calendar.HOUR_OF_DAY, hour)
                selectedDate.set(Calendar.MINUTE, minute)
                updateDateTimeFields()
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        ).show()
    }

    private fun updateDateTimeFields() {
        val dateFormat = SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

        etEventDate.setText(dateFormat.format(selectedDate.time))
        etEventTime.setText(timeFormat.format(selectedDate.time))
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        if (etEventTitle.text.isNullOrBlank()) {
            etEventTitle.error = "Title is required"
            isValid = false
        }

        if (spinnerEventType.text.isNullOrBlank()) {
            spinnerEventType.error = "Event type is required"
            isValid = false
        }

        if (etEventDate.text.isNullOrBlank()) {
            etEventDate.error = "Date is required"
            isValid = false
        }

        if (etEventTime.text.isNullOrBlank()) {
            etEventTime.error = "Time is required"
            isValid = false
        }

        if (etEventLocation.text.isNullOrBlank()) {
            etEventLocation.error = "Location is required"
            isValid = false
        }

        if (etEventDescription.text.isNullOrBlank()) {
            etEventDescription.error = "Description is required"
            isValid = false
        }

        return isValid
    }

    private fun createEvent() {
        val dateFormat = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            Locale.getDefault()
        ).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        val request = CreateEventRequest(
            title = etEventTitle.text.toString(),
            description = etEventDescription.text.toString(),
            date = dateFormat.format(selectedDate.time),
            location = etEventLocation.text.toString(),
            type = spinnerEventType.text.toString(),
            imageUrl = selectedImageUri,
            maxAttendees = etMaxAttendees.text?.toString()?.toIntOrNull()
        )

        viewModel.createEvent(request)
    }
}
