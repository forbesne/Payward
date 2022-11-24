package com.payward.mobile

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.lifecycle.ViewModelProvider
import com.payward.mobile.dto.Request

class RequestActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var btnSubmit: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        viewModel.initializeFirebase()

        btnSubmit = findViewById(R.id.btnSubmit)
        btnSubmit.setOnClickListener {
            var request = Request()
            request.issueType = findViewById<EditText>(R.id.txtCategory).text.toString()
            request.text = findViewById<EditText>(R.id.txtDescription).text.toString()
            viewModel.save(request)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

}
