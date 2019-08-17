package com.example.video

import android.media.CamcorderProfile
import android.media.MediaRecorder
import android.util.Size
import java.io.FileDescriptor

private fun MediaRecorder.setDefaultValues() {
    setOrientationHint(90)
    setAudioSource(MediaRecorder.AudioSource.MIC)
    setVideoSource(MediaRecorder.VideoSource.SURFACE)
    setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH))
}

fun MediaRecorder.defaultSetup(maxDuration: Int, videoSize: Size) {
    setDefaultValues()
    setMaxDuration(maxDuration)
}

fun MediaRecorder.defaultSetup(outputFilePath: String, maxDuration: Int, videoSize: Size) {
    defaultSetup(maxDuration, videoSize)
    setOutputFile(outputFilePath)
}

fun MediaRecorder.defaultSetup(outputFileDescriptor: FileDescriptor, maxDuration: Int, videoSize: Size) {
    defaultSetup(maxDuration, videoSize)
    setOutputFile(outputFileDescriptor)
}

fun MediaRecorder.defaultPrepare(outputFilePath: String, maxDuration: Int, videoSize: Size) {
    defaultSetup(outputFilePath, maxDuration, videoSize)
    prepare()
}






private fun MediaRecorder.setDefaultValues(cameraId: String) {
    setOrientationHint(90)
    setAudioSource(MediaRecorder.AudioSource.MIC)
    setVideoSource(MediaRecorder.VideoSource.SURFACE)
    setProfile(CamcorderProfile.get(cameraId.toInt(), CamcorderProfile.QUALITY_LOW))
}

fun MediaRecorder.defaultSetup(outputFileDescriptor: FileDescriptor, cameraId: String) {
    setDefaultValues(cameraId)
    setOutputFile(outputFileDescriptor)
}

fun MediaRecorder.defaultPrepare(outputFileDescriptor: FileDescriptor, cameraId: String) {
    defaultSetup(outputFileDescriptor, cameraId)
    prepare()
}

