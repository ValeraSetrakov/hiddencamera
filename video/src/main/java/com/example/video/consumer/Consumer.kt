package com.example.video.consumer

import com.example.video.videoModulePrintThreadName

abstract class Consumer {

    var consumerListener: ConsumerListener? = null

    open fun log(message: String) {
        videoModulePrintThreadName("Consumer. $message")
    }

    abstract fun startConsume()
    abstract fun stopConsume()

    interface ConsumerListener {
        fun onStartConsume()
        fun onStopConsume()
    }
}