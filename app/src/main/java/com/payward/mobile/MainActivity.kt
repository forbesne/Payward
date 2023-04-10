package com.payward.mobile

import android.Manifest
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.DynamicColors
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.payward.mobile.dto.LocationDetails
import com.payward.mobile.dto.Request
import com.payward.mobile.dto.User
import java.math.RoundingMode
import kotlin.properties.Delegates

class MainActivity : PaywardActivity() {

    private var requestList = ArrayList<Request>()
    private var requestListFiltered = ArrayList<Request>()
    override lateinit var locationDetails: LocationDetails
    lateinit var categorySelected: String
    var milesSelected: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        DynamicColors.applyToActivitiesIfAvailable(application)
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_main)



        if (auth.currentUser == null) {
            val intent = Intent(this, MainFragment::class.java)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Already logged in", Toast.LENGTH_LONG).show()
        }

        initMenu()

        val rvRequests = findViewById<RecyclerView>(R.id.rvRequests)
        rvRequests.hasFixedSize()
        rvRequests.layoutManager = LinearLayoutManager(applicationContext)
        rvRequests.itemAnimator = DefaultItemAnimator()
        rvRequests.adapter = RequestsAdapter(requestListFiltered, R.layout.item_request)

        viewModel.requests.observeForever {
                requests ->
            requestList.removeAll(requestList.toSet())
            requestList.addAll(requests)
            requestListFiltered.removeAll(requestListFiltered.toSet())
            requestListFiltered.addAll(requests)
            rvRequests.adapter!!.notifyDataSetChanged()
        }

        val btnFilter = findViewById<Button>(R.id.filter_btn)
        val txtCategory = findViewById<AutoCompleteTextView>(R.id.txtCategory)
        btnFilter.setOnClickListener {
            milesSelected = findViewById<TextView>(R.id.txtDistance).text.toString().toDoubleOrNull()
            categorySelected = txtCategory.text.toString()
            if (categorySelected == "") {
                categorySelected = "All Categories"
            }
            (rvRequests.adapter as RequestsAdapter).filter.filter("10")
        }

//        requestLocationPermissions()
    }

    private fun requestLocationPermissions() {
        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    // Precise location access granted.
                    requestLocationUpdates()
                }
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    // Only approximate location access granted.
                } else -> {
                // No location access granted.
                Toast.makeText(this, "GPS Unavailable", Toast.LENGTH_LONG).show()
            }
            }
        }

// ...

// Before you perform the actual permission request, check whether your app
// already has the permissions, and whether your app needs to show a permission
// rationale dialog. For more details, see Request permissions.
        locationPermissionRequest.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION))
    }

        inner class RequestsAdapter(private val requests: ArrayList<Request>, private val itemPost: Int) : RecyclerView.Adapter<MainActivity.RequestViewHolder>(), Filterable {

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(itemPost, parent, false)
                return RequestViewHolder(view)
            }

            override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
                val request = requests[position]
                holder.updateRequest(request)
            }

            override fun getItemCount(): Int {
                return requests.size
            }

            override fun getFilter(): Filter {
                return requestFilter
            }

            private val requestFilter: Filter = object : Filter() {
                override fun performFiltering(constraint: CharSequence?): FilterResults {
                    var filteredList: MutableList<Request> = java.util.ArrayList()
                    val filterMiles = milesSelected
                    if ((filterMiles == null) && (categorySelected == "All Categories")) {
                        filteredList = requestList
                    } else if (filterMiles == null) {
                        for (request in requestList) {
                            if (categorySelected == request.issueType) {
                                filteredList.add(request)
                            }
                        }
                    } else {
                        for (request in requestList) {
                            val locationProvided = (request.latitude.isNotEmpty() && request.longitude.isNotEmpty())
                            if (((categorySelected == "All Categories") || (categorySelected == request.issueType)) && locationProvided) {
                                val milesDistance = getDistanceInMiles(
                                    request.latitude.toDouble(),
                                    request.longitude.toDouble(),
                                    locationDetails
                                )
                                if (milesDistance <= filterMiles) {
                                    filteredList.add(request)
                                }
                            }

                        }
                    }

                    val results = FilterResults()
                    results.values = filteredList
                    return results
                }

                override fun publishResults(constraint: CharSequence?, results: FilterResults) {
                    requestListFiltered.clear()
                    requestListFiltered.addAll((results.values as List<Request>))
                    notifyDataSetChanged()
                }
            }
        }

    inner class RequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        private var lblUserName: TextView = itemView.findViewById(R.id.username)
        private var lblDescription: TextView = itemView.findViewById(R.id.description)
        private var lblPoints: TextView = itemView.findViewById(R.id.points)
        private var btnRespond: Button = itemView.findViewById(R.id.btnRespond)
        private var lblCategory: TextView = itemView.findViewById(R.id.txtCategory)
        private var lblDistance: TextView = itemView.findViewById(R.id.distance)

        fun updateRequest (request: Request) {

            lblUserName.text = request.userDisplayName
            lblDescription.text = request.text
            lblPoints.text = request.helpingPoints.toString()
            lblCategory.text = request.issueType
            lblDistance.text = ""

            if (request.latitude.isNotEmpty() && request.longitude.isNotEmpty()) {

                if (::locationDetails.isInitialized) {
                    val milesDistance = getDistanceInMiles(request.latitude.toDouble(), request.longitude.toDouble(),locationDetails)
                    lblDistance.text = "$milesDistance miles"
                }
            }
            else {
                lblDistance.text = "Location not provided"
            }
            val user = auth.currentUser
            user?.let {
                btnRespond.isVisible = request.userId != user.uid
            }

            btnRespond.setOnClickListener {
                respondRequest(request)
            }

        }
    }

    private fun respondRequest(request: Request) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser != null) {
            val fromUid = firebaseUser.uid
            val rootRef = FirebaseFirestore.getInstance()
            val uidRef = rootRef.collection("users").document(fromUid)
            uidRef.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document.exists()) {
                        val fromUser = document.toObject(User::class.java)

                        val toUidRef = rootRef.collection("users").document(request.userId)
                        toUidRef.get().addOnCompleteListener { taskTo ->
                            if (taskTo.isSuccessful) {
                                val documentTo = taskTo.result
                                if (documentTo.exists()) {
                                    val toUser = documentTo.toObject(User::class.java)

                                    val intent = Intent(this, ChatActivity::class.java)
                                    intent.putExtra("fromUser", fromUser)
                                    intent.putExtra("toUser", toUser)
                                    intent.putExtra("roomId", "noRoomId")
                                    intent.putExtra("request", request)
                                    startActivity(intent)
                                    finish()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}

