package com.reactnativesafeareaime;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;

import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.facebook.jni.HybridData;
import com.facebook.jni.annotations.DoNotStrip;
import com.facebook.react.bridge.JavaScriptContextHolder;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.turbomodule.core.CallInvokerHolderImpl;
import com.facebook.react.turbomodule.core.interfaces.CallInvokerHolder;
import com.facebook.react.uimanager.PixelUtil;

public class SafeArea {
  @DoNotStrip
  @SuppressWarnings("unused")
  HybridData mHybridData;
  ReactApplicationContext context;

  public SafeArea(ReactApplicationContext context) {
    this.context = context;
  }

  public native HybridData initHybrid(
    long jsContext,
    CallInvokerHolderImpl jsCallInvokerHolder);

  public native void installJSIBindings();

  public boolean install(ReactApplicationContext context) {
    try {
      System.loadLibrary("react-native-safe-area-ime");
      JavaScriptContextHolder jsContext = context.getJavaScriptContextHolder();
      CallInvokerHolder jsCallInvokerHolder = context.getCatalystInstance().getJSCallInvokerHolder();

      mHybridData = initHybrid(
        jsContext.get(),
        (CallInvokerHolderImpl) jsCallInvokerHolder
      );

      installJSIBindings();
      return true;
    } catch (Exception exception) {
      return false;
    }
  }

  @DoNotStrip
  void toggleFitsSystemWindows(boolean isDisabled) {
    Activity currentActivity = context.getCurrentActivity();
    if (currentActivity == null) return;
    currentActivity.runOnUiThread(() ->
      WindowCompat.setDecorFitsSystemWindows(currentActivity.getWindow(), isDisabled)
    );
  }

  @DoNotStrip
  double[] safeArea() {
    Activity currentActivity = context.getCurrentActivity();
    if (currentActivity == null) return new double[6];
    View viewById = currentActivity.findViewById(android.R.id.content);
    if (viewById == null) return new double[6];
    int heightPixels = Resources.getSystem().getDisplayMetrics().heightPixels;
    int systemHeight = viewById.getHeight() - heightPixels;
    WindowInsetsCompat rootWindowInsets = ViewCompat.getRootWindowInsets(viewById);
    if (rootWindowInsets == null) return new double[6];
    int top = rootWindowInsets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
    return new double[]{
      top,
      0,
      PixelUtil.toDIPFromPixel(Math.abs(systemHeight) == top ? 0 : Math.abs(systemHeight)),
      0,
      PixelUtil.toDIPFromPixel(viewById.getWidth()),
      PixelUtil.toDIPFromPixel(viewById.getHeight()),
    };
  }

  @DoNotStrip
  void startListenKeyboard() {
    double keyboardHeight = 0.0;
    setWindowSoftInput {
      if (keyboardHeight == 0.0) {
        keyboardHeight = PixelUtil.toDIPFromPixel(getSoftInputHeight().toFloat()).toDouble()
      }
      val method = if (hasSoftInput()) "keyboardWillShow" else "keyboardWillHide"
      MainApplication.methodChannel.invokeMethod(method, keyboardHeight)
    }
  }
}
