##VIN OCR 识别 module
 
    -vinocr
        -libs // 项目所需jar包
        -src
            -main
                -asssets
                    -SmartVisition // 识别配置文件
                        ...
                        smartvisition.lsc // 签名文件，和包名，应用名，公司名绑定
                -java
                    -com.dede.vinocr
                        -keyboard //vin输入键盘
                        -view // 自定义的控件
                            VinEditText.java // 车架号输入框
                            VinFinderView.kt // 识别框
                        ...
                         Ex.kt // 扩展方法 internal fun
                         ScanTipsView.kt // 输入框上的提示tip
                         VinOcrActivity.kt // 识别页面
                -jniLibs // so lib
                -res
                    ...
                    -values
                        resources.xml // 会校验company_name的String资源（公司名）
                 AndroidManifest.xml // 清单配置 识别Service和识别Activity
         .gitignore
         proguard-rules.pro
         README.md