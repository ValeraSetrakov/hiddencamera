package com.example.video.mock

import com.example.commonandroid.log
import com.example.video.Controller
import com.example.video.consumer.Consumer
import com.example.video.producer.Producer
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

class MockController: Controller(
    MockConsumer(),
    MockProducer()
) {

    companion object {
        private fun print(message: String) {
            log("Test controller. $message")
        }
    }





    class MockConsumer: Consumer() {

        private lateinit var worker: Thread
        private val isContinue = AtomicBoolean(true)

        override fun startConsume() {
            print("Consumer start")
            isContinue.set(true)
            worker = thread {

                consumerListener?.onStartConsume()
                var iteration = 0

                while (isContinue.get()) {
                    print("Consume iteration: $iteration")
                    Thread.sleep(1000)
                    iteration++
                }

                consumerListener?.onStopConsume()
            }
        }

        override fun stopConsume() {
            print("Consumer stop")
            isContinue.set(false)
        }
    }

    class MockProducer: Producer() {
        private lateinit var worker: Thread
        private val isContinue = AtomicBoolean(true)

        override fun startProduce() {
            print("Producer start")
            isContinue.set(true)
            worker = thread {
                producerListener?.onStartProduce()
                var iteration = 0

                while (isContinue.get()) {
                    print("Produce iteration: $iteration")
                    Thread.sleep(1000)
                    iteration++
                }

                producerListener?.onStopProduce()
            }
        }

        override fun stopProduce() {
            print("Producer stop")
            isContinue.set(false)
        }
    }

    override fun start() {

    }

    override fun stop() {

    }
}