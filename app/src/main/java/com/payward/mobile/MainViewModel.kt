package com.payward.mobile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.payward.mobile.dto.Request
import com.payward.mobile.dto.User
import com.payward.mobile.dto.UserRoom
import com.payward.mobile.service.FirebaseService
import com.payward.mobile.service.RequestService

class MainViewModel : ViewModel() {
    var testRequests: MutableLiveData<ArrayList<Request>> = MutableLiveData<ArrayList<Request>>()
    var requestService: RequestService = RequestService()
    private var firebaseService: FirebaseService = FirebaseService()

    fun fetchRequests(requestId: String) {
        testRequests = requestService.fetchRequests(requestId)
    }

    fun initializeFirebase(){
        firebaseService.initialize()
    }

    fun save(request: Request) {
        firebaseService.save(request)
    }

    fun save(user: User) {
        firebaseService.save(user)
    }

    fun respond(request: Request) {
        firebaseService.respond(request)
    }

    fun createUser() {
        firebaseService.createUser()
    }

    fun acceptHelp(request: Request, toUid: String) {
        firebaseService.acceptHelp(request, toUid)
    }
    fun sendMessage(
        roomId: String,
        msgRequest: Request,
        fromUser: User,
        toUid: String,
        toUser: User,
        messageText: String
    ) {
        firebaseService.sendMessage(roomId, msgRequest, fromUser, toUid, toUser, messageText)
    }

    internal var requests:MutableLiveData<ArrayList<Request>>
        get() { return firebaseService.requests}
        set(value) {firebaseService.requests = value}

    internal var request:Request
        get() { return firebaseService.request}
        set(value) {firebaseService.request = value}

    internal var userRooms:MutableLiveData<ArrayList<UserRoom>>
        get() { return firebaseService.userRooms}
        set(value) {firebaseService.userRooms = value}

    internal var userRoom:UserRoom
        get() { return firebaseService.userRoom}
        set(value) {firebaseService.userRoom = value}

    internal var currentUser:MutableLiveData<User>
        get() { return firebaseService.currentUser}
        set(value) {firebaseService.currentUser = value}
}