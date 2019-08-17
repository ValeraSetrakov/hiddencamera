package com.example.commonandroid

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat


fun Context.checkPermissionGranted(permission: String) =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

fun Context.checkPermissionsGranted(vararg permissions: String): Boolean =
    permissions.all { checkPermissionGranted(it) }