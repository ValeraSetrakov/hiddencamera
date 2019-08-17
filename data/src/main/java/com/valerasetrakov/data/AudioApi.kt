package com.valerasetrakov.data

import okhttp3.Call
import okhttp3.Request

object AudioApi {
    private val okHttpClient = ApiProvider.okHttpClient

    fun downloadAudioFromGlitter (path: String) =
            downloadAudio("$BASE_URL/$path")

    fun downloadAudio (path: String): Call {
        val request = Request.Builder()
            .url(path)
            .get()
            .build()
        return okHttpClient.newCall(request)
    }
}