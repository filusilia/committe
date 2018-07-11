package com.hxht.mobile.committee.utils

import android.content.Context
import android.content.Context.STORAGE_SERVICE
import android.os.Environment
import android.os.storage.StorageManager
import java.lang.reflect.InvocationTargetException
import java.util.*
import android.os.StatFs
import android.os.Build
import android.annotation.TargetApi
import java.io.File


object StorageUtil {
    /**
     * 判断当前存储卡是否可用
     */
    fun checkSDCardAvailable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    fun getStorage(): File? {
        return Environment.getExternalStorageDirectory()
    }

    /**
     * 通过反射调用获取内置存储和外置sd卡根路径(通用)
     *
     * @param mContext    上下文
     * @param is_removale 是否可移除，false返回内部存储，true返回外置sd卡
     * @return
     */
    fun getStoragePath(mContext: Context, is_removale: Boolean): String? {

        val mStorageManager = mContext.getSystemService(STORAGE_SERVICE) as StorageManager
        var storageVolumeClazz: Class<*>? = null
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume")
            val getVolumeList = mStorageManager.javaClass.getMethod("getVolumeList")
            val getPath = storageVolumeClazz!!.getMethod("getPath")
            val isRemovable = storageVolumeClazz.getMethod("isRemovable")
            val result = getVolumeList.invoke(mStorageManager)
            val list = Arrays.asList(result)
            val length = Arrays.asList(result).size
            for (i in 0 until length) {
                val storageVolumeElement = list[i]
                val path = getPath.invoke(storageVolumeElement) as String
                val removable = isRemovable.invoke(storageVolumeElement) as Boolean
                if (is_removale == removable) {
                    return path
                }
            }
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }

        return null
    }

    /**
     * 获取手机内部可用空间大小
     * @return
     */
    fun getAvailableInternalMemorySize(): Long {
        val path = Environment.getDataDirectory().path
        val statFs = StatFs(path)
        val blockSize = statFs.blockSizeLong
        val availableBlocks = statFs.availableBlocksLong//获取当前可用的存储空间
        return availableBlocks * blockSize
    }

    /**
     * 获取SD卡信息
     *
     * @return SDCardInfo
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun getSDCardInfo(): String {
        val sd = SDCardInfo()
        if (!checkSDCardAvailable()) return "sdcard unable!"
        sd.isExist = true
        val sf = StatFs(Environment.getExternalStorageDirectory().path)
        sd.totalBlocks = sf.blockCountLong
        sd.blockByteSize = sf.blockSizeLong
        sd.availableBlocks = sf.availableBlocksLong
        sd.availableBytes = sf.availableBytes
        sd.freeBlocks = sf.freeBlocksLong
        sd.freeBytes = sf.freeBytes
        sd.totalBytes = sf.totalBytes
        return sd.toString()
    }

    class SDCardInfo {
        internal var isExist: Boolean = false
        internal var totalBlocks: Long = 0
        internal var freeBlocks: Long = 0
        internal var availableBlocks: Long = 0
        internal var blockByteSize: Long = 0
        internal var totalBytes: Long = 0
        internal var freeBytes: Long = 0
        internal var availableBytes: Long = 0

        override fun toString(): String {
            return "isExist=" + isExist +
                    "\ntotalBlocks=" + totalBlocks +
                    "\nfreeBlocks=" + freeBlocks +
                    "\navailableBlocks=" + availableBlocks +
                    "\nblockByteSize=" + blockByteSize +
                    "\ntotalBytes=" + totalBytes +
                    "\nfreeBytes=" + freeBytes +
                    "\navailableBytes=" + availableBytes
        }
    }
}