package com.valerasetrakov.hiddencamerarecorder

import androidx.work.*
import com.example.commonandroid.log
import com.valerasetrakov.data.MobileState
import com.valerasetrakov.data.VideoFragment
import java.util.concurrent.TimeUnit

object WorkerManager {

    private val workManager = App.workManager

    private fun print(message: String) {
        log("WorkerManager. $message")
    }

    /**
     * Setup and post work for sending mobile state to server
     */
    fun sendMobileState (mobileState: MobileState) {
        print("send mobile state $mobileState")
        val arguments = mapOf(
            UploadLocationWorker.LOCATION_DEVICE_ID_KEY to mobileState.device,
            UploadLocationWorker.LOCATION_TIMESTAMP_KEY to mobileState.markTime,
            UploadLocationWorker.LOCATION_COORDINATES_KEY to mobileState.location.coordinates,
            UploadLocationWorker.LOCATION_CHARGE_LEVEL_KEY to mobileState.chargeLevel,
            UploadLocationWorker.LOCATION_FREE_MEMORY_KEY to mobileState.freeMemory,
            UploadLocationWorker.LOCATION_START_KEY to mobileState.isStartGeoPoint,
            UploadLocationWorker.LOCATION_END_KEY to mobileState.isEndGeoPoint
        )

        val data = Data.Builder()
            .putAll(arguments)
            .build()

        val constraint = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val uploadVideoWorker = OneTimeWorkRequestBuilder<UploadLocationWorker>()
            .setInputData(data)
//            .setConstraints(constraint)
            .setBackoffCriteria(BackoffPolicy.LINEAR, WorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueue(uploadVideoWorker)
    }

    fun sendVideo (videoFragment: VideoFragment) {
        print("sendVideo videoFragment $videoFragment")
        val arguments = mapOf(
            UploadVideoWorker.VIDEO_FRAGMENT_DEVICE_ID_KEY to videoFragment.device,
            UploadVideoWorker.VIDEO_FRAGMENT_PATH_KEY to videoFragment.video,
            UploadVideoWorker.VIDEO_FRAGMENT_CHARGE_LEVEL_KEY to videoFragment.chargeLevel,
            UploadVideoWorker.VIDEO_FRAGMENT_TIMESTAMP_KEY to videoFragment.markTime,
            UploadVideoWorker.VIDEO_FRAGMENT_FREE_MEMORY_KEY to videoFragment.freeMemory,
            UploadVideoWorker.VIDEO_FRAGMENT_START_KEY to videoFragment.isStartFrame,
            UploadVideoWorker.VIDEO_FRAGMENT_END_KEY to videoFragment.isEndFrame
        )

        val data = Data.Builder().putAll(arguments).build()
        val constraint = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

        val uploadVideoWorker = OneTimeWorkRequestBuilder<UploadVideoWorker>()
            .setInputData(data)
//            .setConstraints(constraint)
            .setBackoffCriteria(BackoffPolicy.LINEAR, WorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
            .build()
        workManager.enqueue(uploadVideoWorker)
    }
}