package com.hxht.mobile.committee.activity

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import com.hxht.mobile.committee.R

class EmptyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_empty)
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimaryDark)

    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}