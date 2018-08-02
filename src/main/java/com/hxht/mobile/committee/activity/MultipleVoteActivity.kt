package com.hxht.mobile.committee.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.blankj.utilcode.util.BarUtils
import com.hxht.mobile.committee.R
import kotlinx.android.synthetic.main.activity_multiple_vote.*

/**
 * 多选投票activity
 */
class MultipleVoteActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multiple_vote)
        setSupportActionBar(multipleToolbar)
        BarUtils.setStatusBarColor(this, resources.getColor(R.color.colorPrimary, null))

        submitMultiple.setOnClickListener { view ->

        }
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
