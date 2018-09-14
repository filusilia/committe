package com.hxht.mobile.committee.entity

import java.io.Serializable
import java.util.*

data class User(
        var id: Long,
        var username: String,
        var password: String?,
        var realName: String?,
        var photo: String?,
        var createDate: String?
) : Serializable {
    constructor() : this(Random().nextLong(), "", null, null, null, null)
}