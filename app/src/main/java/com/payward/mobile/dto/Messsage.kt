package com.payward.mobile.dto

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

class Message(
    var messageText: String = "",
    var fromUid: String = "",
    @ServerTimestamp
    val sentAt: Date? = null
)