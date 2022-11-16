package com.payward.mobile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.payward.mobile.dto.Request
import com.payward.mobile.dto.Response

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: MainViewModel
    private var requestList = ArrayList<Request>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_main)

        val helpRequestButton = findViewById<Button>(R.id.helpRequestBtn)
        helpRequestButton.setOnClickListener {
            this.setContentView(R.layout.activity_help_request)
        }

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        viewModel.initializeFirebase()

        var rvRequests = findViewById<RecyclerView>(R.id.rvRequests)
        rvRequests.hasFixedSize()
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

        private var lblUserName: TextView = itemView.findViewById(R.id.username)
        private var lblDescription: TextView = itemView.findViewById(R.id.description)

        fun updateRequest (request: Request) {

            lblUserName.text = request.userId
            lblDescription.text = request.text

        }
    }

}

