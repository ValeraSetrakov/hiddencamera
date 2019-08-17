package com.example.video.video.producer

import android.content.Context
import android.hardware.camera2.*
import android.media.MediaRecorder
import android.os.Handler
import android.util.Size
import android.view.Surface
import androidx.annotation.RequiresPermission
import com.example.video.OnVideoRecordListener
import com.example.video.VideoUtil
import com.example.video.defaultSetup
import com.example.video.videoModulePrintThreadName
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean



//abstract class VideoProducerCamera2NotStream(context: Context): VideoProducer() {
//
//    protected val availableSurfaces = mutableListOf<Surface>()
//    protected var cameraDevice: CameraDevice? = null
//    protected var mediaRecorder: MediaRecorder? = null
//    protected var cameraManager: CameraManager
//    protected var cameraId: String
//    protected var videoSize: Size = Size(0, 0)
//    protected var filesDir: File
//    protected lateinit var videoFilePath: String
//    protected lateinit var captureRequestBuilder: CaptureRequest.Builder
//    protected var captureSession: CameraCaptureSession? = null
//    protected lateinit var currentSurface: Surface
//    protected val isNeedRestartCaptureSession = AtomicBoolean(true)
//    protected val isFirstVideoFragment = AtomicBoolean(true)
//    var onRecordVideoListener: OnVideoRecordListener? = null
//    var handler: Handler? = null
//
//
//
//
//    init {
//        videoModulePrintThreadName("prepare")
//        cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
//        filesDir = context.filesDir
//        val cameraIds = cameraManager.cameraIdList
//        cameraId = cameraIds[0]
//        videoSize = VideoUtil.getVideoSize(cameraManager, cameraId)
//    }
//
//    /**
//     * Start record. Open camera, create media controller.
//     */
//    @RequiresPermission(android.Manifest.permission.CAMERA)
//    override fun startProduce() {
//        videoModulePrintThreadName("startProduce")
//        isNeedRestartCaptureSession.set(true)
//        isFirstVideoFragment.set(true)
//        cameraManager.openCamera(cameraId, object: CameraDevice.StateCallback() {
//            override fun onOpened(camera: CameraDevice) {
//                videoModulePrintThreadName("onOpenCamera")
//                cameraDevice = camera
//                captureRequestBuilder = createCaptureRequestBuilder(cameraDevice!!)
//                createCaptureSession(cameraDevice!!)
//            }
//
//            override fun onDisconnected(camera: CameraDevice) {
//                videoModulePrintThreadName("onDisconnectedCamera")
//                camera.close()
//            }
//
//            override fun onError(camera: CameraDevice, error: Int) {
//                videoModulePrintThreadName("onErrorCamera error $error")
//                camera.close()
//            }
//
//            override fun onClosed(camera: CameraDevice) {
//                videoModulePrintThreadName("onClosedCamera")
//                this@VideoProducerCamera2NotStream.cameraDevice = null
//                if (isFirstVideoFragment()) {
//                    onRecordVideoListener?.onRecordStartEndVideo(videoFilePath)
//                } else {
//                    onRecordVideoListener?.onRecordEndVideo(videoFilePath)
//                }
//            }
//        }, handler)
//    }
//
//    /**
//     * Stop record. Release media record, close camera device.
//     */
//    @RequiresPermission(android.Manifest.permission.CAMERA)
//    override fun stopProduce() {
//        videoModulePrintThreadName("stopProduce")
//        isNeedRestartCaptureSession.set(false)
//    }
//
//    /**
//     * Setup request capture builder and media recorder
//     */
//    private fun setupRequestCaptureBuilderAndMediaRecorder () {
//        videoModulePrintThreadName("setupRequestCaptureBuilderAndMediaRecorder")
//        if (this::currentSurface.isInitialized) {
//            captureRequestBuilder.removeTarget(currentSurface)
//            availableSurfaces.remove(currentSurface)
//        }
//        mediaRecorder = MediaRecorder().apply {
//            setupMediaRecorder(this)
//            prepare()
//        }
//        currentSurface = mediaRecorder?.surface!!
//        availableSurfaces.add(currentSurface)
//        captureRequestBuilder.addTarget(currentSurface)
//    }
//
//    /**
//     * Create capture session and prepare media recorder
//     */
//    private fun createCaptureSession (cameraDevice: CameraDevice) {
//        videoModulePrintThreadName("createCaptureSession")
//        setupRequestCaptureBuilderAndMediaRecorder()
//        cameraDevice.createCaptureSession(availableSurfaces, object : CameraCaptureSession.StateCallback() {
//
//            override fun onConfigured(session: CameraCaptureSession) {
//                videoModulePrintThreadName("onConfiguredSession")
//                captureSession = session
//                startCaptureSession(captureSession!!)
//                startMediaRecorder(mediaRecorder!!)
//                restartCaptureSession()
//            }
//
//            override fun onConfigureFailed(session: CameraCaptureSession) {
//                videoModulePrintThreadName("onConfiguredSessionFailed")
//            }
//
//            override fun onClosed(session: CameraCaptureSession) {
//                super.onClosed(session)
//                videoModulePrintThreadName("onCloseSession")
//                stopMediaRecorder(mediaRecorder!!)
//                mediaRecorder = null
//                if(isNeedRestartCaptureSession()) {
//                    if (isFirstVideoFragment()) {
//                        onRecordVideoListener?.onRecordStartVideo(videoFilePath)
//                        isFirstVideoFragment.set(false)
//                    } else {
//                        onRecordVideoListener?.onRecordVideo(videoFilePath)
//                    }
//                    createCaptureSession(cameraDevice)
//                } else {
//                    cameraDevice.close()
//                }
//            }
//
//        }, handler)
//    }
//
//    /**
//     * Start repeating request and media recorder start
//     */
//    private fun startCaptureSession(cameraCaptureSession: CameraCaptureSession) {
//        videoModulePrintThreadName("startCaptureSession")
//        cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, handler)
//    }
//
//    /**
//     * Restart capture session.
//     */
//    protected open fun restartCaptureSession() {
//        videoModulePrintThreadName("restartCaptureSession")
//        stopCaptureSession()
//    }
//
//    /**
//     * Stop repeating request
//     */
//    private fun stopCaptureSession () {
//        videoModulePrintThreadName("stopCaptureSession")
//        captureSession?.close()
//        captureSession = null
//    }
//
//
//
//
//
//
//
//
//
//
//
//
//
//    /**
//     * Start media recorder for record video and audio.
//     */
//    protected open fun startMediaRecorder (mediaRecorder: MediaRecorder) = mediaRecorder.run {
//        videoModulePrintThreadName("startMediaRecorder")
//        start()
//    }
//
//    /**
//     * Stop media recorder.
//     */
//    protected open fun stopMediaRecorder (mediaRecorder: MediaRecorder) = mediaRecorder.run {
//        videoModulePrintThreadName("stopMediaRecorder")
//        stop()
//        reset()
//        release()
//    }
//
//    /**
//     * Prepare media recorder.
//     */
//    protected open fun setupMediaRecorder(mediaRecorder: MediaRecorder) = mediaRecorder.apply {
//        videoModulePrintThreadName("setupMediaRecorder")
//        videoFilePath = VideoUtil.getVideoFilePath(filesDir)
//        defaultSetup(videoFilePath, 3000, videoSize)
//    }
//
//    /**
//     * create builder for capture request.
//     */
//    protected open fun createCaptureRequestBuilder(cameraDevice: CameraDevice): CaptureRequest.Builder =
//        cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD)
//            .apply {
//                videoModulePrintThreadName("createCaptureRequestBuilder")
//                set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
//            }
//
//    /**
//     * Determine would be restart video record session
//     */
//    protected open fun isNeedRestartCaptureSession() =
//        isNeedRestartCaptureSession.get()
//
//    /**
//     * Determine would be restart video record session
//     */
//    protected open fun isFirstVideoFragment() =
//        isFirstVideoFragment.get()
//
//
//
//
//
//
//
//
//
//
//}