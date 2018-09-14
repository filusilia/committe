package com.hxht.mobile.committee.activity

import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.CacheDiskUtils
import com.blankj.utilcode.util.LogUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hxht.mobile.committee.R
import com.hxht.mobile.committee.common.Constants
import com.hxht.mobile.committee.common.Constants.JCM_URL
import com.hxht.mobile.committee.entity.User
import com.hxht.mobile.committee.utils.KalleConfigUtil
import com.hxht.mobile.committee.utils.OkHttpUtil
import com.hxht.mobile.committee.utils.TokenInterceptor
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog
import com.yanzhenjie.kalle.Kalle
import com.yanzhenjie.kalle.KalleConfig
import com.yanzhenjie.kalle.OkHttpConnectFactory
import com.yanzhenjie.kalle.simple.SimpleCallback
import com.yanzhenjie.kalle.simple.SimpleResponse
import kotlinx.android.synthetic.main.activity_user_info.*
import kotlinx.android.synthetic.main.content_user_info.*
import kotlinx.android.synthetic.main.nav_header_main.*
import okhttp3.Request
import org.json.JSONObject
import java.lang.Exception

class UserInfoActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private var tipDialog: QMUITipDialog? = null
    private var drawUserTask: DrawUserTask? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_info)
        userToolbar.title = "个人信息"
        setSupportActionBar(userToolbar)
        BarUtils.setStatusBarColor(this, resources.getColor(R.color.colorPrimary, null))

        tipDialog = QMUITipDialog.Builder(this)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord("正在获取信息")
                .create()
        tipDialog?.show()
        drawUserTask = DrawUserTask()
        drawUserTask?.execute()
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

    /**
     * 导航栏个人信息
     */
    inner class DrawUserTask internal constructor() : AsyncTask<Void, Void, Boolean>() {
        val user = User()
        override fun doInBackground(vararg params: Void): Boolean? {
            // TODO: attempt authentication against a network service.
            return try {
                val request = Request.Builder().url("${Constants.JCM_URL}api/currentUser")
                        .addHeader(Constants.JCM_URL_HEADER, CacheDiskUtils.getInstance().getString(Constants.JCM_TOKEN))
                        .build()
                val call = OkHttpUtil.client.newCall(request)
                val response = call.execute()
                tipDialog?.dismiss()
                if (response.code() == 200) {
                    val resultStr = response.body()?.string()
                    val result = JSONObject(resultStr)
                    if (result["code"] == 0) {
                        val userJson = JSONObject(result["data"].toString())
                        user.username = userJson["username"].toString()
                        user.realName = userJson["realName"].toString()
                        user.photo = userJson["photo"].toString()
                    }
                } else {
                    tipDialog = QMUITipDialog.Builder(this@UserInfoActivity)
                            .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                            .setTipWord("个人信息获取失败！")
                            .create()
                    tipDialog?.show()
                }
                false
            } catch (e: InterruptedException) {
                false
            }
        }

        override fun onPostExecute(success: Boolean?) {
            initDrawUser(user)
        }

        override fun onCancelled() {

        }
    }

    private fun initDrawUser(user: User) {
        Glide.with(this@UserInfoActivity)
                .load(JCM_URL + user.photo).apply(RequestOptions().placeholder(R.drawable.user))
                .into(userHeadImg)
        userName.text = user.realName
    }
}
