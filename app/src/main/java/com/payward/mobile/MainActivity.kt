package com.payward.mobile

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.payward.mobile.dto.LocationDetails
import com.payward.mobile.dto.Request
import com.payward.mobile.dto.User

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private var requestList = ArrayList<Request>()
    private lateinit var auth: FirebaseAuth
    private lateinit var logoutBtn: Button
    private lateinit var appViewModel: AppViewModel
    private lateinit var locationDetails: LocationDetails

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser == null) {
            val intent = Intent(this, MainFragment::class.java)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Already logged in", Toast.LENGTH_LONG).show()
        }

        logoutBtn = findViewById(R.id.logout_btn)

        logoutBtn.setOnClickListener{
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, MainFragment::class.java)
            startActivity(intent)
            finish()
        }

        viewModel.initializeFirebase()

        var btnHelpRequest = findViewById<Button>(R.id.helpRequestBtn)
        btnHelpRequest.setOnClickListener {
            val intent = Intent(this, RequestActivity::class.java)
            startActivity(intent)
            finish()
        }

        var btnCharity = findViewById<Button>(R.id.charityBtn)
        btnCharity.setOnClickListener {
            val intent = Intent(this, MessageActivity::class.java)
            startActivity(intent)
            finish()
        }

        var rvRequests = findViewById<RecyclerView>(R.id.rvRequests)
        rvRequests.hasFixedSize()
        rvRequests.layoutManager = LinearLayoutManager(applicationContext)
        rvRequests.itemAnimator = DefaultItemAnimator()
        rvRequests.adapter = RequestsAdapter(requestList, R.layout.item_post)

        viewModel.requests.observeForever {
                requests ->
            requestList.removeAll(requestList)
            requestList.addAll(requests)
            rvRequests.adapter!!.notifyDataSetChanged()
        }


        requestLocationPermissions()
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

        inner class RequestsAdapter(val requests: ArrayList<Request>, val itemPost: Int) : RecyclerView.Adapter<MainActivity.RequestViewHolder>() {

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
        }

    inner class RequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        private var lblUserName: TextView = itemView.findViewById(R.id.username)
        private var lblDescription: TextView = itemView.findViewById(R.id.description)
        private var lblPoints: TextView = itemView.findViewById(R.id.points)
        private var btnRespond: Button = itemView.findViewById(R.id.btnRespond)

        fun updateRequest (request: Request) {

            lblUserName.text = request.userDisplayName
            lblDescription.text = request.text
            lblPoints.text = request.helpingPoints.toString()

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

