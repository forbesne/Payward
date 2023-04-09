package com.payward.mobile

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.color.DynamicColors
import com.payward.mobile.dto.LocationDetails
import com.payward.mobile.dto.Request

class RequestActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var btnSubmit: Button
    private lateinit var appViewModel: AppViewModel
    private lateinit var locationDetails: LocationDetails
    lateinit var categorySelected: String
    override fun onCreate(savedInstanceState: Bundle?) {
        DynamicColors.applyToActivitiesIfAvailable(application)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        viewModel.initializeFirebase()
        appViewModel = ViewModelProvider(this).get(AppViewModel::class.java)

        appViewModel.getLocationLiveData().observeForever{
            locationDetails = it
        }

        btnSubmit = findViewById(R.id.btnSubmit)
        btnSubmit.setOnClickListener {
            var request = Request()
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

        val categoryList = resources.getStringArray(R.array.categories_array)

//        val spinner: Spinner = findViewById(R.id.spinnerCategories)
//// Create an ArrayAdapter using the string array and a default spinner layout
//        ArrayAdapter.createFromResource(
//            this,
//            R.array.categories_array,
//            android.R.layout.simple_spinner_item
//        ).also { adapter ->
//            // Specify the layout to use when the list of choices appears
//            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//            // Apply the adapter to the spinner
//            spinner.adapter = adapter
//        }
//
//        spinner.onItemSelectedListener = object :
//            AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
//                categorySelected = categoryList[position]
//                val txtCategory: TextView = findViewById(R.id.txtCategory)
//                txtCategory.text = categorySelected
////                Toast.makeText(this@MainActivity,
////                    " " +
////                            "" + categoryList[position], Toast.LENGTH_SHORT).show()
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>) {
//                // write code to perform some action
//            }
//        }

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

    }

}
