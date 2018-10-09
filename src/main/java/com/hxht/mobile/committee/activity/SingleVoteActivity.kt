package com.hxht.mobile.committee.activity

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.TextView
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.CacheDiskUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.hxht.mobile.committee.R
import com.hxht.mobile.committee.adapter.SingleVoteAdapter
import com.hxht.mobile.committee.common.Constants
import com.hxht.mobile.committee.dialog.NormalDialog
import com.hxht.mobile.committee.entity.Meet
import com.hxht.mobile.committee.entity.Vote
import com.hxht.mobile.committee.utils.DialogUtil
import com.hxht.mobile.committee.utils.OkHttpUtil
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog
import kotlinx.android.synthetic.main.activity_choose_vote.*
import kotlinx.android.synthetic.main.content_choose_vote.*
import okhttp3.FormBody
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.*


class SingleVoteActivity : AppCompatActivity() {

    private var meet: Meet? = null
    private var vote: Vote? = null
    private var okVoteTask: OkVoteTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_vote)
        includeToolbar.title = "选择投票"
        setSupportActionBar(includeToolbar)
        BarUtils.setStatusBarColor(this, resources.getColor(R.color.colorPrimary, null))
//        complete.setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                    .setAction("Action", null).show()
//        }
        try {
            meet = intent.getSerializableExtra("meet") as Meet
            vote = intent.getSerializableExtra("vote") as Vote
            chooseVoteHint2.text = "请根据下方的投票主题进行投票。"
            chooseVoteTitle.text = vote?.title
            initVoteSelect(vote!!.item!!)
        } catch (e: Exception) {
            val builder = AlertDialog.Builder(this@SingleVoteActivity)
            builder.setTitle("警告 ！")
            builder.setMessage("投票出现问题，现在将会返回上一个页面")
            builder.setPositiveButton("好的") { _: DialogInterface, _: Int ->
                builder.show()
                setResult(RESULT_CANCELED, intent)
                finish()
            }
        }
    }

    private fun initVoteSelect(item: String) {
        val arr = JSONArray(item)
        val list = ArrayList<String>()
        for (i in 0..(arr.length() - 1)) {
            list.add(arr.getString(i))
        }
        // 获取RecyclerView对象
        val chooseVoteChild = findViewById<View>(R.id.chooseVoteChild) as RecyclerView

        // 创建线性布局管理器（默认是垂直方向）
        val layoutManager = GridLayoutManager(this, 1)
        // 为RecyclerView指定布局管理对象
        chooseVoteChild.layoutManager = layoutManager
        val sampleRecyclerAdapter = SingleVoteAdapter(list)
        sampleRecyclerAdapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
            val voteStr = sampleRecyclerAdapter.getItem(position)
            okVote(voteStr)
        }
        sampleRecyclerAdapter.setOnItemChildClickListener { adapter, view, position ->
            val voteStr = sampleRecyclerAdapter.getItem(position)
            okVote(voteStr)
        }
        sampleRecyclerAdapter.openLoadAnimation()
        // 没有数据的时候默认显示该布局
        val view = this.layoutInflater.inflate(R.layout.activity_empty, null)
        view.findViewById<TextView>(R.id.emptyTextHint).text = "投票项！"
        sampleRecyclerAdapter.emptyView = view
        chooseVoteChild.adapter = sampleRecyclerAdapter
    }

    private fun okVote(voteStr: String?) {
        val voteJson = JSONObject(voteStr)
//        val map = mapOf(voteJson.getLong("id") to voteJson.getString("name"))
        val selfDialog = NormalDialog(this)
        selfDialog.setTitle("投票确定")
        selfDialog.setMessage("您选择投票：\n  ${voteJson.getString("name")}")
        selfDialog.setYesClickListener(getString(R.string.yes), object : NormalDialog.YesClickListener {
            override fun onYesClick() {
                okVoteTask = OkVoteTask(voteJson.getString("id"))
                okVoteTask?.execute()
                selfDialog.dismiss()
//                Toast.makeText(this@SingleVoteActivity, "点击了--确定--按钮,\n投票了$voteStr,\n感谢您的配合", Toast.LENGTH_LONG).show()
            }
        })
        selfDialog.setNoClickListener(getString(R.string.no), object : NormalDialog.NoClickListener {
            override fun onNoClick() {
                selfDialog.dismiss()
            }
        })
        selfDialog.show()
    }


    inner class OkVoteTask internal constructor(val param: String?) : AsyncTask<Void, Void, Boolean>() {
        //        private val tipDialog = QMUITipDialog.Builder(this@SingleVoteActivity)
//                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
//                .create()
        var message = "感谢您的投票"

        override fun doInBackground(vararg params: Void): Boolean? {
            // TODO: attempt authentication against a network service.
            return try {
                if (param == null) return false
//                tipDialog.show()
                val formBody = FormBody.Builder()
                        .add("meeting", meet?.id.toString())
                        .add("item", param)
//                val requestBody = RequestBody.create(OkHttpUtil.JSON, param.toString())
                val request = Request.Builder().url("${Constants.JCM_URL}api/vote")
                        .addHeader(Constants.JCM_URL_HEADER, CacheDiskUtils.getInstance().getString(Constants.JCM_TOKEN))
                        .post(formBody.build())
                        .build()
                val call = OkHttpUtil.client.newCall(request)
                try {
                    call.execute().use { response ->
                        if (response.code() == 200) {
                            val resultStr = response.body()?.string()
                            val result = JSONObject(resultStr)
                            if (result["code"] == 0) {
//                        tipDialog.dismiss()
                                return true
                            }
                            if (result["code"] == 411) {
                                message = "您已经投过票啦！"
                                return true
                            }
                        }
                    }
                } catch (e: IOException) {
                    return false
                }
//                tipDialog.dismiss()
                false
            } catch (e: InterruptedException) {
                false
            }
        }

        override fun onPostExecute(success: Boolean?) {
            Log.v("", "post execute")
            if (success == true) {
                val intent = Intent(this@SingleVoteActivity, NowMeetingActivity::class.java)
                intent.putExtra("meet", meet)
                intent.putExtra("vote", vote)
                setResult(Constants.SINGLE_VOTE_CODE, intent)
                finish()
            } else {
                val intent = Intent(this@SingleVoteActivity, NowMeetingActivity::class.java)
                intent.putExtra("meet", meet)
                intent.putExtra("vote", vote)
                setResult(Constants.SINGLE_VOTE_CODE, intent)
                finish()
            }
        }

        override fun onCancelled() {

        }
    }
}
