package com.payward.mobile

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.payward.mobile.dto.User


class ProfileActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var logoutBtn: Button
    private lateinit var currentUser: User
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        viewModel.initializeFirebase()

        val txtName = findViewById<TextView>(R.id.txtName)
        val txtHelped = findViewById<TextView>(R.id.txtHelped)
        val txtPoints = findViewById<TextView>(R.id.txtPoints)
        val txtAddress = findViewById<TextView>(R.id.txtAddress)
        val txtMobileNumber = findViewById<TextView>(R.id.txtMobileNumber)

        viewModel.currentUser.observeForever {
                user ->
            currentUser = user
            txtName.text = currentUser.userName
            txtHelped.text = currentUser.helped.toString()
            txtPoints.text = currentUser.helpingPoints.toString()
            txtAddress.text = currentUser.address
            txtMobileNumber.text = currentUser.mobileNumber
        }

        val homeBtn = findViewById<Button>(R.id.homeBtn)
        homeBtn.setOnClickListener {
            currentUser.address = txtAddress.text.toString()
            currentUser.mobileNumber = txtMobileNumber.text.toString()
            viewModel.save(currentUser)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

//        var btnHelpRequest = findViewById<Button>(R.id.helpRequestBtn)
//        btnHelpRequest.setOnClickListener {
//            currentUser.address = txtAddress.text.toString()
//            currentUser.mobileNumber = txtMobileNumber.text.toString()
//            viewModel.save(currentUser)
//            val intent = Intent(this, RequestActivity::class.java)
//            startActivity(intent)
//            finish()
//        }
//
//        var btnMaps = findViewById<Button>(R.id.mapsBtn)
//        btnMaps.setOnClickListener {
//            currentUser.address = txtAddress.text.toString()
//            currentUser.mobileNumber = txtMobileNumber.text.toString()
//            viewModel.save(currentUser)
//            val intent = Intent(this, MapsActivity::class.java)
//            startActivity(intent)
//            finish()
//        }
//
//        var btnMessages = findViewById<Button>(R.id.messagesBtn)
//        btnMessages.setOnClickListener {
//            currentUser.address = txtAddress.text.toString()
//            currentUser.mobileNumber = txtMobileNumber.text.toString()
//            viewModel.save(currentUser)
//            val intent = Intent(this, MessageActivity::class.java)
//            startActivity(intent)
//            finish()
//        }
//
//        var btnProfile = findViewById<Button>(R.id.btnProfile)
//        btnProfile.setOnClickListener {
//            currentUser.address = txtAddress.text.toString()
//            currentUser.mobileNumber = txtMobileNumber.text.toString()
//            viewModel.save(currentUser)
//            val intent = Intent(this, ProfileActivity::class.java)
//            startActivity(intent)
//            finish()
//        }
        logoutBtn = findViewById(R.id.logout_btn)

        logoutBtn.setOnClickListener{
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, MainFragment::class.java)
            startActivity(intent)
            finish()
        }

    }
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v: View? = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    val imm: InputMethodManager =
                        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

}