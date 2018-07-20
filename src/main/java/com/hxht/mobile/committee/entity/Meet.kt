package com.hxht.mobile.committee.entity

import com.chad.library.adapter.base.entity.MultiItemEntity
import java.io.Serializable
import java.util.*

data class Meet(val meetName:String,val meetTime :Date?,val meetCover:String?) : MultiItemEntity,Serializable {

    constructor(meetName: String) : this(meetName,null,null)

    private var itemType: Int = 0

    fun MultipleItem(itemType: Int) {
        this.itemType = itemType
    }
    override fun getItemType(): Int {
        return itemType;
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}