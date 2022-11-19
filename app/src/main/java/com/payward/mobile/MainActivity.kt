package com.payward.mobile

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.payward.mobile.databinding.ActivityMainBinding
import com.payward.mobile.dto.Request
import com.payward.mobile.dto.Response

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    private var requestList = ArrayList<Request>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_main)

        val homeFragment = HomeFragment()
        val helpRequestFragment = HelpRequestFragment()
        val charityFragment = CharityFragment()



       /* val helpRequestButton = findViewById<Button>(R.id.helpRequestBtn1)
        helpRequestButton.setOnClickListener {
            this.setContentView(R.layout.activity_help_request)
        }

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        viewModel.initializeFirebase()

        var btnHelpRequest = findViewById<Button>(R.id.helpRequestBtn)
        btnHelpRequest.setOnClickListener {

        }
        var rvRequests = findViewById<RecyclerView>(R.id.rvRequests)
        rvRequests.hasFixedSize()
        rvRequests.layoutManager = LinearLayoutManager(applicationContext)
        rvRequests.itemAnimator = DefaultItemAnimator()
        rvRequests.adapter = RequestsAdapter(requestList, R.layout.item_post)

        viewModel.requests.observeForever {
                requests ->
            requestList.removeAll(requestList)
            requestList.addAll(requests)
            rvRequests.adapter!!.notifyDataSetChanged()
        }


    }


    inner class RequestsAdapter(val requests: ArrayList<Request>, val itemPost: Int) : RecyclerView.Adapter<MainActivity.RequestViewHolder>() {

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(itemPost, parent, false)
                return RequestViewHolder(view)
            }

            override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
                val request = requests.get(position)
                holder.updateRequest(request)
            }

            override fun getItemCount(): Int {
                return requests.size
            }
        }

    inner class RequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        private var lblUserName: TextView = itemView.findViewById(R.id.user_name)
        private var lblDescription: TextView = itemView.findViewById(R.id.description)
        private var btnRespond: Button = itemView.findViewById(R.id.btnRespond)

        fun updateRequest (request: Request) {

            lblUserName.text = request.userId
            lblDescription.text = request.text

        }*/
            btnRespond.setOnClickListener {
                respondRequest(request)
            }

        }
    }

    private fun respondRequest(request: Request) {
       viewModel.respond(request)
    }

}

