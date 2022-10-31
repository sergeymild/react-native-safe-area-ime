import * as React from 'react';

import {
  Dimensions,
  ScrollView,
  StatusBar,
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  View,
} from 'react-native';
import { safeArea } from 'react-native-safe-area-ime';
import { useState } from 'react';

let fitted = true;

export default function App() {
  const [b, sB] = useState(0);
  const [t, sT] = useState(0);

  return (
    <ScrollView
      keyboardDismissMode={'interactive'}
      style={{ flex: 1 }}
      contentContainerStyle={{ flex: 1, backgroundColor: 'red' }}
    >
      <View style={styles.container}>
        <TouchableOpacity
          style={{ height: 56 }}
          onPress={() => {
            const window = Dimensions.get('window');
            const screen = Dimensions.get('screen');
            console.log(
              JSON.stringify(
                {
                  native: safeArea.safeArea,
                  window,
                  screen,
                  wP: { h: window.height - StatusBar.currentHeight! },
                  sP: { h: screen.height - StatusBar.currentHeight! },
                  stb: StatusBar.currentHeight,
                },
                undefined,
                2
              )
            );
            sB(safeArea.safeArea.height - 10 - safeArea.safeArea.bottom);
            sT(safeArea.safeArea.top);
          }}
        >
          <Text>Get</Text>
        </TouchableOpacity>
        <TouchableOpacity
          style={{ height: 56 }}
          onPress={() => {
            fitted = !fitted;
            StatusBar.setBackgroundColor(
              fitted ? 'yellow' : 'transparent',
              false
            );
            safeArea.toggleFitsSystemWindows(fitted);
          }}
        >
          <Text>Nav color</Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={{ height: 56 }}
          onPress={() => {
            safeArea.listenKeyboard((params) => {
              console.log('[🥸App.keyboardWillShow]', params);
            });
          }}
        >
          <Text>Listen</Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={{ height: 56 }}
          onPress={() => {
            safeArea.stopListenKeyboard();
          }}
        >
          <Text>Stop</Text>
        </TouchableOpacity>
        <TextInput placeholder={'tap'} />
        <View
          style={{
            height: 10,
            backgroundColor: 'yellow',
            width: 100,
            position: 'absolute',
            top: b,
          }}
        />

        <View
          style={{
            height: 10,
            backgroundColor: 'green',
            width: 100,
            position: 'absolute',
            top: t,
          }}
        />
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
