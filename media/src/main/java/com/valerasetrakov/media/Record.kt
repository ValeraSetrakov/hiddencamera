package com.valerasetrakov.media

import android.media.MediaCodec
import java.nio.ByteBuffer

data class Record (
    val data: ByteArray,
    val offset: Int,
    val size: Int,
    val timestamp: Long,
    val flags: Int
) {
    companion object {
        fun create(byteBuffer: ByteBuffer, info: MediaCodec.BufferInfo): Record {
            val byteArray = ByteArray(info.size - info.offset)
            byteBuffer.get(byteArray)
            val audioData = Record(byteArray, 0, info.size - info.offset, info.presentationTimeUs, info.flags)
            return audioData
        }
    }

    fun revert(): Pair<ByteBuffer, MediaCodec.BufferInfo> {
        val info = MediaCodec.BufferInfo()
        info.set(offset, size, timestamp, flags)
        val byteBuffer = ByteBuffer.allocateDirect(size)
        byteBuffer.put(data)
        return Pair(byteBuffer, info)
    }
}