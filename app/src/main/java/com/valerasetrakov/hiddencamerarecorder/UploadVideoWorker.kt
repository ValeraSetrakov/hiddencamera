package com.valerasetrakov.hiddencamerarecorder

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.crashlytics.android.Crashlytics
import com.example.commonandroid.CrashlyticsManager
import com.example.commonandroid.log
import com.valerasetrakov.data.VideoFragment
import com.valerasetrakov.data.VideoRepository

class UploadVideoWorker(context: Context, workerParameters: WorkerParameters): Worker(context, workerParameters) {

    companion object {
        const val VIDEO_FRAGMENT_PATH_KEY = "VIDEO_FRAGMENT_PATH_KEY"
        const val VIDEO_FRAGMENT_DEVICE_ID_KEY = "VIDEO_FRAGMENT_DEVICE_ID_KEY"
        const val VIDEO_FRAGMENT_START_KEY = "VIDEO_FRAGMENT_START_KEY"
        const val VIDEO_FRAGMENT_END_KEY = "VIDEO_FRAGMENT_END_KEY"
        const val VIDEO_FRAGMENT_TIMESTAMP_KEY = "VIDEO_FRAGMENT_TIMESTAMP_KEY"
        const val VIDEO_FRAGMENT_CHARGE_LEVEL_KEY = "VIDEO_FRAGMENT_CHARGE_LEVEL_KEY"
        const val VIDEO_FRAGMENT_FREE_MEMORY_KEY = "VIDEO_FRAGMENT_FREE_MEMORY_KEY"
    }

    private fun print(message: String) {
        log("UploadVideoWorker. $message")
    }

    override fun doWork(): Result {

        print("Start work")

        val videoFragmentPath = inputData.getString(VIDEO_FRAGMENT_PATH_KEY)
        val videoFragmentDeviceId = inputData.getString(VIDEO_FRAGMENT_DEVICE_ID_KEY)
        val videoFragmentStart = inputData.getBoolean(VIDEO_FRAGMENT_START_KEY, false)
        val videoFragmentEnd = inputData.getBoolean(VIDEO_FRAGMENT_END_KEY, false)
        val videoFragmentTimeStamp = inputData.getString(VIDEO_FRAGMENT_TIMESTAMP_KEY)
        val videoFragmentChargeLevel = inputData.getInt(VIDEO_FRAGMENT_CHARGE_LEVEL_KEY, 0)
        val videoFragmentFreeMemory = inputData.getLong(VIDEO_FRAGMENT_FREE_MEMORY_KEY, 0)

        val videoFragment = VideoFragment(
            device = videoFragmentDeviceId!!,
            video = videoFragmentPath!!,
            chargeLevel = videoFragmentChargeLevel,
            markTime = videoFragmentTimeStamp!!,
            freeMemory = videoFragmentFreeMemory,
            isStartFrame = videoFragmentStart,
            isEndFrame = videoFragmentEnd)

        print("video fragment $videoFragment")

        try {
            CrashlyticsManager.log("Start uploading video ${videoFragment.video}")
            VideoRepository.handleVideo(videoFragment)
            CrashlyticsManager.log("End uploading video ${videoFragment.video}")
        } catch (e: Throwable) {
            print("Error $e")
            CrashlyticsManager.logException(e)
            return Result.retry()
        }

        print("Success")
        return Result.success()
    }
}