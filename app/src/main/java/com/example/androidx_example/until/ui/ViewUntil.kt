package com.example.androidx_example.until.ui

import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import com.example.androidx_example.R
import com.example.androidx_example.data.PhotoSize
import com.example.androidx_example.fragments.album.AlbumViewHolder


object ViewUntil {

    /**
     * 设置全屏模式
     */
    fun setFullMode(window: Window) {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }

    /**
     * 获取图片默认加载动画-Drawable对象
     */
    fun getAnimationDrawable(context: Context): AnimationDrawable {
        val animationDrawable =
            ContextCompat.getDrawable(context, R.drawable.bg_loading) as AnimationDrawable
        animationDrawable.start()
        return animationDrawable
    }

    /**
     * 关闭虚拟键盘
     */
    fun closeKeyBoard(context: Context, window: Window) {
        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).run {
            hideSoftInputFromWindow(window.decorView.windowToken, 0)
        }
    }

    /**
     * 创建ViewHolder的ItemView
     */
    fun createViewHolderView(parentView: ViewGroup, layoutId: Int): View {
        return LayoutInflater.from(parentView.context)
            .inflate(layoutId, parentView, false)
    }

    /**
     * 获取一个合适容器的尺寸
     */
    fun getFitSize(originSize: Size, containerSize: Size): Size {
        val cwOW = 1f * containerSize.width / originSize.width
        val expW = containerSize.width
        val expH = (originSize.height * cwOW).toInt()
        return if (expH < containerSize.height)
            Size(expW, expH)
        else return Size(
            (containerSize.height * (1f * expW / expH)).toInt(),
            containerSize.height
        )
    }
}