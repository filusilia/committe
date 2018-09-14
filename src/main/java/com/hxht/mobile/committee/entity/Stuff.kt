package com.hxht.mobile.committee.entity

import java.io.Serializable
import java.util.*

data class Stuff(var id: Long?, var fileName: String?, var fileType: String?, var fileAddress: String?) : Serializable {
    constructor(fileType: String, fileAddress: String) : this(0, "", fileType, fileAddress)
    constructor() : this(Random().nextLong(), null, null, null)
}