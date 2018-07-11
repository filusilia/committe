package com.hxht.mobile.committee.adapter

import android.content.Context
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.hxht.mobile.committee.R
import com.hxht.mobile.committee.entity.Stuff


class NowMeetingStuffAdapter : BaseQuickAdapter<Stuff, BaseViewHolder> {
    private var context: Context
    override fun convert(helper: BaseViewHolder, item: Stuff?) {
        helper.setText(R.id.stuffTitle, item?.fileName)
//        helper.setText(R.id.stuffAddress, item.fileAddress)
        val imageView = helper.getView(R.id.stuffThumbnail) as ImageView
//        val waveProgressView = helper.getView<WaveProgressView>(R.id.wave_progress)
//        waveProgressView.bringToFront()
//        waveProgressView.setDrawSecondWave(true)
        val uri = Uri.parse(item?.fileAddress)
        when (item?.fileType) {
            "docx" -> imageView.setImageResource(R.drawable.ic_stuff_type_word)
            "xls" -> imageView.setImageResource(R.drawable.ic_stuff_type_xlsx)
            "xlsx" -> imageView.setImageResource(R.drawable.ic_stuff_type_xlsx)
            "txt" -> imageView.setImageResource(R.drawable.ic_stuff_type_txt)
            "pdf" -> imageView.setImageResource(R.drawable.ic_stuff_type_pdf)
            "jar" -> imageView.setImageResource(R.drawable.ic_stuff_type_java)
            "mp4", "avi", "flv", "rmvb", "swf" -> imageView.setImageResource(R.drawable.ic_stuff_type_video)
            "mp3", "flac", "wav" -> imageView.setImageResource(R.drawable.ic_stuff_type_audio)
            "jpg", "png" -> {
                imageView.setImageResource(R.drawable.ic_stuff_type_jpg)
                loadImage(item.fileAddress, imageView)
            }
            "gif" -> {
                imageView.setImageResource(R.drawable.ic_stuff_type_gif)
            }
        }
    }

    private fun loadImage(address: String, view: ImageView) {
        Glide.with(context)
                .load(address).apply(RequestOptions().placeholder(R.drawable.ic_stuff_type_loading_pic))
                .into(view)
    }

    private var meetStuff:ArrayList<Stuff>? = ArrayList()

    constructor(list: ArrayList<Stuff>?, context: Context) : super(R.layout.now_meeting_staff_card, list) {
        meetStuff = list
        this.context = context
    }

    /**
     * 重写getItem 返回实体
     */
    override fun getItem(position: Int): Stuff? {
        return meetStuff?.get(position)
    }

    init {
//        addItemType(Meet.TEXT, R.layout.item_text_view)
    }
}