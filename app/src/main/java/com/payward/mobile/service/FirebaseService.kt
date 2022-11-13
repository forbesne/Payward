package com.payward.mobile.service

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.payward.mobile.dto.Request

class FirebaseService {
    var _requests: MutableLiveData<ArrayList<Request>> = MutableLiveData<ArrayList<Request>>()

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

    internal var requests:MutableLiveData<ArrayList<Request>>
        get() { return _requests}
        set(value) {_requests = value}

}