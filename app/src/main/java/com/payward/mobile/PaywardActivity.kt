package com.payward.mobile

import android.content.Intent
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.color.DynamicColors
import com.google.firebase.auth.FirebaseAuth
import com.payward.mobile.dto.LocationDetails
import java.math.RoundingMode

open class PaywardActivity : AppCompatActivity() {

    protected lateinit var viewModel: MainViewModel
    protected lateinit var appViewModel: AppViewModel
    protected open lateinit var locationDetails: LocationDetails
    protected lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        DynamicColors.applyToActivitiesIfAvailable(application)
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        viewModel.initializeFirebase()
        appViewModel = ViewModelProvider(this)[AppViewModel::class.java]

        auth = FirebaseAuth.getInstance()
    }

    fun requestLocationUpdates() {
        appViewModel.getLocationLiveData().observeForever{
            locationDetails = it
        }
    }

    fun getDistanceInMiles(
        firstLatitude: Double, firstLongitude: Double,
        locationDetails: LocationDetails
    ): Double {
        val resultMeters = FloatArray(1)
        Location.distanceBetween(
            firstLatitude, firstLongitude,
            locationDetails.latitude.toDouble(), locationDetails.longitude.toDouble(), resultMeters
        )
        val resultMiles = resultMeters[0] * 0.000621371192
        return (resultMiles).toBigDecimal().setScale(1, RoundingMode.UP).toDouble()
    }

    fun initMenu() {
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
}