package com.hxht.mobile.committee.common

object Constants {

//    const val JCM_IP = "192.168.10.129:8080"
    const val JCM_IP = "192.168.10.14:8180/jcm/"
    const val JCM_URL = "http://$JCM_IP/"
    const val JCM_URL_HEADER = "Authorization"
    const val JCM_TOKEN = "jcmToken"
    const val CACHE_USERID = "cache_userId"
    const val CACHE_USERNAME = "cache_username"
    const val CACHE_PASSWORD = "cache_password"


    /**
     * 下载路径
     */
    const val DOWNLOAD_PATH = "/Download"
    /**
     * 下拉延迟
     */
    const val DELAY_TIME = 2000L

    const val LOGIN_CODE:Int = 1000
    const val NOW_MEETING_CODE:Int = 1001
    const val MEETING_LIST_CODE:Int = 1002
    const val VOTE_CODE:Int = 1003
    const val CHOOSE_VOTE_CODE:Int = 1004
    const val EXP_PLAYER_CODE:Int = 1005
    const val USER_INFO_CODE:Int = 1005
}