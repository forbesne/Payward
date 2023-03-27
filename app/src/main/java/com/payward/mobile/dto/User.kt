package com.payward.mobile.dto

import java.io.Serializable

class User (
    val uid: String = "",
    var userName: String = "",
    var helpingPoints: Int = 0,
    var rooms: MutableMap<String, Any>? = null,
    var address: String = "",
    var mobileNumber: String = "",
    var helped: Int = 0
) : Serializable