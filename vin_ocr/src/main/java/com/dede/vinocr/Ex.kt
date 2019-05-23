package com.dede.vinocr

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import com.dede.vinocr.view.VinFinderView

internal fun VinFinderView.getBitmap(resId: Int, width: Int, height: Int): Bitmap {
    val options = BitmapFactory.Options()
    options.inPreferredConfig = Bitmap.Config.ARGB_8888
    val stream = context.resources.openRawResource(resId)
    val bitmap = BitmapFactory.decodeStream(stream, null, options)
    val w = bitmap.width
    val h = bitmap.height
    val matrix = Matrix()
    var scale = width.toFloat() / w
//    val scale1 = height.toFloat() / h
//    if (scale > scale1) scale = scale1
    matrix.postScale(scale, scale)
    return Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true)
}
