package com.example.video.audio

import android.media.*
import com.example.video.consumer.MediaCodecConsumer
import com.example.video.consumer.SourceHolder
import com.example.video.record.MediaCodecRecord
import java.util.concurrent.atomic.AtomicBoolean

class AudioConsumer(private val setting: Setting, mediaCodecRecord: MediaCodecRecord, source: SourceHolder<AudioRecord>)
    : MediaCodecConsumer<AudioRecord>(mediaCodecRecord, source) {

    override fun log(message: String) {
        super.log("AudioConsumer. $message")
    }

    lateinit var audioRecord: AudioRecord
    var bufferSize: Int = 0

    private val isEnding = AtomicBoolean(false)

    override fun handleInputBuffer(codec: MediaCodec, index: Int) {
        val byteBuffer = codec.getInputBuffer(index) ?: return
        val size = audioRecord.read(byteBuffer, bufferSize)
        val offset = byteBuffer.position()
        var presentationTimeUs = System.nanoTime() / 1000L
//        presentationTimeUs = getJitterFreePTS(presentationTimeUs, size/2L)
        var flags = if(isEnding.get())
            MediaCodec.BUFFER_FLAG_END_OF_STREAM
        else
            MediaCodec.BUFFER_FLAG_KEY_FRAME

        log("Read audio data from audio record." +
                "\nOffset = $offset," +
                "\nsize = $size," +
                "\npresentationTimeUs = $presentationTimeUs," +
                "\nflags = $flags")
        codec.queueInputBuffer(index, offset, size, presentationTimeUs, flags)
    }

    override fun prepareMediaFormat(): MediaFormat {
        val sampleRate = setting.sampleRate
        val channelCount = setting.channelCount
        val bitRate = setting.bitRate
        val mime = setting.mime

        val format: MediaFormat = MediaFormat.createAudioFormat(mime, sampleRate, channelCount)

        format.setInteger(MediaFormat.KEY_AAC_PROFILE, setting.profile)
        format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, setting.maxInputSize)
        format.setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
        return format
    }

    override fun createSource(): AudioRecord {
        log("Prepare audio consumer")
        val audioSource = setting.audioSource
        val sampleRate = setting.sampleRate
        val channelCount = setting.channelCount
        val channel = setting.channelType
        val encoding = setting.encodingType
        val minBuffSize = AudioRecord.getMinBufferSize(sampleRate, channel, encoding)
        bufferSize = minBuffSize * channelCount
        audioRecord = AudioRecord(audioSource, sampleRate, channel, encoding, bufferSize)
        return audioRecord
    }

    override fun startConsume() {
        log("Start audio consumer")
        isEnding.set(false)
        super.startConsume()
    }

    override fun stopConsume() {
        log("Stop audio consumer")
        isEnding.set(true)
    }


    var startPTS: Long = 0
    var totalSamplesNum: Long = 0

    /**
     * Ensures that each audio pts differs by a constant amount from the previous one.
     * @param bufferPts presentation timestamp in us
     * @param bufferSamplesNum the number of samples of the buffer's frame
     * @return
     */
    private fun getJitterFreePTS(bufferPts: Long, bufferSamplesNum: Long): Long {
        var mutableBufferPts = bufferPts
        var correctedPts: Long = 0
        val bufferDuration = 1000000 * bufferSamplesNum / setting.sampleRate
        mutableBufferPts -= bufferDuration // accounts for the delay of acquiring the audio buffer
        if (totalSamplesNum == 0L) {
            // reset
            startPTS = mutableBufferPts
            totalSamplesNum = 0
        }
        correctedPts = startPTS + 1000000 * totalSamplesNum / setting.sampleRate
        if (mutableBufferPts - correctedPts >= 2 * bufferDuration) {
            // reset
            startPTS = mutableBufferPts
            totalSamplesNum = 0
            correctedPts = startPTS
        }
        totalSamplesNum += bufferSamplesNum
        return correctedPts
    }

    open class Setting (
        val sampleRate: Int,
        val channelCount: Int,
        val bitRate: Int,
        val mime: String,
        val profile: Int,
        val maxInputSize: Int,
        val audioSource: Int,
        val channelType: Int,
        val encodingType: Int
    )
}