#import <React/RCTBridgeModule.h>
#import <React/RCTBridge.h>
#import "Macros.h"

#import <React/RCTBlobManager.h>
#import <React/RCTUIManager.h>
#import <React/RCTBridge+Private.h>
#import <ReactCommon/RCTTurboModule.h>
#import "map"

using namespace facebook;

@interface SafeAreaIme : NSObject <RCTBridgeModule> {
    CGFloat keyboardheight;
}
@end


@implementation SafeAreaIme
RCT_EXPORT_MODULE()

jsi::Runtime* runtime_;
std::shared_ptr<facebook::react::CallInvoker> jsCallInvoker_;
std::map<std::string, std::shared_ptr<facebook::jsi::Function>> callbacks_;

UIEdgeInsets safeAreaInsets = UIEdgeInsetsZero;
CGSize screenSize = CGSizeZero;

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
            [[NSNotificationCenter defaultCenter] addObserver:self
             selector:@selector(keyboardDidShow:)
                 name:UIKeyboardWillShowNotification
               object:nil];

            [[NSNotificationCenter defaultCenter] addObserver:self
                selector:@selector(keyboardDidShow:)
                    name:UIKeyboardWillChangeFrameNotification
                    object:nil];
            
            [[NSNotificationCenter defaultCenter] addObserver:self
             selector:@selector(keyboardDidHide:)
                 name:UIKeyboardWillHideNotification
               object:nil];
        });
        
       
        
        return jsi::Value::undefined();
    });
    
    auto stopListenKeyboard = JSI_HOST_FUNCTION("stopListenKeyboard", 0) {
        if (!callbacks_["listenKeyboard"]) return jsi::Value::undefined();
        NSLog(@"🥸 stopListenKeyboard");
        callbacks_.erase("listenKeyboard");
        
        
        dispatch_sync(dispatch_get_main_queue(), ^{
            [[NSNotificationCenter defaultCenter]
             removeObserver:self
             name:UIKeyboardWillShowNotification
             object:nil];

            [[NSNotificationCenter defaultCenter]
             removeObserver:self
             name:UIKeyboardWillChangeFrameNotification
             object:nil];
            
            [[NSNotificationCenter defaultCenter]
             removeObserver:self
             name:UIKeyboardWillHideNotification
             object:nil];
        });
    
        
        return jsi::Value::undefined();
    });

    auto exportModule = jsi::Object(*runtime_);
    exportModule.setProperty(*runtime_, "safeArea", std::move(safeArea));
    exportModule.setProperty(*runtime_, "listenKeyboard", std::move(listenKeyboard));
    exportModule.setProperty(*runtime_, "stopListenKeyboard", std::move(stopListenKeyboard));
    runtime_->global().setProperty(*runtime_, "__safeAreaIme", exportModule);
}


- (void)keyboardDidShow: (NSNotification *) notif{
    
    NSDictionary* keyboardInfo = [notif userInfo];
    NSValue* keyboardFrameBegin = [keyboardInfo valueForKey:UIKeyboardFrameEndUserInfoKey];
    CGRect keyboardFrameBeginRect = [keyboardFrameBegin CGRectValue];
    keyboardheight = keyboardFrameBeginRect.size.height;
    
    jsCallInvoker_->invokeAsync([=]() {
        std::shared_ptr<jsi::Function> c = callbacks_["listenKeyboard"];
        if (!c) return;
        
        jsi::Object object = jsi::Object(*runtime_);
        object.setProperty(*runtime_, "type", jsi::String::createFromUtf8(*runtime_, "show"));
        object.setProperty(*runtime_, "height", jsi::Value(keyboardheight));
        c->call(*runtime_, std::move(object));
    });
}

- (void)keyboardDidHide: (NSNotification *) notif{
    
    jsCallInvoker_->invokeAsync([=]() {
        std::shared_ptr<jsi::Function> c = callbacks_["listenKeyboard"];
        if (!c) return;
        
        jsi::Object object = jsi::Object(*runtime_);
        object.setProperty(*runtime_, "type", jsi::String::createFromUtf8(*runtime_, "hide"));
        object.setProperty(*runtime_, "height", jsi::Value(keyboardheight));
        
        c->call(*runtime_, std::move(object));
    });
}

@end
