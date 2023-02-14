package com.payward.mobile.service

import android.content.Intent
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.payward.mobile.ChatActivity
import com.payward.mobile.dto.Message
import com.payward.mobile.dto.Request
import com.payward.mobile.dto.Response
import com.payward.mobile.dto.User

class FirebaseService {
    var _requests: MutableLiveData<ArrayList<Request>> = MutableLiveData<ArrayList<Request>>()
    var _request = Request()
    private  var _responses = MutableLiveData<List<Response>>()
    var response = Response()

    private lateinit var auth: FirebaseAuth

    fun initialize() {
        listenForRequests()
        auth = FirebaseAuth.getInstance()
    }

    private fun listenForRequests() {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("requests").addSnapshotListener {
                snapshot, e ->
            if (e != null) {
                Log.w("Listen failed", e)
                return@addSnapshotListener
            }
            snapshot?.let {
                val allRequests = ArrayList<Request>()
                val documents = snapshot.documents
                documents.forEach {
                    val request = it.toObject(Request::class.java)
                    request?.let {
                        allRequests.add(it)
                    }
                }
                _requests.value = allRequests
            }
        }
    }

    fun save(request: Request) {
        val firestore = FirebaseFirestore.getInstance()
        val user = auth.currentUser
        user?.let {
            request.userDisplayName = user.displayName.toString()
            request.userId = user.uid
            request.user = user.uid.let { User(it, user.displayName!!) }
        }
        val document = if (request.requestId.isEmpty()) {
            //add
            request.rqStatus = "open"
            firestore.collection("requests").document()
        } else {
            //update
            firestore.collection("requests").document(request.requestId)
        }
        request.requestId = document.id
        val handle = document.set(request)
        handle.addOnSuccessListener { Log.d("Firebase", "Document Saved") }
        handle.addOnFailureListener { Log.e("Firebase", "Save failed $it ")}
    }

    internal fun save(response: Response) {
        val firestore = FirebaseFirestore.getInstance()
        val collection = firestore.collection("requests").document(request.requestId).collection("responses")
        val task = collection.add(response)
        task.addOnSuccessListener {
            response.responseId = it.id
        }
        task.addOnFailureListener {
            Log.d("Firebase", "Save Failed")
        }

    }

    fun respond(request: Request) {
        val firestore = FirebaseFirestore.getInstance()
        val collection = firestore.collection("requests").document(request.requestId).collection("responses")
        val user = auth.currentUser
        user?.let {
            response.userId = user.displayName.toString()
        }
        val task = collection.add(response)
        task.addOnSuccessListener {
            response.responseId = it.id
        }
        task.addOnFailureListener {
            Log.d("Firebase", "Save Failed")
        }
    }

    fun transferPoints(toUid: String, points: Int) {
        val firebaseUser = auth.currentUser
        val firestore = FirebaseFirestore.getInstance()
        if (firebaseUser != null) {
            val fromUid = firebaseUser.uid

            val uidRef = firestore.collection("users").document(fromUid)
            uidRef.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document.exists()) {
                        val fromUser = document.toObject(User::class.java)
                        if (fromUser != null) {
                            fromUser.helpingPoints = fromUser.helpingPoints - points
                            uidRef.set(fromUser)
                        }
                    }
                }
            }
        }
        val toUidRef = firestore.collection("users").document(toUid)
        toUidRef.get().addOnCompleteListener { taskTo ->
            if (taskTo.isSuccessful) {
                val documentTo = taskTo.result
                if (documentTo.exists()) {
                    val toUser = documentTo.toObject(User::class.java)
                    if (toUser != null) {
                        toUser.helpingPoints = toUser.helpingPoints + points
                        toUidRef.set(toUser)
                    }
                }
            }
        }
    }

    fun createUser() {
        val firebaseUser = auth.currentUser
        val uid = firebaseUser?.uid

        var user = User()
        user.userName = firebaseUser?.displayName.toString()
        user.helpingPoints = 20

        val firestore = FirebaseFirestore.getInstance()
        val uidRef = uid?.let { firestore.collection("users").document(it) }
        uidRef?.get()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (!document.exists()) {
                    uidRef.set(user)
                }
            }
        }
    }

    fun sendMessage(
        roomId: String,
        fromUser: User,
        toUid: String,
        toUser: User,
        messageText: String
    ) {
        val firestore = FirebaseFirestore.getInstance()
        val fromUid = fromUser.uid
        var fromRooms = fromUser.rooms
        if (fromRooms == null) {
            fromRooms = mutableMapOf()
        }
        fromRooms[roomId] = true
        fromUser.rooms = fromRooms
        firestore.collection("users").document(fromUid).set(fromUser, SetOptions.merge())
        firestore.collection("contacts").document(toUid).collection("userContacts").document(fromUid).set(fromUser, SetOptions.merge())
        firestore.collection("rooms").document(toUid).collection("userRooms").document(roomId).set(fromUser, SetOptions.merge())
        var toRooms = toUser.rooms
        if (toRooms == null) {
            toRooms = mutableMapOf()
        }
        toRooms[roomId] = true
        toUser.rooms = toRooms
        firestore.collection("users").document(toUid).set(toUser, SetOptions.merge())
        firestore.collection("contacts").document(fromUid).collection("userContacts").document(toUid).set(toUser, SetOptions.merge())
        firestore.collection("rooms").document(fromUid).collection("userRooms").document(roomId).set(toUser, SetOptions.merge())

        val message = Message(messageText, fromUid)
        firestore.collection("messages").document(roomId).collection("roomMessages").add(message)
    }

    internal var requests:MutableLiveData<ArrayList<Request>>
        get() { return _requests}
        set(value) {_requests = value}

    internal var request:Request
        get() { return _request}
        set(value) {_request = value}
}