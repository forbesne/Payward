package com.payward.mobile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.payward.mobile.dto.Request
import com.payward.mobile.service.FirebaseService
import com.payward.mobile.service.RequestService

class MainViewModel : ViewModel() {
    var requests: MutableLiveData<ArrayList<Request>> = MutableLiveData<ArrayList<Request>>()
    var requestService: RequestService = RequestService()
    var firebaseService: FirebaseService = FirebaseService()

    fun fetchRequests(requestId: String) {
        requests = requestService.fetchRequests(requestId)
    }

    fun save(request: Request) {
        firebaseService.save(request)
    }
}