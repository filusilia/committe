package com.hxht.mobile.committee.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.hxht.mobile.committee.R
import java.util.ArrayList

class ChooseVoteAdapter : BaseQuickAdapter<String, BaseViewHolder> {
    private var list: ArrayList<String>? = ArrayList()

    constructor() : super(R.layout.choose_vote_card, ArrayList<String>())

    constructor(list: ArrayList<String>?) : super(R.layout.choose_vote_card, list) {
        this.list = list
    }

    override fun convert(helper: BaseViewHolder, item: String) {
        helper.setText(R.id.voteCardText, item)
        helper.addOnClickListener(R.id.voteCardText)
    }

    /**
     * 重写getItem 返回实体
     */
    override fun getItem(position: Int): String? {
        return list?.get(position)
    }
}