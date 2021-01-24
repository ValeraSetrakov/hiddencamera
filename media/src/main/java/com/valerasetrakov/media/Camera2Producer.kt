package com.valerasetrakov.media

import android.content.Context
import android.hardware.camera2.*
import android.os.Handler
import android.view.Surface
import androidx.annotation.RequiresPermission
import com.example.commonandroid.log
import timber.log.Timber

class Camera2Producer(context: Context) {

    companion object {
        fun logd(message: String) {
            Timber.d("Camera2Producer. $message")
        }
    }


    private var distance: Surface? = null
    var cameraDevice: CameraDevice? = null
    var cameraManager: CameraManager
    var cameraId: String
    lateinit var captureRequestBuilder: CaptureRequest.Builder
    var captureSession: CameraCaptureSession? = null
    var handler: Handler? = null

    var cameraListener: CameraListener? = null

    init {
        logd("Create VideoProducerCamera2")
        cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraIds = cameraManager.cameraIdList
        cameraId = if (cameraIds.size > 2)
            cameraIds.last()
        else
            cameraIds.first()
    }

    /**
     * Open camera, create video request, create session for video
     */
    @RequiresPermission(android.Manifest.permission.CAMERA)
    fun openCamera () {
        logd("Open camera")
        cameraManager.openCamera(cameraId, object: CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                logd("Camera opened")
                cameraDevice = camera
                captureRequestBuilder = createCaptureRequestBuilder(cameraDevice!!)
                createCaptureSession(cameraDevice!!)
                cameraListener?.onOpen()
            }

            override fun onDisconnected(camera: CameraDevice) {
                logd("Camera disconnected")
                camera.close()
            }

            override fun onError(camera: CameraDevice, error: Int) {
                logd("Camera error $error")
                camera.close()
            }

            override fun onClosed(camera: CameraDevice) {
                logd("Camera closed")
                this@Camera2Producer.cameraDevice = null
                cameraListener?.onClose()
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
                logd("Capture session configured")
                captureSession = session
                onConfiguredCaptureSession(captureSession!!)
            }

            override fun onConfigureFailed(session: CameraCaptureSession) {
                logd("Capture session failed")
                session.close()
            }

            override fun onClosed(session: CameraCaptureSession) {
                super.onClosed(session)
                logd("Capture session closed")
                captureSession = null
                onClosedCaptureSession(session)
            }

            override fun onActive(session: CameraCaptureSession) {
                super.onActive(session)
                logd("Capture session active produce video")
                cameraListener?.onStart()
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
        logd("Setup request capture builder")
        distance?.let { captureRequestBuilder.addTarget(it) }
    }


    /**
     * Start repeating request and media recorder start
     */
    protected fun startCaptureSession(cameraCaptureSession: CameraCaptureSession) {
        logd("Start capture session")
        cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, handler)
    }

    /**
     * Stop repeating request
     */
    protected fun stopCaptureSession () {
        logd("Stop capture session")
        captureSession?.close()
    }

    /**
     * create builder for capture request.
     */
    protected open fun createCaptureRequestBuilder(cameraDevice: CameraDevice): CaptureRequest.Builder =
        cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD)
            .apply {
                logd("Create capture request builder")
                set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
            }

    @RequiresPermission(android.Manifest.permission.CAMERA)
    fun start(distance: Surface) {
        this.distance = distance
        openCamera()
    }

    fun stop() {
        cameraDevice?.close()
    }

    interface CameraListener {
        fun onClose()
        fun onOpen()
        fun onStart()
    }

}