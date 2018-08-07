package com.hxht.mobile.committee.utils

import com.blankj.utilcode.util.CacheDiskUtils
import com.blankj.utilcode.util.LogUtils
import com.hxht.mobile.committee.common.Constants
import com.hxht.mobile.committee.common.Constants.JCM_URL
import com.hxht.mobile.committee.utils.OkHttpUtil.client
import okhttp3.*
import org.json.JSONObject


class TokenInterceptor : Interceptor {
    private val mediaType = MediaType.parse("application/json; charset=utf-8")

    override fun intercept(chain: Interceptor.Chain?): Response {
        val originalRequest: Request = chain!!.request()
        val originResponse = chain.proceed(originalRequest)
        if (originResponse.code() == 401) { // 业务失败，因为登录失效。
            LogUtils.i("token过期，现在刷新token")
            val content = JSONObject()
            content.put("username", CacheDiskUtils.getInstance().getString(Constants.CACHE_USERNAME))
            content.put("password", CacheDiskUtils.getInstance().getString(Constants.CACHE_PASSWORD))
            val body = RequestBody.create(mediaType, content.toString())
            val request = Request.Builder().url("$JCM_URL/api/token/").post(body)
                    .build()
            val loginResponse = client.newCall(request).execute()
            if (loginResponse.code() == 200) {
                //重新保存token
                val resultStr = loginResponse.body()?.string()
                val result = JSONObject(resultStr)
                CacheDiskUtils.getInstance().put(Constants.JCM_TOKEN, result["data"].toString())
                // 重新执行原始请求。
                return chain.call().execute()
            }
        }
        return originResponse
    }


//    override fun intercept(chain: Chain?): Response {
//        val request: Request = chain!!.request()
//        val originResponse = chain.proceed(request)
//        if (originResponse.code() == 401) { // 业务失败，因为登录失效。
//            // 调用登录接口。
//            val loginRequest = BodyRequest.newBuilder("$JCM_URL/api/token/", RequestMethod.POST)
//                    .param("username", CacheDiskUtils.getInstance().getString(Constants.CACHE_USERNAME)) // 添加请求参数。
//                    .param("password", CacheDiskUtils.getInstance().getString(Constants.CACHE_PASSWORD)) // 添加请求参数。
//                    .build()
//            val loginResponse = Call(loginRequest).execute()
//            // 登录成功。
//            if (loginResponse.code() == 200) {
//                //重新保存token
//                val resultStr = loginResponse.body().string()
//                val result = JSONObject(resultStr)
//                CacheDiskUtils.getInstance().put(Constants.JCM_TOKEN, result["data"].toString())
//                // 关闭原始请求的连接。
//                IOUtils.closeQuietly(originResponse)
//                // 关闭登录请求的连接。
//                IOUtils.closeQuietly(loginResponse)
//                // 重新执行原始请求。
//                return chain.call().execute()
//            } else {
//                // 尝试登录未成功关闭登录连接，极少出现，除非服务器挂了。
//                IOUtils.closeQuietly(loginResponse)
//                // 不关闭原始请求连接，因为下面要返回原始请求的结果。
//            }
//        }
//        return originResponse
//    }
}