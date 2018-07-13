package com.hxht.mobile.committee.utils

import okhttp3.*
import java.io.IOException


object OkHttpUtil {
    val JSON = MediaType.parse("application/json; charset=utf-8")
    var client = OkHttpClient()
    //
//    fun get():String  {
//
//        String run(String url) throws IOException {
//            Request request = new Request.Builder()
//                    .url(url)
//                    .build();
//
//            Response response = client.newCall(request).execute();
//            return response.body().string();
//        }
//    }
//
    @Throws(IOException::class)
    fun post(url: String, json: String): String {
        val body = RequestBody.create(JSON, json)
        val request = Request.Builder()
                .url(url)
                .post(body)
                .build()
        val response = client.newCall(request).execute()
        return response.body()!!.string()
    }
}