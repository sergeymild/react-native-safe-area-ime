package com.reactnativesafeareaime;

import com.facebook.jni.HybridData;
import com.facebook.proguard.annotations.DoNotStrip;

@DoNotStrip
public class KeyboardListenerCallback {
  @DoNotStrip
  private final HybridData mHybridData;

  @DoNotStrip
  public KeyboardListenerCallback(HybridData mHybridData) {
    System.out.println("🥸 KeyboardListenerCallback.constructor");
    this.mHybridData = mHybridData;
  }

  public synchronized void destroy() {
    if (mHybridData != null) {
      mHybridData.resetNative();
    }
  }

  @SuppressWarnings("JavaJniMissingFunction")
  native void onChange(String type, double height);
}
