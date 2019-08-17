package com.valerasetrakov.hiddencamerarecorder

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.commonandroid.threadSave

open class BaseViewModel: ViewModel() {
    val isLoad = MutableLiveData<Boolean>().apply { value = false }
    fun startLoad() = isLoad.postValue(true)
    fun stopLoad() = isLoad.postValue(false)
    fun threadLoad (block: () -> Unit) {
        startLoad()
        threadSave(onFinally = {stopLoad()}, block = block)
    }
}