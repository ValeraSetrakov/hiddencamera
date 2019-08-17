package com.example.video.consumer

import java.util.concurrent.atomic.AtomicInteger

class CompositeConsumer: Consumer() {

    override fun log(message: String) {
        super.log("CompositeConsumer. $message")
    }

    val consumers = mutableListOf<Consumer>()

    private val startedConsumersCounter = AtomicInteger(0)

    fun add(consumer: Consumer) {
        log("Add new consumer")
        consumer.consumerListener = object: ConsumerListener {
            override fun onStartConsume() {
                log("On start one of consumers")
                val countOfStartedConsumers = startedConsumersCounter.incrementAndGet()
                log("Count of started consumers $countOfStartedConsumers")
                if (countOfStartedConsumers == consumers.size) {
                    log("Composite consumer started")
                    consumerListener?.onStartConsume()
                }
            }

            override fun onStopConsume() {
                log("On stop one of consumers")
                val countOfStartedConsumers = startedConsumersCounter.decrementAndGet()
                log("Count of started consumers $countOfStartedConsumers")
                if (countOfStartedConsumers == 0) {
                    log("Composite consumer stopped")
                    consumerListener?.onStopConsume()
                }
            }
        }
        consumers.add(consumer)
    }

//    fun remove(consumer: Consumer): Boolean =
//        consumers.remove(consumer)

    override fun startConsume() {
        log("Start composite consumer")
        consumers.forEach {it.startConsume()}
    }

    override fun stopConsume() {
        log("Stop composite consumer")
        consumers.forEach {it.stopConsume()}
    }
}