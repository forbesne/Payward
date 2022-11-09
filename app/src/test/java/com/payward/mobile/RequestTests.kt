package com.payward.mobile

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.payward.mobile.dto.Request
import com.payward.mobile.service.RequestService
import io.mockk.every
import io.mockk.mockk
import junit.framework.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class RequestTests {

    @get:Rule
    var rule: TestRule =  InstantTaskExecutorRule()
    lateinit var mvm:MainViewModel

    var requestService = mockk<RequestService>()

    @Test
    fun confirmRequest_outputsRequest () {
        var request = Request("1",1,"Cincinnati", "Car", "my car broke down")
        assertEquals("1", request.requestId)
    }

    @Test
    fun searchForCincinnati_returnsCincinnati() {
        givenAFeedOfRequestDataAreAvailable()
        whenSearchForCincinnati()
        thenResultContainsCincinnati()
    }

    @Test
    fun searchForCar_returnsCar() {
        givenAFeedOfRequestDataAreAvailable()
        whenSearchForCar()
        thenResultContainsCar()
    }

    private fun givenAFeedOfRequestDataAreAvailable() {
        mvm = MainViewModel()
        createMockData()
    }

    private fun createMockData() {
        var allRequestsLiveData = MutableLiveData<ArrayList<Request>>()
        var allRequests = ArrayList<Request>()
        // create and add Requests to our collection.
        var first = Request("1",1, "Cincinnati", "Car", "my car broke down")
        allRequests.add(first)
        var second = Request("2",2, "New York", "Plumbing", "my kitchen sink has a leak")
        allRequests.add(second)
        var third = Request("3",2, "New York", "Car", "I have a flat tire")
        allRequests.add(third)
        allRequestsLiveData.postValue(allRequests)
        every {requestService.fetchRequests(or("1", "2"))} returns allRequestsLiveData
        every {requestService.fetchRequests(not(or("1", "2")))} returns MutableLiveData<ArrayList<Request>>()
        mvm.requestService = requestService}

    private fun whenSearchForCincinnati() {
        mvm.fetchRequests("1")
    }

    private fun thenResultContainsCincinnati() {
        var qtyFound = 0
        mvm.requests.observeForever {
            // /here is where we do the observing
            assertNotNull(it)
            assertTrue(it.size > 0)
            it.forEach {
                if (it.city == "Cincinnati") {
                    qtyFound += 1
                }
            }
        }
        assertTrue(qtyFound == 1)
    }

    private fun whenSearchForCar() {
        mvm.fetchRequests("1")
    }

    private fun thenResultContainsCar() {
        var qtyFound = 0
        mvm.requests.observeForever {
            // /here is where we do the observing
            assertNotNull(it)
            assertTrue(it.size > 0)
            it.forEach {
                if (it.issueType == "Car") {
                    qtyFound += 1
                }
            }
        }
        assertTrue(qtyFound == 2)
    }
}