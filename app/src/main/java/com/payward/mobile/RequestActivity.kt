package com.payward.mobile

import android.content.Intent
import android.os.Bundle
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import com.payward.mobile.dto.Request

class RequestActivity : PaywardActivity() {

    private lateinit var btnSubmit: Button
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request)

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

        initMenu()

    }
}
