package com.payward.mobile

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.payward.mobile.dto.Request
import com.payward.mobile.service.RequestService

class MainViewModel : ViewModel() {
    var requests: MutableLiveData<ArrayList<Request>> = MutableLiveData<ArrayList<Request>>()
    var requestService: RequestService = RequestService()

    private lateinit var firestore : FirebaseFirestore

    init {
        firestore = FirebaseFirestore.getInstance()
        firestore.firestoreSettings = FirebaseFirestoreSettings.Builder().build()
        listenForRequests()
    }

    private fun listenForRequests() {
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
                requests.value = allRequests
            }
        }
    }

    fun fetchRequests(requestId: String) {
        requests = requestService.fetchRequests(requestId)
    }

    fun save(request: Request) {
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
}