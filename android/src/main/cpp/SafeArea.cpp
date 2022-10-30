//
// Created by Sergei Golishnikov on 06/03/2022.
//

#include "SafeArea.h"
#include "Macros.h"

#include <utility>
#include "iostream"

namespace safeArea {

using namespace facebook;
using namespace facebook::jni;
using namespace facebook::jsi;

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *) {
    return facebook::jni::initialize(vm, [] {
        SafeArea::registerNatives();
        KeyboardListenerCallback::registerNatives();
    });
};


using TSelf = local_ref<HybridClass<SafeArea>::jhybriddata>;

// JNI binding
void SafeArea::registerNatives() {
    __android_log_print(ANDROID_LOG_ERROR, "SafeArea", "🥸 registerNatives");
    registerHybrid({
       makeNativeMethod("initHybrid", SafeArea::initHybrid),
       makeNativeMethod("installJSIBindings", SafeArea::installJSIBindings),
   });
}

void SafeArea::installJSIBindings() {
    auto exportModule = jsi::Object(*runtime_);
    auto safeArea = JSI_HOST_FUNCTION("safeArea", 0) {
         auto safeArea = jsi::Object(runtime);

         auto method = javaPart_->getClass()->getMethod<jni::JArrayDouble()>("safeArea");
         auto jarray1 = method(javaPart_.get());
         auto a = jarray1->pin();
         safeArea.setProperty(runtime, "top", jsi::Value((double)a[0]));
         safeArea.setProperty(runtime, "right", jsi::Value((double)a[1]));
         safeArea.setProperty(runtime, "bottom", jsi::Value((double)a[2]));
         safeArea.setProperty(runtime, "left", jsi::Value((double)a[3]));
         safeArea.setProperty(runtime, "width", jsi::Value((double)a[4]));
         safeArea.setProperty(runtime, "height", jsi::Value((double)a[5]));

         return safeArea;
     });

    auto toggleFitsSystemWindows = JSI_HOST_FUNCTION("toggleFitsSystemWindows", 1) {
         auto method = javaPart_->getClass()->getMethod<void(bool )>("toggleFitsSystemWindows");
         auto isDisabled = args[0].getBool();
         method(javaPart_.get(), isDisabled);
         return jsi::Value::undefined();
     });

    auto listenKeyboard = JSI_HOST_FUNCTION("listenKeyboard", 1) {
         if (callbacks_["listenKeyboard"]) return jsi::Value::undefined();
         __android_log_print(ANDROID_LOG_ERROR, "SafeArea", "🥸 listenKeyboard");

         auto callback = args[0].asObject(runtime).asFunction(runtime);
         callbacks_["listenKeyboard"] = std::make_shared<jsi::Function>(std::move(callback));

         javaPart_->getClass()->getMethod<void()>()


         return jsi::Value::undefined();
     });

    auto stopListenKeyboard = JSI_HOST_FUNCTION("stopListenKeyboard", 0) {
         if (!callbacks_["listenKeyboard"]) return jsi::Value::undefined();
         callbacks_.erase("listenKeyboard");
         __android_log_print(ANDROID_LOG_ERROR, "SafeArea", "🥸 stopListenKeyboard");

         return jsi::Value::undefined();
     });

    exportModule.setProperty(*runtime_, "safeArea", std::move(safeArea));
    exportModule.setProperty(*runtime_, "toggleFitsSystemWindows", std::move(toggleFitsSystemWindows));
    exportModule.setProperty(*runtime_, "listenKeyboard", std::move(listenKeyboard));
    exportModule.setProperty(*runtime_, "stopListenKeyboard", std::move(stopListenKeyboard));
    runtime_->global().setProperty(*runtime_, "__safeAreaIme", exportModule);
}


SafeArea::SafeArea(
        jni::alias_ref<SafeArea::javaobject> jThis,
        jsi::Runtime *rt,
        std::shared_ptr<facebook::react::CallInvoker> jsCallInvoker)
        : javaPart_(jni::make_global(jThis)),
          runtime_(rt),
          jsCallInvoker_(std::move(jsCallInvoker))
          {}

// JNI init
TSelf SafeArea::initHybrid(
        alias_ref<jhybridobject> jThis,
        jlong jsContext,
        jni::alias_ref<facebook::react::CallInvokerHolder::javaobject> jsCallInvokerHolder
) {
    __android_log_print(ANDROID_LOG_ERROR, "SafeArea", "🥸 initHybrid");
    auto jsCallInvoker = jsCallInvokerHolder->cthis()->getCallInvoker();
    return makeCxxInstance(
            jThis,
            (jsi::Runtime *) jsContext,
            jsCallInvoker
    );
}

}
