package com.hxht.mobile.committee.activity

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.blankj.utilcode.util.CacheDiskUtils
import com.blankj.utilcode.util.LogUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.hxht.mobile.committee.R
import com.hxht.mobile.committee.adapter.MeetListAdapter
import com.hxht.mobile.committee.common.Constants
import com.hxht.mobile.committee.dialog.NormalDialog
import com.hxht.mobile.committee.entity.Meet
import com.hxht.mobile.committee.utils.OkHttpUtil
import kotlinx.android.synthetic.main.activity_meet_list.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.*


class MeetListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    private val meets = ArrayList<Meet>()
    private var nowMeeting: Boolean? = null

    private var adapterTask: MeetListTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meet_list)
        setSupportActionBar(meetToolbar)
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimaryDark)

        val meet = intent.getSerializableExtra("meet")
        nowMeeting = meet != null
        //透明状态
//        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        //透明导航
//        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        adapterTask = MeetListTask()
        adapterTask?.execute()
    }
    private var firstTime = 0L
    override fun onBackPressed() {
        if (nowMeeting == true) {
            val selfDialog = NormalDialog(this)
            selfDialog.setTitle("返回之前的会议")
            selfDialog.setMessage("确定想要退回到刚才的会议吗？")
            selfDialog.setYesClickListener("是的", object : NormalDialog.YesClickListener {
                override fun onYesClick() {
                    back()
                    selfDialog.dismiss()
                }
            })
            selfDialog.setNoClickListener("取消", object : NormalDialog.NoClickListener {
                override fun onNoClick() {
                    selfDialog.dismiss()
                }
            })
            selfDialog.show()

        } else {
            val secondTime = System.currentTimeMillis()
            LogUtils.i(secondTime)
            if (secondTime - firstTime > 2000) {
                Toast.makeText(this@MeetListActivity, "再按一次返回到登录页面", Toast.LENGTH_SHORT).show()
                firstTime = secondTime
            } else {
                finish()
                System.exit(0)
            }
        }
    }

    fun back() {
        super.onBackPressed()
    }

    private var page = 1
    private var pageSize = 10

    inner class MeetListTask internal constructor() : AsyncTask<Void, Void, Boolean>() {

        override fun doInBackground(vararg params: Void): Boolean? {
            // TODO: attempt authentication against a network service.
            // http 请求登录在这里
            return try {
                val request = Request.Builder().url("${Constants.JCM_URL}api/meeting?page=$page&pageSize=$pageSize")
                        .addHeader(Constants.JCM_URL_HEADER, CacheDiskUtils.getInstance().getString(Constants.JCM_TOKEN))
                        .build()
                val call = OkHttpUtil.client.newCall(request)
                val response = call.execute()
                if (response.code() == 200) {
                    val resultStr = response.body()?.string()
                    val result = JSONObject(resultStr)
                    if (result["code"] == 0) {
                        val meetStr = JSONArray(result["data"].toString())
                        for (i in 0 until meetStr.length()) {
                            val temp = meetStr.getJSONObject(i)
                            val meetTemp = Meet(temp["id"].toString().toInt())
                            meetTemp.meetName = temp["name"].toString()
                            meetTemp.meetCover = temp["logo"].toString()
                            meetTemp.summary = temp["summary"].toString()
//                                            meetTemp.summary =temp["summary"].toString()
                            meets.add(meetTemp)
                        }
                        true
                    }
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
        recyclerView = findViewById<View>(R.id.my_recycler_view) as RecyclerView

        // 创建线性布局管理器（默认是垂直方向）
        val layoutManager = GridLayoutManager(this, 2)
        // 为RecyclerView指定布局管理对象
        recyclerView.layoutManager = layoutManager
        // 创建Adapter
        val sampleRecyclerAdapter = MeetListAdapter(meets, this)
//        sampleRecyclerAdapter.setEmptyView(getView());
        sampleRecyclerAdapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
            Log.d("", "onItemClick: ")
            val meet = sampleRecyclerAdapter.getItem(position)
            if (meet != null) {
                meetGo(meet)
            }
            Toast.makeText(this, "onItemClick$position", Toast.LENGTH_SHORT).show()
        }
        sampleRecyclerAdapter.setOnItemChildClickListener { adapter, view, position ->
            Log.d("", "onItemChildClick: ")
            val meet = sampleRecyclerAdapter.getItem(position)
            if (meet != null) {
                meetGo(meet)
            }
        }
        sampleRecyclerAdapter.setOnLoadMoreListener({
            /**
             * 上滑加载更多
             */
            recyclerView.postDelayed({
                if (meets.size < 10) {
                    sampleRecyclerAdapter.loadMoreEnd()
                } else {
                    page++

                    val request = Request.Builder().url("${Constants.JCM_URL}api/meeting?page=$page&pageSize=$pageSize")
                            .addHeader(Constants.JCM_URL_HEADER, CacheDiskUtils.getInstance().getString(Constants.JCM_TOKEN))
                            .build()
                    val call = OkHttpUtil.client.newCall(request)
                    call.enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            LogUtils.i("onFailure: ")
                            sampleRecyclerAdapter.loadMoreFail()
                        }

                        @Throws(IOException::class)
                        override fun onResponse(call: Call, response: Response) {
//                        LogUtils.i("onResponse: " + response.body()?.string()+"${response.code()}")
                            if (response.code() == 200) {
                                val resultStr = response.body()?.string()
                                val result = JSONObject(resultStr)
                                if (result["code"] == 0) {
                                    if (result["data"] == null) sampleRecyclerAdapter.loadMoreEnd()
                                    val meetStr = JSONArray(result["data"].toString())
                                    for (i in 0 until meetStr.length()) {
                                        val temp = meetStr.getJSONObject(i)
                                        val meetTemp = Meet(temp["id"].toString().toInt())
                                        meetTemp.meetName = temp["name"].toString()
                                        meetTemp.meetCover = temp["logo"].toString()
                                        meetTemp.summary = temp["summary"].toString()
//                                            meetTemp.summary =temp["summary"].toString()
                                        meets.add(meetTemp)
                                    }
//                                        val meetStr = Meet(meetStr)
                                }
                                sampleRecyclerAdapter.loadMoreComplete()
                            }
                        }
                    })

//                    sampleRecyclerAdapter.loadMoreFail()
                }
            }, Constants.DELAY_TIME)
        }, recyclerView)
        sampleRecyclerAdapter.openLoadAnimation()
//        // 当列表滑动到倒数第N个Item的时候(默认是1)回调onLoadMoreRequested方法
//        sampleRecyclerAdapter.setPreLoadNumber(int);
        // 没有数据的时候默认显示该布局
        val view = this.layoutInflater.inflate(R.layout.activity_empty, null)
        view.findViewById<TextView>(R.id.emptyTextHint).text = "没有找到案件"
        sampleRecyclerAdapter.emptyView = view
        recyclerView.adapter = sampleRecyclerAdapter
    }

    private fun meetGo(meet: Meet) {
        val selfDialog = NormalDialog(this)

        selfDialog.setTitle("选择会议")
        selfDialog.setMessage("将会开启会议：\n${meet.meetName}")
        selfDialog.setYesClickListener("没错", object : NormalDialog.YesClickListener {
            override fun onYesClick() {
                selfDialog.dismiss()
                val intent = Intent(this@MeetListActivity, NowMeetingActivity::class.java)
                intent.putExtra("meet", meet)
                startActivityForResult(intent, Constants.MEETING_LIST_CODE)
            }
        })
        selfDialog.setNoClickListener("好像不对", object : NormalDialog.NoClickListener {
            override fun onNoClick() {
                Toast.makeText(this@MeetListActivity, "点击了--取消--按钮", Toast.LENGTH_LONG).show()
                selfDialog.dismiss()
            }
        })
        selfDialog.show()
    }

    /**
     * 菜单方法
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        onBackPressed()
        return super.onOptionsItemSelected(item)
    }

    private fun demoAdapter() {
        meets.add(Meet("现在，我们将会对张老三进行正义的审判！"))
        meets.add(Meet("好的，高举人民的旗帜，为我国社会主义现代化贡献力量！"))
        meets.add(Meet("mac版本Termius图形化命令行工具的使用"))
        meets.add(Meet("2011年8月12日，历经三年的研发时间、三次平台切换，终于迎来了国产第一款侧滑Android手机，OPPOFindX903。"))
    }
}