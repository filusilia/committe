package com.hxht.mobile.committee.handler

import android.os.Handler
import android.os.Message
import com.hxht.mobile.committee.activity.NowMeetingActivity
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import java.lang.ref.WeakReference

class NowMeetingHandler(activity: NowMeetingActivity) : Handler() {
    private var voteId: Long = 0
    fun setVoteId(id: Long) {
        voteId = id
    }

    private var mCurrentDialogStyle = com.qmuiteam.qmui.R.style.QMUI_Dialog
    private val mActivity: WeakReference<NowMeetingActivity> = WeakReference(activity)
    override fun handleMessage(msg: Message) {
        val activity = mActivity.get()
        if (activity != null) {
            QMUIDialog.MessageDialogBuilder(activity)
                    .setTitle("投票结果")
                    .setMessage(msg.obj.toString())
                    .addAction("确定") { dialog, index -> dialog.dismiss() }
                    .create(mCurrentDialogStyle).show()
        }
    }
}