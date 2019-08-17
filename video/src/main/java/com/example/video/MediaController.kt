package com.example.video

import android.content.Context
import android.media.CamcorderProfile
import android.media.MediaCodecInfo
import com.example.video.audio.AudioConsumer
import com.example.video.audio.AudioProducer
import com.example.video.audio.DefaultAudioSetting
import com.example.video.consumer.CompositeConsumer
import com.example.video.consumer.Consumer
import com.example.video.producer.CompositeProducer
import com.example.video.producer.Producer
import com.example.video.record.HostRecords
import com.example.video.video.consumer.SurfaceMediaCodecConsumer
import com.example.video.video.consumer.VideoMediaCodecConsumer
import com.example.video.video.producer.VideoProducerCamera2Stream

class MediaController(consumer: Consumer,
                      producer: Producer,
                      private val mediaFileController: MediaFileController)
    : Controller(consumer, producer) {

    companion object {

        fun create(consumer: Consumer, producer: Producer, mediaFileController: MediaFileController): MediaController =
                MediaController(consumer, producer, mediaFileController)

        fun create(context: Context, onVideoRecordListener: OnVideoRecordListener): MediaController {

            val hostRecords = HostRecords()
            val videoRecord = hostRecords.createRecord()
            val audioRecord = hostRecords.createRecord()

            val surfaceHolder = SurfaceHolder()
            val audioRecordHolder = AudioRecordHolder()

            val filesDir = context.filesDir
            val videoProducer = VideoProducerCamera2Stream(context, surfaceHolder)
            val cameraId = videoProducer.cameraId
            val camcorderProfile = CamcorderProfile.get(cameraId.toInt(), CamcorderProfile.QUALITY_720P)
            val videoSetting = VideoMediaCodecConsumer.Setting(
                width = camcorderProfile.videoFrameWidth,
                height = camcorderProfile.videoFrameHeight,
                colorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface,
                videoBitrate = camcorderProfile.videoBitRate,
                videoFramePerSecond = camcorderProfile.videoFrameRate,
                iframeInterval = 0,
                rotation = 90
            )

            val videoConsumer = SurfaceMediaCodecConsumer(videoRecord, videoSetting, surfaceHolder)

            val audioConsumer = AudioConsumer(DefaultAudioSetting(camcorderProfile), audioRecord, audioRecordHolder)
            val audioProducer = AudioProducer(audioRecordHolder)

            val consumers = CompositeConsumer()
//            consumers.add(videoConsumer)
            consumers.add(audioConsumer)

            val producers = CompositeProducer()
//            producers.add(videoProducer)
            producers.add(audioProducer)

            val mediaFileController = MediaFileController(filesDir, hostRecords)
            mediaFileController.onVideoRecordListener = onVideoRecordListener

            return create(consumers, producers, mediaFileController)
        }
    }

    override fun log(message: String) {
        super.log("MediaController. $message")
    }

    init {

        producer.producerListener = object: Producer.ProducerListener {
            override fun onStartProduce() {
                log("Producer started")
                mediaFileController.start()
            }

            override fun onStopProduce() {
                log("Producer stopped")
                consumer.stopConsume()
            }
        }

        consumer.consumerListener = object: Consumer.ConsumerListener {
            override fun onStartConsume() {
                log("Consumer started")
                producer.startProduce()
            }

            override fun onStopConsume() {
                log("Consumer stopped")
                controllerListener?.onStop()
            }
        }

        mediaFileController.onMediaFileControllerListener = object: MediaFileController.MediaFileControllerListener {
            override fun onStart() {
                log("Media file controller started")
                controllerListener?.onStart()
            }

            override fun onStop() {
                log("Media file controller stopped")
                producer.stopProduce()
            }
        }
    }

    override fun start() {
        log("Start controller")
        consumer.startConsume()
    }

    override fun stop() {
        log("Stop controller")
        mediaFileController.stop()
    }
}