package com.example.common

import java.text.SimpleDateFormat
import java.util.*

fun SimpleDateFormat.now(): String = format(Date())
val defaultSDF = SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss").apply {
    timeZone = TimeZone.getTimeZone("UTC")
}
val defaultSDFFileName = SimpleDateFormat("yyyy-MM-dd-hh-mm-ss-SS").apply {
    timeZone = TimeZone.getTimeZone("UTC")
}
fun defaultNow() = defaultSDF.now()
fun fileNameLikeDate() = defaultSDFFileName.now()