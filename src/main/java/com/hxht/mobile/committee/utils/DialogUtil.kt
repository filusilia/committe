package com.hxht.mobile.committee.utils

import android.content.Context
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog
import java.util.*

object DialogUtil {
    fun show(context: Context, str: String, iconType: Int) {
        val tipDialog = QMUITipDialog.Builder(context)
                .setIconType(iconType)
                .setTipWord(str)
                .create()
        tipDialog.show()
        Timer().schedule(object : TimerTask() {
            override fun run() {
                tipDialog.dismiss()
            }
        }, 2000)
    }
}