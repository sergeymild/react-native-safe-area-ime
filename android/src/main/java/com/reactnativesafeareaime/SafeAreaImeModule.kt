package com.reactnativesafeareaime

import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter
import com.facebook.react.uimanager.PixelUtil


// Is Deprecated?
class SafeAreaImeModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  private val safeArea = SafeArea(reactContext)

  override fun getName(): String {
    return "SafeAreaIme"
  }

  // Example method
  // See https://reactnative.dev/docs/native-modules-android
  @ReactMethod(isBlockingSynchronousMethod = true)
  fun install() {
    safeArea.install(reactApplicationContext)
  }

  var callback: Callback? = null

  private fun sendEvent(
    reactContext: ReactContext,
    eventName: String,
    params: WritableMap
  ) {
    reactContext
      .getJSModule(RCTDeviceEventEmitter::class.java)
      .emit(eventName, params)
  }

  @ReactMethod
  fun stopListenKeyboard() {
    currentActivity?.removeWindowSoftInput()
    println("🥸 stopListenKeyboard")
  }

  @ReactMethod
  fun startListenKeyboard() {
    val currentActivity = reactApplicationContext.currentActivity ?: return
    currentActivity.runOnUiThread {
      var keyboardHeight = 0.0
      currentActivity.removeWindowSoftInput()
      currentActivity.setWindowSoftInput {
        if (keyboardHeight == 0.0) {
          keyboardHeight =
            PixelUtil.toDIPFromPixel(currentActivity.getSoftInputHeight().toFloat()).toDouble()
        }
        val type = if (currentActivity.hasSoftInput()) "show" else "hide"
        sendEvent(reactApplicationContext, "keyboardVisibilityChange", Arguments.createMap().also {
          it.putString("type", type)
          it.putDouble("height", keyboardHeight)
        })
      }
    }
  }
}
