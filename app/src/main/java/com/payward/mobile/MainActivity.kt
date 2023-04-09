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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.payward.mobile.dto.LocationDetails
import com.payward.mobile.dto.Request
import com.payward.mobile.dto.User
import java.math.RoundingMode
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private var requestList = ArrayList<Request>()
    private var requestListFiltered = ArrayList<Request>()
    private lateinit var auth: FirebaseAuth
    private lateinit var appViewModel: AppViewModel
    private lateinit var locationDetails: LocationDetails
    lateinit var categorySelected: String
    var milesSelected by Delegates.notNull<Double>()

    override fun onCreate(savedInstanceState: Bundle?) {
        DynamicColors.applyToActivitiesIfAvailable(application)
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_main)

        milesSelected = 10.0

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser == null) {
            val intent = Intent(this, MainFragment::class.java)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Already logged in", Toast.LENGTH_LONG).show()
        }


        viewModel.initializeFirebase()

        var btnHome = findViewById<Button>(R.id.homeBtn)
        btnHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        var btnHelpRequest = findViewById<Button>(R.id.helpRequestBtn)
        btnHelpRequest.setOnClickListener {
            val intent = Intent(this, RequestActivity::class.java)
            startActivity(intent)
            finish()
        }

        var btnMaps = findViewById<Button>(R.id.mapsBtn)
        btnMaps.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
            finish()
        }

        var btnMessages = findViewById<Button>(R.id.messagesBtn)
        btnMessages.setOnClickListener {
            val intent = Intent(this, MessageActivity::class.java)
            startActivity(intent)
            finish()
        }

        var btnProfile = findViewById<Button>(R.id.btnProfile)
        btnProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
            finish()
        }

        var rvRequests = findViewById<RecyclerView>(R.id.rvRequests)
        rvRequests.hasFixedSize()
        rvRequests.layoutManager = LinearLayoutManager(applicationContext)
        rvRequests.itemAnimator = DefaultItemAnimator()
        rvRequests.adapter = RequestsAdapter(requestListFiltered, R.layout.item_request)

        viewModel.requests.observeForever {
                requests ->
            requestList.removeAll(requestList)
            requestList.addAll(requests)
            requestListFiltered.removeAll(requestListFiltered)
            requestListFiltered.addAll(requests)
            rvRequests.adapter!!.notifyDataSetChanged()
        }

        var btnFilter = findViewById<Button>(R.id.filter_btn)
        var txtCategory = findViewById<AutoCompleteTextView>(R.id.txtCategory)
        btnFilter.setOnClickListener {
            categorySelected = txtCategory.text.toString()
            if (categorySelected == "") {
                categorySelected = "All Categories"
            }
            (rvRequests.adapter as RequestsAdapter).getFilter().filter("10")
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

        inner class RequestsAdapter(val requests: ArrayList<Request>, val itemPost: Int) : RecyclerView.Adapter<MainActivity.RequestViewHolder>(), Filterable {

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(itemPost, parent, false)
                return RequestViewHolder(view)
            }

            override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
                val request = requests.get(position)
                holder.updateRequest(request)
            }

            override fun getItemCount(): Int {
                return requests.size
            }

            override fun getFilter(): Filter {
                return requestFilter
            }

            private val requestFilter: Filter = object : Filter() {
                override fun performFiltering(constraint: CharSequence?): FilterResults? {
                    val filteredList: MutableList<Request> = java.util.ArrayList()
                    val filterMiles = milesSelected
                    for (request in requestList) {
                        if (request.rqStatus == "open") {
                            if (categorySelected == "All Categories") {
                                if (request.latitude.isNotEmpty() && request.longitude.isNotEmpty()) {
                                    val milesDistance = getDistanceInMiles(request.latitude.toDouble(), request.longitude.toDouble(), 39.13447904988019, -84.51552473741883)
                                    if (milesDistance < filterMiles) {
                                        filteredList.add(request)
                                    }

                                }
                            }
                            if (categorySelected == request.issueType) {
                                if (request.latitude.isNotEmpty() && request.longitude.isNotEmpty()) {
                                    val milesDistance = getDistanceInMiles(request.latitude.toDouble(), request.longitude.toDouble(), 39.13447904988019, -84.51552473741883)
                                    if (milesDistance < filterMiles) {
                                        filteredList.add(request)
                                    }
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
                val milesDistance = getDistanceInMiles(request.latitude.toDouble(), request.longitude.toDouble(), 39.29345029085822, -84.45687723750659)
                lblDistance.text = milesDistance.toString() + " miles"
            }
            else {
                lblDistance.text = "Location not provided"
            }
            var user = auth.currentUser
            user?.let {
                btnRespond.isVisible = request.userId != user.uid
            }

            btnRespond.setOnClickListener {
                respondRequest(request)
            }

        }
    }

    internal fun requestLocationUpdates() {
        appViewModel = ViewModelProvider(this).get(AppViewModel::class.java)

//        appViewModel.startLocationUpdates()

        appViewModel.getLocationLiveData().observeForever{
            locationDetails = it
        }

    }

    private fun getDistanceInMiles(firstLatitude: Double, firstLongitude: Double,
                                   secondLatitude: Double, secondLongitude: Double): Double {
        val resultMeters = FloatArray(1)
        Location.distanceBetween(firstLatitude, firstLongitude,
            secondLatitude, secondLongitude, resultMeters)
        val resultMiles = resultMeters[0]*0.000621371192
        val distanceMiles = (resultMiles).toBigDecimal().setScale(1, RoundingMode.UP).toDouble()
        return distanceMiles
    }

    private fun respondRequest(request: Request) {
       viewModel.respond(request)
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

                        val touidRef = rootRef.collection("users").document(request.userId)
                        touidRef.get().addOnCompleteListener { taskTo ->
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

