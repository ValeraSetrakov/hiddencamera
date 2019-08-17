package com.valerasetrakov.media

import android.content.Context
import android.media.MediaMuxer
import android.os.Handler
import android.os.HandlerThread
import androidx.annotation.RequiresPermission
import com.crashlytics.android.Crashlytics
import com.example.commonandroid.CrashlyticsManager
import timber.log.Timber
import java.io.File
import java.util.*

class MediaController private constructor(context: Context): VideoRecorder.VideoRecorderListener {

    companion object {
        
        const val DELAY = 5_000L
        const val PERIOD = 5_000L
        private val workerThread = HandlerThread("MediaController thread").also { it.start() }
        private val handler = Handler(workerThread.looper)

        private var instance: MediaController? = null

        @RequiresPermission(allOf = [android.Manifest.permission.CAMERA, android.Manifest.permission.RECORD_AUDIO])
        fun start(context: Context, onSaveMediaDataListener: OnSaveMediaDataListener, mediaControllerListener: MediaControllerListener) {
            handler.post{
                instance = MediaController(context).also {
                    it.onSaveMediaDataListener = onSaveMediaDataListener
                    it.mediaControllerListener = mediaControllerListener
                }
                instance!!.start()
            }
        }

        fun stop() {
            handler.post {
                instance!!.stop()
            }
        }
    }

    fun logd(message: String) {
        Timber.d("MediaController. $message")
    }

    private var filesDir: File
    init {
        val mediaFileDirs = context.externalMediaDirs
        filesDir = mediaFileDirs?.let {
            val firstMediaFilesDir = mediaFileDirs[0]
            if (firstMediaFilesDir == null || !firstMediaFilesDir.exists()) {
                context.filesDir
            } else {
                firstMediaFilesDir
            }
        } ?: context.filesDir// context.filesDir
    }
    private var audioRecorder: AudioRecorder = AudioRecorder()
    private var videoRecorder :VideoRecorder = VideoRecorder(context).apply { videoRecorderListener = this@MediaController }
    var onSaveMediaDataListener: OnSaveMediaDataListener? = null
    var mediaControllerListener: MediaControllerListener? = null

    private val timer = Timer()
    private lateinit var timerTask: SaveMediaDataToFileTask
    @Volatile private var taskType: TaskType = TaskType.Start

    @RequiresPermission(allOf = [android.Manifest.permission.CAMERA, android.Manifest.permission.RECORD_AUDIO])
    fun start() {
        logd("Start media recorder")
        CrashlyticsManager.log("Start media record")
        videoRecorder.start()
    }

    fun stop() {
        logd("Stop media recorder")
        CrashlyticsManager.log("Stop media record")
        stopTask()
    }

    private fun startTask () {
        logd("Start task")
        taskType = TaskType.Start
        timerTask = SaveMediaDataToFileTask()
        timer.schedule(timerTask, DELAY, PERIOD)
    }

    private fun stopTask () {
        logd("Stop task")
        taskType = if (taskType == TaskType.Start)
            TaskType.StartEnd
        else
            TaskType.End
    }

    override fun onStartVideoRecorder() {
        logd("On video recorder started")
        audioRecorder.start()
        startTask()
        mediaControllerListener?.onStartMediaController()
    }

    override fun onStopVideoRecorder() {
        logd("On video recorder stopped")
        audioRecorder.stop()
        mediaControllerListener?.onStopMediaController()
    }



    interface MediaControllerListener {
        fun onStartMediaController()
        fun onStopMediaController()
    }

    private inner class SaveMediaDataToFileTask: TimerTask() {

        override fun run() {

            logd("Start doing task")
            logd("Task type $taskType")
            logd("Start file recording")
            val mediaFile = MediaUtil.getVideoFilePath(filesDir = filesDir)
            val muxer = MediaMuxer(mediaFile, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            muxer.setOrientationHint(90)
            val audioTrack = muxer.addTrack(audioRecorder.mediaFormat)
            val videoTrack = muxer.addTrack(videoRecorder.mediaFormat)
            val audioData = audioRecorder.read()
            val videoData = videoRecorder.read()
            muxer.start()

            videoData.forEach {
//                logd("Video record time stamp ${it.timestamp}")
                val (byteBuffer, info) = it.revert()
                muxer.writeSampleData(videoTrack, byteBuffer, info)
            }

            audioData.forEach {
//                logd("Audio record time stamp ${it.timestamp}")
                val (byteBuffer, info) = it.revert()
                muxer.writeSampleData(audioTrack, byteBuffer, info)
            }

            muxer.stop()
            muxer.release()
            logd("End file recording")

            when(taskType) {
                TaskType.Start -> {
                    onSaveMediaDataListener?.onRecordVideoStart(mediaFile)
                    taskType = TaskType.Regular
                }
                TaskType.Regular ->
                    onSaveMediaDataListener?.onRecordVideo(mediaFile)
                TaskType.End, TaskType.StartEnd -> {
                    timerTask.cancel()
                    timer.purge()
                    when(taskType) {
                        TaskType.End ->
                            onSaveMediaDataListener?.onRecordVideoEnd(mediaFile)
                        TaskType.StartEnd ->
                            onSaveMediaDataListener?.onRecordVideoStartEnd(mediaFile)
                    }
                    videoRecorder.stop()
                }

            }

            logd("End doing task")
        }

    }

    private enum class TaskType {
        Start, Regular, End, StartEnd
    }

    interface OnSaveMediaDataListener {
        fun onRecordVideo(file: String)
        fun onRecordVideoStart(file: String)
        fun onRecordVideoEnd(file: String)
        fun onRecordVideoStartEnd(file: String)
    }
}