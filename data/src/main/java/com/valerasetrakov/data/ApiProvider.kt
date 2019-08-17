package com.valerasetrakov.data

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit

object ApiProvider {

    var logger: HttpLoggingInterceptor.Logger? = null
    val okHttpClient: OkHttpClient = OkHttpClient.Builder().apply {
        addInterceptor(HeaderInterceptor())
        addInterceptor(HttpLoggingInterceptor(HttpLoggingInterceptor.Logger {
            logger?.log(it)
        }))
        connectTimeout(10_000, TimeUnit.MILLISECONDS)
    }.build()
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL_API)
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttpClient)
        .build()
    val api: Api = retrofit.create(Api::class.java)

}

class HeaderInterceptor: Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        requestBuilder.addHeader("Api-Token", API_TOKEN)
        requestBuilder.addHeader("Api-Secret-Key", API_SECRETE_KEY)
        val request = requestBuilder.build()
        val response = chain.proceed(request)
        return response
    }
}
