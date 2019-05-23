package com.dede.vinocr.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.DisplayMetrics
import android.view.View
import com.dede.vinocr.R
import com.dede.vinocr.getBitmap
import com.kernal.smartvisionocr.model.ConfigParamsModel
import com.kernal.smartvisionocr.utils.KernalLSCXMLInformation
import org.jetbrains.anko.dip
import org.jetbrains.anko.sp

/**
 *
 * @author gengqiquan
 * @date 2018/6/28 下午6:01
 * 扫描控件
 */
class VinFinderView : View {
    var wlci: KernalLSCXMLInformation

    constructor(context: Context, wlci: KernalLSCXMLInformation, type: String?) : super(context) {
        this.wlci = wlci

        this.strokeWidth = dip(1).toFloat()
        this.lineLength = dip(15)

        this.shadowPaint = Paint()
        shadowPaint.color = 0x99000000.toInt()

        paint = Paint()
        paint.color = 0xff2a8cff.toInt()
        paint.isAntiAlias = true

        textPaint = Paint()
        textPaint.isAntiAlias = true
        if (type != null && type != "") {
            configParamsModel = wlci.fieldType[type]
        }
        count = 1f
        isAdd = true
        dm = DisplayMetrics()
        dm = context.applicationContext.resources.displayMetrics
    }

    private lateinit var line: Bitmap
    private var frame: Rect? = null
    private var mWidth: Int = 0
    var mHeight: Int = 0

    var configParamsModel: List<ConfigParamsModel>? = null
    private var count = 1f
    private var isAdd = true
    private var dm: DisplayMetrics? = null
    private val paint: Paint
    private var shadowPaint: Paint
    private var textPaint: Paint

    private var strokeWidth: Float
    private val lineLength: Int
    private var scrollLength = 0f
    private val speed = dip(2)
    @SuppressLint("DrawAllocation")
    public override fun onDraw(c: Canvas) {
        mWidth = c.width
        mHeight = c.height

        if (configParamsModel != null) {
            /**
             * 这个矩形就是中间显示的那个框框
             */
            frame = Rect((configParamsModel!![fieldsPosition].leftPointX * mWidth).toInt(),
                    (mHeight * configParamsModel!![fieldsPosition].leftPointY).toInt(),
                    ((configParamsModel!![fieldsPosition].leftPointX + configParamsModel!![fieldsPosition].width) * mWidth).toInt(),
                    (mHeight * (configParamsModel!![fieldsPosition].leftPointY + configParamsModel!![fieldsPosition].height)).toInt())
            if (frame == null) {
                return
            }


            //4 四个角，先画横线后画竖线，strokeWidth画笔宽度的一般作为偏移量，去除横竖交界部分的空白角
            paint.strokeWidth = strokeWidth * 2
            val storke = strokeWidth

            //左上
            c.drawLine(frame!!.left.toFloat() - strokeWidth, frame!!.top.toFloat() + storke, frame!!.left.toFloat() + lineLength, frame!!.top.toFloat() + storke, paint)
            c.drawLine(frame!!.left.toFloat() + storke, frame!!.top.toFloat(), frame!!.left.toFloat() + storke, frame!!.top.toFloat() + lineLength, paint)
            //右上
            c.drawLine(frame!!.right.toFloat() - lineLength, frame!!.top.toFloat() + storke, frame!!.right.toFloat(), frame!!.top.toFloat() + storke, paint)
            c.drawLine(frame!!.right.toFloat() - storke, frame!!.top.toFloat(), frame!!.right.toFloat() - storke, frame!!.top.toFloat() + lineLength, paint)
            //左下
            c.drawLine(frame!!.left.toFloat(), frame!!.bottom.toFloat() - storke, frame!!.left.toFloat() + lineLength, frame!!.bottom.toFloat() - storke, paint)
            c.drawLine(frame!!.left.toFloat() + storke, frame!!.bottom.toFloat() - lineLength - strokeWidth, frame!!.left.toFloat() + storke, frame!!.bottom.toFloat(), paint)
            //右下
            c.drawLine(frame!!.right.toFloat() - lineLength - strokeWidth, frame!!.bottom.toFloat() - storke, frame!!.right.toFloat(), frame!!.bottom.toFloat() - storke, paint)
            c.drawLine(frame!!.right.toFloat() - storke, frame!!.bottom.toFloat() - lineLength - strokeWidth, frame!!.right.toFloat() - storke, frame!!.bottom.toFloat() - strokeWidth, paint)
            //四条边,上下左右
            paint.strokeWidth = strokeWidth
            c.drawLine(frame!!.left.toFloat(), frame!!.top.toFloat(), frame!!.right.toFloat(), frame!!.top.toFloat(), paint)
            c.drawLine(frame!!.left.toFloat(), frame!!.bottom.toFloat(), frame!!.right.toFloat(), frame!!.bottom.toFloat(), paint)
            c.drawLine(frame!!.left.toFloat(), frame!!.top.toFloat(), frame!!.left.toFloat(), frame!!.bottom.toFloat(), paint)
            c.drawLine(frame!!.right.toFloat(), frame!!.top.toFloat(), frame!!.right.toFloat(), frame!!.bottom.toFloat(), paint)
            //上下左右阴影
            c.drawRect(0f, 0f, mWidth.toFloat(), frame!!.top.toFloat(), shadowPaint)
            c.drawRect(0f, frame!!.bottom.toFloat(), mWidth.toFloat(), mHeight.toFloat(), shadowPaint)
            c.drawRect(0f, frame!!.top.toFloat(), frame!!.left.toFloat(), frame!!.bottom.toFloat(), shadowPaint)
            c.drawRect(frame!!.right.toFloat(), frame!!.top.toFloat(), mWidth.toFloat(), frame!!.bottom.toFloat(), shadowPaint)

            //文字
            textPaint.color = Color.WHITE
            textPaint.textSize = sp(16).toFloat()
            c.drawText(title, (mWidth - textPaint.measureText(title)) / 2, (frame!!.top - dip(40)).toFloat(), textPaint)

            textPaint.color = 0xffcccccc.toInt()
            textPaint.textSize = sp(10).toFloat()
            c.drawText(tips, (mWidth - textPaint.measureText(tips)) / 2, (frame!!.top - dip(20)).toFloat(), textPaint)
        }
        line = getBitmap(R.drawable.img_scan_line, frame!!.width(), dip(4))
        //滚动线
        c.drawBitmap(line, frame!!.left.toFloat(), frame!!.top.toFloat() + scrollLength, paint)
        if (scrollLength > frame!!.bottom - frame!!.top - dip(4)) {
            down = false
        }
        if (scrollLength <= 0) {
            down = true
        }
        if (down) {
            scrollLength += speed
        } else {
            scrollLength = 0f
        }
        postInvalidateDelayed(50, frame!!.left + 100, frame!!.top, frame!!.right - 100, frame!!.bottom)
    }


    val title = "请将车架号放置于扫描框内"
    val tips = "可支持扫描行驶证上、车辆铭牌以及前挡风玻璃下的车架号"
    var down = true

    companion object {

        var fieldsPosition = 0// 输出结果的先后顺序
    }

}
