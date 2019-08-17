package com.valerasetrakov.media

import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.MediaRecorder
import android.util.Size
import com.example.common.defaultSDFFileName
import com.example.common.fileNameLikeDate
import com.example.common.now
import com.example.commonandroid.printThreadName
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object MediaUtil {

//    private val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd-hh-mm-ss")

    fun getVideoFilePath(filesDir: File): String {
        val fileName = generateFileName()
        val file = File(filesDir, fileName)
        return file.absolutePath
    }

    fun getVideoSize(cameraManager: CameraManager, cameraId: String): Size {
        val characteristics = cameraManager.getCameraCharacteristics(cameraId)
        val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            ?: throw RuntimeException("Cannot get available preview/video sizes")
        val availableVideoSizes = map.getOutputSizes(MediaRecorder::class.java)
        printThreadName("available videoSizes ${Arrays.toString(availableVideoSizes)}")
        val videoSize = chooseVideoSize(availableVideoSizes)
        printThreadName("videoSize $videoSize")
        return videoSize
    }

    /**
     * In this sample, we choose a video size with 3x4 aspect ratio. Also, we don't use sizes
     * larger than 1080p, since MediaRecorder cannot handle such a high-resolution video.
     *
     * @param choices The list of available sizes
     * @return The video size
     */
    private fun chooseVideoSize(choices: Array<Size>) = choices.firstOrNull { it.width <= 1080 && it.height <= 1080 } ?: choices[choices.size - 1]

    private fun generateFileName () = "${fileNameLikeDate()}.mp4"//"${simpleDateFormat.now()}.mp4"
}