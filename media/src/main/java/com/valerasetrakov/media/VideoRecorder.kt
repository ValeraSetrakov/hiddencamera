package com.valerasetrakov.media

import android.content.Context
import android.media.CamcorderProfile
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.view.Surface
import androidx.annotation.RequiresPermission
import com.example.common.FlagsUtil
import timber.log.Timber

class VideoRecorder(context: Context): Recorder(), Camera2Producer.CameraListener {

    companion object {
        fun logd(message: String) {
            Timber.d("VideoRecorder. $message")
        }
    }

    lateinit var camera2Producer: Camera2Producer
    lateinit var mediaCodec: MediaCodec
    lateinit var mediaFormat: MediaFormat
    lateinit var currentDistance: Surface
    private var setting: Setting
    var videoRecorderListener: VideoRecorderListener? = null

    init {
        createCamera2Producer(context)
        val camcorderProfile = CamcorderProfile.get(camera2Producer.cameraId.toInt(), CamcorderProfile.QUALITY_720P)
        setting = Setting(
            width = camcorderProfile.videoFrameWidth,
            height = camcorderProfile.videoFrameHeight,
            colorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface,
            videoBitrate = camcorderProfile.videoBitRate,
            videoFramePerSecond = camcorderProfile.videoFrameRate,
            iframeInterval = 0,
            rotation = 90
        )
    }

    fun createCamera2Producer (context: Context) {
        camera2Producer = Camera2Producer(context).apply { cameraListener = this@VideoRecorder }
    }

    fun createMediaFormat() {
        val width = setting.width
        val height = setting.height
        val colorFormat = setting.colorFormat
        val videoBitrate = setting.videoBitrate
        val videoFramePerSecond = setting.videoFramePerSecond
        val iframeInterval = setting.iframeInterval

        mediaFormat = MediaFormat.createVideoFormat("video/avc", width, height)
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat)
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, videoBitrate)
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, videoFramePerSecond)
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, iframeInterval)
    }

    fun createMediaCodec() {
        createMediaFormat()
        mediaCodec = MediaCodec.createEncoderByType(mediaFormat.getString(MediaFormat.KEY_MIME))
        mediaCodec.setCallback(object: MediaCodec.Callback() {
            override fun onOutputBufferAvailable(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
                val byteBuffer = codec.getOutputBuffer(index) ?: return

//                logd("Write video data to file")

                if (FlagsUtil.isFlagSet(info.flags, MediaCodec.BUFFER_FLAG_CODEC_CONFIG)) {
                    logd("Video fragment is codec config")
                    codec.releaseOutputBuffer(index, false)
                    return
                }

                if (FlagsUtil.isFlagSet(info.flags, 0)) {
//                    logd("Video fragment is video data")
                    val audioData = Record.create(byteBuffer, info)
                    synchronized(records) {
                        records.add(audioData)
                    }
                    codec.releaseOutputBuffer(index, false)
                }

                if (FlagsUtil.isFlagSet(info.flags, MediaCodec.BUFFER_FLAG_END_OF_STREAM)) {
                    logd("Video fragment is stop signal")
                    camera2Producer.stop()
                }
            }

            override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
                // Don't call, input buffers come from surface
            }

            override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
                logd("Change video format")
                mediaFormat = format
            }

            override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {

            }
        })
        mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        currentDistance = mediaCodec.createInputSurface()
    }

    /** On camera close.*/
    override fun onClose() {
        mediaCodec.stop()
        mediaCodec.release()
        videoRecorderListener?.onStopVideoRecorder()
    }

    /** On camera open.*/
    override fun onOpen() {

    }

    /** On camera start.*/
    override fun onStart() {
        mediaCodec.start()
        videoRecorderListener?.onStartVideoRecorder()
    }

    @RequiresPermission(android.Manifest.permission.CAMERA)
    override fun start() {
        createMediaCodec()
        camera2Producer.start(currentDistance)
    }

    override fun stop() {
        mediaCodec.signalEndOfInputStream()
    }


    interface VideoRecorderListener {
        fun onStartVideoRecorder()
        fun onStopVideoRecorder()
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