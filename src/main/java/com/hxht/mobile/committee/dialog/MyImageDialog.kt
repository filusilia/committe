package com.hxht.mobile.committee.dialog

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.github.chrisbanes.photoview.PhotoView
import com.hxht.mobile.committee.R
import kotlinx.android.synthetic.main.image_dialog.*


/**
 * 点击图片控件查看大图
 */
class MyImageDialog (context: Context, private val url: String) : AlertDialog(context, R.style.dialogStyle) {

    private var photoView: PhotoView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.image_dialog)
        photoView = findViewById<View>(R.id.photo_view) as PhotoView
//        photoView.width
        //按空白处不能取消动画
        setCanceledOnTouchOutside(true)
        Glide.with(context)
                .load(url).apply(RequestOptions().placeholder(R.drawable.ic_stuff_type_loading_pic))
                .into(photoView!!)
//        photoView.setImageResource(R.drawable.image);

        fullImageProgress.visibility = View.GONE

        fullImageClose.setOnClickListener {
            this.hide()
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private fun showProgress(show: Boolean) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        fullImageProgress.visibility = View.VISIBLE
    }

    override fun show() {
        super.show()
        /**
         * 设置宽度全屏，要设置在show的后面
         */
        val layoutParams = window!!.attributes
        layoutParams.gravity = Gravity.BOTTOM
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT

        window!!.decorView.setPadding(0, 0, 0, 0)

        window!!.attributes = layoutParams
    }
}