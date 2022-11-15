package com.payward.mobile

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity

import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.databinding.DataBindingUtil
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.payward.mobile.ui.theme.PaywardTheme
import androidx.lifecycle.ViewModelProvider


class MainActivity : ComponentActivity() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PaywardTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Greeting("Android")
                }
            }
        }
    }
    class MainActivity : AppCompatActivity() {


        lateinit var binding: ActivityMainBinding
        val auth = FirebaseAuth.getInstance().currentUser

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = DataBindingUtil.setContentView(this,R.layout.activity_main)


            if(auth!=null && intent!= null){

                createUI()
            }
            else{
                startActivity(Intent(this,LoginActivity::class.java))
                this.finish()
            }

        }

        override fun onResume() {
            super.onResume()
            if(auth!=null&& intent!= null){

                createUI()
            }
            else{
                startActivity(Intent(this,LoginActivity::class.java))
                this.finish()

            }
        }

        fun createUI(){

            val list = auth?.providerData
            var providerData:String = ""
            list?.let {
                for(provider in list){
                    providerData = providerData+ " " +provider.providerId
                }
            }


            auth?.let {
                txtName.text = auth.displayName
                txtEmail.text = auth.email
                txtPhone.text = auth.phoneNumber

            }

            btnLogOut.setOnClickListener{
                AuthUI.getInstance().signOut(this)
                    .addOnSuccessListener {
                        val intent = Intent(this,LoginActivity::class.java)
                        startActivity(intent)
//                    this.finish()
                        Toast.makeText(this, "Successfully Log Out", Toast.LENGTH_SHORT).show()
                    }

            }
        }

    @Composable
    fun Greeting(name: String) {
        Text(text = "Hello $name!")
    }

    @Preview(showBackground = true)
    @Composable
    fun DefaultPreview() {
        PaywardTheme {
            Greeting("Android")
        }

        this.setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        viewModel.initializeFirebase()

    }
}