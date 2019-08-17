package com.example.video.record

import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaMuxer
import com.example.video.videoModulePrintThreadName
import java.nio.ByteBuffer

class MediaCodecRecord {

    fun log(message: String) {
        videoModulePrintThreadName("Media codec record. $message")
    }

    private val fragments = mutableListOf<Fragment>()
    private val fragmentsInfo = mutableListOf<FragmentInfo>()
    var format: MediaFormat? = null
    val isEmpty: Boolean
        get() = fragments.isEmpty() && fragmentsInfo.isEmpty()

    fun addFragment(byteBuffer: ByteBuffer, byteBufferInfo: MediaCodec.BufferInfo) {
        val offset = byteBufferInfo.offset
        val size = byteBufferInfo.size
        val limit = offset + size

        byteBuffer.position(offset)
        byteBuffer.limit(limit)

        val data = ByteArray(size)
        byteBuffer.get(data, offset, size)

        val frameInfo =
            FragmentInfo(offset, size, byteBufferInfo.presentationTimeUs, byteBufferInfo.flags)
        val frame = Fragment(data)

        fragments.add(frame)
        fragmentsInfo.add(frameInfo)
    }

    fun save(muxer: MediaMuxer, track: Int) {
        log("Save media codec data throw muxer. Track = $track")
        val frameSize = fragments.size

        log("Duration = ${(fragmentsInfo.last().presentationTimeUs - fragmentsInfo.first().presentationTimeUs) / 1000000}")

        for(i in 0 until frameSize) {
            val frame = fragments[i]
            val frameInfo = fragmentsInfo[i]

            val byteBuffer = ByteBuffer.allocateDirect(frameInfo.size)
            byteBuffer.put(frame.bytes)

            val bufferInfo = MediaCodec.BufferInfo()
            bufferInfo.set(0, frameInfo.size, frameInfo.presentationTimeUs, frameInfo.flags)
            log("Timestamp ${frameInfo.presentationTimeUs}")
            muxer.writeSampleData(track, byteBuffer, bufferInfo)
        }
        flush()
    }

    fun flush() {
        log("Flush media codec data")
        fragments.clear()
        fragmentsInfo.clear()
    }

    private data class Fragment (
        val bytes: ByteArray
    )

    private data class FragmentInfo (
        val offset: Int,
        val size: Int,
        val presentationTimeUs: Long,
        val flags: Int
    )
}