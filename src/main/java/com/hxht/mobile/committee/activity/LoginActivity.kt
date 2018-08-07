package com.hxht.mobile.committee.activity

import android.Manifest.permission.READ_CONTACTS
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.app.LoaderManager.LoaderCallbacks
import android.content.CursorLoader
import android.content.Intent
import android.content.Loader
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.provider.ContactsContract
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.CacheDiskUtils
import com.blankj.utilcode.util.LogUtils
import com.hxht.mobile.committee.R
import com.hxht.mobile.committee.common.Constants
import com.hxht.mobile.committee.common.Constants.CACHE_PASSWORD
import com.hxht.mobile.committee.common.Constants.CACHE_USERNAME
import com.hxht.mobile.committee.common.Constants.JCM_TOKEN
import com.hxht.mobile.committee.common.Constants.JCM_URL
import com.hxht.mobile.committee.entity.Meet
import com.hxht.mobile.committee.utils.OkHttpUtil
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog
import com.yanzhenjie.kalle.Kalle
import com.yanzhenjie.kalle.simple.SimpleCallback
import com.yanzhenjie.kalle.simple.SimpleResponse
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.content_login.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.lang.Exception
import java.util.*


/**
 * A login screen that offers login via email/password.
 */
class LoginActivity : AppCompatActivity(), LoaderCallbacks<Cursor> {
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private var mAuthTask: UserLoginTask? = null
    private var checkMeetTask: CheckMeet? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setSupportActionBar(loginToolbar)
        // 设置状态栏底色颜色
//        BarUtils.setNavBarColor(this, resources.getColor(R.color.colorPrimary, null))
        BarUtils.setStatusBarColor(this, resources.getColor(R.color.colorPrimary, null))
        val sharedPreferences = getSharedPreferences(LOGIN_INFO, 0)
        // Simulate network access.
        // token verify before network
        val usernameStr = sharedPreferences.getString("username", null)
        val passwordStr = sharedPreferences.getString("password", null)
        if (username != null) {
            username.setText(usernameStr)
        }
        if (passwordStr != null) {
            password.setText(passwordStr)
        }
        // Set up the login form.
        populateAutoComplete()
        /**
         * 密码框监控键盘的回车
         */
        password.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin()
                return@OnEditorActionListener true
            }
            false
        })
        sign_in_button.setOnClickListener { view ->
            attemptLogin()
        }
//        sign_in_button.setOnClickListener { attemptLogin() }
    }

    private fun populateAutoComplete() {
        if (!mayRequestContacts()) {
            return
        }

        loaderManager.initLoader(0, null, this)
    }

    private fun mayRequestContacts(): Boolean {
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
//            Snackbar.make(username, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
//                    .setAction(android.R.string.ok,
//                            { requestPermissions(arrayOf(READ_CONTACTS), REQUEST_READ_CONTACTS) })
        } else {
            requestPermissions(arrayOf(READ_CONTACTS), REQUEST_READ_CONTACTS)
        }
        return false
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete()
            }
        }
    }

    /**
     * 登录验证通过方法
     */
    private fun loginStart(username: String, psd: String) {

        val tipDialog = QMUITipDialog.Builder(this)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord("正在登录")
                .create()
        tipDialog.show()
        /**
         * 网络登录验证
         */
        Kalle.post("${JCM_URL}api/token/")
                .param("username", username) // 添加请求参数。
                .param("password", psd) // 添加请求参数。
                .perform(object : SimpleCallback<String>() {
                    override fun onResponse(response: SimpleResponse<String, String>?) {
                        //请求成功
                        if (response?.code() == 200) {
                            val resultStr = response.succeed()
                            val result = JSONObject(resultStr)
                            if (result["code"] != 0) {
                                tipDialog.dismiss()
                                diaMessageFail(result["msg"].toString())
                            } else {
                                //登录成功，写下用户名密码 下次直接登录
                                val sharedPreferences = getSharedPreferences(LOGIN_INFO, 0)
                                val editor = sharedPreferences.edit()
                                editor.putString("username", username)
                                editor.putString("password", psd)
                                editor.apply()

                                //保存token
                                CacheDiskUtils.getInstance().put(JCM_TOKEN, result["data"].toString())
                                CacheDiskUtils.getInstance().put(CACHE_USERNAME, username)
                                CacheDiskUtils.getInstance().put(CACHE_PASSWORD, psd)
                                tipDialog.dismiss()
                                diaMessage("欢迎您，$username")
                                checkMeetTask = CheckMeet()
                                checkMeetTask?.execute()
                            }
                        } else {
                            tipDialog.dismiss()
                            diaMessageFail("登录失败")
                        }

                    }

                    override fun onException(e: Exception?) {
                        super.onException(e)
                        LogUtils.e("登录异常${e?.message}")
                        tipDialog.dismiss()
                        diaMessageFail("登录失败")
                    }
                })
    }

    inner class CheckMeet internal constructor() : AsyncTask<Void, Void, Boolean>() {

        override fun doInBackground(vararg params: Void): Boolean? {
            // TODO: attempt authentication against a network service.
            // http 请求登录在这里
            return try {
                val request = Request.Builder().url("$JCM_URL/api/duty/").addHeader(Constants.JCM_URL_HEADER, CacheDiskUtils.getInstance().getString(Constants.JCM_TOKEN))
                        .build()
                val call = OkHttpUtil.client.newCall(request)
                call.enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        LogUtils.i("onFailure: ")
                    }

                    @Throws(IOException::class)
                    override fun onResponse(call: Call, response: Response) {
//                        LogUtils.i("onResponse: " + response.body()?.string()+"${response.code()}")
                        if (response.code() == 200) {
                            val dutyStr = response.body()?.string()
                            val duty = JSONObject(dutyStr)
                            if (duty["code"] == 0) {
                                val intent = Intent(this@LoginActivity, NowMeetingActivity::class.java)
                                intent.putExtra("meet", Meet("王老汉碰瓷案"))
                                startActivity(intent)
                            }
                        }
                        val intent = Intent(this@LoginActivity, MeetListActivity::class.java)
                        startActivity(intent)
                    }
                })
                true
            } catch (e: InterruptedException) {
                false
            }

        }

        override fun onPostExecute(success: Boolean?) {
            Log.v("", "post execute")

        }

        override fun onCancelled() {

        }
    }

    private fun diaMessage(message: String?) {
        val tipDialog = QMUITipDialog.Builder(this@LoginActivity)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_SUCCESS)
                .setTipWord(message)
                .create()
        tipDialog.show()
        Timer().schedule(object : TimerTask() {
            override fun run() {
                tipDialog.dismiss()
            }
        }, 2000)
    }

    private fun diaMessageFail(message: String?) {
        val tipDialog = QMUITipDialog.Builder(this@LoginActivity)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                .setTipWord(message)
                .create()
        tipDialog.show()
        Timer().schedule(object : TimerTask() {
            override fun run() {
                tipDialog.dismiss()
            }
        }, 2000)
    }

    /**
     * 登录验证
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private fun attemptLogin() {
        if (mAuthTask != null) {
            return
        }

        // Reset errors.
        username.error = null
        password.error = null

        // Store values at the time of the login attempt.
        val usernameStr = username.text.toString()
        val passwordStr = password.text.toString()

        var cancel = false
        var focusView: View? = null

        if (TextUtils.isEmpty(usernameStr)) {
            username.error = getString(R.string.error_field_required)
            focusView = username
            cancel = true
        }
        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(passwordStr)) {
            password.error = getString(R.string.error_field_required)
            focusView = password
            cancel = true
        } else if (!isPasswordValid(passwordStr)) {

        }

        // Check for a valid email address.
        else if (!isUsernameValid(usernameStr)) {
            username.error = getString(R.string.error_invalid_username)
            focusView = username
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView?.requestFocus()
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
//            showProgress(true)
            /**
             * 验证通过开始登录任务
             * UserLoginTask
             */
            loginStart(usernameStr, passwordStr)
//            mAuthTask = UserLoginTask(usernameStr, passwordStr)
//            mAuthTask!!.execute(null as Void?)
        }
    }

    private fun isUsernameValid(username: String): Boolean {
//        return username.contains("")
        return true
    }

    private fun isPasswordValid(password: String): Boolean {
        //网络验证用户名密码
        return password.length > 4
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private fun showProgress(show: Boolean) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

            login_form.visibility = if (show) View.GONE else View.VISIBLE
            login_form.animate()
                    .setDuration(shortAnimTime)
                    .alpha((if (show) 0 else 1).toFloat())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            login_form.visibility = if (show) View.GONE else View.VISIBLE
                        }
                    })

            login_progress.visibility = if (show) View.VISIBLE else View.GONE
            login_progress.animate()
                    .setDuration(shortAnimTime)
                    .alpha((if (show) 1 else 0).toFloat())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            login_progress.visibility = if (show) View.VISIBLE else View.GONE
                        }
                    })
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            login_progress.visibility = if (show) View.VISIBLE else View.GONE
            login_form.visibility = if (show) View.GONE else View.VISIBLE
        }
    }

    override fun onCreateLoader(i: Int, bundle: Bundle?): Loader<Cursor> {
        return CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE + " = ?", arrayOf(ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE),

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC")
    }

    override fun onLoadFinished(cursorLoader: Loader<Cursor>, cursor: Cursor) {
        val emails = ArrayList<String>()
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS))
            cursor.moveToNext()
        }

//        addEmailsToAutoComplete(emails)
    }

    override fun onLoaderReset(cursorLoader: Loader<Cursor>) {
        Log.i("info", "reload")
    }

    object ProfileQuery {
        val PROJECTION = arrayOf(
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY)
        val ADDRESS = 0
        val IS_PRIMARY = 1
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    inner class UserLoginTask internal constructor(private val mUsername: String, private val mPassword: String) : AsyncTask<Void, Void, Boolean>() {

        override fun doInBackground(vararg params: Void): Boolean? {
            // TODO: attempt authentication against a network service.
            // http 请求登录在这里
            try {
                val sharedPreferences = getSharedPreferences(LOGIN_INFO, 0)
                // Simulate network access.
                // token verify before network
                val username = sharedPreferences.getString("username", null)
                val password = sharedPreferences.getString("password", null)
                if (username == null || password == null) {
//                    Toast.makeText(applicationContext,"注册成功", Toast.LENGTH_SHORT).show()
                }
                val param = JSONObject()
                param.put("username", mUsername)
                param.put("password", mPassword)

                val editor = sharedPreferences.edit()
                editor.putString("username", mUsername)
                editor.putString("password", mPassword)
                editor.apply()
                Thread.sleep(2000)
                Log.v("", "do background")
                return false
            } catch (e: InterruptedException) {
                return false
            }

//            return DUMMY_CREDENTIALS.map { it.split(":") }.firstOrNull { it[0] == mUsername }?.let {
//                // Account exists, return true if the password matches.
//                it[1] == mPassword
//            } ?: true
        }

        override fun onPostExecute(success: Boolean?) {
            Log.v("", "post execute")
            mAuthTask = null
            showProgress(false)
            Log.v("", success.toString())
            if (success!!) {
                finish()
                Snackbar.make(login_form, "登录可以了", Snackbar.LENGTH_SHORT)

                Log.v("", "success ===>$LOGIN_SUCCESS")
                if (LOGIN_SUCCESS == 0) {
//                    intent.setClass(loginActivity,MeetListActivity::class.java)
//                    val intent = Intent(mainx, NowMeetingActivity::class.java)
//                    intent.putExtra(EXTRA_MESSAGE, "-")
//                    startActivity(intent)
                } else {
//                    val intent = Intent(this, MeetListActivity::class.java)
//                    intent.putExtra(EXTRA_MESSAGE, "-")
//                    startActivity(intent)
                }

                LOGIN_SUCCESS = 1
                intent.putExtra(EXTRA_MESSAGE, "-")
                startActivity(intent)
            } else {
                password.error = getString(R.string.error_incorrect_password)
                password.requestFocus()
            }
        }

        override fun onCancelled() {
            mAuthTask = null
            showProgress(false)
        }
    }

    override fun onResume() {
        super.onResume()
    }

    companion object {

        /**
         * Id to identity READ_CONTACTS permission request.
         */
        private const val REQUEST_READ_CONTACTS = 0
        private const val LOGIN_INFO = "loginInfo"

        private var LOGIN_SUCCESS = 0

        private val loginActivity = this

        private val intent = Intent()

        /**
         * A dummy authentication store containing known user names and passwords.
         * TODO: remove after connecting to a real authentication system.
         */
        private val DUMMY_CREDENTIALS = arrayOf("foo@example.com:hello", "bar@example.com:world")
    }
}
