package com.payward.mobile.service

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
        var user = auth.currentUser
        user?.let {
            request.userDisplayName = user.displayName.toString()
            request.userId = user.uid
            request.user = user.uid?.let { User(it, user.displayName!!) }
        }
        val document = if (request.requestId == null || request.requestId.isEmpty()) {
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
        var user = auth.currentUser
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

    fun createUser() {
        var firebaseUser = auth.currentUser
        val uid = firebaseUser?.uid
        val userName = firebaseUser?.displayName
        val user = uid?.let { User(it, userName!!) }

        val firestore = FirebaseFirestore.getInstance()
        val uidRef = uid?.let { firestore.collection("users").document(it) }
        if (uidRef != null) {
            uidRef.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (!document.exists()) {
                        if (user != null) {
                            uidRef.set(user)
                        }
                    }
                }
            }
        }
    }

    internal var requests:MutableLiveData<ArrayList<Request>>
        get() { return _requests}
        set(value) {_requests = value}

    internal var request:Request
        get() { return _request}
        set(value) {_request = value}
}