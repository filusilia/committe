package com.hxht.mobile.committee.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.hxht.mobile.committee.R
import com.hxht.mobile.committee.entity.Meet
import com.hxht.mobile.committee.entity.Stuff
import java.util.*


open class MeetListAdapter : BaseQuickAdapter<Meet, BaseViewHolder> {
    private var meets: ArrayList<Meet>? = ArrayList()

    constructor() : super(R.layout.meet_list_recycler_item_card, ArrayList<Meet>())

    constructor(list: ArrayList<Meet>?) : super(R.layout.meet_list_recycler_item_card, list) {
        meets = list
    }

    init {
//        addItemType(Meet.TEXT, R.layout.item_text_view)
    }

    override fun convert(helper: BaseViewHolder, item: Meet) {
        helper.setText(R.id.tvTitle, item.meetName)
        helper.setText(R.id.selectMeet, "确认")
        helper.addOnClickListener(R.id.selectMeet)
//        helper.setImageResource(R.id.icon, item.meetTime)
//        val cardView = helper.getView(R.id.meet_i)
//        cardView.setCardBackgroundColor(Color.parseColor(item.getColorStr()))
        when (item.itemType) {

        }
        when (helper.itemViewType) {
//            Meet.meetName -> {
//                helper.addOnClickListener(R.id.btn)
//            }
        }
    }

    /**
     * 重写getItem 返回实体
     */
    override fun getItem(position: Int): Meet? {
        return meets?.get(position)
    }

}
