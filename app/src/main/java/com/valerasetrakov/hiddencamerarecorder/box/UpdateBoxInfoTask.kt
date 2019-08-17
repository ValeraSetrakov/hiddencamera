package com.valerasetrakov.hiddencamerarecorder.box

import com.valerasetrakov.hiddencamerarecorder.App
import java.util.*

class UpdateBoxInfoTask: TimerTask() {

    private val boxInfoController = App.boxInfoController

    override fun run() {
        boxInfoController.update()
    }
}