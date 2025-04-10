package com.example.alumni.ui.events

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.example.alumni.R
import com.example.alumni.data.model.Event
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.LinearLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class EventsFragment : Fragment(), CoroutineScope {

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private val viewModel: EventsViewModel by viewModels()
    private lateinit var eventsAdapter: EventsAdapter
    private lateinit var tabLayout: TabLayout
    private lateinit var searchInput: TextInputEditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var emptyState: LinearLayout
    private lateinit var fabCreateEvent: ExtendedFloatingActionButton

    private var searchJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_events, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupRecyclerView()
        setupListeners()
        setupObservers()
        loadEvents()
    }

    private fun initViews(view: View) {
        tabLayout = view.findViewById(R.id.tabLayout)
        searchInput = view.findViewById(R.id.etSearch)
        recyclerView = view.findViewById(R.id.rvEvents)
        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        emptyState = view.findViewById(R.id.emptyState)
        fabCreateEvent = view.findViewById(R.id.fabCreateEvent)
    }

    private fun setupRecyclerView() {
        eventsAdapter = EventsAdapter(
            onEventClick = { event ->
                navigateToEventDetails(event)
            },
            onRegisterClick = { event ->
                handleEventRegistration(event)
            }
        )

        recyclerView.apply {
            adapter = eventsAdapter
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
        }
    }

    private fun setupListeners() {
        tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> viewModel.loadUpcomingEvents()
                    1 -> viewModel.loadPastEvents()
                    2 -> viewModel.loadMyEvents()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        searchInput.addTextChangedListener { text ->
            searchJob?.cancel()
            searchJob = launch {
                delay(300) // Debounce search
                text?.toString()?.let { query ->
                    if (query.isNotEmpty()) {
                        viewModel.searchEvents(query)
                    } else {
                        loadEvents() // Reset to current tab's events
                    }
                }
            }
        }

        swipeRefresh.setOnRefreshListener {
            loadEvents()
        }

        fabCreateEvent.setOnClickListener {
            findNavController().navigate(R.id.action_eventsFragment_to_createEventFragment)
        }
    }

    private fun setupObservers() {
        viewModel.events.observe(viewLifecycleOwner) { events ->
            handleEventsList(events)
        }

        viewModel.searchResults.observe(viewLifecycleOwner) { events ->
            handleEventsList(events)
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            swipeRefresh.isRefreshing = isLoading
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    private fun handleEventsList(events: List<Event>) {
        eventsAdapter.submitList(events)
        emptyState.isVisible = events.isEmpty()
        recyclerView.isVisible = events.isNotEmpty()
    }

    private fun loadEvents() {
        when (tabLayout.selectedTabPosition) {
            0 -> viewModel.loadUpcomingEvents()
            1 -> viewModel.loadPastEvents()
            2 -> viewModel.loadMyEvents()
        }
    }

    private fun navigateToEventDetails(event: Event) {
        val action = EventsFragmentDirections.actionEventsFragmentToEventDetails(event.id)
        findNavController().navigate(action)
    }

    private fun handleEventRegistration(event: Event) {
        if (event.isActive() && event.hasAvailableSpots()) {
            if (viewModel.isUserRegistered(event.id)) {
                viewModel.unregisterFromEvent(event.id)
            } else {
                viewModel.registerForEvent(event.id)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
