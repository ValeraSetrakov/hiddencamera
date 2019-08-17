package com.valerasetrakov.hiddencamerarecorder

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.crashlytics.android.Crashlytics
import com.example.commonandroid.CrashlyticsManager
import com.example.commonandroid.log
import com.example.commonandroid.threadSave
import com.valerasetrakov.data.MobileDevice
import com.valerasetrakov.hiddencamerarecorder.databinding.FragmentCameraBinding
import com.valerasetrakov.media.AudioPlayer
import com.valerasetrakov.media.MediaController
import retrofit2.Response
import java.util.concurrent.atomic.AtomicBoolean

@SuppressLint("MissingPermission")
class CameraFragment: Fragment(), MediaController.MediaControllerListener {

    companion object {
        val TAG = CameraFragment::class.java.simpleName
        fun create(): CameraFragment {
            return CameraFragment()
        }
        private fun print(message: String) {
            log("Camera Fragment. $message")
        }
    }

    private lateinit var binding: FragmentCameraBinding
    private val registerObserver = RegistrationLiveData()
    private val updateBoxInfoObserver = UpdateBoxInfoLiveData()
    private lateinit var locationManager: LocationManager
    /**
     * If media controller is ready to start of stop
     */
    private val isMediaControllerReady = AtomicBoolean(true)
    private val isMediaControllerStarted = AtomicBoolean(false)

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationManager = LocationManager(activity!!)
    }

    /** all start buttons, like audio, location, video */
    private val additionalButtons = mutableListOf<Button>()
    /** all buttons but retry */
    private val successButtons = mutableListOf<Button>()
    /** all buttons */
    private val buttons = mutableListOf<Button>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_camera, container, false)
        additionalButtons.apply {
            add(binding.buttonRememberLocation)
            add(binding.buttonStartAudio)
        }
        successButtons.apply {
            addAll(additionalButtons)
            add(binding.buttonStopRecordVideo)
            add(binding.buttonStartRecordVideo)
        }
        buttons.apply {
            addAll(successButtons)
            add(binding.buttonRetry)
        }

        binding.applicationId.text = "Device id ${App.deviceId}"
        registerObserver.observe(viewLifecycleOwner, Observer {
            when(it.status) {
                RegistrationLiveData.Status.START -> {
                    CrashlyticsManager.log("Start registration")
                    showProgress()
                }
                RegistrationLiveData.Status.END -> {
                    CrashlyticsManager.log("End registration, ${it.mobileDevice?.phoneId}")
                    it?.mobileDevice?.run {
                        updateInfo(this)
                    }
                    binding.errorMessage.visibility = View.GONE
                    showStartRecordButton()
                }
                RegistrationLiveData.Status.ERROR -> {
                    binding.errorMessage.apply {
                        text = "Error ${it?.throwable?.message}"
                        visibility = View.VISIBLE
                    }

                    if (it?.throwable != null)
                        CrashlyticsManager.logException(it.throwable)
                    showRetryButton()
                }
            }

        })

        updateBoxInfoObserver.observe(viewLifecycleOwner, Observer {
            when(it.status) {
                UpdateBoxInfoLiveData.Status.START -> {
                    showProgress()
                }
                UpdateBoxInfoLiveData.Status.END -> {
                    it?.mobileDevice?.run {
                        updateInfo(this)
                    }
                    binding.errorMessage.visibility = View.GONE
                    showStartRecordButton()
                    startLocationService()

                }
                UpdateBoxInfoLiveData.Status.ERROR -> {
                    binding.errorMessage.apply {
                        text = "Error ${it?.throwable?.message}"
                        visibility = View.VISIBLE
                    }
                    showStartRecordButton()
                }
            }

        })

        App.startPosition.observe(viewLifecycleOwner, Observer {
            it?.run {
                binding.textViewStartPosition.text = "Start position $it"
            }
        })

        App.currnetPosition.observe(viewLifecycleOwner, Observer {
            it?.run {
                binding.textViewCurrentPosition.text = "Current position $it"
            }
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonStartRecordVideo.setOnClickListener(::onStartRecordClick)
        binding.buttonStopRecordVideo.setOnClickListener(::onStopRecordClick)
        binding.buttonRetry.setOnClickListener(::onRetryClick)
        binding.buttonRememberLocation.setOnClickListener(::onRememberLocationClick)
        binding.buttonStartAudio.setOnClickListener (::onStartAudioClick)
    }

    fun onStartAudioClick(view: View) =
            startAudio()

    fun startAudio() {
//        Crashlytics.getInstance().crash()
        if (!PermissionsController.requestMediaPermissions(this)) {
            try {
                AudioPlayer.startPlay(App.boxInfoController.audioFilePath)
            } catch (e: Exception) {
                App.toastManager.show(e.message ?: "Some error occurred")
            }
        }
    }

    private fun updateInfo(mobileDevice: MobileDevice) {
        binding.applicationId.text = "Device id ${mobileDevice?.phoneId}"
        binding.textViewSiren.text = "Siren url ${mobileDevice?.siren}"
        binding.textViewPhones.text = "Phones ${mobileDevice.users?.joinToString { it.phoneNumber }}"
        binding.textViewEmails.text = "Emails ${mobileDevice.users?.joinToString { it.email }}"
    }

    private fun startLocationService() {
        print("startLocationService")
        if (!PermissionsController.requestSmsAndLocationPermissions(this)) {
            val intent = Intent(activity!!, LocationService::class.java)
            ContextCompat.startForegroundService(activity!!, intent)
        }
    }

    private fun requestNeedPermissions() =
        PermissionsController.requestMediaPermissions(this)

    private fun onStartRecordClick(view: View) {
        startRecord()
    }

    private fun onRememberLocationClick(view: View) {
        updateBoxInfoObserver.update()
    }

    private fun onRetryClick(view: View) {
        registerObserver.register()
    }

    override fun onStartMediaController() {
        activity!!.runOnUiThread {
            showStopRecordButton()
            isMediaControllerReady.set(true)
        }
    }

    override fun onStopMediaController() {
        activity!!.runOnUiThread {
            showStartRecordButton()
            isMediaControllerReady.set(true)
        }
    }

    @RequiresPermission(android.Manifest.permission.CAMERA)
    fun startRecord() {
        if (!requestNeedPermissions()) {
            if (isMediaControllerReady.get() && !isMediaControllerStarted.get()) {
                print("Start record")
                isMediaControllerReady.set(false)
                isMediaControllerStarted.set(true)
                showProgress()
                MediaController.start(activity!!, OnVideoRecordListenerImpl(), this@CameraFragment)
            }
        }
    }

    fun onStopRecordClick(view: View) {
        stopRecord()
    }

    fun stopRecord() {
        if (isMediaControllerReady.get() && isMediaControllerStarted.get()) {
            print("Stop record")
            isMediaControllerReady.set(false)
            isMediaControllerStarted.set(false)
            showProgress()
            MediaController.stop()
        }
    }

    fun toggleRecord() {
        if (isMediaControllerReady.get() && isMediaControllerStarted.get()) {
            stopRecord()
        } else if (isMediaControllerReady.get() && !isMediaControllerStarted.get()) {
            startRecord()
        }
    }

    private fun showStartRecordButton () {
        additionalButtons.forEach { it.visibility = View.VISIBLE }
        binding.buttonStartRecordVideo.visibility = View.VISIBLE
        binding.buttonStopRecordVideo.visibility = View.INVISIBLE
        binding.buttonRetry.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
    }

    private fun showStopRecordButton () {
        additionalButtons.forEach { it.visibility = View.VISIBLE }
        binding.buttonStartRecordVideo.visibility = View.INVISIBLE
        binding.buttonStopRecordVideo.visibility = View.VISIBLE
        binding.buttonRetry.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
    }

    private fun showRetryButton () {
        successButtons.forEach { it.visibility = View.INVISIBLE }
        binding.buttonRetry.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
    }

    private fun showProgress () {
        buttons.forEach { it.visibility = View.INVISIBLE }
        binding.progressBar.visibility = View.VISIBLE
    }

    fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val lastKeyEvent = getString(R.string.last_event_key, keyCode)
        binding.textViewLastEventKey.text = lastKeyEvent
        return when(keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_A -> {
                startRecord()
                true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_S -> {
                stopRecord()
                true
            }
            KeyEvent.KEYCODE_MEDIA_PLAY, KeyEvent.KEYCODE_F -> {
                startAudio()
                true
            }
            KeyEvent.KEYCODE_D -> {
                startLocationService()
                true
            }
            else ->
                false
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        print("onRequestPermissionsResult requestCode $requestCode")
        when(requestCode) {
//            REQUEST_MEDIA_PERMISSIONS_CODE ->
//                startRecord()
            REQUEST_SEND_SMS_AND_LOCATION_PERMISSION_CODE ->
                startLocationService()
            else ->
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }


}


class RegistrationLiveData: MutableLiveData<RegistrationLiveData.RegisterResponse>() {

    data class RegisterResponse (
        val mobileDevice: MobileDevice? = null,
        val throwable: Throwable? = null,
        val status: Status
    )

    enum class Status {
        START, END, ERROR
    }

    override fun observe(owner: LifecycleOwner, observer: Observer<in RegisterResponse>) {
        super.observe(owner, observer)
        register()
    }

    fun register () {
        threadSave (onError = {error(it)}){
            start()
            val response = App.boxInfoController.register()
            end(response)
        }
    }

    private fun start() {
        postValue(RegisterResponse(status = Status.START))
    }

    private fun end(response: Response<MobileDevice>) {
        if (response.isSuccessful)
            postValue(RegisterResponse(mobileDevice = response.body(), status = Status.END))
        else
            postValue(RegisterResponse(throwable = Exception("Register failed message = ${response.errorBody()?.string()}"), status = Status.ERROR))
    }

    private fun error(throwable: Throwable) {
        postValue(RegisterResponse(throwable = Exception("Register failed message = ${throwable.message}"), status = Status.ERROR))
    }
}


class UpdateBoxInfoLiveData: MutableLiveData<UpdateBoxInfoLiveData.UpdateBoxInfoResponse>() {

    data class UpdateBoxInfoResponse (
        val mobileDevice: MobileDevice? = null,
        val throwable: Throwable? = null,
        val status: Status
    )

    enum class Status {
        START, END, ERROR
    }

    fun update() {
        threadSave (onError = {error(it)}){
            start()
            val response = App.boxInfoController.update(false)
            end(response)
        }
    }

    private fun start() {
        postValue(UpdateBoxInfoResponse(status = Status.START))
    }

    private fun end(response: Response<MobileDevice>) {
        if (response.isSuccessful)
            postValue(UpdateBoxInfoResponse(mobileDevice = response.body(), status = Status.END))
        else
            postValue(UpdateBoxInfoResponse(throwable = Exception("Register failed message = ${response.errorBody()?.string()}"), status = Status.ERROR))
    }

    private fun error(throwable: Throwable) {
        postValue(UpdateBoxInfoResponse(throwable = Exception("Register failed message = ${throwable.message}"), status = Status.ERROR))
    }
}