package com.hxht.mobile.committee.dialog

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.hxht.mobile.committee.R
import kotlinx.android.synthetic.main.choose_vote_dialog.*

class VoteDialog(context: Context, private val url: String) : AlertDialog(context, R.style.dialogStyle) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.choose_vote_dialog)
        //按空白处不能取消动画
        setCanceledOnTouchOutside(true)

        val title = findViewById<TextView>(R.id.createVoteDialogTitle)
        title?.text = "选择投票"

        //初始化界面控件
        initView()
        //初始化界面数据
        initData()
        //初始化界面控件的事件
        initEvent()
    }

    private lateinit var okBtn: Button
    private lateinit var cancelBtn: Button
    private lateinit var messageTextView: TextView
    /**
     * 初始化界面控件
     */
    private fun initView() {
        okBtn = findViewById<View>(R.id.createVoteDialogOk) as Button
        cancelBtn = findViewById<View>(R.id.createVoteDialogCancel) as Button
        messageTextView = findViewById<View>(R.id.createVoteDialogMessage) as TextView
    }

    //确定文本和取消文本的显示内容
    private var okHint: String? = null
    private var cancelHint: String? = null
    private var messageHint: String? = null

    /**
     * 初始化界面控件的显示数据
     */
    private fun initData() {
        //如果设置按钮的文字
        if (okHint != null) {
            okBtn.text = okHint
        }
        if (cancelHint != null) {
            cancelBtn.text = cancelHint
        }
        if (messageHint != null) {
            messageTextView.text = messageHint
        }
    }

    private var cancelClickListener: VoteDialog.CancelClickListener? = null//取消按钮被点击了的监听器
    private var okClickListener: VoteDialog.OkClickListener? = null//确定按钮被点击了的监听器

    /**
     * 初始化界面的确定和取消监听器
     */
    private fun initEvent() {
        //设置确定按钮被点击后，向外界提供监听
        okBtn.setOnClickListener {
            okClickListener?.onYesClick()
        }
        //设置取消按钮被点击后，向外界提供监听
        cancelBtn.setOnClickListener {
            cancelClickListener?.onNoClick()
        }
    }


    fun addVote(view: View) {
        view.setOnClickListener { view->

        }
        chooseVoteLinear.addView(view)
    }


    fun setMessage(str: String) {
        messageHint = str
    }

    /**
     * 设置取消按钮的显示内容和监听
     *
     * @param str
     * @param noClickListener
     */
    fun setCancelClickListener(str: String?, cancelClickListener: CancelClickListener) {
        if (str != null) {
            cancelHint = str
        }
        this.cancelClickListener = cancelClickListener
    }

    /**
     * 设置确定按钮的显示内容和监听
     *
     * @param str
     * @param okClickListener
     */
    fun setOkClickListener(str: String?, okClickListener: OkClickListener) {
        if (str != null) {
            okHint = str
        }
        this.okClickListener = okClickListener
    }

    /**
     * 设置确定按钮和取消被点击的接口
     */
    interface OkClickListener {
        fun onYesClick()
    }

    interface CancelClickListener {
        fun onNoClick()
    }


    override fun show() {
        super.show()
        /**
         * 设置宽度全屏，要设置在show的后面
         */
        val layoutParams = window!!.attributes
        layoutParams.gravity = Gravity.CENTER
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT

        window!!.decorView.setPadding(0, 0, 0, 0)

        window!!.attributes = layoutParams
    }
}