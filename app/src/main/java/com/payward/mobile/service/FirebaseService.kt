package com.payward.mobile.service

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.payward.mobile.dto.*

class FirebaseService {
    private var _requests: MutableLiveData<ArrayList<Request>> = MutableLiveData<ArrayList<Request>>()
    private var _request = Request()
    private var _userRooms: MutableLiveData<ArrayList<UserRoom>> = MutableLiveData<ArrayList<UserRoom>>()
    private var _userRoom = UserRoom()
    private var _user: MutableLiveData<User> = MutableLiveData<User>()

    private lateinit var auth: FirebaseAuth

    fun initialize() {
        listenForRequests()
        auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            listenForUserRooms()
            fetchCurrentUser()
        }
    }

    private fun fetchCurrentUser() {

        val firebaseUser = auth.currentUser
        val uid = firebaseUser?.uid

        val firestore = FirebaseFirestore.getInstance()
        if (uid != null) {
            firestore.collection("users").document(uid).addSnapshotListener {
                    snapshot, e ->
                if (e != null) {
                    Log.w("Listen failed", e)
                    return@addSnapshotListener
                }
                snapshot?.let {
                    var currentUser = User()
                    val user = snapshot.toObject(User::class.java)
                    user?.let {
                        currentUser = user
                    }
                    _user.value = currentUser
                }
            }
        }

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

    private fun listenForUserRooms() {
        val firestore = FirebaseFirestore.getInstance()
        val firebaseUser = auth.currentUser
        val uid = firebaseUser?.uid

        if (uid != null) {
            firestore.collection("rooms").document(uid).collection("userRooms").addSnapshotListener {
                    snapshot, e ->
                if (e != null) {
                    Log.w("Listen failed", e)
                    return@addSnapshotListener
                }
                snapshot?.let {
                    val allUserRooms = ArrayList<UserRoom>()
                    val documents = snapshot.documents
                    documents.forEach {
                        val userRoom = it.toObject(UserRoom::class.java)
                        if (userRoom != null) {
                            userRoom.roomId = it.id
                        }
                        userRoom?.let {
                            allUserRooms.add(it)
                        }
                    }
                    _userRooms.value = allUserRooms
                }
            }
        }
    }

    fun save(request: Request) {
        val firestore = FirebaseFirestore.getInstance()
        val user = auth.currentUser
        user?.let {
            request.userDisplayName = user.displayName.toString()
            request.userId = user.uid
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

    fun acceptHelp(request: Request, toUid: String) {
        request.rqStatus = "closed"
        save(request)
        transferPoints(toUid, request.helpingPoints)

    }
    private fun transferPoints(toUid: String, points: Int) {
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
                        toUser.helped = toUser.helped + 1
                        toUser.helpingPoints = toUser.helpingPoints + points
                        toUidRef.set(toUser)
                    }
                }
            }
        }
    }

    fun save(user: User) {
        val firestore = FirebaseFirestore.getInstance()
        val document = if (user.uid.isEmpty()) {
            //add
            firestore.collection("users").document()
        } else {
            //update
            firestore.collection("users").document(user.uid)
        }
        val handle = document.set(user)
        handle.addOnSuccessListener { Log.d("Firebase", "Document Saved") }
        handle.addOnFailureListener { Log.e("Firebase", "Save failed $it ")}
    }

    fun createUser() {
        val firebaseUser = auth.currentUser
        val uid = firebaseUser?.uid

        val user = User()
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
        msgRequest: Request,
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
        fromRooms[roomId] = msgRequest.requestId
        fromUser.rooms = fromRooms
        val fromUserRoom = UserRoom()
        fromUserRoom.user = fromUser
        fromUserRoom.request = msgRequest
        firestore.collection("users").document(fromUid).set(fromUser, SetOptions.merge())
        firestore.collection("rooms").document(toUid).collection("userRooms").document(roomId).set(fromUserRoom, SetOptions.merge())
        var toRooms = toUser.rooms
        if (toRooms == null) {
            toRooms = mutableMapOf()
        }
        toRooms[roomId] = msgRequest.requestId
        toUser.rooms = toRooms
        val toUserRoom = UserRoom()
        toUserRoom.user = toUser
        toUserRoom.request = msgRequest
        firestore.collection("users").document(toUid).set(toUser, SetOptions.merge())
        firestore.collection("rooms").document(fromUid).collection("userRooms").document(roomId).set(toUserRoom, SetOptions.merge())

        val message = Message(messageText, fromUid)
        firestore.collection("messages").document(roomId).collection("roomMessages").add(message)
    }

    internal var requests:MutableLiveData<ArrayList<Request>>
        get() { return _requests}
        set(value) {_requests = value}

    internal var request:Request
        get() { return _request}
        set(value) {_request = value}

    internal var userRooms:MutableLiveData<ArrayList<UserRoom>>
        get() { return _userRooms}
        set(value) {_userRooms = value}

    internal var userRoom:UserRoom
        get() { return _userRoom}
        set(value) {_userRoom = value}

    internal var currentUser:MutableLiveData<User>
        get() { return _user}
        set(value) {_user = value}
}