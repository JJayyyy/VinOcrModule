package com.dede.vinocrmodule

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.dede.vin_component.keyboard.VinKeyboard
import com.dede.vin_component.view.TextWatcherImpl
import com.dede.vin_component.view.VinEditText
import com.dede.vinocr.EXTRA_VIN_CODE
import com.dede.vinocr.VinOcrActivity
import kotlinx.android.synthetic.main.activity_main.*

private const val REQUEST_VIN_OCR = 1
private const val REQUEST_PERMISSION = 2

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        VinKeyboard(this, et_vin)

        et_vin.addTextChangedListener(VinEditText.VinTextWatcher(et_vin, object : TextWatcherImpl() {
            override fun afterTextChanged(s: Editable) {
                val l = s.length
                tv_hint.text = "已输入${l}位，还差${17 - l}位"

                // todo something
            }
        }))
    }

    fun vinOcr(view: View) {
        val permission = checkPermission(Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (permission.isEmpty()) {
            startOcr()
        } else {
            ActivityCompat.requestPermissions(this, permission.toTypedArray(), REQUEST_PERMISSION)
        }
    }

    private fun startOcr() {
        startActivityForResult(Intent(this, VinOcrActivity::class.java), REQUEST_VIN_OCR)
    }

    private fun checkPermission(vararg permissions: String): List<String> {
        val deniedList = ArrayList<String>()
        for (permission in permissions) {
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, permission)) {
                deniedList.add(permission)
            }
        }
        return deniedList
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != REQUEST_PERMISSION) return

        val list = grantResults.filter { it == PackageManager.PERMISSION_DENIED }
        if (list.isEmpty()) {
            startOcr()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        if (requestCode != REQUEST_VIN_OCR) return
        val vin = (data ?: return).getStringExtra(EXTRA_VIN_CODE)
        et_vin.setText(vin)
    }
}
