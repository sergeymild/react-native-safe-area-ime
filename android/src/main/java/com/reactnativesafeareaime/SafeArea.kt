package com.reactnativesafeareaime
import android.content.res.Resources
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.jni.HybridData
import com.facebook.jni.annotations.DoNotStrip
import com.facebook.react.turbomodule.core.CallInvokerHolderImpl
import com.facebook.react.uimanager.PixelUtil
import java.lang.Exception
import kotlin.math.abs


@DoNotStrip
class KeyboardListenerCallback {
  @DoNotStrip
  private var mHybridData: HybridData? = null

  @DoNotStrip
  constructor(mHybridData: HybridData?) {
    this.mHybridData = mHybridData
  }

  external fun onChange(type: String, height: Double)
}

class SafeArea(var context: ReactApplicationContext) {
  @DoNotStrip
  var mHybridData: HybridData? = null
  external fun initHybrid(
    jsContext: Long,
    jsCallInvokerHolder: CallInvokerHolderImpl?
  ): HybridData?

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
    val viewById = currentActivity.findViewById<View>(R.id.content)
      ?: return DoubleArray(6)
    val heightPixels = Resources.getSystem().displayMetrics.heightPixels
    val systemHeight = viewById.height - heightPixels
    val rootWindowInsets = ViewCompat.getRootWindowInsets(viewById)
      ?: return DoubleArray(6)
    val top = rootWindowInsets.getInsets(WindowInsetsCompat.Type.statusBars()).top
    return doubleArrayOf(
      top.toDouble(), 0.0,
      PixelUtil.toDIPFromPixel(
        if (abs(systemHeight) == top) 0f else abs(systemHeight).toFloat()
      ).toDouble(), 0.0,
      PixelUtil.toDIPFromPixel(viewById.width.toFloat()).toDouble(),
      PixelUtil.toDIPFromPixel(viewById.height.toFloat()).toDouble()
    )
  }

  @DoNotStrip
  fun startListenKeyboard(callback: KeyboardListenerCallback) {
    val currentActivity = context.currentActivity ?: return
    var keyboardHeight = 0.0
    currentActivity.setWindowSoftInput {
      if (keyboardHeight == 0.0) {
        keyboardHeight =
          PixelUtil.toDIPFromPixel(currentActivity.getSoftInputHeight().toFloat()).toDouble()
      }
      val type = if (currentActivity.hasSoftInput()) "keyboardWillShow" else "keyboardWillHide"
      callback.onChange(type, keyboardHeight)
    }
  }
}
