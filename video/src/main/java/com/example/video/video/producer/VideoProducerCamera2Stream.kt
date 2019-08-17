package com.example.video.video.producer

import android.content.Context
import androidx.annotation.RequiresPermission
import com.example.video.SurfaceHolder


class VideoProducerCamera2Stream(context: Context, distanceHolder: SurfaceHolder): VideoProducerCamera2(context, distanceHolder) {

    override fun log(message: String) {
        super.log("VideoProducerCamera2Stream. $message")
    }

    /**
     * Start record. Open camera.
     */
    @RequiresPermission(android.Manifest.permission.CAMERA)
    override fun startProduce() {
        log("Start produce video data")
        openCamera()
    }

    /**
     * Stop record. Close camera device.
     */
    @RequiresPermission(android.Manifest.permission.CAMERA)
    override fun stopProduce() {
        log("Stop produce video data")
        cameraDevice?.close()
    }

}