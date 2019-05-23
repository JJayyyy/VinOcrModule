package com.dede.vinocr

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Service
import android.content.Intent
import android.hardware.Camera
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.os.Vibrator
import android.util.Log
import android.view.KeyEvent
import android.view.SurfaceHolder
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.dede.vinocr.utils.*
import com.dede.vinocr.view.VinFinderView
import com.kernal.smartvisionocr.utils.KernalLSCXMLInformation
import kotlinx.android.synthetic.main.activity_vin_ocr.*
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

const val EXTRA_VIN_CODE = "vin"

/**
 *
 * @author gengqiquan
 * @date 2018/6/28 下午5:58
 * ocr 识别vin码
 * 通过[startActivityForResult]开启
 * 会在[onActivityResult]的[Intent]中携带参数
 * key为[String]类型的车架号，和key为[VINRecogResult]类型的图片信息
 */
class VinOcrActivity : AppCompatActivity(), SurfaceHolder.Callback, Camera.PreviewCallback {
    private var srcWidth: Int = 0
    private var srcHeight: Int = 0
    private var surfaceHolder: SurfaceHolder? = null
    private var camera: Camera? = null
    private val srcList = ArrayList<Int>()// 拍照分辨率集合
    private val selectedTemplateTypePosition = 0
    private var mVibrator: Vibrator? = null
    private var rotation = 0 // 屏幕取景方向
    private var cameraParametersUtils: CameraParametersUtils? = null
    private var myVinFinderView: VinFinderView? = null
    private var wlci: KernalLSCXMLInformation? = null
    private var isRecogSuccess = false
    private var size: Camera.Size? = null
    private var isFirstIn = true
    private val islandscape = false// 是否为横向
    private var isSetZoom = false
    private var mRecogOpera: RecogOpera? = null
    @SuppressLint("HandlerLeak")
    private val mAutoFocusHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            if (msg.what == 100) {
                CameraSetting.getInstance(this@VinOcrActivity).autoFocus(camera)
                this.sendEmptyMessageDelayed(
                        100, 2500)
            }

        }
    }
    private var uiRot: Int = 0
    private val threadPoolExecutor = ThreadPoolExecutor(1, 1, 3,
            TimeUnit.MILLISECONDS, ArrayBlockingQueue(1))
    private var sum = 0
    private var lightIsOn = false
    private val initCameraParams = Runnable {
        if (camera != null) {
            CameraSetting.getInstance(this)
                    .setCameraParameters(this,
                            surfaceHolder, this, camera,
                            srcHeight.toFloat() / srcWidth, srcList, false,
                            rotation, isSetZoom)

            //				}
            size = camera!!.parameters.previewSize

            lightIsOn = false
            CameraSetting.getInstance(this)
                    .closedCameraFlash(camera)
            tv_flash.text = "轻触点亮手电筒"
            iv_flash.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_ocr_flashlight_off))
        }
    }

    private var vinRecogResult: VINRecogResult? = null
    private val vinRecogParameter = VINRecogParameter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        this.window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_vin_ocr)
        // 已写入的情况下，根据version.txt中版本号判断是否需要更新，如不需要不执行写入操作
        cameraParametersUtils = CameraParametersUtils(this)
        uiRot = windowManager.defaultDisplay.rotation// 获取屏幕旋转的角度
        getPhoneSizeAndRotation()
        mRecogOpera = RecogOpera(this)
        mRecogOpera!!.initOcr()
        wlci = mRecogOpera!!.wlci_Portrait
        ClickEvent()
        AddView()
        surfaceHolder = surface_view.holder
        surfaceHolder!!.addCallback(this)
        surfaceHolder!!.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
    }

    // 获取设备分辨率 不受虚拟按键影O响
    fun getPhoneSizeAndRotation() {
        cameraParametersUtils!!.setScreenSize(this)
        srcWidth = cameraParametersUtils!!.srcWidth
        srcHeight = cameraParametersUtils!!.srcHeight
    }

    override fun onStart() {
        super.onStart()
        vinRecogParameter.isFirstProgram = true
    }

    fun ClickEvent() {

        iv_back!!.setOnClickListener {
            backLastActivtiy()
        }

        cb_flash.setOnClickListener {
            if (!lightIsOn) {
                lightIsOn = true
                CameraSetting.getInstance(this)
                        .openCameraFlash(camera)
                tv_flash.text = "轻触关闭手电筒"
                iv_flash.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_ocr_flashlight_on))
            } else {
                lightIsOn = false
                CameraSetting.getInstance(this)
                        .closedCameraFlash(camera)
                tv_flash.text = "轻触点亮手电筒"
                iv_flash.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_ocr_flashlight_off))
            }
        }

    }

    fun RemoveView() {
        if (myVinFinderView != null) {
            myVinFinderView!!.destroyDrawingCache()
            rl_content.removeView(myVinFinderView)
            myVinFinderView = null
        }
    }

    fun AddView() {

        myVinFinderView = VinFinderView(
                this,
                wlci!!,
                wlci!!.template[selectedTemplateTypePosition].templateType)
        rl_content.addView(myVinFinderView, 1)

    }


    override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
        this.runOnUiThread(initCameraParams)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int,
                                height: Int) {

        val msg = Message()
        msg.what = 100
        mAutoFocusHandler.sendMessage(msg)
    }

    override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {

    }

    // 小米PAD 解锁屏时执行surfaceChanged surfaceCreated，容易出现超时卡死现象，故在此处打开相机和设置参数
    override fun onResume() {
        super.onResume()
        if (wlci!!.fieldType[wlci!!.template[selectedTemplateTypePosition].templateType]
                !!.get(VinFinderView.fieldsPosition).ocrId == "SV_ID_YYZZ_MOBILEPHONE") {
            isSetZoom = true
        } else {
            isSetZoom = false
        }
        OpenCameraAndSetParameters()

    }

    public override fun onPause() {
        super.onPause()
        CloseCameraAndStopTimer(0)
    }

    override fun onPreviewFrame(bytes: ByteArray, camera: Camera) {
        if (isRecogSuccess) {
            return
        }
        Log.d("iTH_InitSmartVisionSDK", mRecogOpera!!.iTH_InitSmartVisionSDK.toString() + "")
        if (mRecogOpera!!.iTH_InitSmartVisionSDK == 0 && sum == 0) {
            threadPoolExecutor.execute {
                sum = sum + 1
                vinRecogParameter.data = bytes
                vinRecogParameter.islandscape = islandscape//是否横屏
                vinRecogParameter.rotation = rotation//屏幕旋转角度
                vinRecogParameter.selectedTemplateTypePosition = selectedTemplateTypePosition//模板位置
                vinRecogParameter.wlci = wlci//配置文件解析内容
                vinRecogParameter.size = size//相机的预览分辨率
                vinRecogResult = mRecogOpera!!.startOcr(vinRecogParameter)

                if (vinRecogResult != null && vinRecogResult!!.result != null) {
                    val recogResult = vinRecogResult!!.result//Vin识别结果
//                    ZhugeTrack(mContext).put("来源", "车架号流识别-SDK").track("OCR-车架号识别成功")
                    if (recogResult != null && "" != recogResult) {
                        Log.d("recogResult", recogResult!!.toString())
                        isRecogSuccess = true
                        mVibrator = application.getSystemService(
                                Service.VIBRATOR_SERVICE) as Vibrator
                        mVibrator!!.vibrate(200)
                        //                            savePath = vinRecogResult.savePath;
                        //                            httpContent = vinRecogResult.httpContent;
                        //                            VinOcrActivity.recogResultModelList.get(ViewfinderView.fieldsPosition).resultValue = recogResult;
                        //                            VinOcrActivity.this.runOnUiThread(updateUI);
                        val intent = Intent()
                        intent.putExtra(EXTRA_VIN_CODE, recogResult)
                        val bundle = Bundle()
                        bundle.putSerializable("result", vinRecogResult)
                        intent.putExtras(bundle)
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    }

                } else {
//                    ZhugeTrack(mContext).put("来源", "车架号流识别-SDK").track("OCR-车架号识别失败")
                }
                sum -= 1
            }
        }
    }

    override fun onDestroy() {
        threadPoolExecutor?.shutdown()
        mRecogOpera!!.freeKernalOpera(this)
        mAutoFocusHandler.removeMessages(100)
        if (myVinFinderView != null) {
            rl_content.removeView(myVinFinderView)
            myVinFinderView!!.destroyDrawingCache()
            VinFinderView.fieldsPosition = 0
            myVinFinderView = null
        }
        super.onDestroy()

    }

    // 监听返回键事件
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.repeatCount == 0) {
            backLastActivtiy()
            return true
        }
        return true
    }

    fun backLastActivtiy() {
        isRecogSuccess = true
        CloseCameraAndStopTimer(0)
        this.finish()
    }

    fun OpenCameraAndSetParameters() {
        try {
            if (null == camera) {
                camera = CameraSetting.getInstance(this).open(0,
                        camera)

                rotation = CameraSetting.getInstance(this)
                        .setCameraDisplayOrientation(uiRot)
                if (!isFirstIn) {
                    CameraSetting.getInstance(this)
                            .setCameraParameters(this,
                                    surfaceHolder, this,
                                    camera, srcHeight.toFloat() / srcWidth,
                                    srcList, false, rotation, isSetZoom)
                }
                isFirstIn = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun CloseCameraAndStopTimer(type: Int) {
        if (camera != null) {
            if (type == 1) {
                if (camera != null) {
                    camera!!.setPreviewCallback(null)
                    camera!!.stopPreview()
                }
            } else {
                camera = CameraSetting.getInstance(this)
                        .closeCamera(camera)
            }

        }
    }

}