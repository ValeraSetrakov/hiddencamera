package com.example.video.video.consumer

import android.media.MediaFormat
import com.example.video.consumer.MediaCodecConsumer
import com.example.video.consumer.SourceHolder
import com.example.video.record.MediaCodecRecord

abstract class VideoMediaCodecConsumer<SOURCE>(private val setting: Setting, mediaCodecRecord: MediaCodecRecord, sourceHolder: SourceHolder<SOURCE>)
    : MediaCodecConsumer<SOURCE>(mediaCodecRecord, sourceHolder) {

    override fun prepareMediaFormat(): MediaFormat {

        val width = setting.width
        val height = setting.height
        val colorFormat = setting.colorFormat
        val videoBitrate = setting.videoBitrate
        val videoFramePerSecond = setting.videoFramePerSecond
        val iframeInterval = setting.iframeInterval

        format = MediaFormat.createVideoFormat("video/avc", width, height)
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat)
        format.setInteger(MediaFormat.KEY_BIT_RATE, videoBitrate)
        format.setInteger(MediaFormat.KEY_FRAME_RATE, videoFramePerSecond)
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, iframeInterval)

        return format

    }



    data class Setting (
        val width: Int,
        val height: Int,
        val colorFormat: Int,
        val videoBitrate: Int,
        val videoFramePerSecond: Int,
        val iframeInterval: Int,
        val rotation: Int
    )
}