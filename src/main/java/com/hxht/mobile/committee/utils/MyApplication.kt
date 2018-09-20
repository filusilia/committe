package com.hxht.mobile.committee.utils

import android.app.Activity
import android.app.Application
import java.util.*


class MyApplication private constructor() : Application() {
    //运用list来保存们每一个activity是关键
    private val mList = LinkedList<Activity>()

    companion object {
        private object Holder {
            val INSTANCE = MyApplication()
        }

        fun getInstance() = Holder.INSTANCE
    }

    //添加要退出的Activity
    fun addActivity(activity: Activity) {
        mList.add(activity)
    }

    //关闭每一个list内的activity
    fun exit() {
        try {
            for (activity in mList) {
                activity.finish()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            System.exit(0)
        }
    }

    //杀进程
    override fun onLowMemory() {
        super.onLowMemory()
        System.gc()
    }
}