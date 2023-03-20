package com.payward.mobile

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import androidx.lifecycle.LiveData
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.payward.mobile.dto.LocationDetails

class LocationLiveData(var context: Context) : LiveData<LocationDetails>() {

    private var fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    override fun onActive() {
        super.onActive()

        fusedLocationClient.lastLocation.addOnSuccessListener {
                location: Location ->
            location.also {
                setLocationData(it)
            }
        }
        startLocationUpdates()
    }


    @SuppressLint("MissingPermission")
    internal fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(locationRequest,
            locationCallback,
            Looper.getMainLooper())
    }

    private fun setLocationData(location: Location?) {
        location?.let {
            value = LocationDetails(it.longitude.toString(), it.latitude.toString())
        }
    }

    override fun onInactive() {
        super.onInactive()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            locationResult ?: return
            for (location in locationResult.locations) {
                setLocationData(location)
            }
        }
    }

    companion object {
        val ONE_MINUTE : Long = 60000
        val locationRequest : LocationRequest = LocationRequest.create().apply {
            interval = ONE_MINUTE
            fastestInterval = ONE_MINUTE/4
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

    }
}