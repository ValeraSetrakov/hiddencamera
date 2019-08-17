package com.valerasetrakov.hiddencamerarecorder

import com.example.commonandroid.log
import com.valerasetrakov.data.VideoFragment
import com.valerasetrakov.media.MediaController

open class OnVideoRecordListenerImpl: MediaController.OnSaveMediaDataListener{

    companion object {
        private fun print(message: String) {
            log("OnVideoRecordListenerImpl. $message")
        }
    }

    private val deviceId = App.deviceId

    private fun createVideoFragment(videoFilePath: String) =
        VideoFragment(
            device = deviceId,
            video = videoFilePath,
            chargeLevel = App.batteryLevel,
            freeMemory = App.phoneStorageFreeLikeMB()
        )

    private fun sendVideo (videoFragment: VideoFragment) {
        print("Video fragment $videoFragment")
        WorkerManager.sendVideo(videoFragment)
    }

    override fun onRecordVideo(videoFilePath: String) {
        log("onRecordVideo videoFilePath $videoFilePath")
        sendVideo(createVideoFragment(videoFilePath))
    }

    override fun onRecordVideoStart(videoFilePath: String) {
        log("onRecordStartVideo videoFilePath $videoFilePath")
        val videoFragment = createVideoFragment(videoFilePath)
        videoFragment.isStartFrame = true
        sendVideo(videoFragment)
    }

    override fun onRecordVideoEnd(videoFilePath: String) {
        log("onRecordEndVideo videoFilePath $videoFilePath")
        val videoFragment = createVideoFragment(videoFilePath)
        videoFragment.isEndFrame = true
        sendVideo(videoFragment)
    }

    override fun onRecordVideoStartEnd(videoFilePath: String) {
        log("onRecordStartEndVideo videoFilePath $videoFilePath")
        val videoFragment = createVideoFragment(videoFilePath)
        videoFragment.isStartFrame = true
        videoFragment.isEndFrame = true
        sendVideo(videoFragment)
    }

}