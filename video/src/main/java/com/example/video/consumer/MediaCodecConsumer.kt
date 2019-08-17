package com.example.video.consumer

import android.media.MediaCodec
import android.media.MediaFormat
import android.view.Surface
import com.example.common.FlagsUtil
import com.example.video.record.MediaCodecRecord
import java.nio.ByteBuffer


abstract class MediaCodecConsumer<SOURCE>(private val mediaCodecRecord: MediaCodecRecord, sourceHolder: SourceHolder<SOURCE>)
    : ConsumerWithSource<SOURCE>(sourceHolder) {

    override fun log (message: String) {
        super.log("MediaCodecConsumer. $message")
    }

    fun logBuffer(info: MediaCodec.BufferInfo) {
        log("Write data to buffer." +
        "\nOffset = ${info.offset}," +
        "\nsize = ${info.size}," +
        "\npresentationTimeUs = ${info.presentationTimeUs}," +
        "\nflags = ${info.flags}")
    }

    protected lateinit var codec: MediaCodec
    protected lateinit var format: MediaFormat

    abstract fun prepareMediaFormat (): MediaFormat
    open fun handleInputBuffer(codec: MediaCodec, index: Int) {
        //nothing
    }
    open fun handleOutputBuffer(byteBuffer: ByteBuffer, info: MediaCodec.BufferInfo) {
        //nothing
    }


    fun createMediaCodec(format: MediaFormat): MediaCodec =
        MediaCodec.createEncoderByType(format.getString(MediaFormat.KEY_MIME))

    override fun prepare() {
        log("Prepare consumer")
        prepareMediaCodec()
        val source = createSource()
        sourceHolder.source = source
    }

    protected fun prepareMediaCodec (): MediaCodec {

        format = prepareMediaFormat()
        codec = createMediaCodec(format).apply {

            setCallback(object: MediaCodec.Callback() {
                override fun onOutputBufferAvailable(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
                    val byteBuffer = codec.getOutputBuffer(index) ?: return
                    logBuffer(info)

                    if (FlagsUtil.isFlagSet(info.flags, MediaCodec.BUFFER_FLAG_CODEC_CONFIG)) {
                        log("Buffer codec config")
                        codec.releaseOutputBuffer(index, false)
                        return
                    }

                    if (FlagsUtil.isFlagSet(info.flags, MediaCodec.BUFFER_FLAG_END_OF_STREAM)) {
                        log("Buffer end of stream")
                        stop()
                        release()
                        consumerListener?.onStopConsume()
                        return
                    }

                    if (FlagsUtil.isFlagSet(info.flags, 0/*MediaCodec.BUFFER_FLAG_KEY_FRAME*/)) {
                        log("Buffer key frame")
                        handleOutputBuffer(byteBuffer, info)
                        mediaCodecRecord.addFragment(byteBuffer, info)
                        codec.releaseOutputBuffer(index, false)
                    }


                }

                override fun onOutputFormatChanged(codec: MediaCodec, mediaFormat: MediaFormat) {
                    log("On output format change")
                    format = mediaFormat
                    mediaCodecRecord.flush()
                    mediaCodecRecord.format = format
                }

                override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
                    log("On input buffer available")
                    handleInputBuffer(codec, index)
                }

                override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) { log("On error $e") }
            })

            configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        }
        return codec
    }

    override fun startConsume() {
        prepare()
        codec.start()
        consumerListener?.onStartConsume()
    }
}