package com.example.video

import com.example.video.consumer.Consumer
import com.example.video.producer.Producer
import com.example.video.producer.ProducerWithDistance

abstract class Controller (protected val consumer: Consumer, protected val producer: Producer) {

    var controllerListener: Controller.ControllerListener? = null

    open fun log (message: String) {
        videoModulePrintThreadName("Controller. $message")
    }

    /**
     * Start producer and after success producer start, consumer will be start too
     */
    abstract fun start()

    /**
     * Stop consumer and after producer will stop too
     */
    abstract fun stop()

    interface ControllerListener {
        fun onStart()
        fun onStop()
    }
}