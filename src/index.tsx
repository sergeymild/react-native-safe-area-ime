import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-safe-area-ime' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo managed workflow\n';

const SafeAreaIme = NativeModules.SafeAreaIme
  ? NativeModules.SafeAreaIme
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

SafeAreaIme.install();

export const safeArea = {
  get safeArea(): { top: number; right: number; bottom: number; left: number } {
    return global.__safeAreaIme.safeArea();
  },

  listenKeyboard(callback: (params: { type: string; height: number }) => void) {
    global.__safeAreaIme.listenKeyboard(callback);
  },

  stopListenKeyboard() {
    global.__safeAreaIme.stopListenKeyboard();
  },
};
