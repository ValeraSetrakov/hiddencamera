package com.example.video

import android.media.MediaMuxer
import com.example.video.record.HostRecords
import java.io.File
import java.util.*

class MediaFileController(private val fileDirs: File, private val hostRecords: HostRecords) {

    companion object {
        const val DELAY: Long = 5_000
        const val PERIOD: Long = 5_000

        private fun log(message: String) {
            videoModulePrintThreadName("MediaFileController. $message")
        }
    }


    private val worker = Timer()
    @Volatile private lateinit var state: State
    var onVideoRecordListener: OnVideoRecordListener? = null
    var onMediaFileControllerListener: MediaFileController.MediaFileControllerListener? = null

    /**
     * Simple work for saving file
     */
    private lateinit var work: SaveTask

    /**
     * Saving data from record to file by path that pass like argument to this method
     */
    private fun save(videoFile: String) {

        val records = hostRecords.records
        val recordsWithPayload = records.filter { !it.isEmpty }
        if (recordsWithPayload.isEmpty()) {
            log("List of records is empty")
            return
        }


        log("Save file $videoFile")

        var muxer: MediaMuxer? = null
        try {
            muxer = MediaMuxer(videoFile, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            muxer.setOrientationHint(90)
            val tracks = mutableListOf<Int>()
            recordsWithPayload.forEach {
                tracks.add(muxer.addTrack(it.format!!))
            }
            muxer.start()
            for (i in 0 until tracks.size) {
                val track = tracks[i]
                val record = recordsWithPayload[i]
                record.save(muxer, track)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        } finally {
            muxer?.run {
                stop()
                release()
            }
        }

    }

    /**
     * Start work for saving data from record
     */
    fun start() {
        log("Start")
        work = SaveTask()
        state = State.STARTING
        hostRecords.flush()
        worker.schedule(work, DELAY, PERIOD)
    }

    /**
     * End work for saving data from record
     */
    fun stop () {
        log("Stop")
        state = if (state == State.STARTING) {
            State.START_END
        } else
            State.ENDING
    }

    enum class State {
        STARTING, WORKING, ENDING, START_END, END
    }

    interface MediaFileControllerListener {
        fun onStart()
        fun onStop()
    }


    inner class SaveTask: TimerTask() {
        override fun run() {
            log("Work for saving data from records")
            val videoFile: String = VideoUtil.getVideoFilePath(fileDirs)
            save(videoFile)
            log("State = $state")
            when(state) {
                State.STARTING -> {
                    onMediaFileControllerListener?.onStart()
                    onVideoRecordListener?.onRecordStartVideo(videoFile)
                    state = State.WORKING
                }
                State.WORKING ->
                    onVideoRecordListener?.onRecordVideo(videoFile)
                State.ENDING, State.START_END -> {
                    cancel()
                    worker.purge()
                    onVideoRecordListener?.run {
                        when(state) {
                            State.ENDING -> {
                                onRecordEndVideo(videoFile)
                            }
                            State.START_END -> {
                                onRecordStartEndVideo(videoFile)
                            }
                        }
                    }
                    state = State.END
                    onMediaFileControllerListener?.onStop()
                }
            }
        }
    }
}