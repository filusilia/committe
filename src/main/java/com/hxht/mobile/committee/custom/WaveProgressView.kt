package com.hxht.mobile.committee.custom

import android.content.Context
import android.graphics.*
import android.support.annotation.Nullable
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.Transformation
import android.widget.TextView
import com.hxht.mobile.committee.R


class WaveProgressView(context: Context, @Nullable attrs: AttributeSet) : View(context, attrs) {
    private var circlePaint: Paint? = null//圆形进度框画笔
    private var wavePaint: Paint? = null//绘制波浪画笔
    private var wavePath: Path? = null//绘制波浪Path
    private var secondWavePaint: Paint? = null//绘制第二个波浪的画笔

    private var bitmap: Bitmap? = null//缓存bitmap
    private var bitmapCanvas: Canvas? = null

    private var waveProgressAnim: WaveProgressAnim? = null
    private var textView: TextView? = null
    private var onAnimationListener: OnAnimationListener? = null

    private var waveWidth: Float = 0.0f//波浪宽度
    private var waveHeight: Float = 0.0f//波浪高度
    private var waveNum: Int = 0//波浪组的数量（一次起伏为一组）
    private var waveMovingDistance: Float = 0.0f//波浪平移的距离

    private var viewSize: Int = 0//重新测量后View实际的宽高
    private var defaultSize: Int = 0//自定义View默认的宽高

    private var percent: Float = 0.0f//进度条占比
    private var progressNum: Float = 0.0f//可以更新的进度条数值
    private var maxNum: Float = 0.0f//进度条最大值

    private var waveColor: Int = 0//波浪颜色
    private var secondWaveColor: Int = 0//第二层波浪颜色
    private var bgColor: Int = 0//背景进度框颜色

    private var isDrawSecondWave: Boolean = false

    init {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.WaveProgressView)
        waveWidth = typedArray.getDimension(R.styleable.WaveProgressView_wave_width, DpOrPxUtils.dip2px(context, 25f).toFloat())
        waveHeight = typedArray.getDimension(R.styleable.WaveProgressView_wave_height, DpOrPxUtils.dip2px(context, 5f).toFloat())
        waveColor = typedArray.getColor(R.styleable.WaveProgressView_wave_color, ContextCompat.getColor(context, R.color.deepskyblue))
        secondWaveColor = typedArray.getColor(R.styleable.WaveProgressView_second_wave_color, ContextCompat.getColor(context, R.color.darkturquoise))
        bgColor = typedArray.getColor(R.styleable.WaveProgressView_wave_bg_color, ContextCompat.getColor(context, R.color.eee))
        typedArray.recycle()

        defaultSize = DpOrPxUtils.dip2px(context, 100f)
        waveNum = Math.ceil((defaultSize / waveWidth / 2).toDouble()).toInt()
        waveMovingDistance = 0.0f

        wavePath = Path()

        wavePaint = Paint()
        wavePaint?.color = waveColor
        wavePaint?.isAntiAlias = true//设置抗锯齿
        wavePaint?.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

        secondWavePaint = Paint()
        secondWavePaint?.color = secondWaveColor
        secondWavePaint?.isAntiAlias = true//设置抗锯齿
        secondWavePaint?.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)//因为要覆盖在第一层波浪上，且要让半透明生效，所以选此模式

        circlePaint = Paint()
        circlePaint?.color = bgColor
        circlePaint?.isAntiAlias = true//设置抗锯齿

        waveProgressAnim = WaveProgressAnim()
        waveProgressAnim?.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}

            override fun onAnimationEnd(animation: Animation) {}

            override fun onAnimationRepeat(animation: Animation) {
                if (percent == progressNum / maxNum) {
                    waveProgressAnim?.duration = 0
                }
            }
        })

        percent = 0.0f
        progressNum = 0.0f
        maxNum = 100.0f
        isDrawSecondWave = false
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val height = measureSize(defaultSize, heightMeasureSpec)
        val width = measureSize(defaultSize, widthMeasureSpec)
        val min = Math.min(width, height)// 获取View最短边的长度
        setMeasuredDimension(min, min)// 强制改View为以最短边为长度的正方形
        viewSize = min
        waveNum = Math.ceil((viewSize / waveWidth / 2).toDouble()).toInt()


    }

    private fun measureSize(defaultSize: Int, measureSpec: Int): Int {
        var result = defaultSize
        val specMode = View.MeasureSpec.getMode(measureSpec)
        val specSize = View.MeasureSpec.getSize(measureSpec)

        if (specMode == View.MeasureSpec.EXACTLY) {
            result = specSize
        } else if (specMode == View.MeasureSpec.AT_MOST) {
            result = Math.min(result, specSize)
        }
        return result
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        bitmap = Bitmap.createBitmap(viewSize, viewSize, Bitmap.Config.ARGB_8888)
        bitmapCanvas = Canvas(bitmap)

        bitmapCanvas?.drawCircle((viewSize / 2).toFloat(), (viewSize / 2).toFloat(), (viewSize / 2).toFloat(), circlePaint)
        bitmapCanvas?.drawPath(getWavePath(), wavePaint)
        if (isDrawSecondWave) {
            bitmapCanvas?.drawPath(getSecondWavePath(), secondWavePaint)
        }

        canvas.drawBitmap(bitmap, 0.0f, 0.0f, null)
    }

    var lastX = 0.0f
    var lastY = 0.0f
    private fun getWavePath(): Path {
        //        float changeWaveHeight = (1 - percent) * waveHeight;
        var changeWaveHeight = waveHeight
        if (onAnimationListener != null) {
            changeWaveHeight = if (onAnimationListener!!.howToChangeWaveHeight(percent, waveHeight) == 0.0f && percent < 1)
                waveHeight
            else
                onAnimationListener!!.howToChangeWaveHeight(percent, waveHeight)
        }

        wavePath?.reset()


        //移动到右上方，也就是p0点
        wavePath?.moveTo(viewSize.toFloat(), (1 - percent) * viewSize)
        //移动到右下方，也就是p1点
        wavePath?.lineTo(viewSize.toFloat(), viewSize.toFloat())
        //移动到左下边，也就是p2点
        wavePath?.lineTo(0.0f, viewSize.toFloat())
        //移动到左上方，也就是p3点（x轴默认方向是向右的，我们要向左平移，因此设为负值）
        //wavePath.lineTo(0, (1-percent)*viewSize);
        wavePath?.lineTo(-waveMovingDistance, (1 - percent) * viewSize)

        //从p3开始向p0方向绘制波浪曲线（曲线宽度为原来的两倍也就是波浪数量*2）
        for (i in 0 until waveNum * 2) {
            wavePath?.rQuadTo(waveWidth / 2, changeWaveHeight, waveWidth, 0.0f)
            wavePath?.rQuadTo(waveWidth / 2, -changeWaveHeight, waveWidth, 0.0f)
        }

        //将path封闭起来
        wavePath?.close()
        return wavePath!!
    }

    private fun getSecondWavePath(): Path {
        //        float changeWaveHeight = (1 - percent) * waveHeight;
        var changeWaveHeight = waveHeight
        if (onAnimationListener != null) {
            changeWaveHeight = if (onAnimationListener?.howToChangeWaveHeight(percent, waveHeight) == 0.0f && percent < 1)
                waveHeight
            else
                onAnimationListener!!.howToChangeWaveHeight(percent, waveHeight)
        }

        wavePath?.reset()

        //移动到左上方，也就是p3点
        wavePath?.moveTo(0.0f, (1 - percent) * viewSize)
        //移动到左下边，也就是p2点
        wavePath?.lineTo(0.0f, viewSize.toFloat())
        //移动到右下方，也就是p1点
        wavePath?.lineTo(viewSize.toFloat(), viewSize.toFloat())
        //移动到右上方，也就是p0点
        wavePath?.lineTo(viewSize + waveMovingDistance, (1 - percent) * viewSize)

        //从p0开始向p3方向绘制波浪曲线
        for (i in 0 until waveNum * 2) {
            wavePath?.rQuadTo(-waveWidth / 2, changeWaveHeight, -waveWidth, 0.0f)
            wavePath?.rQuadTo(-waveWidth / 2, -changeWaveHeight, -waveWidth, 0.0f)
        }

        //将path封闭起来
        wavePath?.close()
        return wavePath!!
    }


    inner class WaveProgressAnim : Animation() {

        override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            super.applyTransformation(interpolatedTime, t)
            //波浪高度到达最大值后就不需要循环了，只需让波浪曲线平移循环即可
            if (percent < progressNum / maxNum) {
                percent = interpolatedTime * progressNum / maxNum
                if (textView != null && onAnimationListener != null) {
                    textView?.text = onAnimationListener?.howToChangeText(interpolatedTime, progressNum, maxNum)
                }
            }
            waveMovingDistance = interpolatedTime * waveNum * waveWidth * 2
            postInvalidate()
        }
    }

    /**
     * 设置进度条数值
     * @param progressNum 进度条数值
     * @param time 动画持续时间
     */
    fun setProgressNum(progressNum: Float) {
        setProgressNum(progressNum, 0)
    }

    fun setProgressNum(progressNum: Float, time: Int) {
        if (this.progressNum == progressNum) return
        this.progressNum = progressNum
        waveProgressAnim?.repeatCount = Animation.INFINITE//让动画无限循环
        waveProgressAnim?.interpolator = LinearInterpolator()//让动画匀速播放，不然会出现波浪平移停顿的现象
        percent = 0.0f
        waveProgressAnim?.duration = time.toLong()
//        waveProgressAnim?.repeatCount = -1
        this.startAnimation(waveProgressAnim)
    }

    /**
     * 是否绘制第二层波浪
     * @param isDrawSecondWave
     */
    fun setDrawSecondWave(isDrawSecondWave: Boolean) {
        this.isDrawSecondWave = isDrawSecondWave
    }

    /**
     * 设置显示文字的TextView
     * @param textView
     */
    fun setTextView(textView: TextView) {
        this.textView = textView
    }

    interface OnAnimationListener {

        /**
         * 如何处理要显示的文字内容
         * @param interpolatedTime 从0渐变成1,到1时结束动画
         * @param updateNum 进度条数值
         * @param maxNum 进度条最大值
         * @return
         */
        fun howToChangeText(interpolatedTime: Float, updateNum: Float, maxNum: Float): String

        /**
         * 如何处理波浪高度
         * @param percent 进度占比
         * @param waveHeight 波浪高度
         * @return
         */
        fun howToChangeWaveHeight(percent: Float, waveHeight: Float): Float
    }

    fun setOnAnimationListener(onAnimationListener: OnAnimationListener) {
        this.onAnimationListener = onAnimationListener
    }
}