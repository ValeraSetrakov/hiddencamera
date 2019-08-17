package com.example.video.audio

import android.media.AudioRecord
import com.example.video.producer.DistanceHolder
import com.example.video.producer.ProducerWithDistance

class AudioProducer(distanceHolder: DistanceHolder<AudioRecord>): ProducerWithDistance<AudioRecord>(distanceHolder) {

    override fun log(message: String) {
        super.log("AudioProducer. $message")
    }

    override fun startProduce() {
        log("Start producer")
        distance.startRecording()
        producerListener?.onStartProduce()
    }

    override fun stopProduce() {
        log("Stop producer")
        distance.stop()
        producerListener?.onStopProduce()
    }


}