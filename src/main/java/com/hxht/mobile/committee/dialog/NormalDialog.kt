package com.hxht.mobile.committee.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.hxht.mobile.committee.R


/**
 * 登录后判断当前是否有正在开始的会议的弹窗
 */
open class NormalDialog(context: Context?) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_dialog)
        //按空白处不能取消动画
        setCanceledOnTouchOutside(false)
        //初始化界面控件
        initView()
        //初始化界面数据
        initData()
        //初始化界面控件的事件
        initEvent()
    }

    private lateinit var yes: Button
    private lateinit var no: Button
    private lateinit var titleTv: TextView
    private lateinit var messageTv: TextView

    /**
     * 初始化界面控件
     */
    private fun initView() {
        yes = findViewById<View>(R.id.yes) as Button
        no = findViewById<View>(R.id.no) as Button
        titleTv = findViewById<View>(R.id.title) as TextView
        messageTv = findViewById<View>(R.id.message) as TextView
    }

    private var titleStr: String? = null//从外界设置的title文本
    private var messageStr: String? = null//从外界设置的消息文本
    //确定文本和取消文本的显示内容
    private var yesStr: String? = null
    private var noStr: String? = null

    /**
     * 初始化界面控件的显示数据
     */
    private fun initData() {
        //如果用户自定了title和message
        if (titleStr != null) {
            titleTv.text = titleStr
        }
        if (messageStr != null) {
            messageTv.text = messageStr
        }
        //如果设置按钮的文字
        if (yesStr != null) {
            yes.text = yesStr
        }
        if (noStr != null) {
            no.text = noStr
        }
    }

    /**
     * 初始化界面的确定和取消监听器
     */
    private fun initEvent() {
        //设置确定按钮被点击后，向外界提供监听
        yes.setOnClickListener {
            yesClickListener?.onYesClick()
        }
        //设置取消按钮被点击后，向外界提供监听
        no.setOnClickListener {
            noClickListener?.onNoClick()
        }
    }
    /**
     * 从外界Activity为Dialog设置标题
     *
     * @param title
     */
    fun setTitle(title: String) {
        titleStr = title
    }

    /**
     * 从外界Activity为Dialog设置dialog的message
     *
     * @param message
     */
    fun setMessage(message: String) {
        messageStr = message
    }

    private var noClickListener: NoClickListener? = null//取消按钮被点击了的监听器
    private var yesClickListener: YesClickListener? = null//确定按钮被点击了的监听器

    /**
     * 设置取消按钮的显示内容和监听
     *
     * @param str
     * @param noClickListener
     */
    fun setNoClickListener(str: String?, noClickListener: NoClickListener) {
        if (str != null) {
            noStr = str
        }
        this.noClickListener = noClickListener
    }

    /**
     * 设置确定按钮的显示内容和监听
     *
     * @param str
     * @param yesClickListener
     */
    fun setYesClickListener(str: String?, yesClickListener: YesClickListener) {
        if (str != null) {
            yesStr = str
        }
        this.yesClickListener = yesClickListener
    }

    /**
     * 设置确定按钮和取消被点击的接口
     */
    interface YesClickListener {
        fun onYesClick()
    }

    interface NoClickListener {
        fun onNoClick()
    }
}