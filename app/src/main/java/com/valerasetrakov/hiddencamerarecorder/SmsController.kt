package com.valerasetrakov.hiddencamerarecorder

import android.telephony.SmsManager

object SmsController {

    private val smsManager = SmsManager.getDefault()

    fun sendSms(phones: List<String>, message: String) {
        phones.forEach { sendSms(it, message) }
    }
    fun sendSms(phone: String, message: String) {
        smsManager.sendTextMessage(phone, null, message, null, null)
    }
}