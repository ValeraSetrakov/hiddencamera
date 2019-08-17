package com.valerasetrakov.hiddencamerarecorder

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.IBinder

@SuppressLint("MissingPermission")
class LocationService: Service() {

    private lateinit var locationManager: LocationManager
    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        locationManager = LocationManager(this)
        notificationManager = NotificationManager(this)
        startForeground(1234, notificationManager.createLocationServiceNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        locationManager.startLocationUpdates()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager.stopLocationUpdates()
    }





    override fun onBind(intent: Intent?): IBinder? = null

}