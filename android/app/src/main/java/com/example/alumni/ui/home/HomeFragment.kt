package com.example.alumni.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.alumni.R
import com.google.android.material.card.MaterialCardView

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var rvRecentActivities: RecyclerView
    private lateinit var tvUserName: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        
        // Initialize ViewModel
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        // Initialize views
        tvUserName = root.findViewById(R.id.tvUserName)
        rvRecentActivities = root.findViewById(R.id.rvRecentActivities)

        // Setup RecyclerView
        rvRecentActivities.layoutManager = LinearLayoutManager(context)
        val adapter = RecentActivitiesAdapter()
        rvRecentActivities.adapter = adapter

        // Setup Quick Action Cards
        setupQuickActionCards(root)

        // Observe ViewModel data
        observeViewModelData(adapter)

        return root
    }

    private fun setupQuickActionCards(root: View) {
        // Jobs Card
        root.findViewById<MaterialCardView>(R.id.cardJobs).setOnClickListener {
            findNavController().navigate(R.id.navigation_jobs)
        }

        // Events Card
        root.findViewById<MaterialCardView>(R.id.cardEvents).setOnClickListener {
            findNavController().navigate(R.id.navigation_events)
        }

        // Chat Card
        root.findViewById<MaterialCardView>(R.id.cardChat).setOnClickListener {
            findNavController().navigate(R.id.navigation_chat)
        }

        // Donate Card
        root.findViewById<MaterialCardView>(R.id.cardDonate).setOnClickListener {
            findNavController().navigate(R.id.navigation_donations)
        }
    }

    private fun observeViewModelData(adapter: RecentActivitiesAdapter) {
        // Observe user data
        homeViewModel.userData.observe(viewLifecycleOwner) { user ->
            tvUserName.text = user.name
        }

        // Observe recent activities
        homeViewModel.recentActivities.observe(viewLifecycleOwner) { activities ->
            adapter.submitList(activities)
        }
    }
}

// Recent Activities Adapter
class RecentActivitiesAdapter : 
    RecyclerView.Adapter<RecentActivitiesAdapter.ActivityViewHolder>() {

    private var activities: List<RecentActivity> = emptyList()

    fun submitList(newList: List<RecentActivity>) {
        activities = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent_activity, parent, false)
        return ActivityViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        holder.bind(activities[position])
    }

    override fun getItemCount() = activities.size

    class ActivityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvActivityTitle)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvActivityDescription)
        private val tvTimestamp: TextView = itemView.findViewById(R.id.tvActivityTimestamp)

        fun bind(activity: RecentActivity) {
            tvTitle.text = activity.title
            tvDescription.text = activity.description
            tvTimestamp.text = activity.timestamp
        }
    }
}

// Data class for Recent Activity
data class RecentActivity(
    val id: String,
    val title: String,
    val description: String,
    val timestamp: String,
    val type: ActivityType
)

enum class ActivityType {
    JOB_POSTED,
    EVENT_CREATED,
    DONATION_MADE,
    CHAT_STARTED
}
