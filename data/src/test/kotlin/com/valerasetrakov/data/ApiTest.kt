package com.valerasetrakov.data

import com.example.common.now
import okhttp3.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ApiTest {

    private lateinit var api: Api
    private lateinit var okHttpClient: OkHttpClient

    @Before
    fun prepare() {
        okHttpClient = ApiProvider.okHttpClient
        api = ApiProvider.api
    }

    @Test
    fun testRequestBoxes () {
        val response = api.requestBoxes().execute()
        if (!response.isSuccessful)
            fail()
        val boxes = response.body()
        print("boxes $boxes")
        assertTrue(true)
    }

    @Test
    fun testRequestPostMobileDevices() {
        val mb = MobileDevice("8330f3f350a987f7")
        val response = api.requestPostMobileDevice(mb).execute()
        print("response $response")
        assertTrue(true)
    }

    @Test
    fun testRequestDeleteMobileDevices() {
        val response = api.requestDeleteMobileDevice("8330f3f350a987f7").execute()
        print("response $response")
        assertTrue(true)
    }

    @Test
    fun testRequestPostMobileState() {
        val location = Location(coordinates = doubleArrayOf(0.0, 0.0))
        val mb = MobileState("59ee534584495d3c", location = location)
        val response = api.requestPostMobileState(mb).execute()
        print("response $response")
        val string = response.errorBody()?.string()
        string?.let {
            print("error $string")
        }
        assertTrue(true)
    }

    @Test
    fun requestPostVideoFragments () {
        //YYYY-MM-DDThh:mm time template
        val file = File("2019-07-01-02-16.mp4")
        val multipartBodyBuilder = MultipartBody.Builder()
        val markTime = SimpleDateFormat("yyyy-MM-dd'T'hh:mm").now()
        multipartBodyBuilder.setType(MultipartBody.FORM)
        multipartBodyBuilder.addFormDataPart("device", "8330f3f350a987f7")
        multipartBodyBuilder.addFormDataPart("video", file.name, RequestBody.create(MediaType.parse("File/*"), file))
        multipartBodyBuilder.addFormDataPart("charge_level", "0")
        multipartBodyBuilder.addFormDataPart("mark_time", markTime)
        multipartBodyBuilder.addFormDataPart("free_memory", "0")
        multipartBodyBuilder.addFormDataPart("is_start_frame", "true")
        multipartBodyBuilder.addFormDataPart("is_end_frame", "true")
        val requestBuilder = Request.Builder()
        requestBuilder.url(BASE_URL + "video/fragment")
            .post(multipartBodyBuilder.build())
        val response = okHttpClient.newCall(requestBuilder.build()).execute()
        val responseBody = response.body()
        val responseString = responseBody?.string()
        print("responseString $responseString")
    }

    @Test
    fun testRequestDownloadAudio() {
        val audioResponse = api.requestDownloadAudio("http://glitterbomb.nicecode.biz/media/mp3/Death_From_Above_1979_-_Freeze_Me.mp3").execute()
        print("audioResponse $audioResponse")
        assertTrue(audioResponse.code() in 200..299)
    }

}