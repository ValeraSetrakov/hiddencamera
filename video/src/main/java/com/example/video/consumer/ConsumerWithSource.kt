package com.example.video.consumer

abstract class ConsumerWithSource<SOURCE>(val sourceHolder: SourceHolder<SOURCE>): Consumer() {
    override fun log(message: String) {
        super.log("ConsumerWithSource. $message")
    }

    abstract fun createSource(): SOURCE
    abstract fun prepare()
}