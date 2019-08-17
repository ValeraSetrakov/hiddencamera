package com.example.video.video.producer

import android.content.Context
import android.hardware.camera2.*
import android.os.Handler
import android.view.Surface
import androidx.annotation.RequiresPermission
import com.example.video.SurfaceHolder

/**
 * Video producer like camera2
 */
abstract class VideoProducerCamera2(context: Context, distanceHolder: SurfaceHolder): VideoProducer<Surface>(distanceHolder) {

    override fun log(message: String) {
        super.log("Camera. $message")
    }

    protected var cameraDevice: CameraDevice? = null
    var cameraManager: CameraManager
    var cameraId: String
    protected lateinit var captureRequestBuilder: CaptureRequest.Builder
    protected var captureSession: CameraCaptureSession? = null
    var handler: Handler? = null

    var cameraOpenListener: CameraOpenListener? = null
    var cameraCloseListener: CameraCloseListener? = null

    init {
        log("Create VideoProducerCamera2")
        cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraIds = cameraManager.cameraIdList
        cameraId = cameraIds[0]
    }

    /**
     * Open camera, create video request, create session for video
     */
    @RequiresPermission(android.Manifest.permission.CAMERA)
    fun openCamera () {
        log("Open camera")
        cameraManager.openCamera(cameraId, object: CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                log("Camera opened")
                cameraDevice = camera
                captureRequestBuilder = createCaptureRequestBuilder(cameraDevice!!)
                createCaptureSession(cameraDevice!!)
                cameraOpenListener?.onOpen()
            }

            override fun onDisconnected(camera: CameraDevice) {
                log("Camera disconnected")
                camera.close()
            }

            override fun onError(camera: CameraDevice, error: Int) {
                log("Camera error $error")
                camera.close()
            }

            override fun onClosed(camera: CameraDevice) {
                log("Camera closed")
                this@VideoProducerCamera2.cameraDevice = null
                cameraCloseListener?.onClose()
                producerListener?.onStopProduce()
            }
        }, handler)
    }

    /**
     * Create capture session and prepare media recorder
     */
    private fun createCaptureSession (cameraDevice: CameraDevice) {
        log("Camera create capture session")
        setupRequestCaptureBuilder()
        cameraDevice.createCaptureSession(listOf(distance), object : CameraCaptureSession.StateCallback() {

            override fun onConfigured(session: CameraCaptureSession) {
                log("Capture session configured")
                captureSession = session
                onConfiguredCaptureSession(captureSession!!)
            }

            override fun onConfigureFailed(session: CameraCaptureSession) {
                log("Capture session failed")
                session.close()
            }

            override fun onClosed(session: CameraCaptureSession) {
                super.onClosed(session)
                log("Capture session closed")
                captureSession = null
                onClosedCaptureSession(session)
            }

            override fun onActive(session: CameraCaptureSession) {
                super.onActive(session)
                log("Capture session active produce video")
                producerListener?.onStartProduce()
            }

        }, handler)
    }

    /**
     * Call after capture session become to configure state
     */
    open fun onConfiguredCaptureSession (session: CameraCaptureSession) {
        log("On configured capture session")
        startCaptureSession(session)
    }

    /**
     * Call after capture session become to close state
     */
    open fun onClosedCaptureSession(session: CameraCaptureSession) {
        log("On closed capture session")
    }


    /**
     * Setup request capture builder
     */
    private fun setupRequestCaptureBuilder () {
        log("Setup request capture builder")
        captureRequestBuilder.addTarget(distance)
    }


    /**
     * Start repeating request and media recorder start
     */
    protected fun startCaptureSession(cameraCaptureSession: CameraCaptureSession) {
        log("Start capture session")
        cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, handler)
    }

    /**
     * Stop repeating request
     */
    protected fun stopCaptureSession () {
        log("Stop capture session")
        captureSession?.close()
    }

    /**
     * create builder for capture request.
     */
    protected open fun createCaptureRequestBuilder(cameraDevice: CameraDevice): CaptureRequest.Builder =
        cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD)
            .apply {
                log("Create capture request builder")
                set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
            }







    interface CameraCloseListener {
        fun onClose()
    }

    interface CameraOpenListener {
        fun onOpen()
    }
}