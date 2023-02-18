package com.payward.mobile.dto


import java.io.Serializable

class User (
    val uid: String = "",
    var userName: String = "",
    var helpingPoints: Int = 0,
    var rooms: MutableMap<String, Any>? = null
) : Serializable