package com.payward.mobile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.DynamicColors
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.payward.mobile.dto.User
import com.payward.mobile.dto.UserRoom
import kotlin.properties.Delegates

class MessageActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private var userRoomsList = ArrayList<UserRoom>()
    private var userRoomsFilteredList = ArrayList<UserRoom>()
    private lateinit var auth: FirebaseAuth
    var tabSelected by Delegates.notNull<Int>()
    override fun onCreate(savedInstanceState: Bundle?) {
        DynamicColors.applyToActivitiesIfAvailable(application)
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_message)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        auth = FirebaseAuth.getInstance()
        viewModel.initializeFirebase()

        val btnHome = findViewById<Button>(R.id.homeBtn)
        btnHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        val btnHelpRequest = findViewById<Button>(R.id.helpRequestBtn)
        btnHelpRequest.setOnClickListener {
            val intent = Intent(this, RequestActivity::class.java)
            startActivity(intent)
            finish()
        }

        val btnMaps = findViewById<Button>(R.id.mapsBtn)
        btnMaps.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
            finish()
        }

        val btnMessages = findViewById<Button>(R.id.messagesBtn)
        btnMessages.setOnClickListener {
            val intent = Intent(this, MessageActivity::class.java)
            startActivity(intent)
            finish()
        }

        val btnProfile = findViewById<Button>(R.id.btnProfile)
        btnProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
            finish()
        }

        val rvUserRooms = findViewById<RecyclerView>(R.id.list_view)
        rvUserRooms.hasFixedSize()
        rvUserRooms.layoutManager = LinearLayoutManager(applicationContext)
        rvUserRooms.itemAnimator = DefaultItemAnimator()
        rvUserRooms.adapter = UserRoomsAdapter(userRoomsFilteredList, R.layout.item_message)

        viewModel.userRooms.observeForever {
                userRooms ->
            userRoomsList.removeAll(userRoomsList.toSet())
            userRoomsList.addAll(userRooms)
            userRoomsFilteredList.removeAll(userRoomsFilteredList.toSet())
            userRoomsFilteredList.addAll(userRooms)
            rvUserRooms.adapter!!.notifyDataSetChanged()
        }

        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab != null) {
                    tabSelected = tab.position
                    (rvUserRooms.adapter as MessageActivity.UserRoomsAdapter).filter.filter("10")
                }
                // Handle tab select
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Handle tab reselect
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // Handle tab unselect
            }
        })

        title = "Messages"
    }

    inner class UserRoomsAdapter(private val userRooms: ArrayList<UserRoom>, val item: Int) : RecyclerView.Adapter<MessageActivity.UserRoomViewHolder>(), Filterable {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserRoomViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(item, parent, false)
            return UserRoomViewHolder(view)
        }

        override fun onBindViewHolder(holder: UserRoomViewHolder, position: Int) {
            val userRoom = userRooms[position]
            holder.updateUserRoom(userRoom)
            holder.itemView.setOnClickListener {
                openChat(userRoom)
            }
        }

        override fun getItemCount(): Int {
            return userRooms.size
        }

        override fun getFilter(): Filter {
            return userRoomFilter
        }

        private val userRoomFilter: Filter = object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                var filteredList: MutableList<UserRoom> = java.util.ArrayList()
                if (tabSelected == 0) {
                    filteredList = userRoomsList
                }
                else {
                    val user = auth.currentUser
                    for (userRoom in userRoomsList) {
                        user?.let {
                            if ((tabSelected == 1) && (user.uid == userRoom.request.userId)) {
                                filteredList.add(userRoom)
                            }
                            if ((tabSelected == 2) && (user.uid != userRoom.request.userId)) {
                                filteredList.add(userRoom)
                            }
                        }

                    }
                }

                val results = FilterResults()
                results.values = filteredList
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults) {
                userRoomsFilteredList.clear()
                userRoomsFilteredList.addAll((results.values as List<UserRoom>))
                notifyDataSetChanged()
            }
        }
    }

    inner class UserRoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        private var lblTitle: TextView = itemView.findViewById(R.id.title)
        private var lblUserName: TextView = itemView.findViewById(R.id.txtName)
        private var lblDescription: TextView = itemView.findViewById(R.id.txtRequest)

        fun updateUserRoom (userRoom: UserRoom) {

            lblTitle.text = userRoom.user.userName[0].toString()
            lblUserName.text = userRoom.user.userName
            lblDescription.text = userRoom.request.text

        }
    }

    private fun openChat(userRoom: UserRoom) {

        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            val fromUid = firebaseUser.uid
            val rootRef = FirebaseFirestore.getInstance()
            val uidRef = rootRef.collection("users").document(fromUid)
            uidRef.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document.exists()) {
                        val fromUser = document.toObject(User::class.java)

                        val intent = Intent(this, ChatActivity::class.java)
                        intent.putExtra("fromUser", fromUser)
                        intent.putExtra("toUser", userRoom.user)
                        intent.putExtra("roomId", userRoom.roomId)
                        intent.putExtra("request", userRoom.request)
                        startActivity(intent)

                    }
                }
            }
        }
    }

}

