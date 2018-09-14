package com.hxht.mobile.committee.utils

import com.blankj.utilcode.util.CacheDiskUtils
import com.hxht.mobile.committee.common.Constants
import okhttp3.*
import java.util.concurrent.TimeUnit


object OkHttpUtil {
    val JSON = MediaType.parse("application/json; charset=utf-8")

    val FORM = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8")
    var client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(3000, TimeUnit.SECONDS)
            .writeTimeout(3000, TimeUnit.SECONDS)
            .addInterceptor(TokenInterceptor())//添加获取token的拦截器
            .build()!!

    fun post(url: String, param: String): Response {
        val requestBody = RequestBody.create(OkHttpUtil.JSON, param)
        val request = Request.Builder().url("${Constants.JCM_URL}$url")
                .addHeader(Constants.JCM_URL_HEADER, CacheDiskUtils.getInstance().getString(Constants.JCM_TOKEN))
                .post(requestBody)
                .build()
        val call = client.newCall(request)
        return call.execute()
    }
}