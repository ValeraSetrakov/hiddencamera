package com.example.video.producer

abstract class ProducerWithDistance<DISTANCE>(val distanceHolder: DistanceHolder<DISTANCE>): Producer() {

    override fun log(message: String) {
        super.log("Producer. $message")
    }

    val distance: DISTANCE
        get() = distanceHolder.distance
}