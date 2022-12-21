# react-native-safe-area-ime

## Installation

add in package.json
```sh
"react-native-safe-area-ime": "sergeymild/react-native-safe-area-ime#0.7.1"
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

  toggleFitsSystemWindows(isDisabled: boolean) {
    if (Platform.OS !== 'android') return;
    global.__safeAreaIme.toggleFitsSystemWindows(isDisabled);
  },
};
