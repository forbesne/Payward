package com.payward.mobile

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
//import com.payward.mobile.databinding.ActivityMainBinding
import com.payward.mobile.dto.Request
import com.payward.mobile.dto.Response
import kotlin.reflect.typeOf

class HomeFragment : Fragment() {

    private lateinit var viewModel: MainViewModel
    private var requestList = ArrayList<Request>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.home_fragment, container, false)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        viewModel.initializeFirebase()


        var rvRequests : RecyclerView? = view?.findViewById(R.id.rvRequests)
        rvRequests?.hasFixedSize()
        rvRequests?.layoutManager = LinearLayoutManager(context)
        rvRequests?.itemAnimator = DefaultItemAnimator()
        rvRequests?.adapter = RequestsAdapter(requestList, R.layout.item_post)

        viewModel.requests.observeForever { requests ->
            requestList.removeAll(requestList)
            requestList.addAll(requests)
            rvRequests?.adapter!!.notifyDataSetChanged()
        }

    }


    inner class RequestsAdapter(val requests: ArrayList<Request>, val itemPost: Int) :
        RecyclerView.Adapter<HomeFragment.RequestViewHolder>() {

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

    inner class RequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var lblUserName: TextView = itemView.findViewById(R.id.username)
        private var lblDescription: TextView = itemView.findViewById(R.id.description)
        private var btnRespond: Button = itemView.findViewById(R.id.btnRespond)

        fun updateRequest(request: Request) {

            lblUserName.text = request.userId
            lblDescription.text = request.text

            btnRespond.setOnClickListener {
                respondRequest(request)
            }

        }

        private fun respondRequest(request: Request) {
            viewModel.respond(request)
        }
    }
}