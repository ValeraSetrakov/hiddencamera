package com.valerasetrakov.hiddencamerarecorder

import android.content.Context
import android.widget.Toast

class ToastManager(val context: Context) {
    fun show(message: String) =
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}