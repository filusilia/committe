package com.hxht.mobile.committee.entity

import com.chad.library.adapter.base.entity.MultiItemEntity
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

data class Meet(
        val id: Int,
        val meetName: String,
        val summary: String?,
        val Logo: String?,
        val time: Date?,
        val status: Int?,
        val reserveStart: String?,
        val reserveEnd: String?,
        val start: String?,
        val end: String?,
        val room: String?,
        val files: ArrayList<String>?,
        val creator: String?,
        val createDate: String?,
        val meetCover: String?) : MultiItemEntity, Serializable {

    constructor(meetName: String) : this(0, meetName, null, null, null, null, null, null, null, null, null, null, null, null, null)

    private var itemType: Int = 0

    fun MultipleItem(itemType: Int) {
        this.itemType = itemType
    }

    override fun getItemType(): Int {
        return itemType;
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}