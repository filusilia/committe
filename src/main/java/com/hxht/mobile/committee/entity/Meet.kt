package com.hxht.mobile.committee.entity

import com.chad.library.adapter.base.entity.MultiItemEntity
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

data class Meet(
        val id: Int,
        var meetName: String?,
        var summary: String?,
        var Logo: String?,
        var time: Date?,
        var status: Int?,
        var reserveStart: String?,
        var reserveEnd: String?,
        var start: String?,
        var end: String?,
        var room: String?,
        var files: ArrayList<String>?,
        var creator: String?,
        var createDate: String?,
        var meetCover: String?) : MultiItemEntity, Serializable {

    constructor(meetName: String) : this(0, meetName, null, null, null, null, null, null, null, null, null, null, null, null, null){
        files = ArrayList()
    }
    constructor(id: Int) : this(id, null, null, null, null, null, null, null, null, null, null, null, null, null, null){
        files = ArrayList()
    }

    private var itemType: Int = 0

    fun MultipleItem(itemType: Int) {
        this.itemType = itemType
    }

    override fun getItemType(): Int {
        return itemType;
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}