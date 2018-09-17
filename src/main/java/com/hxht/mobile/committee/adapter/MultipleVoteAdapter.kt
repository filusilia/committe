package com.hxht.mobile.committee.adapter

import android.widget.CheckBox
import com.blankj.utilcode.util.LogUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.hxht.mobile.committee.R
import org.json.JSONObject
import java.util.*

class MultipleVoteAdapter : BaseQuickAdapter<String, BaseViewHolder> {
    private var list: ArrayList<String>? = ArrayList()

    constructor() : super(R.layout.multiple_vote_card, ArrayList<String>())

    constructor(list: ArrayList<String>?) : super(R.layout.multiple_vote_card, list) {
        this.list = list
    }

    override fun convert(helper: BaseViewHolder, item: String) {
        val json = JSONObject(item)
        val checkBox = helper.getView<CheckBox>(R.id.multipleCheckBox)
        LogUtils.i("checkBox ${helper.adapterPosition}" + checkBox.id)
        val map = mutableMapOf<String, Any>()
        map["check"] = checkBox
        map["value"] = json.getString("id")
        map["display"] = json.getString("name")
        checkBoxList.add(map)
//        checkBox.id = helper.adapterPosition
        helper.setText(R.id.multipleVoteCardText, json.getString("name"))
//        helper.addOnClickListener(R.id.multipleVoteCardText)
    }

    /**
     * 重写getItem 返回实体
     */
    override fun getItem(position: Int): String? {
        return list?.get(position)
    }

    private val checkBoxList = ArrayList<Map<String, Any>>()
    fun getCheckBoxList(): ArrayList<Map<String, Any>> {
        return checkBoxList
    }
}