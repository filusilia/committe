package com.hxht.mobile.committee.activity

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.LogUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hxht.mobile.committee.R
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog
import com.yanzhenjie.kalle.Kalle
import com.yanzhenjie.kalle.KalleConfig
import com.yanzhenjie.kalle.OkHttpConnectFactory
import com.yanzhenjie.kalle.simple.SimpleCallback
import com.yanzhenjie.kalle.simple.SimpleResponse
import kotlinx.android.synthetic.main.activity_user_info.*
import kotlinx.android.synthetic.main.content_user_info.*
import java.lang.Exception

class UserInfoActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_info)
        userToolbar.title = "个人信息"
        setSupportActionBar(userToolbar)
        BarUtils.setStatusBarColor(this, resources.getColor(R.color.colorPrimary, null))

        var tipDialog = QMUITipDialog.Builder(this)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord("正在获取信息")
                .create()
        tipDialog.show()
        /**
         * 网络登录验证
         */
        KalleConfig.newBuilder()
                .connectFactory(OkHttpConnectFactory.newBuilder().build())
                .build()

        Kalle.post("http://www.example.com")
                .setHeader("name", "kalle") // 设置请求头，会覆盖默认头和之前添加的头。
                .param("name", "kalle") // 添加请求参数。
                .perform(object : SimpleCallback<String>() {
                    override fun onResponse(response: SimpleResponse<String, String>?) {
                        tipDialog.dismiss()
                        LogUtils.i(response)

                        Glide.with(this@UserInfoActivity)
                                .load("http://104.224.152.210:8080/pic/61781299_p0.jpg").apply(RequestOptions().placeholder(R.drawable.user))
                                .into(userHeadImg)
                        userName.text = "人事部蔡蔡"
                    }

                    override fun onException(e: Exception?) {
                        super.onException(e)
                        tipDialog.dismiss()
                    }
                })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        return true
    }

    /**
     * 菜单方法
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        super.onBackPressed()
        this.finish()
        return super.onOptionsItemSelected(item)
    }


}
