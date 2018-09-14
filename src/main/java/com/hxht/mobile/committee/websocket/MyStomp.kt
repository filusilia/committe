package com.hxht.mobile.committee.websocket

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import com.blankj.utilcode.util.ActivityUtils.startActivityForResult
import com.blankj.utilcode.util.CacheDiskUtils
import com.blankj.utilcode.util.LogUtils
import com.hxht.mobile.committee.activity.MultipleVoteActivity
import com.hxht.mobile.committee.activity.SingleVoteActivity
import com.hxht.mobile.committee.common.Constants
import com.hxht.mobile.committee.entity.Vote
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import ua.naiksoftware.stomp.LifecycleEvent
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.client.StompClient
import java.util.*

object MyStomp {
    /**
     * websocket
     */
    private var mStompClient: StompClient? = null
    private var meetId: String? = null
//    private var viewContext:Context = null

    fun getStomp(): StompClient? {
        return mStompClient
    }

    fun connectStomp(meetId: String?) {
        connectStomp(false, meetId)
    }

    fun connectStomp(reload: Boolean, meetId: String?) {
        this.meetId = meetId
        val url = "ws://${Constants.JCM_IP}/stomp"
        when (reload) {
            true -> {
                try {
                    if (mStompClient != null)
                        mStompClient?.disconnect()
                } catch (e: Exception) {
                    LogUtils.e("webSocket准备断开，信息：${e.message}")
                }
                LogUtils.i("webSocket 准备就绪 $url")
                mStompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, url)
                stompClient()
            }
            false -> {
                LogUtils.i("webSocket 准备就绪 $url")
                if (mStompClient == null) {
                    mStompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, url)
                }
                if (mStompClient!!.isConnected || mStompClient!!.isConnecting) {
                    return
                }
                stompClient()
            }
        }

    }

    private fun disconnectStomp() {
        mStompClient?.disconnect()
    }

    private fun stompClient() {
        val timer = Timer()
        mStompClient!!.lifecycle()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe { lifecycleEvent ->
                    when (lifecycleEvent.type) {
                        LifecycleEvent.Type.OPENED -> {
                            LogUtils.i("OPENED")
                            timer.schedule(object : TimerTask() {
                                override fun run() {
                                    sendEchoViaStomp()
                                }
                            }, 0, 15000)
                        }
                        LifecycleEvent.Type.CLOSED -> {
                            LogUtils.i("CLOSED", lifecycleEvent.message)
                            timer.cancel()
                        }
                        LifecycleEvent.Type.ERROR -> {
                            LogUtils.i("Stomp connection error", lifecycleEvent.exception)
                            timer.cancel()
                            Timer().schedule(object : TimerTask() {
                                override fun run() {
                                    connectStomp(meetId)
                                }
                            }, 30000)
                        }
                        else -> {
                            LogUtils.i("未知状态")
                        }
                    }

                }
        //
        mStompClient!!.topic("/topic/arthur/law/data")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { topicMessage ->
                    LogUtils.i("object class receive:${topicMessage.payload}")
                    val receive = "{\"code\":2001,\"msg\":\"发起投票\",\"data\":{\"meeting\":{\"id\":6,\"name\":\"瓦窑堡会议\"},\"vote\":{\"name\":\"固态是否老电脑提升体验的关键\",\"summary\":\"如题\",\"creator\":{\"id\":1,\"realName\":\"super\"},\"multiple\":false,\"participants\":[1,2,3,4,5],\"dateCreated\":\"2018-09-11 15:59:23\",\"item\":[{\"id\":32,\"name\":\"是是是，赶紧买\"},{\"id\":33,\"name\":\"关我吊事\"}]}}}"
//                    handleTopicMessage(receive)
                }
        mStompClient!!.connect()
    }

    private fun sendEchoViaStomp() {
        val param = JSONObject()
        param.put("meeting", meetId)
        param.put("token", CacheDiskUtils.getInstance().getString(Constants.JCM_TOKEN))
//        LogUtils.i(param.toString())
        mStompClient!!.send("/app/arthur/law/data", param.toString())
                .subscribe({
                    //                    LogUtils.i("STOMP echo send successfully")
                }, { throwable ->
                    LogUtils.e("Error send STOMP echo,$throwable")
                })
    }

//    private fun handleTopicMessage(str: String?) {
//        if (str == null) return
//        val json = JSONObject(str)
//        LogUtils.i(json)
//        when (json["code"]) {
//            2001 -> {
//                val data = json.getJSONObject("data")
//                if (deal(data)) {
//                    val nowVote = data.getJSONObject("vote")
//                    val entity = Vote(nowVote.getBoolean("multiple"))
//                    entity.title = nowVote.getString("name")
//                    entity.summary = nowVote.getString("summary")
//                    entity.item = nowVote.getString("item")
//                    startVote(entity)
//                }
//            }
//        }
//    }

//    private fun startVote(vote: Vote) {
////        val vote = arrayListOf("还行，就判他五年 ","不行，这么认真的队伍要无罪释放 "," 爱咋咋地我弃权")
//        val builder = AlertDialog.Builder(this@NowMeetingActivity)
//        builder.setCancelable(false)
//        builder.setTitle("发现投票项")
//        builder.setMessage("有人发起了投票\n将会带你跳转到新页面进行投票。")
//        builder.setPositiveButton("好的") { _: DialogInterface, _: Int ->
//            if (vote.multiple) {
//                val temp = Intent(this@NowMeetingActivity, MultipleVoteActivity::class.java)
//                temp.putExtra("vote", vote)
//                temp.putExtra("meet", meet)
//                startActivityForResult(temp, Constants.NOW_MEETING_CODE)
//            } else {
//                val temp = Intent(this@NowMeetingActivity, SingleVoteActivity::class.java)
//                temp.putExtra("vote", vote)
//                temp.putExtra("meet", meet)
//                startActivityForResult(temp, Constants.NOW_MEETING_CODE)
//            }
//        }
//        builder.show()
//    }

    private fun deal(json: JSONObject?): Boolean {
        if (json == null) return false
        val nowMeet = json.getJSONObject("meeting")
        if (nowMeet["id"] != meetId) return false
        val nowVote = json.getJSONObject("vote")
        val participants = nowVote.getJSONArray("participants")
        for (i in 0..(participants.length() - 1)) {
            val id = participants.getString(i)
            if (CacheDiskUtils.getInstance().getString(Constants.CACHE_USERID) == id)
                return true
        }
        return false
    }
}