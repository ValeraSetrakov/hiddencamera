package com.example.video.video.consumer

import android.view.Surface
import com.example.video.SurfaceHolder
import com.example.video.record.MediaCodecRecord


class SurfaceMediaCodecConsumer(videoRecord: MediaCodecRecord, setting: Setting, surfaceHolder: SurfaceHolder):
    VideoMediaCodecConsumer<Surface>(setting, videoRecord, surfaceHolder) {

    override fun log (message: String) {
        super.log("Surface media codec. $message")
    }

    override fun createSource(): Surface {
        return codec.createInputSurface()
    }

    override fun stopConsume() {
        codec.signalEndOfInputStream()
    }
}