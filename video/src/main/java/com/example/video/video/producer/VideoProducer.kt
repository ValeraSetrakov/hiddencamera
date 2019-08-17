package com.example.video.video.producer

import com.example.video.producer.DistanceHolder
import com.example.video.producer.ProducerWithDistance

abstract class VideoProducer<DISTANCE>(distanceHolder: DistanceHolder<DISTANCE>): ProducerWithDistance<DISTANCE>(distanceHolder) {
    override fun log(message: String) {
        super.log("VideoProducer. $message")
    }
}
