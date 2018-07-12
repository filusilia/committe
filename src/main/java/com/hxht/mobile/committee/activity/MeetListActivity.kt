package com.hxht.mobile.committee.activity

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.chad.library.adapter.base.BaseQuickAdapter
import com.hxht.mobile.committee.R
import com.hxht.mobile.committee.adapter.MeetListAdapter
import com.hxht.mobile.committee.common.Constants
import com.hxht.mobile.committee.dialog.NormalDialog
import com.hxht.mobile.committee.entity.Meet
import kotlinx.android.synthetic.main.meet_list.*
import java.util.*


class MeetListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    private val meets = ArrayList<Meet>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.meet_list)
        setSupportActionBar(meetToolbar)
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimaryDark)
        //透明状态
//        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        //透明导航
//        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        initAdapter()

        // 获取RecyclerView对象
        recyclerView = findViewById<View>(R.id.my_recycler_view) as RecyclerView

        // 创建线性布局管理器（默认是垂直方向）
        val layoutManager = GridLayoutManager(this, 2)
        // 为RecyclerView指定布局管理对象
        recyclerView.layoutManager = layoutManager
        // 创建Adapter
        val sampleRecyclerAdapter = MeetListAdapter(meets)
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
                Log.i("info", "load yes")
                meets.add(Meet("现在，我们将会对张老三进行正义的审判！", Date()))
                meets.add(Meet("现在，我们将会对张老三进行正义的审判！", Date()))
                meets.add(Meet("现在，我们将会对张老三进行正义的审判！", Date()))
                meets.add(Meet("现在，我们将会对张老三进行正义的审判！", Date()))
                meets.add(Meet("现在，我们将会对张老三进行正义的审判！", Date()))
                if (meets.size>10) {
                    sampleRecyclerAdapter.loadMoreEnd()
                } else {
                    sampleRecyclerAdapter.loadMoreComplete()
//                    sampleRecyclerAdapter.loadMoreFail()
                }
            }, Constants.DELAY_TIME)
        }, recyclerView)
        sampleRecyclerAdapter.openLoadAnimation()
//        // 当列表滑动到倒数第N个Item的时候(默认是1)回调onLoadMoreRequested方法
//        sampleRecyclerAdapter.setPreLoadNumber(int);
        // 没有数据的时候默认显示该布局
        val view = this.layoutInflater.inflate(R.layout.empty_view,null)
        view.findViewById<TextView>(R.id.emptyTextHint).text = "没有找到案件"
        sampleRecyclerAdapter.emptyView = view
        recyclerView.adapter = sampleRecyclerAdapter
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    private fun meetGo(meet:Meet) {
        val selfDialog = NormalDialog(this)

        selfDialog.setTitle("选择会议")
        selfDialog.setMessage("将会开启会议：\n${meet.meetName}")
        selfDialog.setYesClickListener("没错", object : NormalDialog.YesClickListener {
            override fun onYesClick() {
                selfDialog.dismiss()
                val intent = Intent(this@MeetListActivity, NowMeetingActivity::class.java)
                intent.putExtra("meet", meet)
                startActivityForResult(intent, 0)
            }
        })
        selfDialog.setNoClickListener("好像不对", object : NormalDialog.NoClickListener {
            override fun onNoClick() {
                Toast.makeText(this@MeetListActivity, "点击了--取消--按钮", Toast.LENGTH_LONG).show();
                selfDialog.dismiss()
            }
        })
        selfDialog.show()
    }

    private fun initAdapter() {
        meets.add(Meet("现在，我们将会对张老三进行正义的审判！", Date()))
        meets.add(Meet("好的，高举人民的旗帜，为我国社会主义现代化贡献力量！", Date()))
        meets.add(Meet("mac版本Termius图形化命令行工具的使用", Date()))
        meets.add(Meet("2011年8月12日，历经三年的研发时间、三次平台切换，终于迎来了国产第一款侧滑Android手机，OPPOFindX903。", Date()))
    }
}