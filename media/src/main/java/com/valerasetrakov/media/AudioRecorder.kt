package com.valerasetrakov.media

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaCodec
import android.media.MediaRecorder
import android.media.MediaFormat
import android.media.MediaCodecInfo
import com.example.common.FlagsUtil.isFlagSet
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

class AudioRecorder: Recorder() {

    companion object {
        fun logd(message: String) {
            Timber.d("AudioRecorder. $message")
        }
    }

    val MIME_TYPE_AUDIO = "audio/mp4a-latm"
    val SAMPLE_RATE = 44100
    val CHANNEL_COUNT = 1
    val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
    val BIT_RATE_AUDIO = 128000
    val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    val AUDIO_SOURCE = MediaRecorder.AudioSource.CAMCORDER

    init {
        createAudioRecord()
        createMediaFormat()
    }

    lateinit var audioRecord: AudioRecord
    lateinit var mediaCodec: MediaCodec
    lateinit var mediaFormat: MediaFormat

    var bufferSize = 0

    fun createAudioRecord() {
        val audioSource = AUDIO_SOURCE
        val sampleRate = SAMPLE_RATE
        val channel = CHANNEL_CONFIG
        val encoding = AUDIO_FORMAT
        val minBuffSize = AudioRecord.getMinBufferSize(sampleRate, channel, encoding)
        bufferSize = minBuffSize
        audioRecord = AudioRecord(audioSource, sampleRate, channel, encoding, bufferSize)
    }

    fun createMediaFormat() {
        val sampleRate = SAMPLE_RATE
        val channelCount = CHANNEL_COUNT
        val bitRate = BIT_RATE_AUDIO
        val mime = MIME_TYPE_AUDIO

        mediaFormat = MediaFormat.createAudioFormat(mime, sampleRate, channelCount)

        mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
        mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 16384)
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
    }

    fun createMediaCodec() {
        startPTS = 0
        totalSamplesNum = 0
        mediaCodec = MediaCodec.createEncoderByType(mediaFormat.getString(MediaFormat.KEY_MIME))
        mediaCodec.setCallback(object: MediaCodec.Callback() {
            override fun onOutputBufferAvailable(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
                val byteBuffer = codec.getOutputBuffer(index) ?: return

//                logd("Write audio data to file")

                if (isFlagSet(info.flags, MediaCodec.BUFFER_FLAG_CODEC_CONFIG)) {
//                    logd("Audio fragment is codec config")
                    codec.releaseOutputBuffer(index, false)
                    return
                }

                if (isFlagSet(info.flags, 0)) {
//                    logd("Audio fragment is audio data")
                    val audioData = Record.create(byteBuffer, info)
                    synchronized(records) {
                        records.add(audioData)
                    }
                    codec.releaseOutputBuffer(index, false)
                }

            }

            override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
                val byteBuffer = codec.getInputBuffer(index) ?: return
//                logd("Read audio data from mic")
                val size = audioRecord.read(byteBuffer, bufferSize)
                val offset = byteBuffer.position()
                var presentationTimeUs = System.nanoTime() / 1000L
                presentationTimeUs = getJitterFreePTS(presentationTimeUs, size/2L)
                codec.queueInputBuffer(index, offset, size, presentationTimeUs, 0)
            }

            override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
                logd("Change audio format")
                mediaFormat = format
            }

            override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {

            }
        })
        mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
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
        val bufferDuration = 1000000 * bufferSamplesNum / SAMPLE_RATE
        mutableBufferPts -= bufferDuration // accounts for the delay of acquiring the audio buffer
        if (totalSamplesNum == 0L) {
            // reset
            startPTS = mutableBufferPts
            totalSamplesNum = 0
        }
        correctedPts = startPTS + 1000000 * totalSamplesNum / SAMPLE_RATE
        if (mutableBufferPts - correctedPts >= 2 * bufferDuration) {
            // reset
            startPTS = mutableBufferPts
            totalSamplesNum = 0
            correctedPts = startPTS
        }
        totalSamplesNum += bufferSamplesNum
        return correctedPts
    }

    override fun start() {
        createMediaCodec()
        audioRecord.startRecording()
        mediaCodec.start()
    }

    override fun stop() {
        audioRecord.stop()
        mediaCodec.stop()
        mediaCodec.release()
    }
}