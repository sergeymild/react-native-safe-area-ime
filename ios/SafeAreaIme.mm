#import <React/RCTBridgeModule.h>
#import <React/RCTBridge.h>
#import "Macros.h"

#import <React/RCTBlobManager.h>
#import <React/RCTUIManager.h>
#import <React/RCTBridge+Private.h>
#import <ReactCommon/RCTTurboModule.h>
#import "map"

using namespace facebook;

@interface Helpers:NSObject
- (CGRect) getKeyboardFrame:( NSNotification* )notification;
@end

@implementation Helpers
- (CGRect) getKeyboardFrame:( NSNotification* )notification {
    NSDictionary* keyboardUserInfo = notification.userInfo;
    NSValue* keyboardFrame = [keyboardUserInfo valueForKey: UIKeyboardFrameEndUserInfoKey];
    CGRect keyboardFrameRect = keyboardFrame.CGRectValue;
    return keyboardFrameRect;
}
@end
Helpers *helpers = [[Helpers alloc]init];


@interface SafeAreaIme : NSObject <RCTBridgeModule> {
    jsi::Runtime* runtime_;
    std::shared_ptr<facebook::react::CallInvoker> jsCallInvoker_;
    std::map<std::string, std::shared_ptr<facebook::jsi::Function>> callbacks_;

    UIEdgeInsets safeAreaInsets;
    CGSize screenSize;
}
@end


@implementation SafeAreaIme
RCT_EXPORT_MODULE()

+ (BOOL)requiresMainQueueSetup
{
  return TRUE;
}

RCT_EXPORT_BLOCKING_SYNCHRONOUS_METHOD(install) {
    NSLog(@"Installing SafeAreaIme polyfill Bindings...");
    auto _bridge = [RCTBridge currentBridge];
    auto _cxxBridge = (RCTCxxBridge*)_bridge;
    if (_cxxBridge == nil) return @false;
    runtime_ = (jsi::Runtime*) _cxxBridge.runtime;
    if (runtime_ == nil) return @false;
    jsCallInvoker_ = _bridge.jsCallInvoker;
    [self installJSIBindings];
    
    screenSize = CGSizeZero;
    safeAreaInsets = UIEdgeInsetsZero;

    return @true;
}

-(void)installJSIBindings {
    auto safeArea = JSI_HOST_FUNCTION("safeArea", 0) {
        auto safeArea = jsi::Object(runtime);

        dispatch_sync(dispatch_get_main_queue(), ^{
            safeAreaInsets = UIApplication.sharedApplication.keyWindow.safeAreaInsets;
            screenSize = UIScreen.mainScreen.bounds.size;
        });
        safeArea.setProperty(runtime, "top", safeAreaInsets.top);
        safeArea.setProperty(runtime, "right", safeAreaInsets.right);
        safeArea.setProperty(runtime, "bottom", safeAreaInsets.bottom);
        safeArea.setProperty(runtime, "left", safeAreaInsets.left);
        safeArea.setProperty(runtime, "width", screenSize.width);
        safeArea.setProperty(runtime, "height", screenSize.height);
        
        return safeArea;
    });
    
    auto listenKeyboard = JSI_HOST_FUNCTION("listenKeyboard", 1) {
        if (callbacks_["listenKeyboard"]) return jsi::Value::undefined();
        NSLog(@"🥸 listenKeyboard");
        
        auto callback = args[0].asObject(runtime).asFunction(runtime);
        callbacks_["listenKeyboard"] = std::make_shared<jsi::Function>(std::move(callback));
        
        dispatch_sync(dispatch_get_main_queue(), ^{
            [NSNotificationCenter.defaultCenter addObserver:self
                selector:@selector(handleKeyboardWillShowNotification:)
                name:UIKeyboardWillShowNotification
                object:nil
            ];
            [NSNotificationCenter.defaultCenter addObserver:self
                selector:@selector(handleKeyboardDidShowNotification:)
                name:UIKeyboardDidShowNotification
                object:nil
            ];

            [NSNotificationCenter.defaultCenter addObserver:self
                selector:@selector(handleKeyboardWillHideNotification:)
                name:UIKeyboardWillHideNotification
                object:nil
            ];
            [NSNotificationCenter.defaultCenter addObserver:self
                selector:@selector(handleKeyboardDidHideNotification:)
                name:UIKeyboardDidHideNotification
                object:nil
            ];
        });

        return jsi::Value::undefined();
    });
    
    auto stopListenKeyboard = JSI_HOST_FUNCTION("stopListenKeyboard", 0) {
        if (!callbacks_["listenKeyboard"]) return jsi::Value::undefined();
        NSLog(@"🥸 stopListenKeyboard");
        callbacks_.erase("listenKeyboard");

        dispatch_sync(dispatch_get_main_queue(), ^{
            [NSNotificationCenter.defaultCenter removeObserver:self
                name:UIKeyboardWillShowNotification
                object:nil
            ];
            [NSNotificationCenter.defaultCenter removeObserver:self
                name:UIKeyboardDidShowNotification
                object:nil
            ];

            [NSNotificationCenter.defaultCenter removeObserver:self
                name:UIKeyboardWillHideNotification
                object:nil
            ];
            [NSNotificationCenter.defaultCenter removeObserver:self
                name:UIKeyboardDidHideNotification
                object:nil
            ];
        });

        return jsi::Value::undefined();
    });

    auto exportModule = jsi::Object(*runtime_);
    exportModule.setProperty(*runtime_, "safeArea", std::move(safeArea));
    exportModule.setProperty(*runtime_, "listenKeyboard", std::move(listenKeyboard));
    exportModule.setProperty(*runtime_, "stopListenKeyboard", std::move(stopListenKeyboard));
    runtime_->global().setProperty(*runtime_, "__safeAreaIme", exportModule);
}

- (void)handleKeyboardWillShowNotification: (NSNotification *) notification {
    CGRect keyboardFrameRect = [helpers getKeyboardFrame:notification];

    jsCallInvoker_->invokeAsync([=]() {
        std::shared_ptr<jsi::Function> c = callbacks_["listenKeyboard"];
        if (!c) return;

        jsi::Object object = jsi::Object(*runtime_);

        object.setProperty(*runtime_, "keyboardHeight", jsi::Value(keyboardFrameRect.size.height));
        object.setProperty(*runtime_, "keyboardState", jsi::String::createFromUtf8(*runtime_, "OPENING"));
        object.setProperty(*runtime_, "isKeyboardPresent", jsi::Value(true));

        c->call(*runtime_, std::move(object));
    });
}
- (void)handleKeyboardDidShowNotification: (NSNotification *) notification{
    CGRect keyboardFrameRect = [helpers getKeyboardFrame:notification];

    jsCallInvoker_->invokeAsync([=]() {
        std::shared_ptr<jsi::Function> c = callbacks_["listenKeyboard"];
        if (!c) return;

        jsi::Object object = jsi::Object(*runtime_);

        object.setProperty(*runtime_, "keyboardHeight", jsi::Value(keyboardFrameRect.size.height));
        object.setProperty(*runtime_, "keyboardState", jsi::String::createFromUtf8(*runtime_, "OPENED"));
        object.setProperty(*runtime_, "isKeyboardPresent", jsi::Value(true));

        c->call(*runtime_, std::move(object));
    });
}

- (void)handleKeyboardWillHideNotification: (NSNotification *) notification {
    jsCallInvoker_->invokeAsync([=]() {
        std::shared_ptr<jsi::Function> c = callbacks_["listenKeyboard"];
        if (!c) return;

        jsi::Object object = jsi::Object(*runtime_);

        object.setProperty(*runtime_, "keyboardHeight", jsi::Value(0));
        object.setProperty(*runtime_, "keyboardState", jsi::String::createFromUtf8(*runtime_, "CLOSING"));
        object.setProperty(*runtime_, "isKeyboardPresent", jsi::Value(true));

        c->call(*runtime_, std::move(object));
    });
}
- (void)handleKeyboardDidHideNotification: (NSNotification *) notification{
    jsCallInvoker_->invokeAsync([=]() {
        std::shared_ptr<jsi::Function> c = callbacks_["listenKeyboard"];
        if (!c) return;

        jsi::Object object = jsi::Object(*runtime_);

        object.setProperty(*runtime_, "keyboardHeight", jsi::Value(0));
        object.setProperty(*runtime_, "keyboardState", jsi::String::createFromUtf8(*runtime_, "CLOSED"));
        object.setProperty(*runtime_, "isKeyboardPresent", jsi::Value(false));

        c->call(*runtime_, std::move(object));
    });
}

@end
