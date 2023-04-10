package com.payward.mobile

import android.content.ContentValues.TAG
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.VectorDrawable
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.color.DynamicColors
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.payward.mobile.databinding.ActivityMapsBinding
import com.payward.mobile.dto.LocationDetails
import com.payward.mobile.dto.Request
import com.payward.mobile.dto.User
import java.math.RoundingMode


class MapsActivity : AppCompatActivity(), GoogleMap.OnMarkerClickListener, OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var previousMarker: Marker
    private lateinit var auth: FirebaseAuth
    private lateinit var appViewModel: AppViewModel
    private lateinit var locationDetails: LocationDetails
    private var firstClick: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        DynamicColors.applyToActivitiesIfAvailable(application)
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        viewModel.initializeFirebase()
        auth = FirebaseAuth.getInstance()
        appViewModel = ViewModelProvider(this)[AppViewModel::class.java]


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val details = findViewById<LinearLayout>(R.id.details)
        details.isVisible = false

        title = "Locations"

        val btnHome = findViewById<Button>(R.id.homeBtn)
        btnHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        val btnHelpRequest = findViewById<Button>(R.id.helpRequestBtn)
        btnHelpRequest.setOnClickListener {
            val intent = Intent(this, RequestActivity::class.java)
            startActivity(intent)
            finish()
        }

        val btnMaps = findViewById<Button>(R.id.mapsBtn)
        btnMaps.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
            finish()
        }

        val btnMessages = findViewById<Button>(R.id.messagesBtn)
        btnMessages.setOnClickListener {
            val intent = Intent(this, MessageActivity::class.java)
            startActivity(intent)
            finish()
        }

        val btnProfile = findViewById<Button>(R.id.btnProfile)
        btnProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this, R.raw.map_style
                )
            )
            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }

        /**
         * User's current location
         */
        appViewModel.getLocationLiveData().observeForever{
            locationDetails = it
//            val position = LatLng(locationDetails.latitude.toDouble(), locationDetails.longitude.toDouble())
            val position = LatLng(39.13447904988019, -84.51552473741883)
            mMap.addMarker(
                MarkerOptions()
                    .position(position)
                    .title("Your current location")
                    .icon(getBitmapDescriptor(R.drawable.baseline_man_4_24))
            )?.tag = "person"
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 12f))
        }

        /**
         * Add markers for each request
         */
        viewModel.requests.observeForever { requests ->
            for (request in requests) {
                if (request.latitude.isNotEmpty() && request.longitude.isNotEmpty()) {
                    val position = LatLng(request.latitude.toDouble(), request.longitude.toDouble())
                    mMap.addMarker(
                        MarkerOptions()
                            .position(position)
                            .icon(getBitmapDescriptor(R.drawable.person_pin_normal))
                    )?.tag = request
//                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 12f))
                }
            }
        }

        mMap.setOnMapClickListener {
            val details = findViewById<LinearLayout>(R.id.details)
            details.isVisible = false
            if (!firstClick) {
                previousMarker.setIcon(getBitmapDescriptor(R.drawable.person_pin_normal))
            }

        }
        // Set a listener for marker click.
        mMap.setOnMarkerClickListener(this)
    }

    override fun onMarkerClick(marker: Marker): Boolean {

        if (firstClick) {
            previousMarker = marker
            firstClick = false
        }
        previousMarker.setIcon(getBitmapDescriptor(R.drawable.person_pin_normal))

        if (marker.tag != "person") {
            val request = marker.tag as? Request

            val details = findViewById<LinearLayout>(R.id.details)
            details.isVisible = true

            marker.setIcon(getBitmapDescriptor(R.drawable.person_pin_focus))

            val lblUserName: TextView = findViewById(R.id.username)
            val lblDescription: TextView = findViewById(R.id.description)
            val lblPoints: TextView = findViewById(R.id.points)
            val btnRespond: Button = findViewById(R.id.btnRespond)
            val lblCategory: TextView = findViewById(R.id.txtCategory)
            val lblDistance: TextView = findViewById(R.id.distance)


            if (request != null) {
                lblUserName.text = request.userDisplayName
                lblDescription.text = request.text
                lblPoints.text = request.helpingPoints.toString()
                lblCategory.text = request.issueType
                lblDistance.text = ""

                if (request.latitude.isNotEmpty() && request.longitude.isNotEmpty()) {
                    val milesDistance = getDistanceInMiles(request.latitude.toDouble(), request.longitude.toDouble(), 39.13447904988019, -84.51552473741883)
                    lblDistance.text = "$milesDistance miles"
                } else {
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
            previousMarker = marker
        }
        else {
            val details = findViewById<LinearLayout>(R.id.details)
            details.isVisible = false
        }




        return false
    }

    private fun getBitmapDescriptor(id: Int): BitmapDescriptor {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val vectorDrawable = getDrawable(id) as VectorDrawable?
            val h = vectorDrawable!!.intrinsicHeight
            val w = vectorDrawable.intrinsicWidth
            vectorDrawable.setBounds(0, 0, w, h)
            val bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bm)
            vectorDrawable.draw(canvas)
            BitmapDescriptorFactory.fromBitmap(bm)
        } else {
            BitmapDescriptorFactory.fromResource(id)
        }
    }
    private fun getDistanceInMiles(
        firstLatitude: Double, firstLongitude: Double,
        secondLatitude: Double, secondLongitude: Double
    ): Double {
        val resultMeters = FloatArray(1)
        Location.distanceBetween(
            firstLatitude, firstLongitude,
            secondLatitude, secondLongitude, resultMeters
        )
        val resultMiles = resultMeters[0] * 0.000621371192
        return (resultMiles).toBigDecimal().setScale(1, RoundingMode.UP).toDouble()
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