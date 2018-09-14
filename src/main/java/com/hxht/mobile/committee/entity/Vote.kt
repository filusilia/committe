package com.hxht.mobile.committee.entity

import org.json.JSONArray
import java.io.Serializable
import java.util.*

data class Vote(var id: Long, var title: String?, var summary: String?, var creator: User?, var multiple: Boolean, var item: String?) : Serializable {
    constructor() : this(Random().nextLong(), null, null, null, false, null)
    constructor(multiple: Boolean) : this(Random().nextLong(), null, null, null, multiple, null)
}