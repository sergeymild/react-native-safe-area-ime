package com.reactnativesafeareaime
import android.content.res.Resources
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.jni.HybridData
import com.facebook.proguard.annotations.DoNotStrip
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.turbomodule.core.CallInvokerHolderImpl
import com.facebook.react.uimanager.PixelUtil
import java.lang.Exception
import java.lang.ref.WeakReference
import kotlin.math.abs

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
    val top = rootWindowInsets.getInsets(WindowInsetsCompat.Type.statusBars()).top
    val t = PixelUtil.toDIPFromPixel(top.toFloat()).toDouble()
    val sh = PixelUtil.toDIPFromPixel(abs(systemHeight).toFloat()).toDouble()
    return doubleArrayOf(
      if (abs(systemHeight) == 0) t else 0.0,
      0.0,
      if (abs(systemHeight) == top) 0.0 else sh,
      0.0,
      PixelUtil.toDIPFromPixel(viewById.width.toFloat()).toDouble(),
      PixelUtil.toDIPFromPixel(viewById.height.toFloat()).toDouble()
    )
  }

  @DoNotStrip
  fun keyboardState(): DoubleArray {
    val currentActivity = context.currentActivity ?: return doubleArrayOf(-1.0, -1.0)
    val keyboardHeight = PixelUtil.toDIPFromPixel(currentActivity.getSoftInputHeight().toFloat()).toDouble()
    val type = if (currentActivity.hasSoftInput()) 1.0 else 0.0
    return doubleArrayOf(type, keyboardHeight)
  }

  var callback: KeyboardListenerCallback? = null

  @ReactMethod
  fun stopListenKeyboard() {
    println("🥸 stopListenKeyboard")
    context.currentActivity?.removeWindowSoftInput()
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
      }
    }
  }
}
