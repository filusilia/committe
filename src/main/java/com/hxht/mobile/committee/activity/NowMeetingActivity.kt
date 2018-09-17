package com.hxht.mobile.committee.activity

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.NavigationView
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.TextView
import android.widget.Toast
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.CacheDiskUtils
import com.blankj.utilcode.util.LogUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.daimajia.numberprogressbar.NumberProgressBar
import com.hxht.mobile.committee.R
import com.hxht.mobile.committee.R.id.number_progress_bar
import com.hxht.mobile.committee.adapter.NowMeetingStuffAdapter
import com.hxht.mobile.committee.common.Constants
import com.hxht.mobile.committee.common.Constants.JCM_IP
import com.hxht.mobile.committee.common.Constants.JCM_URL
import com.hxht.mobile.committee.dialog.MyImageDialog
import com.hxht.mobile.committee.dialog.NormalDialog
import com.hxht.mobile.committee.entity.Meet
import com.hxht.mobile.committee.entity.Stuff
import com.hxht.mobile.committee.entity.User
import com.hxht.mobile.committee.entity.Vote
import com.hxht.mobile.committee.utils.DialogUtil
import com.hxht.mobile.committee.utils.MimeUtil
import com.hxht.mobile.committee.utils.OkHttpUtil
import com.hxht.mobile.committee.utils.StorageUtil
import com.hxht.mobile.committee.websocket.MyStomp
import com.qmuiteam.qmui.util.QMUIDisplayHelper
import com.qmuiteam.qmui.widget.QMUIRadiusImageView
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog
import com.qmuiteam.qmui.widget.popup.QMUIPopup
import com.tbruyelle.rxpermissions2.RxPermissions
import com.yanzhenjie.kalle.Canceller
import com.yanzhenjie.kalle.Headers
import com.yanzhenjie.kalle.Kalle
import com.yanzhenjie.kalle.download.Callback
import com.yanzhenjie.kalle.download.Download
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_now_meeting.*
import kotlinx.android.synthetic.main.content_now_meeting.*
import kotlinx.android.synthetic.main.now_meeting_app_bar.*
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import ua.naiksoftware.stomp.LifecycleEvent
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.client.StompClient
import java.io.IOException
import java.util.*

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
/**
 * 当前会议 activity
 */
class NowMeetingActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var recyclerView: RecyclerView
    private val files = arrayListOf<Stuff>()
    private var meet: Meet? = null
    private val PERMISSIONS_STORAGE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private val REQUEST_EXTERNAL_STORAGE = 1
    private var rxPermissions: RxPermissions? = null
    private var nowMeetingTask: NowMeetingTask? = null
    private var drawUserTask: DrawUserTask? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_now_meeting)
        setSupportActionBar(nowMeetingToolbar)
        BarUtils.setStatusBarColor(this, resources.getColor(R.color.colorPrimary, null))
        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, nowMeetingToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        rxPermissions = RxPermissions(this)
        grantPermissions()
        nav_view.setNavigationItemSelectedListener(this)
        //加载导航个人信息
        drawUserTask = DrawUserTask()
        drawUserTask?.execute()

        //加载会议资料
        meet = intent.getSerializableExtra("meet") as Meet
        if (null != meet) {
            LogUtils.i("当前会议室这个！：$meet")
            nowTitle.text = "当前会议： ${meet?.meetName}"
            nowMeetingTask = NowMeetingTask(meet?.id)
            nowMeetingTask?.execute()
        }

//        initDemoFile()
        nowTitle.setOnClickListener { view ->
            //            stompClient()
            initNormalPopupIfNeed()
            mNormalPopup?.setAnimStyle(QMUIPopup.ANIM_GROW_FROM_CENTER)
            mNormalPopup?.setPreferredDirection(QMUIPopup.DIRECTION_TOP)
            mNormalPopup?.show(view)
        }

//        val btn = findViewById<Button>(R.id.nowMeetingBtn)
//        btn.setOnClickListener {
//            Log.i("info", "cancel btn")
//            Kalle.Download.cancel(cancelTag)
//        }
//        val createVoteBtn = findViewById<Button>(R.id.createVote)
//        createVoteBtn.setOnClickListener {
//            val intent = Intent(this@NowMeetingActivity, VoteActivity::class.java)
//            intent.putExtra("id", "336699999x")
//            intent.putExtra("meet", meet)
//            startActivityForResult(intent, 0)
//        }
    }

    private var mNormalPopup: QMUIPopup? = null
    private fun initNormalPopupIfNeed() {
        if (mNormalPopup == null) {
            mNormalPopup = QMUIPopup(this, QMUIPopup.DIRECTION_BOTTOM)
            val view: View? = View.inflate(this, R.layout.now_meeting_pop, null)
            val con: ConstraintLayout = view!!.findViewById(R.id.popMeet)
            val popMeetTitle = con.findViewById<TextView>(R.id.popMeetTitle)
            popMeetTitle.text = "会议详情：${meet?.summary}"
            popMeetTitle.setTextColor(ContextCompat.getColor(this, R.color.gray))
            val popMeetParticipants = con.findViewById<TextView>(R.id.popMeetParticipants)
//            popMeetParticipants.text = "与会人员：佘太君、翠花、特朗普"
            popMeetParticipants.setTextColor(ContextCompat.getColor(this, R.color.gray))
            con.layoutParams = mNormalPopup?.generateLayoutParam(
                    QMUIDisplayHelper.dp2px(this, 250), WRAP_CONTENT)
            mNormalPopup?.setContentView(con)
            mNormalPopup?.setOnDismissListener {
                LogUtils.i("关闭了")
            }
        }
    }


    inner class NowMeetingTask internal constructor(val id: Int?) : AsyncTask<Void, Void, Boolean>() {

        override fun doInBackground(vararg params: Void): Boolean? {
            // TODO: attempt authentication against a network service.
            return try {
                if (id == null) return false
                val request = Request.Builder().url("${Constants.JCM_URL}api/meeting/$id")
                        .addHeader(Constants.JCM_URL_HEADER, CacheDiskUtils.getInstance().getString(Constants.JCM_TOKEN))
                        .build()
                val call = OkHttpUtil.client.newCall(request)
                try {
                    call.execute().use { response ->
                        if (response.code() == 200) {
                            val resultStr = response.body()?.string()
                            val result = JSONObject(resultStr)
                            if (result["code"] == 0) {
                                val meetStr = JSONObject(result["data"].toString())
                                meet?.meetName = meetStr["name"].toString()
                                meet?.meetCover = meetStr["logo"].toString()
                                meet?.summary = meetStr["summary"].toString()
                                val fileJSONArray = meetStr["files"]
                                if (fileJSONArray != null && (fileJSONArray as JSONArray).length() > 0) {
                                    for (i in 0..(fileJSONArray.length() - 1)) {
                                        val json = JSONObject(fileJSONArray.get(i).toString())
                                        val temp = Stuff()
                                        temp.fileName = json["name"].toString()
                                        temp.fileType = json["type"].toString()
                                        temp.fileAddress = JCM_URL + json["url"].toString()
                                        files.add(temp)
                                    }
                                }
                                return true
                            }
                        }
                    }
                } catch (e: IOException) {
                    return false
                }
                false
            } catch (e: InterruptedException) {
                false
            }
        }

        override fun onPostExecute(success: Boolean?) {
            Log.v("", "post execute")
            initAdapter()
        }

        override fun onCancelled() {

        }
    }


    private fun initAdapter() {
        // 获取RecyclerView对象
        recyclerView = findViewById<View>(R.id.meetStuffRecycler) as RecyclerView

        // 创建线性布局管理器（默认是垂直方向）
        val layoutManager = GridLayoutManager(this, 4)
//        layoutManager.spanSizeLookup =GridLayoutManager.SpanSizeLookup(){
//
//        }
        // 为RecyclerView指定布局管理对象
        recyclerView.layoutManager = layoutManager
        // 创建Adapter
        val sampleRecyclerAdapter = NowMeetingStuffAdapter(files, this)
//        sampleRecyclerAdapter.setEmptyView(getView());
        sampleRecyclerAdapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
            val stuff = sampleRecyclerAdapter.getItem(position)
            Log.d("Glide", "onItemChildClick:")
//            Toast.makeText(this, "点击的文件获取地址${stuff.fileAddress},文件名${stuff.fileName}", Toast.LENGTH_SHORT).show()
            when (stuff?.fileType) {
                "docx", "xls", "xlsx", "txt", "pdf" -> {
                    downloadFile(stuff)// 用户已经同意该权限
                }
                "mp3", "flac", "wav" -> {
                    val intent = Intent(this@NowMeetingActivity, ExpPlayerActivity::class.java)
                    intent.putExtra("url", stuff.fileAddress)
                    intent.putExtra("name", stuff.fileName)
                    intent.putExtra("meet", meet)
                    startActivityForResult(intent, Constants.NOW_MEETING_CODE)
                }
                "mp4", "flv", "rmvb", "avi" -> {
                    val intent = Intent(this@NowMeetingActivity, ExpPlayerActivity::class.java)
                    intent.putExtra("url", stuff.fileAddress)
                    startActivityForResult(intent, Constants.NOW_MEETING_CODE)
                }
                "swf" -> {
                    Toast.makeText(this, "视频格式暂不支持！", Toast.LENGTH_LONG).show()
                }
                "jpg", "png", "gif" -> {
                    //点击查看大图
                    val imageDialog = MyImageDialog(this, stuff.fileAddress!!)
                    val window = imageDialog.window
                    window.setGravity(Gravity.TOP)
                    imageDialog.show()
                }
                else -> {
                    downloadFile(stuff)// 用户已经同意该权限
                }
            }
        }
        sampleRecyclerAdapter.setOnItemChildClickListener { adapter, view, position ->
            Log.d("", "onItemChildClick: ")
            Toast.makeText(this, "onItemChildClick$position", Toast.LENGTH_SHORT).show()
        }
        sampleRecyclerAdapter.setOnLoadMoreListener({
            /**
             * 上滑加载更多
             */
            recyclerView.postDelayed({
                sampleRecyclerAdapter.loadMoreEnd()
//                if (true) {
//                    if (files.size > 30) {
//                        sampleRecyclerAdapter.loadMoreEnd()
//                    } else {
//                        sampleRecyclerAdapter.loadMoreComplete()
//                    }
//                } else {
//                    sampleRecyclerAdapter.loadMoreFail()
//                }
            }, Constants.DELAY_TIME)
        }, recyclerView)
//        sampleRecyclerAdapter.openLoadMore(PAGE_SIZE, true);
        sampleRecyclerAdapter.openLoadAnimation()
        // 没有数据的时候默认显示该布局
        val view = this.layoutInflater.inflate(R.layout.activity_empty, null)
        view.findViewById<TextView>(R.id.emptyTextHint).text = "没有找到案件相关资料"
        sampleRecyclerAdapter.emptyView = view
        recyclerView.adapter = sampleRecyclerAdapter
    }

    private fun grantPermissions() {
        rxPermissions!!.requestEach(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe { permission ->
            when {
                permission.granted -> {
                    Log.i("INFO", permission.name + " is granted.")
                }
                permission.shouldShowRequestPermissionRationale -> {
                    // 用户拒绝了该权限，没有选中『不再询问』（Never ask again）,那么下次再次启动时，还会提示请求权限的对话框
                    Log.i("INFO", permission.name + " is denied. More info should be provided.")
                    Toast.makeText(this, "很抱歉，APP没有写入设备的权限，将无法下载文件！", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    // 用户拒绝了该权限，并且选中『不再询问』
                    Log.i("INFO", permission.name + " is denied.")
                    Toast.makeText(this, "很抱歉，APP没有写入设备的权限，将无法下载文件！\n请到‘设置-权限管理’中赋予APP存储权限！", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private var canceller: Canceller? = null
    private var cancelTag = "downloadFile"
    private fun downloadFile(stuff: Stuff?) {
        if (!rxPermissions?.isGranted(Manifest.permission.READ_EXTERNAL_STORAGE)!! && !rxPermissions?.isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)!!) {
            Toast.makeText(this, "没有权限，下载失败！", Toast.LENGTH_LONG).show()
            return
        }
        if (stuff == null)
            return
        val index = files.indexOf(stuff)
        val card = recyclerView.layoutManager.findViewByPosition(index) as CardView
//        val waveProgressView = card.findViewById<WaveProgressView>(wave_progress) ?: return
        val numberProgressBar = card.findViewById<NumberProgressBar>(number_progress_bar) ?: return

//        val textProgress: TextView = card.findViewById(R.id.text_progress)
//        waveProgressView.setTextView(textProgress)
//        waveProgressView.setOnAnimationListener(object : WaveProgressView.OnAnimationListener {
//            override fun howToChangeText(interpolatedTime: Float, updateNum: Float, maxNum: Float): String {
//                val decimalFormat = DecimalFormat("0.00")
//                return decimalFormat.format(interpolatedTime * updateNum / maxNum * 100) + "%"
//
//            }
//
//            override fun howToChangeWaveHeight(percent: Float, waveHeight: Float): Float {
//                return (1 - percent) * waveHeight
//            }
//        })
        canceller = Kalle.Download.get(stuff.fileAddress).tag(cancelTag)
                .setHeader(Constants.JCM_URL_HEADER, CacheDiskUtils.getInstance().getString(Constants.JCM_TOKEN)) // 设置请求头，会覆盖默认头和之前添加的头。
                .directory(StorageUtil.getStorage()?.path + Constants.DOWNLOAD_PATH)
                .fileName(stuff.fileName).onProgress { progress, byteCount, speed ->
                    // progress：进度，[0, 100]。
                    // byteCount: 目前已经下载的byte大小。
                    // speed：此时每秒下载的byte大小。
                    Log.i("e", "下载进度 $stuff ：$progress,目前下载$byteCount,下载速度$speed")
                    numberProgressBar.progress = progress
//                    waveProgressView.setProgressNum(progress.toFloat())
                }.policy(object : Download.Policy {
                    override fun allowDownload(code: Int, headers: Headers?): Boolean {
                        return true
                    }

                    override fun isRange(): Boolean {
                        return true
                    }

                    override fun oldAvailable(path: String?, code: Int, headers: Headers?): Boolean {
                        Log.i("下载", "$path,$code,$headers")
                        return true
                    }

                }).perform(object : Callback {
                    override fun onStart() {
                        // 请求开始了。
                        Log.i("info", "download onStart")
//                        waveProgressView.visibility = View.VISIBLE
                        numberProgressBar.visibility = View.VISIBLE
                    }

                    override fun onFinish(path: String) {
                        // 请求完成，文件路径：path。
                        Log.i("info", "download finsh $path")
//                        waveProgressView.visibility = View.GONE
                        MimeUtil.openFileByPath(applicationContext, StorageUtil.getStorage()?.path + Constants.DOWNLOAD_PATH + "/" + stuff.fileName)
                    }

                    override fun onException(e: Exception) {
                        // 请求发生异常了。
                        Log.i("info", "download onException" + e.message)
//                        waveProgressView.visibility = View.GONE
                        numberProgressBar.visibility = View.VISIBLE
                    }

                    override fun onCancel() {
                        Log.i("info", "download onCancel")
                        // 请求被取消了。
//                        waveProgressView.visibility = View.GONE
                        numberProgressBar.visibility = View.VISIBLE
                    }

                    override fun onEnd() {
                        // 请求结束了。
                        Log.i("info", "download onEnd")
//                        waveProgressView.visibility = View.GONE
                        numberProgressBar.visibility = View.VISIBLE
                    }
                })
    }

    private fun handleTopicMessage(str: String?) {
        if (str == null) return
        val json = JSONObject(str)
        LogUtils.i(json)
        when (json["code"]) {
            2001 -> {
                val data = json.getJSONObject("data")
                if (deal(data)) {
                    val nowVote = data.getJSONObject("vote")
                    val entity = Vote(nowVote.getBoolean("multiple"))
                    entity.title = nowVote.getString("name")
                    entity.summary = nowVote.getString("summary")
                    entity.item = nowVote.getString("item")
                    startVote(entity)
                }
            }
        }
    }

    private fun startVote(vote: Vote) {
//        val vote = arrayListOf("还行，就判他五年 ","不行，这么认真的队伍要无罪释放 "," 爱咋咋地我弃权")
        val builder = AlertDialog.Builder(this@NowMeetingActivity)
        builder.setCancelable(false)
        builder.setTitle("发现投票项")
        builder.setMessage("有人发起了投票\n将会带你跳转到新页面进行投票。")
        builder.setPositiveButton("好的") { _: DialogInterface, _: Int ->
            if (vote.multiple) {
                val temp = Intent(this@NowMeetingActivity, MultipleVoteActivity::class.java)
                temp.putExtra("vote", vote)
                temp.putExtra("meet", meet)
                startActivityForResult(temp, Constants.NOW_MEETING_CODE)
            } else {
                val temp = Intent(this@NowMeetingActivity, SingleVoteActivity::class.java)
                temp.putExtra("vote", vote)
                temp.putExtra("meet", meet)
                startActivityForResult(temp, Constants.NOW_MEETING_CODE)
            }
        }
        builder.show()
    }

    private fun deal(json: JSONObject?): Boolean {
        if (json == null) return false
        val nowMeet = json.getJSONObject("meeting")
        if (nowMeet["id"] != meet?.id) return false
        val nowVote = json.getJSONObject("vote")
        val participants = nowVote.getJSONArray("participants")
        for (i in 0..(participants.length() - 1)) {
            val id = participants.getLong(i)
            if (user?.id == id)
                return true
        }
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        LogUtils.i("$requestCode,$resultCode,$data")
        if (null != data) {
            when (requestCode) {
                Constants.VOTE_CODE -> {
                    val voteTitle = data.getStringExtra("voteTitle")
                    val vote = data.getSerializableExtra("vote") as Vote
                    if (null == voteTitle || null == vote) {
                        LogUtils.i("投票信息不完整 跳过。")
                    }
                    startVote(vote)
                }
                else -> {

                }
            }
        }

    }


    override fun onDestroy() {
        super.onDestroy()
        Kalle.Download.cancel(cancelTag)
//        disconnectStomp()
    }

    private var firstTime = 0L
    override fun onBackPressed() {
//        disconnectStomp()
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            val secondTime = System.currentTimeMillis()
            LogUtils.i(secondTime)
            if (secondTime - firstTime > 2000) {
                Toast.makeText(this@NowMeetingActivity, "再按一次退出程序", Toast.LENGTH_SHORT).show()
                firstTime = secondTime
            } else {
                finish()
                System.exit(0)
            }
//            super.onBackPressed()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    /**
     * 菜单方法
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings ->
                menuCreateVote()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun menuCreateVote(): Boolean {
        val intent = Intent(this@NowMeetingActivity, VoteActivity::class.java)
        intent.putExtra("id", "336699999x")
        intent.putExtra("meet", meet)
        startActivityForResult(intent, Constants.VOTE_CODE)
        return true
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_user_info -> {
                // Handle the camera action
                val intent = Intent(this@NowMeetingActivity, UserInfoActivity::class.java)
                startActivityForResult(intent, Constants.NOW_MEETING_CODE)
            }
            R.id.nav_change_meet -> {

                val selfDialog = NormalDialog(this)
                selfDialog.setTitle("切换会议")
                selfDialog.setMessage("确定想要更换当前正在进行的会议吗？\n点击‘确定’将会带您跳转到会议列表")
                selfDialog.setYesClickListener(getString(R.string.yes), object : NormalDialog.YesClickListener {
                    override fun onYesClick() {
//                        disconnectStomp()
                        Toast.makeText(this@NowMeetingActivity, "点击了--确定--按钮", Toast.LENGTH_LONG).show()
                        selfDialog.dismiss()
                        val intent = Intent(this@NowMeetingActivity, MeetListActivity::class.java)
                        intent.putExtra("meet", meet)
                        startActivityForResult(intent, Constants.NOW_MEETING_CODE)
                    }
                })
                selfDialog.setNoClickListener(getString(R.string.no), object : NormalDialog.NoClickListener {
                    override fun onNoClick() {
                        Toast.makeText(this@NowMeetingActivity, "点击了--取消--按钮", Toast.LENGTH_LONG).show()
                        selfDialog.dismiss()
                    }
                })
                selfDialog.show()
//                Toast.makeText(this@NowMeetingActivity, "按钮", Toast.LENGTH_SHORT).show()
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    private var user: User? = null

    /**
     * 导航栏个人信息
     */
    inner class DrawUserTask internal constructor() : AsyncTask<Void, Void, Boolean>() {

        override fun doInBackground(vararg params: Void): Boolean? {
            // TODO: attempt authentication against a network service.
            return try {
                val request = Request.Builder().url("http:dfdf/fdapi/currentUser")
                        .addHeader(Constants.JCM_URL_HEADER, CacheDiskUtils.getInstance().getString(Constants.JCM_TOKEN))
                        .build()
                val call = OkHttpUtil.client.newCall(request)
//                val response = call.execute()
                try {
                    call.execute().use { response ->
                        val resultStr = response.body()?.string()
                        val result = JSONObject(resultStr)
                        LogUtils.i("result:$result")
                        if (result["code"] == 0) {
                            user = User()
                            val userJson = JSONObject(result["data"].toString())
                            user?.id = userJson.getLong("id")
                            user?.username = userJson["username"].toString()
                            user?.realName = userJson["realName"].toString()
                            user?.photo = userJson["photo"].toString()
                            CacheDiskUtils.getInstance().put(Constants.CACHE_USERID, user?.id)
                            MyStomp.connectStomp(meet?.id.toString())
                            if (MyStomp.getStomp() != null) {
                                MyStomp.getStomp()!!.topic("/topic/arthur/law/data")
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe { topicMessage ->
                                            LogUtils.i("object class receive:${topicMessage.payload}")
                                            val receive = "{\"code\":2001,\"msg\":\"发起投票\",\"data\":{\"meeting\":{\"id\":5,\"name\":\"瓦窑堡会议\"},\"vote\":{\"name\":\"固态是否老电脑提升体验的关键\",\"summary\":\"如题\",\"creator\":{\"id\":1,\"realName\":\"super\"},\"multiple\":true,\"participants\":[1,2,3,4,5],\"dateCreated\":\"2018-09-11 15:59:23\",\"item\":[{\"id\":32,\"name\":\"是是是，赶紧买\"},{\"id\":33,\"name\":\"关我吊事\"}]}}}"
                                            handleTopicMessage(receive)
                                        }
                            }
                            return true
                        }
                    }
                } catch (e: IOException) {
                    return false
                }
                false
            } catch (e: InterruptedException) {
                false
            }
        }

        override fun onPostExecute(success: Boolean?) {
            Log.v("", "post execute")
            initDrawUser()
        }

        override fun onCancelled() {

        }
    }

    var navUsername: TextView? = null
    var navUserHead: QMUIRadiusImageView? = null
    private fun initDrawUser() {
        if (user == null) {
            DialogUtil.show(this@NowMeetingActivity, "用户获取失败！", QMUITipDialog.Builder.ICON_TYPE_FAIL)
//            val tipDialog = QMUITipDialog.Builder(this@NowMeetingActivity)
//                    .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
//                    .setTipWord("用户获取失败！")
//                    .create()
//            tipDialog.show()
//            Timer().schedule(object : TimerTask() {
//                override fun run() {
//                    tipDialog.dismiss()
//                }
//            }, 2000)
            return
        }
        navUsername = findViewById(R.id.navUsername)
        navUserHead = findViewById(R.id.navUserHead)

        if (navUsername != null) {
            navUsername?.text = user?.realName
        }
        if (navUserHead != null) {
            Glide.with(this)
                    .load(JCM_URL + user?.photo).apply(RequestOptions().placeholder(R.drawable.user))
                    .into(navUserHead!!)
        }

    }

    private fun initDemoFile() {
        files.add(Stuff(0, "61781299_p0", "jpg", "http://104.224.152.210:8080/pic/61781299_p0.jpg"))
        files.add(Stuff(13, "mp-alice-exec.jar", "jar", "http://104.224.152.210:8080/pic/mp-alice-exec.jar"))
        files.add(Stuff(1, "test_video", "jpg", "http://104.224.152.210:8080/pic/test_video.jpg"))
        files.add(Stuff(10, "场景曲.mp3", "mp3", "http://104.224.152.210:8080/pic/场景曲.mp3"))
        files.add(Stuff(11, "新建文本文档.txt", "txt", "http://104.224.152.210:8080/pic/新建文本文档.txt"))
        files.add(Stuff(12, "PMBOK指南第6版-中文.pdf", "pdf", "http://104.224.152.210:8080/pic/PMBOK指南第6版-中文.pdf"))
        files.add(Stuff(2, "miao.gif", "gif", "http://104.224.152.210:8080/pic/miao.gif"))
        files.add(Stuff(3, "xls.xls", "xls", "http://104.224.152.210:8080/pic/xls.xls"))
        files.add(Stuff(4, "综合管理平台.docx", "docx", "http://104.224.152.210:8080/pic/综合管理平台.docx"))
        files.add(Stuff(5, "oceans", "mp4", "http://104.224.152.210:8080/pic/oceans.mp4"))
        files.add(Stuff(6, "1234.flv", "flv", "http://104.224.152.210:8080/pic/1234.flv"))
        files.add(Stuff(7, "flvplayer.swf", "swf", "http://104.224.152.210:8080/pic/flvplayer.swf"))
        files.add(Stuff(8, "61781299_p0.jpg", "jpg", "http://104.224.152.210:8080/pic/61781299_p0.jpg"))
        files.add(Stuff(9, "test_video.jpg", "jpg", "http://104.224.152.210:8080/pic/test_video.jpg"))
    }
}
