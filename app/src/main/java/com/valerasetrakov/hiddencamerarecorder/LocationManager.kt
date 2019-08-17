package com.valerasetrakov.hiddencamerarecorder

import android.content.Context
import android.os.HandlerThread
import androidx.annotation.RequiresPermission
import com.example.commonandroid.log
import com.google.android.gms.location.*
import com.valerasetrakov.data.Location
import com.valerasetrakov.data.MobileState

class  LocationManager (context: Context) {

    companion object {
        private fun print(message: String) {
            log("LocationManager. $message")
        }
    }

    private val backgroundThread = HandlerThread("Location manager thread").apply { start() }

    private val deviceId = App.deviceId
    private var fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    private val boxInfoController = App.boxInfoController
    /** available radius for box moving in meters */
    private val availableDistance: Int
        get() = boxInfoController.roadMin

    private var locationRequest: LocationRequest? = LocationRequest.create()?.apply {
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        smallestDisplacement = availableDistance.toFloat()
        interval = 5000
    }

    private var isSmsAlreadySended = false

    private var startLocation: Location? = null

    private var locationCallback: LocationCallback = object: LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            print("onLocationResult")
            print("Location result $locationResult")
            val lastLocation = locationResult.lastLocation
            val lat = lastLocation.latitude
            val long = lastLocation.longitude
            val batteryLevel = App.batteryLevel
            val freeMemory = App.freeMemory
            val location = Location(coordinates = doubleArrayOf(long, lat))
            val mobileState = MobileState(
                device = deviceId,
                chargeLevel = batteryLevel,
                freeMemory = freeMemory,
                location = location)

            App.currnetPosition.postValue(location)
            print("availableDistance $availableDistance")
            if (startLocation == null) {
                startLocation = location
                App.startPosition.postValue(startLocation)
                print("Start location $startLocation")
                mobileState.isStartGeoPoint = true
                mobileState.isEndGeoPoint = false
            } else {
                print("""
                    start location lat ${startLocation!!.coordinates[1]} long ${startLocation!!.coordinates[0]}
                    current location lat $lat lon $long
                """.trimIndent())
                val distance = distance(startLocation!!.coordinates[1], lat, startLocation!!.coordinates[0], long)
                print("distance $distance")

                if (distance.compareTo(availableDistance) > 0 && !isSmsAlreadySended) {
                    print("Go out from available radius")
                    App.sendNotificationAboutBoxOut()
                    isSmsAlreadySended = true
                } else if (distance.compareTo(availableDistance) <= 0 && isSmsAlreadySended) {
                    print("Come back to available radius")
                    App.sendNotificationAboutBoxComeBack()
                    isSmsAlreadySended = false
                }
            }

            print("mobileState $mobileState")
            sendMobileState(mobileState)
        }

        override fun onLocationAvailability(locationAvailability: LocationAvailability?) {
            log("Location is available? ${locationAvailability?.isLocationAvailable}")
        }
    }

    /**
     * Calculate distance between two points in latitude and longitude taking
     * into account height difference. If you are not interested in height
     * difference pass 0.0. Uses Haversine method as its base.
     *
     * lat1, lon1 Start point lat2, lon2 End point el1 Start altitude in meters
     * el2 End altitude in meters
     * @returns Distance in Meters
     */
    fun distance(
        lat1: Double, lat2: Double, lon1: Double,
        lon2: Double, el1: Double = 0.0, el2: Double = 0.0
    ): Double {

        val R = 6371 // Radius of the earth

        val latDistance = Math.toRadians(lat2 - lat1)
        val lonDistance = Math.toRadians(lon2 - lon1)
        val a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                (Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2))
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        var distance = R.toDouble() * c * 1000.0 // convert to meters

        val height = el1 - el2

        distance = Math.pow(distance, 2.0) + Math.pow(height, 2.0)
        val result = Math.sqrt(distance)

        return result
    }

    /**
     * Setup and post work for sending mobile state to server
     */
    private fun sendMobileState (mobileState: MobileState) {
        WorkerManager.sendMobileState(mobileState)
    }

    @RequiresPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)
    fun startLocationUpdates() {
        print("Start location updates")
        val task = fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            backgroundThread.looper)
    }

    fun stopLocationUpdates() {
        print("Stop location updates")
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

//    private data class Location (
//        var lon: Double,
//        var lat: Double
//    )

}