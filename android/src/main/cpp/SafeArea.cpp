//
// Created by Sergei Golishnikov on 06/03/2022.
//

#include "SafeArea.h"
#include "Macros.h"

#include <utility>
#include "iostream"

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *) {
    return facebook::jni::initialize(vm, [] {
        safeArea::SafeArea::registerNatives();
        safeArea::KeyboardListenerCallback::registerNatives();
    });
};

namespace safeArea {

using namespace facebook;
using namespace facebook::jni;
using namespace facebook::jsi;


using TSelf = local_ref<HybridClass<SafeArea>::jhybriddata>;
using JCallback = std::function<void(std::string, double)>;
using Callbacks = std::map<std::string, std::shared_ptr<facebook::jsi::Function>>;

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

         __android_log_print(ANDROID_LOG_ERROR, "SafeArea", "🥸 listenKeyboard.method");


         JCallback wrapperOnChange =
                 [j = jsCallInvoker_, cc = &callbacks_, r = runtime_](const std::string& type, double height) {
             __android_log_print(ANDROID_LOG_ERROR, "SafeArea", "🥸 listenKeyboardIII %s %f", type.c_str(), height);


             j->invokeAsync([&cc, r, height, type]() {
                 std::shared_ptr<jsi::Function> c = (*cc)["listenKeyboard"];
                 if (!c) return;

                 std::string keyboardState = type == "show" ? "OPENED" : "CLOSED";
                 bool isKeyboardPresent = keyboardState == "OPENED";

                 jsi::Object object = jsi::Object(*r);
                 object.setProperty(*r, "keyboardHeight", jsi::Value(height));
                 object.setProperty(*r, "keyboardState", jsi::String::createFromUtf8(*r, keyboardState));
                 object.setProperty(*r, "isKeyboardPresent", jsi::Value(isKeyboardPresent));
                 c->call(*r, std::move(object));
             });
         };

         __android_log_print(ANDROID_LOG_ERROR, "SafeArea", "🥸 listenKeyboard.wrapped");

         auto obj = KeyboardListenerCallback::newObjectCxxArgs(std::move(wrapperOnChange));
         __android_log_print(ANDROID_LOG_ERROR, "SafeArea", "🥸 listenKeyboard.obj.get");

         auto method =
                 javaPart_->getClass()->getMethod<void(KeyboardListenerCallback::javaobject)>("startListenKeyboard");
         method(javaPart_.get(), obj.get());

         __android_log_print(ANDROID_LOG_ERROR, "SafeArea", "🥸 listenKeyboard.called");

         return jsi::Value::undefined();
     });

    auto closeKeyboard = JSI_HOST_FUNCTION("closeKeyboard", 1) {
         auto callback =  std::make_shared<jsi::Function>(args[0].asObject(runtime).asFunction(runtime));
         JCallback wrapperOnChange =
                 [j = jsCallInvoker_, cc = callback, r = runtime_](const std::string& type, double height) {
             j->invokeAsync([&cc, r, type]() {
                 const std::shared_ptr<jsi::Function>& invoke = cc;
                 if (!invoke) return;
                 jsi::Object object = jsi::Object(*r);
                 object.setProperty(*r, "keyboardHeight", jsi::Value(0));
                 object.setProperty(*r, "keyboardState", jsi::String::createFromUtf8(*r, "hide"));
                 object.setProperty(*r, "isKeyboardPresent", jsi::Value(false));
                 invoke->call(*r, std::move(object));
             });
         };

         auto obj = KeyboardListenerCallback::newObjectCxxArgs(std::move(wrapperOnChange));
         auto method =
                 javaPart_->getClass()->getMethod<void(KeyboardListenerCallback::javaobject)>("closeKeyboard");
         method(javaPart_.get(), obj.get());
         return jsi::Value::undefined();
     });

    auto stopListenKeyboard = JSI_HOST_FUNCTION("stopListenKeyboard", 0) {
         if (!callbacks_["listenKeyboard"]) return jsi::Value::undefined();
         callbacks_.erase("listenKeyboard");
         __android_log_print(ANDROID_LOG_ERROR, "SafeArea", "🥸 stopListenKeyboard");
         auto method =javaPart_->getClass()->getMethod<void()>("stopListenKeyboard");
         method(javaPart_.get());

         return jsi::Value::undefined();
     });

    exportModule.setProperty(*runtime_, "safeArea", std::move(safeArea));
    exportModule.setProperty(*runtime_, "toggleFitsSystemWindows", std::move(toggleFitsSystemWindows));
    exportModule.setProperty(*runtime_, "listenKeyboard", std::move(listenKeyboard));
    exportModule.setProperty(*runtime_, "stopListenKeyboard", std::move(stopListenKeyboard));
    exportModule.setProperty(*runtime_, "closeKeyboard", std::move(closeKeyboard));
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
