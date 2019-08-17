package com.valerasetrakov.hiddencamerarecorder

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

const val REQUEST_MEDIA_PERMISSIONS_CODE = 1
const val REQUEST_LOCATION_PERMISSIONS_CODE = 2
const val REQUEST_SEND_SMS_PERMISSION_CODE = 3
const val REQUEST_SEND_SMS_AND_LOCATION_PERMISSION_CODE = 4

object PermissionsController {

    fun checkPermmision(context: Context, permission: String) =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    /** If return true, then something permissions need be requested else return false */
    fun requestPermissions(activity: Activity, permissions: Array<String>, requestCode: Int): Boolean {
        val notGrantedPermissions = permissions.filter{ !checkPermmision(activity, it) }.toTypedArray()
        if (notGrantedPermissions.isEmpty())
            return false
        ActivityCompat.requestPermissions(activity, notGrantedPermissions, requestCode)
        return true
    }

    fun requestPermission(activity: Activity, permission: String, requestCode: Int) =
            requestPermissions(activity, arrayOf(permission), requestCode)








    fun requestPermissions(fragment: Fragment, permissions: Array<String>, requestCode: Int): Boolean {
        val notGrantedPermissions = permissions.filter{ !checkPermmision(fragment.context!!, it) }.toTypedArray()
        if (notGrantedPermissions.isEmpty())
            return false
        fragment.requestPermissions(notGrantedPermissions, requestCode)
        return true
    }

    fun requestPermission(fragment: Fragment, permission: String, requestCode: Int) =
        requestPermissions(fragment, arrayOf(permission), requestCode)






    fun requestSendSmsPermission (fragment: Fragment) =
            requestPermission(fragment, Manifest.permission.SEND_SMS, REQUEST_SEND_SMS_PERMISSION_CODE)

    fun requestMediaPermissions(fragment: Fragment) =
        requestPermissions(
            fragment,
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO),
            REQUEST_MEDIA_PERMISSIONS_CODE)

    fun requestSmsAndLocationPermissions(fragment: Fragment) =
        requestPermissions(
            fragment,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.SEND_SMS),
            REQUEST_SEND_SMS_AND_LOCATION_PERMISSION_CODE)
}