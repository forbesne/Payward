package com.payward.mobile

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.payward.mobile.dto.Message
import com.payward.mobile.dto.Request
import com.payward.mobile.dto.User

class ChatActivity : AppCompatActivity() {
    private var rootRef: FirebaseFirestore? = null
    private var fromUid: String? = ""
    private var adapter: MessageAdapter? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var viewModel: MainViewModel
    private var requestId: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        rootRef = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        viewModel.initializeFirebase()

        val fromUser = intent.extras?.get("fromUser") as User
        fromUid = fromUser.uid
        val fromRooms = fromUser.rooms
        val toUser = intent.extras!!.get("toUser") as User
        val toUid = toUser.uid
        val toRooms = toUser.rooms

        var roomId = intent.extras!!.get("roomId") as String
        val request = intent.extras?.get("request") as Request
        requestId = request.requestId

        if (roomId == "noRoomId") {
            roomId = rootRef!!.collection("messages").document().id
            if (fromRooms != null) {
                for ((key, _) in fromRooms) {
                    if (toRooms != null) {
                        if (toRooms.contains(key)) {
                            if (toRooms.getValue(key) == requestId) {
                                roomId = key
                            }
                        }
                    }
                }
            }
        }

        val txtRequest = findViewById<TextView>(R.id.txtRequest)
        txtRequest.text = request.text


        val btnAccept = findViewById<Button>(R.id.btnAccept)
        if (fromUid != request.userId) {
            btnAccept.isVisible = false
        }
        btnAccept.setOnClickListener {

            val builder = AlertDialog.Builder(this)
            builder.setMessage("Are you sure you want to accept help and close this request?")
                .setCancelable(false)
                .setPositiveButton("Yes") { dialog, id ->
                    viewModel.acceptHelp(request, toUid)
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("No") { dialog, id ->
                    // Dismiss the dialog
                    dialog.dismiss()
                }
            val alert = builder.create()
            alert.show()

        }

        val btnChat = findViewById<Button>(R.id.btnSend)
        btnChat.setOnClickListener {

            val messageText = findViewById<EditText>(R.id.etMessage).text.toString()

            viewModel.sendMessage(roomId, request, fromUser, toUid, toUser, messageText)

            findViewById<EditText>(R.id.etMessage).text.clear()
        }

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

        val query = rootRef!!.collection("messages").document(roomId).collection("roomMessages").orderBy("sentAt", Query.Direction.ASCENDING)
        val options = FirestoreRecyclerOptions.Builder<Message>().setQuery(query, Message::class.java).build()
        adapter = MessageAdapter(options)
        val recycler_view = findViewById<RecyclerView>(R.id.recyclerView)
        recycler_view.adapter = adapter

        title = toUser.userName
    }

    inner class MessageViewHolder internal constructor(private val view: View) : RecyclerView.ViewHolder(view) {
        internal fun setMessage(message: Message) {
            val textView = view.findViewById<TextView>(R.id.text_view)
            textView.text = message.messageText
        }
    }

    inner class MessageAdapter internal constructor(options: FirestoreRecyclerOptions<Message>) : FirestoreRecyclerAdapter<Message, MessageViewHolder>(options) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
            return if (viewType == R.layout.item_message_to) {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message_to, parent, false)
                MessageViewHolder(view)
            } else {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message_from, parent, false)
                MessageViewHolder(view)
            }
        }

        override fun onBindViewHolder(holder: MessageViewHolder, position: Int, model: Message) {
            holder.setMessage(model)
        }

        override fun getItemViewType(position: Int): Int {
            return if (fromUid != getItem(position).fromUid) {
                R.layout.item_message_to
            } else {
                R.layout.item_message_from
            }
        }

        override fun onDataChanged() {
            val recycler_view = findViewById<RecyclerView>(R.id.recyclerView)
            recycler_view.layoutManager?.scrollToPosition(itemCount - 1)
        }
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            android.R.id.home -> {
                finish()
                true
            }

            else -> super.onOptionsItemSelected(menuItem)
        }
    }

    override fun onStart() {
        super.onStart()

        if (adapter != null) {
            adapter!!.startListening()
        }
    }

    override fun onStop() {
        super.onStop()

        if (adapter != null) {
            adapter!!.stopListening()
        }
    }
}