# react-native-safe-area-ime

## Installation

add in package.json
```sh
"react-native-safe-area-ime": "sergeymild/react-native-safe-area-ime#0.71.2"
```

## Usage

```js
import { layout } from 'react-native-safe-area-ime';

// ...

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

  listenKeyboard(callback: (params: { type: string; height: number }) => void) {
    global.__safeAreaIme.listenKeyboard(callback);
  },

  stopListenKeyboard() {
    global.__safeAreaIme.stopListenKeyboard();
  },

  // close and invoke callback after keyboard dismiss
  closeKeyboard(callback: (params: { type: string; height: number } => void) {
    global.__safeAreaIme.closeKeyboard();
  },

  toggleFitsSystemWindows(isDisabled: boolean) {
    if (Platform.OS !== 'android') return;
    global.__safeAreaIme.toggleFitsSystemWindows(isDisabled);
  },
};
