package com.valerasetrakov.hiddencamerarecorder.box

import androidx.annotation.WorkerThread
import com.example.commonandroid.loge
import com.valerasetrakov.data.ApiProvider
import com.valerasetrakov.data.MobileDevice
import com.valerasetrakov.hiddencamerarecorder.App
import com.valerasetrakov.hiddencamerarecorder.SmsController

@WorkerThread
object BoxMoveController {
    fun notifyAboutOut (deviceId: String) {
        notifyAboutMove(deviceId, "Box out")
    }

    fun notifyAboutComeBack (deviceId: String) {
        notifyAboutMove(deviceId, "Box come back")
    }

    fun notifyAboutMove(deviceId: String, message: String) {
        try {
            SmsController.sendSms(App.boxInfoController.phones, message)
            ApiProvider.api.requestSendMail(MobileDevice(phoneId = deviceId)).execute()
        } catch (e: Throwable) {
            loge(exception = e)
        }
    }
}