package com.hxht.mobile.committee.activity

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.blankj.utilcode.util.BarUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.hxht.mobile.committee.R
import com.hxht.mobile.committee.adapter.ChooseVoteAdapter
import com.hxht.mobile.committee.common.Constants
import com.hxht.mobile.committee.dialog.NormalDialog
import com.hxht.mobile.committee.entity.Meet
import kotlinx.android.synthetic.main.activity_multiple_vote.*
import kotlinx.android.synthetic.main.content_multiple_vote.*

/**
 * 多选投票activity
 */
class MultipleVoteActivity : AppCompatActivity() {

    private var meet: Meet? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multiple_vote)
        setSupportActionBar(multipleToolbar)
        BarUtils.setStatusBarColor(this, resources.getColor(R.color.colorPrimary, null))

        submitMultiple.setOnClickListener { view ->

        }

        meet = intent.getSerializableExtra("meet") as Meet
        val title = intent.getStringExtra("voteTitle")
        val vote = intent.getStringArrayListExtra("vote")
        if (null == title || null == vote) {
            val builder = AlertDialog.Builder(this@MultipleVoteActivity)
            builder.setTitle("警告 ！")
            builder.setMessage("没有找到投票项，回到上个页面")
            builder.setPositiveButton("好的") { _: DialogInterface, _: Int ->
                builder.show()
                setResult(RESULT_CANCELED, intent)
                finish()
            }
        }

        multipleVoteHint2.text = "请根据下方的投票主题进行投票。"
        initVoteSelect(title, vote)
    }

    private fun initVoteSelect(title: String, vote: ArrayList<String>) {
        multipleVoteTitle.text = title

        // 获取RecyclerView对象
        val chooseVoteChild = findViewById<View>(R.id.multipleVoteChild) as RecyclerView

        // 创建线性布局管理器（默认是垂直方向）
        val layoutManager = GridLayoutManager(this, 1)
        // 为RecyclerView指定布局管理对象
        chooseVoteChild.layoutManager = layoutManager
        val sampleRecyclerAdapter = ChooseVoteAdapter(vote)
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
        val selfDialog = NormalDialog(this)

        selfDialog.setTitle("投票确定")
        selfDialog.setMessage("您选择投票：\n$voteStr")
        selfDialog.setYesClickListener("没错", object : NormalDialog.YesClickListener {
            override fun onYesClick() {
                Toast.makeText(this@MultipleVoteActivity, "点击了--确定--按钮,\n投票了$voteStr,\n感谢您的配合", Toast.LENGTH_LONG).show()
                selfDialog.dismiss()
                val intent = Intent(this@MultipleVoteActivity, NowMeetingActivity::class.java)
                intent.putExtra("meet", meet)
                startActivityForResult(intent, Constants.CHOOSE_VOTE_CODE)
            }
        })
        selfDialog.setNoClickListener("我看错了再选", object : NormalDialog.NoClickListener {
            override fun onNoClick() {
                Toast.makeText(this@MultipleVoteActivity, "点击了--取消--按钮", Toast.LENGTH_LONG).show()
                selfDialog.dismiss()
            }
        })
        selfDialog.show()
    }
}
