package com.example.commonandroid

import timber.log.Timber
import kotlin.concurrent.thread

fun Any.printThreadName (optional: String = "") {
    log("$optional. Current thread name is ${Thread.currentThread().name}")
}

fun log(message: String) {
    Timber.d("Hidden camera. $message")
}

fun loge(message: String = "", exception: Throwable?) {
    Timber.e(exception)
}

fun threadSave(onFinally: (()-> Unit)? = null, onError: ((throwable: Throwable) -> Unit)? = null, block: ()-> Unit) {
    thread {
        try {
            block()
        } catch (e: Exception) {
            loge("thread", e)
            onError?.invoke(e)
        } finally {
            onFinally?.invoke()
        }
    }
}