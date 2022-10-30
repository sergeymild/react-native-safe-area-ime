package com.reactnativesafeareaime

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod

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

}
