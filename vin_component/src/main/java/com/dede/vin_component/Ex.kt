package com.dede.vin_component

import android.content.Context
import android.view.View
import org.jetbrains.anko.defaultSharedPreferences

internal fun View.gone() {
    this.visibility = View.GONE
}

internal fun View.show() {
    this.visibility = View.VISIBLE
}

internal fun Context.putBoolean(key: String, b: Boolean) {
    this.defaultSharedPreferences
            .edit()
            .putBoolean(key, b)
            .apply()
}

internal fun Context.getBoolean(key: String, default: Boolean): Boolean {
    return this.defaultSharedPreferences
            .getBoolean(key, default)
}

