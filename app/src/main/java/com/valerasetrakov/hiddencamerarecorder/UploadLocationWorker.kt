package com.valerasetrakov.hiddencamerarecorder

import android.content.Context
import android.widget.Toast
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.commonandroid.log
import com.example.commonandroid.loge
import com.valerasetrakov.data.ApiProvider
import com.valerasetrakov.data.Location
import com.valerasetrakov.data.MobileState
import java.lang.Exception

class UploadLocationWorker(context: Context, workerParameters: WorkerParameters): Worker(context, workerParameters) {

    companion object {
        const val LOCATION_DEVICE_ID_KEY = "LOCATION_DEVICE_ID_KEY"
        const val LOCATION_TIMESTAMP_KEY = "LOCATION_TIMESTAMP_KEY"
        const val LOCATION_COORDINATES_KEY = "LOCATION_COORDINATES_KEY"
        const val LOCATION_CHARGE_LEVEL_KEY = "LOCATION_CHARGE_LEVEL_KEY"
        const val LOCATION_FREE_MEMORY_KEY = "LOCATION_FREE_MEMORY_KEY"
        const val LOCATION_START_KEY = "LOCATION_START_KEY"
        const val LOCATION_END_KEY = "LOCATION_END_KEY"
    }

    val api = ApiProvider.api

    private fun locationLog(message: String) {
        log("${this::class.java.simpleName} $message")
    }

    private fun locationLogE(message: String, e: Throwable) {
        loge("${this::class.java.simpleName} $message", e)
    }

    override fun doWork(): Result {

        locationLog("Start location work")

        val device = inputData.getString(LOCATION_DEVICE_ID_KEY)!!
        val timeStamp = inputData.getString(LOCATION_TIMESTAMP_KEY)!!
        val coordinates = inputData.getDoubleArray(LOCATION_COORDINATES_KEY)!!
        val chargeLevel = inputData.getInt(LOCATION_CHARGE_LEVEL_KEY, 0)
        val freeMemory = inputData.getLong(LOCATION_FREE_MEMORY_KEY, 0)
        val isStartGeoPoint = inputData.getBoolean(LOCATION_START_KEY, false)
        val isEndGeoPoint = inputData.getBoolean(LOCATION_END_KEY, false)

        val location = Location(coordinates = coordinates)

        val mobileState = MobileState(
            device = device,
            markTime = timeStamp,
            location = location,
            chargeLevel = chargeLevel,
            freeMemory = freeMemory,
            isStartGeoPoint = isStartGeoPoint,
            isEndGeoPoint = isEndGeoPoint
        )

        locationLog("mobileState $mobileState")

        try {
            val response = api.requestPostMobileState(mobileState).execute()
            if (!response.isSuccessful) {
                throw Exception("${response.errorBody()?.string()}")
            }
        } catch (e: Throwable) {
            locationLogE("Error", e)
            return Result.retry()
        }

        locationLog("Location work success")

        return Result.success()
    }
}