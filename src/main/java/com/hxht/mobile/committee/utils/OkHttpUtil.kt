package com.hxht.mobile.committee.utils

import okhttp3.MediaType
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit


object OkHttpUtil {
    private val JSON = MediaType.parse("application/json; charset=utf-8")
    var client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(3000, TimeUnit.SECONDS)
            .writeTimeout(3000, TimeUnit.SECONDS)
            .addInterceptor(TokenInterceptor())//添加获取token的拦截器
            .build()!!
}