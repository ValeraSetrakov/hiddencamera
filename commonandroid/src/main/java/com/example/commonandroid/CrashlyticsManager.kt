package com.example.commonandroid

import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent
import okhttp3.logging.HttpLoggingInterceptor

object CrashlyticsManager {

    fun logWithAttrs(message: String, attrs: List<String>) {
        val event = CustomEvent(message)
        attrs.forEachIndexed { index, attr ->
            event.putCustomAttribute("$index", attr)
        }
        Answers.getInstance().logCustom(event)
    }

    fun log(message: String) {
        Answers.getInstance().logCustom(CustomEvent(message))
//        Crashlytics.log(message)
    }

    fun logException(e: Throwable) {
        log("error ${e.message}")
//        Crashlytics.logException(e)
    }

}

class CrashlyticsHttpLoggingInterceptorLogger: HttpLoggingInterceptor.Logger {
    override fun log(message: String) {
        CrashlyticsManager.log(message)
    }
}