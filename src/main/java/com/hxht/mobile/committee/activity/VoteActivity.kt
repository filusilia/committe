package com.hxht.mobile.committee.activity

import android.content.Intent
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import com.blankj.utilcode.util.BarUtils
import com.chaychan.viewlib.PowerfulEditText
import com.hxht.mobile.committee.R
import com.hxht.mobile.committee.dialog.NormalDialog
import com.hxht.mobile.committee.entity.Meet
import kotlinx.android.synthetic.main.activity_vote.*
import org.jetbrains.anko.forEachChild


class VoteActivity : AppCompatActivity() {
    private var meet: Meet? = null
    private var constraintLayout: ConstraintLayout? = null
    //    private var scrollView: ScrollView? = null
    private var voteScrollViewChild: LinearLayout? = null
    private var powerfulEditText: PowerfulEditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vote)
        toolbar.title = "创建投票"
        setSupportActionBar(toolbar)
        BarUtils.setStatusBarColor(this, resources.getColor(R.color.colorPrimary, null))

        meet = intent.getSerializableExtra("meet") as Meet
        fab.setOnClickListener { view ->
            if (TextUtils.isEmpty(powerfulEditText?.text)) {
                Snackbar.make(view, "请填写本次的标题", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show()
                return@setOnClickListener
            }
            val arrayList = arrayListOf<String>()
            voteScrollViewChild?.forEachChild { view ->
                val temp = view as PowerfulEditText
                if (!TextUtils.isEmpty(temp.text)) {
                    arrayList.add(temp.text.toString())
                }
            }
            if (arrayList.size == 0) {
                Snackbar.make(view, "请填写投票项", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show()
                return@setOnClickListener
            }
            val hintDialog = NormalDialog(this)
            hintDialog.setTitle(powerfulEditText?.text.toString())
            hintDialog.setMessage("您创建的投票选项为：$arrayList")
            hintDialog.setYesClickListener("确定", object : NormalDialog.YesClickListener {
                override fun onYesClick() {
                    Toast.makeText(this@VoteActivity, "点击了--确定--按钮", Toast.LENGTH_LONG).show()
                    hintDialog.dismiss()
                    val intent = Intent(this@VoteActivity, NowMeetingActivity::class.java)
                    intent.putExtra("voteTitle", powerfulEditText?.text.toString())
                    intent.putExtra("vote", arrayList)
                    intent.putExtra("meet", meet)
                    startActivityForResult(intent, 0)
                }
            })
            hintDialog.setNoClickListener("取消", object : NormalDialog.NoClickListener {
                override fun onNoClick() {
                    Toast.makeText(this@VoteActivity, "点击了--取消--按钮", Toast.LENGTH_LONG).show()
                    hintDialog.dismiss()
                }
            })

            hintDialog.show()
            Log.i(powerfulEditText?.text.toString(), arrayList.toString())
        }
        constraintLayout = this.findViewById(R.id.voteLayout)
//        scrollView = findViewById(R.id.voteScrollView)
        voteScrollViewChild = findViewById(R.id.voteScrollViewChild)
        powerfulEditText = findViewById(R.id.voteEditTitle)

        val url = intent.getStringExtra("url")
        powerfulEditText?.setOnRightClickListener { p0 ->
            Log.w("tag", p0?.id.toString())
            p0.isFocusable = true
            p0.isFocusableInTouchMode = true
            addVote()
        }
    }

    private fun addVote() {
//        val powerfulEditTextChild = EditText(this)
        val powerfulEditTextChild = PowerfulEditText(this)
        powerfulEditTextChild.id = View.generateViewId()
//        powerfulEditTextChild.hint = getString(R.string.createVoteDialogHint)
        powerfulEditTextChild.hint = "请填写本投票项"
        powerfulEditTextChild.textSize = 18.00f
//        powerfulEditTextChild.right = R.drawable.ic_dialog_subtract
//        powerfulEditTextChild.setCompoundDrawablesWithIntrinsicBounds(null, null, resources.getDrawable(R.drawable.ic_dialog_subtract, null), null)

        val rightDrawable = resources.getDrawable(R.drawable.ic_dialog_subtract, null)
        rightDrawable.setBounds(0, 0, 60, 60)
        powerfulEditTextChild.setCompoundDrawables(null, null, rightDrawable, null)
        powerfulEditTextChild.setOnRightClickListener { param ->
            //获取当前控件的位置，如果是最后一个不处理，如果在上面需要把下面的控件布局位置修改。
            //不需要单独判断最上面，因为最上面的主题也是powerEdit
//            val last: Int = (voteScrollViewChild?.childCount ?: 1) - 1
//            val now = voteScrollViewChild!!.indexOfChild(param)
//            if (now != last) {
//                val up = voteScrollViewChild?.getChildAt(now - 1) as PowerfulEditText
//                val down = voteScrollViewChild?.getChildAt(now + 1) as PowerfulEditText

//                val tempConstraintSet = ConstraintSet()
//                tempConstraintSet.clone(voteScrollViewChild)
//                tempConstraintSet.constrainHeight(down.id, ConstraintLayout.LayoutParams.WRAP_CONTENT)
//                tempConstraintSet.constrainWidth(down.id, ConstraintLayout.LayoutParams.MATCH_PARENT)
//                tempConstraintSet.connect(down.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 256)
//                tempConstraintSet.connect(down.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 512)
//                tempConstraintSet.connect(down.id, ConstraintSet.TOP, up.id, ConstraintSet.BOTTOM)
//                tempConstraintSet.applyTo(voteScrollViewChild)
//            }
            voteScrollViewChild!!.removeView(powerfulEditTextChild)
        }
        powerfulEditTextChild.requestFocus()
        voteScrollViewChild?.addView(powerfulEditTextChild)
        //获取当前activity共有多少控件
        val last: Int = voteScrollViewChild?.childCount ?: 1
//        powerfulEditTextChild.id = R.id.powerfulEditTextChild
        //为什么减2，因为上一句又添加了一个控件
        val lastView = voteScrollViewChild?.getChildAt(last - 2)
        Log.i("tag", "lastview id: ${lastView?.id}")
        val constraintSet = ConstraintSet()
//        constraintSet.clone(voteScrollViewChild)
//        constraintSet.constrainHeight(powerfulEditTextChild.id, ConstraintLayout.LayoutParams.WRAP_CONTENT)
//        constraintSet.constrainWidth(powerfulEditTextChild.id, ConstraintLayout.LayoutParams.MATCH_PARENT)
//        constraintSet.connect(powerfulEditTextChild.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 256)
//        constraintSet.connect(powerfulEditTextChild.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 512)
//        constraintSet.connect(powerfulEditTextChild.id, ConstraintSet.TOP, lastView!!.id, ConstraintSet.BOTTOM)
//        constraintSet.applyTo(voteScrollViewChild)
    }
}
