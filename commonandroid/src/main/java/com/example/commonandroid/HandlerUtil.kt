package com.example.commonandroid

import android.os.Handler

fun Handler.postAndLog (message: String = "", block: () -> Unit) {
    log(message)
    post(block)
}

fun Handler.postAtFrontOfQueueAndLog (message: String = "", block: () -> Unit) {
    log(message)
    postAtFrontOfQueue(block)
}