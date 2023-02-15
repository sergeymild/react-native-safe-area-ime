package com.reactnativesafeareaime

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment


//<editor-fold desc="显示隐藏软键盘">

/**
 * 弹出软键盘
 * 如果要求页面显示立刻弹出软键盘，建议在onResume方法中调用
 */
fun EditText.showSoftInput() {
    isFocusable = true
    isFocusableInTouchMode = true
    requestFocus()
    if (isSystemInsetsAnimationSupport()) {
        ViewCompat.getWindowInsetsController(this)?.show(WindowInsetsCompat.Type.ime())
    } else {
        postDelayed({
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(this, 0)
        }, 300)
    }
}

/** 隐藏软键盘 */
fun Activity.hideSoftInput() {
    currentFocus?.let {
        val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(it.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    } ?: let {
        ViewCompat.getWindowInsetsController(window.decorView)?.hide(WindowInsetsCompat.Type.ime())
    }
}


fun Activity.hasSoftInput(): Boolean {
    return ViewCompat.getRootWindowInsets(window.decorView)?.isVisible(WindowInsetsCompat.Type.ime()) ?: false
}

fun Activity.getSoftInputHeight(): Int {
    val navBars = ViewCompat.getRootWindowInsets(window.decorView)?.getInsets(WindowInsetsCompat.Type.navigationBars())?.bottom ?: 0
    val softInputHeight = ViewCompat.getRootWindowInsets(window.decorView)?.getInsets(WindowInsetsCompat.Type.ime())?.bottom
    return ((softInputHeight) ?: 0) - navBars
}

fun Fragment.getSoftInputHeight(): Int {
    return requireActivity().getSoftInputHeight()
}
