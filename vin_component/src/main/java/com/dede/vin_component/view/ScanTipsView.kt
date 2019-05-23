package com.dede.vin_component.view

import android.app.Activity
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.core.content.ContextCompat
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.Keep
import com.dede.vin_component.*
import org.jetbrains.anko.*

/**
 *
 * @author gengqiquan
 * @date 2018/6/28 下午6:01
 */
@Keep
class ScanTipsView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {
    private val tv_msg = TextView(context)
    private val v_close = View(context)

    init {
        orientation = HORIZONTAL
        backgroundDrawable = ContextCompat.getDrawable(context, R.drawable.img_vin_scan_tips1)
        gone()

        tv_msg.setPadding(dip(10), dip(5), 0, 0)
        tv_msg.textSize = 12f
        tv_msg.textColor = Color.WHITE

        v_close.setOnClickListener {
            this@ScanTipsView.gone()
            context.putBoolean(key, false)
        }
        val lay = LinearLayout.LayoutParams(dip(30), dip(30))
        lay.gravity = Gravity.RIGHT
        lay.rightMargin = dip(10)
        addView(tv_msg, wrapContent, matchParent)
        addView(v_close, lay)

    }

    var key = ""
    fun bind(activity: Activity) {
        val name = activity.javaClass.simpleName
        key = "showVinTips" + name + getVersionName(context)
        if (activity.getBoolean(key, true) && show_vin) {
            tv_msg.text = vin_msg
            show()
        }
    }

    private fun getVersionName(context: Context): String {
        val pkgInfo: PackageInfo?
        try {
            pkgInfo = context.packageManager.getPackageInfo(
                    context.packageName, PackageManager.GET_SIGNATURES)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            return ""
        }
        return pkgInfo.versionName
    }

    companion object {
        var vin_msg = "扫一扫，自动识别车架号"
        var show_vin = true
    }
}