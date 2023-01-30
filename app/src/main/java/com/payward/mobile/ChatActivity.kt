package com.payward.mobile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.payward.mobile.dto.Message
import com.payward.mobile.dto.User

class ChatActivity : AppCompatActivity() {
    private var rootRef: FirebaseFirestore? = null
    private var fromUid: String? = ""
    private var adapter: MessageAdapter? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        rootRef = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        viewModel.initializeFirebase()

        val fromUser = intent.extras?.get("fromUser") as User
        fromUid = fromUser.uid
        var fromRooms = fromUser.rooms
        val toUser = intent.extras!!.get("toUser") as User
        val toUid = toUser.uid
        var toRooms = toUser.rooms

        var roomId = intent.extras!!.get("roomId") as String

        if (roomId == "noRoomId") {
            roomId = rootRef!!.collection("messages").document().id
            if (fromRooms != null) {
                for ((key, _) in fromRooms) {
                    if (toRooms != null) {
                        if (toRooms.contains(key)) {
                            roomId = key
                        }
                    }
                }
            }
        }
        var btnChat = findViewById<Button>(R.id.btnSend)
        btnChat.setOnClickListener {

            val messageText = findViewById<EditText>(R.id.etMessage).text.toString()

            viewModel.sendMessage(roomId, fromUser, toUid, toUser, messageText)

            findViewById<EditText>(R.id.etMessage).text.clear()
        }

        val query = rootRef!!.collection("messages").document(roomId).collection("roomMessages").orderBy("sentAt", Query.Direction.ASCENDING)
        val options = FirestoreRecyclerOptions.Builder<Message>().setQuery(query, Message::class.java).build()
        adapter = MessageAdapter(options)
        var recycler_view = findViewById<RecyclerView>(R.id.recyclerView)
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
            var recycler_view = findViewById<RecyclerView>(R.id.recyclerView)
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