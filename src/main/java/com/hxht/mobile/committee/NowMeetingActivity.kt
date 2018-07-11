package com.hxht.mobile.committee

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.chad.library.adapter.base.BaseQuickAdapter
import com.daimajia.numberprogressbar.NumberProgressBar
import com.hxht.mobile.committee.R.id.number_progress_bar
import com.hxht.mobile.committee.activity.ExpPlayerActivity
import com.hxht.mobile.committee.adapter.NowMeetingStuffAdapter
import com.hxht.mobile.committee.common.Constants
import com.hxht.mobile.committee.dialog.MyImageDialog
import com.hxht.mobile.committee.entity.Meet
import com.hxht.mobile.committee.entity.Stuff
import com.hxht.mobile.committee.utils.MimeUtil
import com.hxht.mobile.committee.utils.StorageUtil
import com.tbruyelle.rxpermissions2.RxPermissions
import com.yanzhenjie.kalle.Canceller
import com.yanzhenjie.kalle.Headers
import com.yanzhenjie.kalle.Kalle
import com.yanzhenjie.kalle.download.Callback
import com.yanzhenjie.kalle.download.Download
import kotlinx.android.synthetic.main.now_meeting.*
import kotlinx.android.synthetic.main.now_meeting_app_bar.*
import kotlinx.android.synthetic.main.now_meeting_content.*
import java.io.Serializable
import java.util.*


class NowMeetingActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var recyclerView: RecyclerView
    private val files = arrayListOf<Stuff>()
    private var meet: Meet? = null
    private val PERMISSIONS_STORAGE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private val REQUEST_EXTERNAL_STORAGE = 1
    private var rxPermissions: RxPermissions? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.now_meeting)
        setSupportActionBar(nowMeetingToolbar)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, nowMeetingToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        rxPermissions = RxPermissions(this)
        grantPermissions()
        nav_view.setNavigationItemSelectedListener(this)

        meet = intent.getSerializableExtra("meet") as Meet
        if (null != meet) {
            nowTitle.text = meet?.meetName
        }
        initDemoFile()
        initDemoVote()
        val btn = findViewById<Button>(R.id.nowMeetingBtn)
        btn.setOnClickListener {
            Log.i("info", "cancel btn")
            Kalle.Download.cancel(cancelTag)
        }
        val createVoteBtn = findViewById<Button>(R.id.createVote)
        createVoteBtn.setOnClickListener {
            Log.i("info", "createVoteBtn btn")
//            val createVoteDialog = VoteDialog(this, "王老五越狱案")
//            createVoteDialog.setMessage("王老五越狱案")
//            createVoteDialog.show()
            val intent = Intent(this@NowMeetingActivity, VoteActivity::class.java)
            intent.putExtra("id", "336699999x")
            intent.putExtra("meet", meet)
            startActivityForResult(intent, 0)
        }
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
                    startActivityForResult(intent, 0)
                }
                "mp4", "flv", "rmvb", "avi" -> {
                    val intent = Intent(this@NowMeetingActivity, ExpPlayerActivity::class.java)
                    intent.putExtra("url", stuff.fileAddress)
                    startActivityForResult(intent, 0)
                }
                "swf" -> {
                    Toast.makeText(this, "视频格式暂不支持！", Toast.LENGTH_LONG).show()
                }
                "jpg", "png", "gif" -> {
                    //点击查看大图
                    val imageDialog = MyImageDialog(this, stuff.fileAddress)
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
                files.add(Stuff(Random().nextLong(), "test_video.jpg", "jpg", "http://104.224.152.210:8080/pic/test_video.jpg"))
                files.add(Stuff(Random().nextLong(), "test_video.jpg", "jpg", "http://104.224.152.210:8080/pic/test_video.jpg"))
                files.add(Stuff(Random().nextLong(), "test_video.jpg", "jpg", "http://104.224.152.210:8080/pic/test_video.jpg"))
                files.add(Stuff(Random().nextLong(), "test_video.jpg", "jpg", "http://104.224.152.210:8080/pic/test_video.jpg"))
                files.add(Stuff(Random().nextLong(), "test_video.jpg", "jpg", "http://104.224.152.210:8080/pic/test_video.jpg"))
                if (true) {
                    if (files.size > 30) {
                        sampleRecyclerAdapter.loadMoreEnd()
                    } else {
                        sampleRecyclerAdapter.loadMoreComplete()
                    }
                } else {
                    sampleRecyclerAdapter.loadMoreFail()
                }
            }, Constants.DELAY_TIME)
        }, recyclerView)
//        sampleRecyclerAdapter.openLoadMore(PAGE_SIZE, true);
        sampleRecyclerAdapter.openLoadAnimation()
        // 没有数据的时候默认显示该布局
        val view = this.layoutInflater.inflate(R.layout.empty_view, null)
        view.findViewById<TextView>(R.id.emptyTextHint).text = "没有找到案件相关资料"
        sampleRecyclerAdapter.emptyView = view
        recyclerView.adapter = sampleRecyclerAdapter

    }

    override fun onDestroy() {
        super.onDestroy()
        Kalle.Download.cancel(cancelTag)
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_camera -> {
                // Handle the camera action
            }
            R.id.nav_manage -> {

            }
            R.id.nav_send -> {

            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
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

    private fun initDemoVote() {
        var voteTitle = intent.getStringExtra("voteTitle")
//        voteTitle = "好吧这次就决定审判火箭队了！"
        val vote = intent.getStringArrayListExtra("vote")
//        val vote = arrayListOf("还行，就判他五年 ","不行，这么认真的队伍要无罪释放 "," 爱咋咋地我弃权")
        if (vote != null) {
            val builder = AlertDialog.Builder(this@NowMeetingActivity)
            builder.setCancelable(false)
            builder.setTitle("发现投票项")
            builder.setMessage("有人发起了投票\n将会带你跳转到新页面进行投票。")
            builder.setPositiveButton("好的") { _: DialogInterface, _: Int ->
                Log.i("info", "okle ")
                val temp = Intent(this@NowMeetingActivity, ChooseVoteActivity::class.java)
                temp.putExtra("voteTitle", voteTitle)
                temp.putExtra("vote", vote)
                intent.putExtra("meet", meet)
                startActivityForResult(temp, 0)
            }
            builder.show()
        }
    }
}
