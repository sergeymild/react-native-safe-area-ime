#include <fbjni/fbjni.h>
#include <jsi/jsi.h>
#include <ReactCommon/CallInvokerHolder.h>
#import "map"

namespace safeArea {

    using namespace facebook::jsi;

    class KeyboardListenerCallback : public facebook::jni::HybridClass<KeyboardListenerCallback> {
    public:
        static auto constexpr kJavaDescriptor =
                "Lcom/reactnativesafeareaime/SafeArea$KeyboardListenerCallback;";

        void onChange(std::string type, double height) {
            callback_(type, height);
        }

        static void registerNatives() {
            registerHybrid({
                makeNativeMethod("onChange", KeyboardListenerCallback::onChange),
           });
        }

    private:
        friend HybridBase;

        explicit KeyboardListenerCallback(std::function<void(std::string, double)> callback)
                : callback_(std::move(callback)) {}

        std::function<void(std::string, double)> callback_;
    };

    class SafeArea : public facebook::jni::HybridClass<SafeArea> {
    public:
        static constexpr auto kJavaDescriptor = "Lcom/reactnativesafeareaime/SafeArea;";

        static facebook::jni::local_ref<jhybriddata> initHybrid(
                facebook::jni::alias_ref<jhybridobject> jThis,
                jlong jsContext,
                facebook::jni::alias_ref<facebook::react::CallInvokerHolder::javaobject> jsCallInvokerHolder
        );

        static void registerNatives();

        void installJSIBindings();

    private:
        friend HybridBase;
        facebook::jni::global_ref<SafeArea::javaobject> javaPart_;
        facebook::jsi::Runtime *runtime_;
        std::shared_ptr<facebook::react::CallInvoker> jsCallInvoker_;
        std::map<std::string, std::shared_ptr<facebook::jsi::Function>> callbacks_;

        explicit SafeArea(
                facebook::jni::alias_ref<SafeArea::jhybridobject> jThis,
                facebook::jsi::Runtime *rt,
                std::shared_ptr<facebook::react::CallInvoker> jsCallInvoker
        );
    };

}
