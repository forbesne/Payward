package com.payward.mobile.service

import androidx.lifecycle.MutableLiveData
import com.payward.mobile.dto.Request

class RequestService {

    fun fetchRequests(requestId: Int) : MutableLiveData<ArrayList<Request>> {
        return MutableLiveData<ArrayList<Request>>()
    }


}