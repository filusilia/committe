package com.hxht.mobile.committee.adapter

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.hxht.mobile.committee.R
import com.hxht.mobile.committee.common.Constants.JCM_URL
import com.hxht.mobile.committee.entity.Meet
import com.hxht.mobile.committee.entity.Stuff
import java.util.*


open class MeetListAdapter : BaseQuickAdapter<Meet, BaseViewHolder> {
    private var context: Context
    private var meets: ArrayList<Meet>? = ArrayList()

    constructor(list: ArrayList<Meet>?, context: Context) : super(R.layout.meet_list_recycler_item_card, list) {
        meets = list
        this.context = context
    }

    init {
//        addItemType(Meet.TEXT, R.layout.item_text_view)
    }

    override fun convert(helper: BaseViewHolder, item: Meet) {
        val imageView = helper.getView(R.id.meetImg) as ImageView
        if (null != item.meetCover) {
            Glide.with(context)
                    .load(JCM_URL+item.meetCover).apply(RequestOptions().placeholder(R.drawable.jcm_mobile))
//                    .error(R.drawable.jcm_mobile)//图片加载失败后，显示的图片
                    .into(imageView)
        }
        helper.setText(R.id.tvTitle, item.meetName)
        helper.setText(R.id.tvDis, item.summary)
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
