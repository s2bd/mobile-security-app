package com.dewanmukto.sechero

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager

fun checkPermissionsForApp(packageName: String, context: Context): String {
    val pm = context.packageManager
    val appInfo: PackageInfo
    val permissionIndicators = StringBuilder()

    try {
        // Get the permissions requested by the app
        appInfo = pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
        val permissions = appInfo.requestedPermissions

        permissions?.forEach { permission ->
            when (permission) {
                android.Manifest.permission.ACCESS_WIFI_STATE -> permissionIndicators.append("🛜 ")
                android.Manifest.permission.ACCESS_NETWORK_STATE -> permissionIndicators.append("📶 ")
                android.Manifest.permission.CAMERA -> permissionIndicators.append("📷 ")
                android.Manifest.permission.RECORD_AUDIO -> permissionIndicators.append("🎙️ ")
                android.Manifest.permission.READ_EXTERNAL_STORAGE -> permissionIndicators.append("📁 ")
                android.Manifest.permission.BLUETOOTH -> permissionIndicators.append("🔷 ")
                android.Manifest.permission.READ_CONTACTS -> permissionIndicators.append("👤 ")
                android.Manifest.permission.CALL_PHONE -> permissionIndicators.append("📞 ")
                android.Manifest.permission.READ_CALL_LOG -> permissionIndicators.append("📃 ")
            }
        }
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    }

    return permissionIndicators.toString()
}
