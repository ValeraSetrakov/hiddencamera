package com.example.video.producer

import java.util.concurrent.atomic.AtomicInteger

class CompositeProducer: Producer() {

    override fun log(message: String) {
        super.log("Composite producer. $message")
    }

    private val producers = mutableListOf<Producer>()

    private val startedProducersCounter = AtomicInteger(0)

    fun add(producer: Producer) {
        log("Add new producer")
        producer.producerListener = object: ProducerListener {
            override fun onStartProduce() {
                log("On start produce")
                val countOfStartedProducers = startedProducersCounter.incrementAndGet()
                log("Count of started producers $countOfStartedProducers")
                if (countOfStartedProducers  == producers.size) {
                    log("Composite producer start")
                    producerListener?.onStartProduce()
                }
            }

            override fun onStopProduce() {
                log("On stop produce")
                val countOfStartedProducers = startedProducersCounter.decrementAndGet()
                log("Count of started producers $countOfStartedProducers")
                if (countOfStartedProducers == 0) {
                    log("Composite producer start")
                    producerListener?.onStopProduce()
                }
            }
        }

        producers.add(producer)
    }

//    fun remove(producer: Producer) =
//        producers.remove(producer)

    override fun startProduce() {
        log("Start composite produce")
        producers.forEach { it.startProduce() }
    }

    override fun stopProduce() {
        log("Stop composite produce")
        producers.forEach { it.stopProduce() }
    }
}