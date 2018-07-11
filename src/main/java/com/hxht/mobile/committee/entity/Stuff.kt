package com.hxht.mobile.committee.entity

import java.io.Serializable

data class Stuff(val id: Long, val fileName: String, val fileType: String, val fileAddress: String) :Serializable{
    constructor(fileType: String, fileAddress: String) : this(0, "", fileType, fileAddress)
}