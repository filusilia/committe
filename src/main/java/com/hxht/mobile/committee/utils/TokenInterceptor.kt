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
            val formBody = FormBody.Builder()
                    .add("username", CacheDiskUtils.getInstance().getString(Constants.CACHE_USERNAME))
                    .add("password", CacheDiskUtils.getInstance().getString(Constants.CACHE_PASSWORD))

            val request = Request.Builder().url("$JCM_URL/api/token/").post(formBody.build()).build()
            try {
                val loginResponse = client.newCall(request).execute()
                if (loginResponse.code() == 200) {
                    //重新保存token
                    val resultStr = loginResponse.body()?.string()
                    val result = JSONObject(resultStr)
                    CacheDiskUtils.getInstance().put(Constants.JCM_TOKEN, result["data"].toString())
                    // 重新执行原始请求。

//                    val originalFormBody = originalRequest.body() as FormBody
//                    val newBuilder = FormBody.Builder()
//                    for (i in 0 until originalFormBody.size()) {
//                        newBuilder.add(originalFormBody.name(i), originalFormBody.value(i))
//                    }
                    val request = originalRequest.newBuilder()
                            .header(Constants.JCM_URL_HEADER, CacheDiskUtils.getInstance().getString(Constants.JCM_TOKEN))
                            .build()
                    return chain.proceed(request)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                LogUtils.e("重新获取token出错。${e.message}")
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