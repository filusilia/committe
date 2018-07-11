package com.hxht.mobile.committee.activity

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.activity_exp_player.*
import com.shuyu.gsyvideoplayer.utils.OrientationUtils
import com.hxht.mobile.committee.R
import com.shuyu.gsyvideoplayer.GSYVideoManager


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class ExpPlayerActivity : AppCompatActivity() {

    private var orientationUtils: OrientationUtils? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_exp_player)
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        orientationUtils = OrientationUtils(this, videoPlayer)
        val url = intent.getStringExtra("url")
        val name = intent.getStringExtra("name")

        val uri = Uri.parse(url)

        videoPlayer.setUp(url, true, name)
        //设置返回键
        videoPlayer.backButton.visibility = View.VISIBLE

        //设置全屏按键功能,这是使用的是选择屏幕，而不是全屏
        videoPlayer.fullscreenButton.setImageResource(videoPlayer.shrinkImageRes)
        videoPlayer.fullscreenButton.setOnClickListener { orientationUtils!!.resolveByClick() }
        //是否可以滑动调整
        videoPlayer.setIsTouchWiget(true)
        //设置返回按键功能
        videoPlayer.backButton.setOnClickListener { onBackPressed() }
        videoPlayer.startPlayLogic()


        //准备播放视频
        // step1. 创建一个默认的TrackSelector
        val mainHandler = Handler()

        // 创建带宽
        val bandwidthMeter = DefaultBandwidthMeter()

        // 创建轨道选择工厂
        val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory(bandwidthMeter)

        // 创建轨道选择器实例
        val trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)

        //step2. 创建播放器
        val player = ExoPlayerFactory.newSimpleInstance(this, trackSelector)

//        expPlay.player = player

        // 测量播放带宽，如果不需要可以传null
        val defaultBandwidthMeter = DefaultBandwidthMeter()

        // 创建加载数据的工厂
        val dataSourceFactory = DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "yourApplicationName"), bandwidthMeter)

        // 创建解析数据的工厂
        val extractorsFactory = DefaultExtractorsFactory()

//                    val uri = Uri()
        // 传入Uri、加载数据的工厂、解析数据的工厂，就能创建出MediaSource
        val videoSource = ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(uri)

        // Prepare
        player.prepare(videoSource)
    }


    override fun onPause() {
        super.onPause()
        videoPlayer.onVideoPause()
    }

    override fun onResume() {
        super.onResume()
        videoPlayer.onVideoResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        GSYVideoManager.releaseAllVideos()
        orientationUtils!!.releaseListener()
    }

    override fun onBackPressed() {
        //先返回正常状态
//        if (orientationUtils!!.screenType == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
//            videoPlayer.fullscreenButton.performClick()
//            return
//        }
        //释放所有
        videoPlayer.setVideoAllCallBack(null)
        super.onBackPressed()
    }

    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private val UI_ANIMATION_DELAY = 300
    }

}
