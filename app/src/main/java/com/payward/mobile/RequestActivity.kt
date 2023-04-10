package com.payward.mobile

import android.content.Intent
import android.os.Bundle
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.color.DynamicColors
import com.payward.mobile.dto.LocationDetails
import com.payward.mobile.dto.Request

class RequestActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var btnSubmit: Button
    private lateinit var appViewModel: AppViewModel
    private lateinit var locationDetails: LocationDetails
    override fun onCreate(savedInstanceState: Bundle?) {
        DynamicColors.applyToActivitiesIfAvailable(application)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        viewModel.initializeFirebase()
        appViewModel = ViewModelProvider(this)[AppViewModel::class.java]

        appViewModel.getLocationLiveData().observeForever {
            locationDetails = it
        }

        btnSubmit = findViewById(R.id.btnSubmit)
        btnSubmit.setOnClickListener {
            val request = Request()
            request.issueType = findViewById<AutoCompleteTextView>(R.id.txtCategory).text.toString()
            request.text = findViewById<EditText>(R.id.txtDescription).text.toString()
            request.helpingPoints = findViewById<EditText>(R.id.txtPoints).text.toString().toInt()
            request.latitude = locationDetails.latitude
            request.longitude = locationDetails.longitude
            viewModel.save(request)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

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
