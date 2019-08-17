package com.valerasetrakov.data

import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody
import java.io.File

object VideoApi {

    private val okHttpClient = ApiProvider.okHttpClient

    fun requestUploadVideoFragment (videoFragment: VideoFragment) =
            VideoApi.requestUploadVideoFragment(
                videoFragment.device,
                videoFragment.video,
                videoFragment.chargeLevel,
                videoFragment.freeMemory,
                videoFragment.isStartFrame,
                videoFragment.isEndFrame,
                videoFragment.markTime)

    private fun requestUploadVideoFragment (device: String, path: String, chargeLevel: Int, freeMemory: Long, isStartFrame: Boolean, isEndFrame: Boolean, markTime: String) {
        val file = File(path)

        val multipartBodyBuilder = MultipartBody.Builder()
        multipartBodyBuilder.setType(MultipartBody.FORM)
        multipartBodyBuilder.addFormDataPart("device", device)
        multipartBodyBuilder.addFormDataPart("video", file.name, RequestBody.create(MediaType.parse("File/*"), file))
        multipartBodyBuilder.addFormDataPart("charge_level", "$chargeLevel")
        multipartBodyBuilder.addFormDataPart("mark_time", markTime)
        multipartBodyBuilder.addFormDataPart("free_memory", "$freeMemory")
        multipartBodyBuilder.addFormDataPart("is_start_frame", "$isStartFrame")
        multipartBodyBuilder.addFormDataPart("is_end_frame", "$isEndFrame")
        val requestBuilder = Request.Builder()
        requestBuilder.url(BASE_URL_API + "video/fragment")
            .post(multipartBodyBuilder.build())
        val response = okHttpClient.newCall(requestBuilder.build()).execute()
        val responseBody = response.body()
        val responseString = responseBody?.string()
        if (!response.isSuccessful)
            throw UploadVideoFragmentException("responseString $responseString. File: ${file.absolutePath}")
    }


    class UploadVideoFragmentException(message: String): Exception("Video upload error $message")
}