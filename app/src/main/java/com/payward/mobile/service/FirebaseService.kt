package com.payward.mobile.service

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.payward.mobile.dto.Request
import com.payward.mobile.dto.Response

class FirebaseService {
    var _requests: MutableLiveData<ArrayList<Request>> = MutableLiveData<ArrayList<Request>>()
    var _request = Request()
    private  var _responses = MutableLiveData<List<Response>>()

    fun initialize() {
        listenForRequests()
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
        val document = if (request.requestId == null || request.requestId.isEmpty()) {
            //add
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

    internal var requests:MutableLiveData<ArrayList<Request>>
        get() { return _requests}
        set(value) {_requests = value}

    internal var request:Request
        get() { return _request}
        set(value) {_request = value}
}