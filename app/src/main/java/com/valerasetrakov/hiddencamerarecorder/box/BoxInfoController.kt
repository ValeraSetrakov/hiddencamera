package com.valerasetrakov.hiddencamerarecorder.box

import android.content.Context
import com.example.commonandroid.log
import com.valerasetrakov.data.Api
import com.valerasetrakov.data.AudioApi
import com.valerasetrakov.data.MobileDevice
import com.valerasetrakov.data.User
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream


private const val BOX_INFO_PREFS_NAME = "BoxInfoPrefs"
private const val BOX_INFO_PREFS_AUDIO_PATH_KEY = "BOX_INFO_PREFS_AUDIO_PATH_KEY"
private const val BOX_INFO_PREFS_USERS_PHONE_KEY = "BOX_INFO_PREFS_USERS_PHONE_KEY"
private const val BOX_INFO_PREFS_USERS_MAIL_KEY = "BOX_INFO_PREFS_USERS_MAIL_KEY"
private const val BOX_INFO_PREFS_ROAD_MIN_KEY = "BOX_INFO_PREFS_ROAD_MIN_KEY"


class BoxInfoController(private val context: Context, private val api: Api, private val deviceId: String) {

    companion object {
        private fun print(message: String) {
            log("BoxInfoController. $message")
        }
    }

    private val boxInfoPrefs = context.getSharedPreferences(BOX_INFO_PREFS_NAME, Context.MODE_PRIVATE)

    var audioFilePath: String
        get() = boxInfoPrefs.getString(BOX_INFO_PREFS_AUDIO_PATH_KEY, "").ifEmpty { throw Exception("Audio file not found") }
        set(value) = boxInfoPrefs
            .edit()
            .putString(BOX_INFO_PREFS_AUDIO_PATH_KEY, value)
            .apply()

    var phones: List<String>
        get() = boxInfoPrefs.getStringSet(BOX_INFO_PREFS_USERS_PHONE_KEY, emptySet())?.toList() ?: emptyList()
        set(value) = boxInfoPrefs
            .edit()
            .putStringSet(BOX_INFO_PREFS_USERS_PHONE_KEY, value.toSet())
            .apply()

    var emails: List<String>
        get() = boxInfoPrefs.getStringSet(BOX_INFO_PREFS_USERS_MAIL_KEY, emptySet())?.toList() ?: emptyList()
        set(value) = boxInfoPrefs
            .edit()
            .putStringSet(BOX_INFO_PREFS_USERS_MAIL_KEY, value.toSet())
            .apply()

    var roadMin: Int
        get() = boxInfoPrefs.getInt(BOX_INFO_PREFS_ROAD_MIN_KEY, 0)
        set(value) = boxInfoPrefs
            .edit()
            .putInt(BOX_INFO_PREFS_ROAD_MIN_KEY, value)
            .apply()

    @Synchronized fun register(): Response<MobileDevice> =
        update(true)

    @Synchronized fun update(withAudioFile: Boolean = false): Response<MobileDevice> {
        val mobileDevice = MobileDevice(deviceId)
        val response = api.requestPostMobileDevice(mobileDevice).execute()
        val responseMobileDevice = response.body()
        responseMobileDevice ?: throw Exception(response.errorBody()?.string() ?: "Mobile device after registration is null")
        if (responseMobileDevice.mobiles?.isEmpty() != false)
            return response

        print("Response device info $responseMobileDevice")

        update(responseMobileDevice, withAudioFile)

        return response
    }

    private fun update(mobileDevice: MobileDevice, withAudioFile: Boolean = false) {
        mobileDevice.siren?.let {
            if (withAudioFile) {
                if (it.isNotEmpty()) {
                    updateAudioFile(it)
                }
            }
        }

        val users = mobileDevice.users
        if (users?.isNotEmpty() == true) {
            updateContacts(users)
        }

        roadMin = mobileDevice.roadMin
    }

    private fun updateAudioFile(audio: String) {
        val audioFile = File(context.filesDir, audio.split('/').last())
        val aos = FileOutputStream(audioFile)
        val audioResponse = AudioApi.downloadAudioFromGlitter(audio).execute()
        audioResponse?.let {
            if (it.isSuccessful) {
                val audioBody = it.body()
                audioBody?.let {
                    it.byteStream()?.use { input ->
                        aos.use { output ->
                            input.copyTo(output)
                            audioFilePath = audioFile.absolutePath
                        }
                    }
                }
            }
        }
    }

    private fun updateContacts (users: List<User>) {
        val phones = users.map { it.phoneNumber }
        val emails = users.map { it.email }

        if (phones.isNotEmpty())
            this.phones = phones
        if (emails.isNotEmpty())
            this.emails = emails
    }
}