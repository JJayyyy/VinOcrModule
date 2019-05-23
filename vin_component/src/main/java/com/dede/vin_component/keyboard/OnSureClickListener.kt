package com.dede.vin_component.keyboard

import android.widget.EditText

/**
 * 返回true表示调用者拦截事件自己处理
 * Created by hsh on 2019-05-23 10:46
 */
interface OnSureClickListener {
    fun onClick(view: EditText): Boolean
}
