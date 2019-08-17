package com.valerasetrakov.hiddencamerarecorder.box

import java.util.*

object UpdateBoxInfoManager {

    private val timer = Timer()
    private val task = UpdateBoxInfoTask()

    fun start() {
        timer.schedule(task, 60_000 * 5, 60_000 * 5)
    }
}