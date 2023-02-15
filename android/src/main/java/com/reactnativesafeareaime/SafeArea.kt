package com.reactnativesafeareaime
import android.content.res.Resources
import android.os.Build
import android.util.DisplayMetrics
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.facebook.jni.HybridData
import com.facebook.proguard.annotations.DoNotStrip
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.turbomodule.core.CallInvokerHolderImpl
import com.facebook.react.uimanager.PixelUtil
import kotlin.math.abs


fun Int.toDp(): Double {
  return PixelUtil.toDIPFromPixel(this.toFloat()).toDouble()
}

class SafeArea(var context: ReactApplicationContext) {
  @DoNotStrip
  var mHybridData: HybridData? = null
  @Suppress("KotlinJniMissingFunction")
  external fun initHybrid(
    jsContext: Long,
    jsCallInvokerHolder: CallInvokerHolderImpl?
  ): HybridData?

  @Suppress("KotlinJniMissingFunction")
  external fun installJSIBindings()
  fun install(context: ReactApplicationContext): Boolean {
    return try {
      System.loadLibrary("react-native-safe-area-ime")
      val jsContext = context.javaScriptContextHolder
      val jsCallInvokerHolder = context.catalystInstance.jsCallInvokerHolder
      mHybridData = initHybrid(
        jsContext.get(),
        jsCallInvokerHolder as CallInvokerHolderImpl
      )
      installJSIBindings()
      true
    } catch (exception: Exception) {
      false
    }
  }

  @DoNotStrip
  fun toggleFitsSystemWindows(isDisabled: Boolean) {
    val currentActivity = context.currentActivity ?: return
    currentActivity.runOnUiThread {
      WindowCompat.setDecorFitsSystemWindows(
        currentActivity.window,
        isDisabled
      )
    }
  }

  @DoNotStrip
  fun safeArea(): DoubleArray {
    val currentActivity = context.currentActivity ?: return DoubleArray(6)
    val viewById = currentActivity.findViewById<View>(android.R.id.content)
      ?: return DoubleArray(6)
    val heightPixels = Resources.getSystem().displayMetrics.heightPixels
    val systemHeight = viewById.height - heightPixels
    val rootWindowInsets = ViewCompat.getRootWindowInsets(viewById)
      ?: return DoubleArray(6)
    val statusBarTop = rootWindowInsets.getInsets(WindowInsetsCompat.Type.statusBars()).top
    val navBottom = rootWindowInsets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
    val bottom = navBottom.toDp()
    //top right bottom left width height
    return doubleArrayOf(
      if (abs(systemHeight) == 0) statusBarTop.toDp() else 0.0,
      0.0,
      // if system height is equal status bar top or zero means that navigation bar height is zero
      if (statusBarTop == abs(systemHeight) || abs(systemHeight) == 0) 0.0 else bottom,
      0.0,
      viewById.width.toDp(),
      viewById.height.toDp()
    )
  }

  var callback: KeyboardListenerCallback? = null
  var closeKeyboardCallback: KeyboardListenerCallback? = null

  @ReactMethod
  fun stopListenKeyboard() {
    println("🥸 stopListenKeyboard")
    context.currentActivity?.window?.removeWindowSoftInput()
    callback?.destroy()
    callback = null
  }

  @ReactMethod
  fun startListenKeyboard(c: KeyboardListenerCallback) {
    this.callback = c
    println("🥸 startListenKeyboard ${callback}")
    val currentActivity = context.currentActivity ?: return
    currentActivity.runOnUiThread {
      currentActivity.removeWindowSoftInput()
      var keyboardHeight = 0.0
      currentActivity.setWindowSoftInput {
        if (keyboardHeight == 0.0) {
          keyboardHeight =
            PixelUtil.toDIPFromPixel(currentActivity.getSoftInputHeight().toFloat()).toDouble()
        }
        val type = if (currentActivity.hasSoftInput()) "show" else "hide"
        callback?.onChange(type, keyboardHeight)
        if (type == "hide" && closeKeyboardCallback != null) {
          closeKeyboardCallback?.onChange(type, 0.0)
          closeKeyboardCallback = null
        }
      }
    }
  }

  @ReactMethod
  fun closeKeyboard(c: KeyboardListenerCallback) {
    val currentActivity = context.currentActivity ?: return
    if (!currentActivity.hasSoftInput()) {
      c.onChange("hide", 0.0)
      return
    }
    if (callback != null) {
      closeKeyboardCallback = c
      currentActivity.hideSoftInput()
      return
    }
    currentActivity.runOnUiThread {
      currentActivity.window.closeKeyboard {
        c.onChange("hide", 0.0)
      }
      currentActivity.hideSoftInput()
    }
  }
}
