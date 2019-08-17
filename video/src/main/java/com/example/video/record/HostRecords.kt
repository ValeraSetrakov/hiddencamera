package com.example.video.record

import com.example.video.videoModulePrintThreadName

class HostRecords {

    open fun log(message: String) {
        videoModulePrintThreadName("Host records. $message")
    }

    val records = mutableListOf<MediaCodecRecord>()
    fun createRecord(): MediaCodecRecord {
        log("Create new record")
        val mediaCodecRecord = MediaCodecRecord()
        records.add(mediaCodecRecord)
        return mediaCodecRecord
    }

    fun flush() {
        records.forEach { it.flush() }
    }
}