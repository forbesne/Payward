package com.payward.mobile.dto

import java.io.Serializable
import java.util.*

class Request(
    var requestId: String = "",
    var userId: String = "",
    var city: String = "",
    var issueType: String = "",
    var text: String = "",
    var requestDate: Date = Date(),
    var rqStatus: String = "",
    var userDisplayName: String = "",
    var helpingPoints: Int = 0,
    var latitude: String = "",
    var longitude: String = ""
) :
    Serializable {

}