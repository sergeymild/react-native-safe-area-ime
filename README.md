# react-native-safe-area-ime

## Installation

```sh
npm install react-native-safe-area-ime
```

## Usage

```js
import { safeArea } from 'react-native-safe-area-ime';

// ...

export interface SafeAreaModel {
  top: number;
  right: number;
  bottom: number;
  left: number;
  width: number;
  height: number;
}

export const safeArea = {
  get safeArea(): SafeAreaModel {
    return global.__safeAreaIme.safeArea();
  },

  listenKeyboard(callback: (params: { type: string; height: number }) => void) {
    global.__safeAreaIme.listenKeyboard(callback);
  },

  stopListenKeyboard() {
    global.__safeAreaIme.stopListenKeyboard();
  },

  toggleFitsSystemWindows(isDisabled: boolean) {
    if (Platform.OS !== 'android') return;
    global.__safeAreaIme.toggleFitsSystemWindows(isDisabled);
  },
};
