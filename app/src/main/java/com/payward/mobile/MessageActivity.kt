package com.payward.mobile

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.payward.mobile.dto.User

class MessageActivity : AppCompatActivity() {
    private var firebaseAuth: FirebaseAuth? = null
    private var authStateListener: FirebaseAuth.AuthStateListener? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        firebaseAuth = FirebaseAuth.getInstance()
        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser

        }
    }

    override fun onStart() {
        super.onStart()
        firebaseAuth!!.addAuthStateListener(this.authStateListener!!)

        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser != null) {
            val fromUid = firebaseUser.uid
            val rootRef = FirebaseFirestore.getInstance()
            val uidRef = rootRef.collection("users").document(fromUid)
            uidRef.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document.exists()) {
                        val fromUser = document.toObject(User::class.java)
                        val userRoomsRef = rootRef.collection("rooms").document(fromUid).collection("userRooms")
                        userRoomsRef.get().addOnCompleteListener{ t ->
                            if (t.isSuccessful) {
                                val listOfToUserNames = ArrayList<String>()
                                val listOfToUsers = ArrayList<User>()
                                val listOfRooms = ArrayList<String>()
                                for (d in t.result) {
                                    val toUser = d.toObject(User::class.java)
                                    listOfToUserNames.add(toUser.userName)
                                    listOfToUsers.add(toUser)
                                    listOfRooms.add(d.id)
                                }

                                val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listOfToUserNames)
                                findViewById<ListView>(R.id.list_view).adapter = arrayAdapter
                                findViewById<ListView>(R.id.list_view).onItemClickListener = AdapterView.OnItemClickListener{ _, _, position, _ ->
                                    val intent = Intent(this, ChatActivity::class.java)
                                    intent.putExtra("fromUser", fromUser)
                                    intent.putExtra("toUser", listOfToUsers[position])
                                    intent.putExtra("roomId", listOfRooms[position])
                                    startActivity(intent)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()

        firebaseAuth!!.removeAuthStateListener(this.authStateListener!!)
    }

}