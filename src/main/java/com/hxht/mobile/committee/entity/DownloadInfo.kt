package com.hxht.mobile.committee.entity

data class DownloadInfo(var fileName: String, var url: String, var total: Long, var progress: Long) {
    constructor(url: String) : this("", url, 0, 0)
}