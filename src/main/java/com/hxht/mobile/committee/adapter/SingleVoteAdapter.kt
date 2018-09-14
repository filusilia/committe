package com.hxht.mobile.committee.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.hxht.mobile.committee.R
import org.json.JSONObject
import java.util.ArrayList

class SingleVoteAdapter : BaseQuickAdapter<String, BaseViewHolder> {
    private var list: ArrayList<String>? = ArrayList()

    constructor() : super(R.layout.choose_vote_card, ArrayList<String>())

    constructor(list: ArrayList<String>?) : super(R.layout.choose_vote_card, list) {
        this.list = list
    }

    override fun convert(helper: BaseViewHolder, item: String) {
        val json = JSONObject(item)
        helper.setText(R.id.voteCardText, json.getString("name"))
        helper.addOnClickListener(R.id.voteCardText)
    }

    /**
     * 重写getItem 返回实体
     */
    override fun getItem(position: Int): String? {
        return list?.get(position)
    }
}