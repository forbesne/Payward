package com.payward.mobile


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.color.DynamicColors
import com.google.firebase.auth.FirebaseAuth


    class MainFragment : AppCompatActivity() {
        // [START declare_auth]
        private lateinit var auth: FirebaseAuth
        // [END declare_auth]

        private lateinit var emailEt: EditText
        private lateinit var passwordEt: EditText
        private lateinit var loginBtn: Button
        private lateinit var signupBtn: Button


        override fun onCreate(savedInstanceState: Bundle?) {
            DynamicColors.applyToActivitiesIfAvailable(application)
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main_fragment)

            emailEt = findViewById(R.id.email_edt_text)
            passwordEt = findViewById(R.id.pass_edt_text)
            loginBtn = findViewById(R.id.login_btn)
            signupBtn = findViewById(R.id.signup_btn)


            auth = FirebaseAuth.getInstance()

            loginBtn.setOnClickListener {
                val email: String = emailEt.text.toString()
                val password: String = passwordEt.text.toString()

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    Toast.makeText(
                        this@MainFragment,
                        "Please fill all the fields",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, OnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this, "Successfully Logged In", Toast.LENGTH_LONG)
                                    .show()
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(this, "Login Failed", Toast.LENGTH_LONG).show()
                            }
                        })
                }
            }
            signupBtn.setOnClickListener{
                val intent = Intent(this, Signup::class.java)
                startActivity(intent)
                finish()
            }

        }
    }


