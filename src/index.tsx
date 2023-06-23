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

export enum KeyboardState {
  CLOSED = 'CLOSED',
  OPENING = 'OPENING',
  OPENED = 'OPENED',
  CLOSING = 'CLOSING',
}

export type NativeKeyboardNotificationsListenerParams = {
  keyboardHeight: number;
  keyboardState: KeyboardState;
  isKeyboardPresent: boolean;
};
export type NativeKeyboardNotificationsListener = (
  params: NativeKeyboardNotificationsListenerParams
) => void;

declare global {
  var __safeAreaIme: {
    safeArea(): SafeAreaModel;
    listenKeyboard(callback: NativeKeyboardNotificationsListener): void;
    stopListenKeyboard(): void;
    toggleFitsSystemWindows(isDisabled: boolean): void;
    keyboardState(): { state: KeyboardState; height: number };
  };
}

export interface SafeAreaModel {
  top: number;
  right: number;
  bottom: number;
  left: number;
  width: number;
  height: number;
}

export const layout = {
  get safeArea(): SafeAreaModel {
    return global.__safeAreaIme.safeArea();
  },

  listenKeyboard(callback: NativeKeyboardNotificationsListener) {
    global.__safeAreaIme.listenKeyboard(callback);
  },

  stopListenKeyboard() {
    global.__safeAreaIme.stopListenKeyboard();
  },

  toggleFitsSystemWindows(isDisabled: boolean) {
    if (Platform.OS !== 'android') return;
    global.__safeAreaIme.toggleFitsSystemWindows(isDisabled);
  },

  keyboardState() {
    if (Platform.OS !== 'android') return;
    return global.__safeAreaIme.keyboardState();
  },
};
