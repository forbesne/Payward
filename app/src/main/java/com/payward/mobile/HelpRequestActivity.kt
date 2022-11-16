package com.payward.mobile

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.ComponentActivity
import com.payward.mobile.dto.Request

class HelpRequestActivity : ComponentActivity() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help_request)

        val submitButton = findViewById<Button>(R.id.submit_button)
        submitButton.setOnClickListener {
            val request = Request()
            request.userId = findViewById<EditText>(R.id.user_name).toString()
            request.text = findViewById<EditText>(R.id.description).toString()
            viewModel.save(request)
        }
    }
}