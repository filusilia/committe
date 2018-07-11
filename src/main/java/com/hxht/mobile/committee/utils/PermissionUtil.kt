package com.hxht.mobile.committee.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build


object PermissionUtil {
    private val PERMISSIONS_STORAGE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    /**
     * 解决安卓6.0以上版本不能读取外部存储权限的问题
     *
     * @param activity
     * @param requestCode
     * @return
     */
    fun grantSTORAGE(activity: Activity, requestCode: Int): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val storagePermission = activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (storagePermission != PackageManager.PERMISSION_GRANTED) {
                activity.requestPermissions(PERMISSIONS_STORAGE, requestCode)
                return false
            }
        }
        return true
    }
}