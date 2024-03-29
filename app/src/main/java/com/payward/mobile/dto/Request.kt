package com.payward.mobile.dto

import com.google.firebase.firestore.Exclude
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

class Request(var requestId : String = "",
              var userId : String = "",
              var city : String = "",
              var issueType : String = "",
              var text : String = "",
              var requestDate : Date = Date(),
              var rqStatus : String = "",
              var userDisplayName : String = "",
              var user: User = User(),
              var helpingPoints: Int = 0,
              var latitude : String = "",
              var longitude : String = "") :
    Serializable {
    private var _responses: ArrayList<Response> = ArrayList<Response>()

    var response : ArrayList<Response>
        @Exclude get() {return _responses}
        set(value) {_responses = value}
}