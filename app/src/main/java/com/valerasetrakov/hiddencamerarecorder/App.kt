package com.valerasetrakov.hiddencamerarecorder

import android.app.Application
import android.content.Context
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import androidx.lifecycle.MutableLiveData
import androidx.work.WorkManager
import com.crashlytics.android.Crashlytics
import com.example.commonandroid.CrashlyticsHttpLoggingInterceptorLogger
import com.valerasetrakov.data.ApiProvider
import com.valerasetrakov.data.Location
import com.valerasetrakov.hiddencamerarecorder.box.BoxInfoController
import com.valerasetrakov.hiddencamerarecorder.box.BoxMoveController
import io.fabric.sdk.android.Fabric
import timber.log.Timber
import com.crashlytics.android.answers.Answers



class App: Application() {

    companion object {

        fun phoneStorageFree(): Long {
            val path = Environment.getDataDirectory()
            val stat = StatFs(path.path)
            return stat.freeBytes
        }

        fun phoneStorageFreeLikeMB(): Long =
            phoneStorageFree().div(1024).div(1024)

        val currnetPosition = MutableLiveData<Location>()
        val startPosition = MutableLiveData<Location>()
        lateinit var deviceId: String
        lateinit var batteryManager: BatteryManager
        lateinit var workManager: WorkManager
        lateinit var toastManager: ToastManager
        lateinit var boxInfoController: BoxInfoController

        val batteryLevel: Int
            get() = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val freeMemory: Long
            get() = phoneStorageFreeLikeMB()


        fun sendNotificationAboutBoxOut() {
            BoxMoveController.notifyAboutOut(deviceId)
        }

        fun sendNotificationAboutBoxComeBack() {
            BoxMoveController.notifyAboutComeBack(deviceId)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        workManager = WorkManager.getInstance()
        toastManager = ToastManager(this)
        boxInfoController = BoxInfoController(
            context = this,
            api = ApiProvider.api,
            deviceId = deviceId
        )
//        Fabric.with(this, Crashlytics())
        Fabric.with(this, Answers())
//        val logger = CrashlyticsHttpLoggingInterceptorLogger()
//        ApiProvider.logger = logger
    }
}