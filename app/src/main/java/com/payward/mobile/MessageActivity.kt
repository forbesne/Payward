package com.payward.mobile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.payward.mobile.dto.User
import com.payward.mobile.dto.UserRoom

class MessageActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private var userRoomsList = ArrayList<UserRoom>()
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_message)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        auth = FirebaseAuth.getInstance()
        viewModel.initializeFirebase()

        var btnHome = findViewById<Button>(R.id.homeBtn)
        btnHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        var btnHelpRequest = findViewById<Button>(R.id.helpRequestBtn)
        btnHelpRequest.setOnClickListener {
            val intent = Intent(this, RequestActivity::class.java)
            startActivity(intent)
            finish()
        }

        var btnMaps = findViewById<Button>(R.id.mapsBtn)
        btnMaps.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
            finish()
        }

        var btnMessages = findViewById<Button>(R.id.messagesBtn)
        btnMessages.setOnClickListener {
            val intent = Intent(this, MessageActivity::class.java)
            startActivity(intent)
            finish()
        }

        var btnProfile = findViewById<Button>(R.id.btnProfile)
        btnProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
            finish()
        }

        val rvUserRooms = findViewById<RecyclerView>(R.id.list_view)
        rvUserRooms.hasFixedSize()
        rvUserRooms.layoutManager = LinearLayoutManager(applicationContext)
        rvUserRooms.itemAnimator = DefaultItemAnimator()
        rvUserRooms.adapter = UserRoomsAdapter(userRoomsList, R.layout.item_message)

        viewModel.userRooms.observeForever {
                userRooms ->
            userRoomsList.removeAll(userRoomsList)
            userRoomsList.addAll(userRooms)
            rvUserRooms.adapter!!.notifyDataSetChanged()
        }

        title = "Messages"
    }

    inner class UserRoomsAdapter(val userRooms: ArrayList<UserRoom>, val item: Int) : RecyclerView.Adapter<MessageActivity.UserRoomViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserRoomViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(item, parent, false)
            return UserRoomViewHolder(view)
        }

        override fun onBindViewHolder(holder: UserRoomViewHolder, position: Int) {
            val userRoom = userRooms.get(position)
            holder.updateUserRoom(userRoom)
            holder.itemView.setOnClickListener {
                openChat(userRoom)
            }
        }

        override fun getItemCount(): Int {
            return userRooms.size
        }

    }

    inner class UserRoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        private var lblUserName: TextView = itemView.findViewById(R.id.txtName)
        private var lblDescription: TextView = itemView.findViewById(R.id.txtRequest)

        fun updateUserRoom (userRoom: UserRoom) {

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

