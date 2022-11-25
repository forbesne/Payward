package com.payward.mobile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class CharityActivity : AppCompatActivity() {

    private lateinit var charity1: Button
    private lateinit var charity2: Button
    private lateinit var charity3: Button
    private lateinit var charity4: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.charity)

        charity1 = findViewById(R.id.charity1)
        charity2 = findViewById(R.id.charity2)
        charity3 = findViewById(R.id.charity3)
        charity4 = findViewById(R.id.charity4)

        charity1.setOnClickListener {
            goLink("https://www.unitedway.org/")
        }

        charity2.setOnClickListener {
            goLink("https://www.feedingamerica.org/")
        }

        charity3.setOnClickListener {
            goLink("https://www.salvationarmy.org/")
        }

        charity4.setOnClickListener {
            goLink("https://www.catholiccharitiesusa.org/")
        }

    }

    private fun goLink(s: String) {
        val uri = Uri.parse(s)
        startActivity(Intent(Intent.ACTION_VIEW, uri))
    }
}