package com.example.androidx_example.fragments.player

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.graphics.PointF
import android.util.AttributeSet
import android.view.*
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import android.view.GestureDetector.SimpleOnGestureListener
import android.widget.FrameLayout
import android.widget.ImageView
import com.example.androidx_example.data.Video
import com.example.androidx_example.until.GlideApp
import com.example.androidx_example.until.debugInfo

class PlayerCtrlView : FrameLayout {

    private var scaleGestureDetector: ScaleGestureDetector? = null
    private var moveGestureDetector: GestureDetector? = null
    private var displayMode = DisplayMode.DEFAULT
    private var windowManager: WindowManager? = null
    var playerView: PlayerView? = null
    var playerImageView: ImageView? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        val height = (width / PlayerView.AspectRatio.AR_16_9.value).toInt()
        playerView?.apply {
            maxWidth = width
            maxHeight = height
            updateViewSize()
        }
        for (i in 0 until childCount) {
            getChildAt(i).measure(width, height)
        }
        setMeasuredDimension(width, height)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        val actionPointMinNum = when (displayMode) {
            DisplayMode.DEFAULT -> 3
            DisplayMode.TINY_WINDOW, DisplayMode.IN_ACTIVITY -> 1
        }
        if (event.pointerCount >= actionPointMinNum) {
            scaleGestureDetector?.onTouchEvent(event)
            moveGestureDetector?.onTouchEvent(event)
        }
        if (event.action == MotionEvent.ACTION_UP) {
            fixPlayViewPosition()
        }
        return true
    }

    init {
        isFocusable = true
        isClickable = true
        // 播放器缩放
        scaleGestureDetector = ScaleGestureDetector(context, object : SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                playerView?.apply {
                    scaleX *= detector.scaleFactor
                    scaleY *= detector.scaleFactor
                }
                return true
            }
        })
        // 播放器拖动
        moveGestureDetector = GestureDetector(context, object : SimpleOnGestureListener() {

            private var downPosition: Point = Point()

            override fun onDown(e: MotionEvent?): Boolean {
                downPosition = getWindowPoint(displayMode, this@PlayerCtrlView)
                return super.onDown(e)
            }

            override fun onScroll(
                start: MotionEvent?,
                current: MotionEvent?,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                when (displayMode) {
                    DisplayMode.TINY_WINDOW -> {
                        val dx = current!!.rawX - start!!.rawX
                        val dy = current.rawY - start.rawY
                        val lp = layoutParams as WindowManager.LayoutParams
                        lp.x = (downPosition.x + dx).toInt()
                        lp.y = (downPosition.y + dy).toInt()
                        windowManager?.updateViewLayout(this@PlayerCtrlView, lp)
                    }
                    DisplayMode.IN_ACTIVITY -> {
                        val dx = current!!.rawX - start!!.rawX
                        val dy = current.rawY - start.rawY
                        x = downPosition.x + dx
                        y = downPosition.y + dy
                    }
                    DisplayMode.DEFAULT -> {
                        playerView?.apply {
                            x -= distanceX
                            y -= distanceY
                        }
                    }
                }
                return true
            }
        })
    }

    /**
     * 启动播放器
     */
    fun startPlay() {
        playerView?.startPlay("https://www.cool1024.com:8000/flv?video=1.flv")
    }

    /**
     * 播放指定视频对象
     */
    fun preparePlayer(video: Video) {
        playerImageView?.apply {
            debugInfo(video.videoThumbUrl)
            layoutParams.width = this@PlayerCtrlView.width
            layoutParams.height = this@PlayerCtrlView.height
            layoutParams = layoutParams
//            GlideApp.with(this@PlayerCtrlView)
//                .load(video.videoThumbUrl)
//                .into(this)
        }
    }

    /**
     * 全局小窗口模式
     */
    fun floatInWindow(activity: Activity) {
        windowManager = moveViewTopWindow(this, activity)
        displayMode = DisplayMode.TINY_WINDOW
        when (displayMode) {
            DisplayMode.DEFAULT -> moveViewTopActivity(this, activity)
            DisplayMode.TINY_WINDOW -> {
                removeFromWindow()
                moveViewTopActivity(this, activity)
            }
            DisplayMode.IN_ACTIVITY -> {
                debugInfo("无需切换模式")
            }
        }
    }

    /**
     * 从全局窗口移除
     */
    fun removeFromWindow(): View {
        if (displayMode == DisplayMode.TINY_WINDOW) {
            windowManager?.removeView(this)
        }
        return this
    }

    /**
     * 活动小窗口模式
     */
    fun floatInActivity(activity: Activity) {
        when (displayMode) {
            DisplayMode.DEFAULT -> moveViewTopActivity(this, activity)
            DisplayMode.TINY_WINDOW -> {
                removeFromWindow()
                moveViewTopActivity(this, activity)
            }
            DisplayMode.IN_ACTIVITY -> {
                debugInfo("无需切换模式")
            }
        }
        displayMode = DisplayMode.IN_ACTIVITY
    }

    /**
     * 从活动中移除视图
     */
    fun removeFromActivity(): View {
        if (displayMode == DisplayMode.IN_ACTIVITY) {
            (this.parent as ViewGroup).removeView(this)
        }
        return this
    }

    /**
     * 修复播放视图位置（如果有必要的话）
     */
    private fun fixPlayViewPosition() {
        playerView?.apply {
            if (scaleX < 1 || scaleY < 1) {
                val point =
                    moveViewToPoint(this, getCenterPoint())
            }
        }
    }

    companion object {

        /**
         * 根据窗口模式获取坐标
         */
        fun getWindowPoint(mode: DisplayMode, view: View): Point {
            return when (mode) {
                DisplayMode.TINY_WINDOW -> {
                    val lp = view.layoutParams as WindowManager.LayoutParams
                    Point(lp.x, lp.y)
                }
                DisplayMode.IN_ACTIVITY -> {
                    Point(view.x.toInt(), view.y.toInt())
                }
                else -> Point()
            }
        }

        /**
         * 应用内小窗口
         */
        fun moveViewTopActivity(view: View, activity: Activity) {
            (view.parent as ViewGroup).removeView(view)
            val contentView = activity.findViewById<ViewGroup>(android.R.id.content)
            val layoutParams = LayoutParams(
                500,
                300,
                Gravity.BOTTOM or Gravity.END
            )
            layoutParams.bottomMargin = 20
            layoutParams.rightMargin = 20
            contentView.addView(view, layoutParams)
        }

        /**
         * 全局小窗口
         */
        fun moveViewTopWindow(view: View, activity: Activity): WindowManager {
            (view.parent as ViewGroup).removeView(view)
            val window = activity.application.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val layoutParams = WindowManager.LayoutParams(WindowManager.LayoutParams.TYPE_PHONE)
            layoutParams.x = 0
            layoutParams.y = 0
            layoutParams.width = 500
            layoutParams.height = 300
            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            window.addView(view, layoutParams)
            return window
        }

        /**
         * 移动视图到指定位置（动画）
         */
        fun moveViewToPoint(view: View, targetPoint: PointF) {

            // X轴方向移动
            val animatorX = ValueAnimator.ofFloat(view.x, targetPoint.x)
            animatorX.duration = 500
            animatorX.addUpdateListener { animation ->
                view.x = animation.animatedValue as Float
            }
            animatorX.start()

            // Y轴方向移动
            val animatorY = ValueAnimator.ofFloat(view.y, targetPoint.y)
            animatorY.duration = 500
            animatorY.addUpdateListener { animation ->
                view.y = animation.animatedValue as Float
            }
            animatorY.start()
        }
    }

    enum class DisplayMode {
        TINY_WINDOW,
        IN_ACTIVITY,
        DEFAULT
    }
}