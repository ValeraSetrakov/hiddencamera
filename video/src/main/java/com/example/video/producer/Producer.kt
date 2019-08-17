package com.example.video.producer

import com.example.video.videoModulePrintThreadName

abstract class Producer {

    var producerListener: ProducerListener? = null

    open fun log(message: String) {
        videoModulePrintThreadName("Producer. $message")
    }

    abstract fun startProduce()
    abstract fun stopProduce()

    interface ProducerListener {
        fun onStartProduce()
        fun onStopProduce()
    }

}