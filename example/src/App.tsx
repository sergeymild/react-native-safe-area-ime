import * as React from 'react';
import { useState } from 'react';

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
import { KeyboardState, layout } from 'react-native-safe-area-ime';
import { KeyboardSpacer } from './KeyboardSpacer';

let fitted = true;

export default function App() {
  const [keyboardHeight, setKeyboardHeight] = useState(0);

  console.log('[App.App]', keyboardHeight);

  return (
    <ScrollView
      keyboardDismissMode={'interactive'}
      style={{ flex: 1 }}
      contentContainerStyle={{ flex: 1, backgroundColor: 'white' }}
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
                  native: layout.safeArea,
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
          }}
        >
          <Text style={{ color: 'black' }}>Get</Text>
        </TouchableOpacity>
        <TouchableOpacity
          style={{ height: 56 }}
          onPress={() => {
            fitted = !fitted;
            StatusBar.setBackgroundColor(
              fitted ? 'yellow' : 'transparent',
              false
            );
            layout.toggleFitsSystemWindows(fitted);
          }}
        >
          <Text style={{ color: 'black' }}>Nav color</Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={{ height: 56 }}
          onPress={() => {
            layout.listenKeyboard((params) => {
              setKeyboardHeight(
                params.keyboardState === KeyboardState.CLOSED
                  ? 0
                  : params.keyboardHeight
              );
              console.log('[🥸App.keyboardWillShow]', params);
            });
          }}
        >
          <Text style={{ color: 'black' }}>Listen</Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={{ height: 56 }}
          onPress={() => {
            layout.stopListenKeyboard();
          }}
        >
          <Text style={{ color: 'black' }}>Stop</Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={{ height: 56 }}
          onPress={() => {
            console.log('[App.]', layout.keyboardState());
          }}
        >
          <Text style={{ color: 'black' }}>State</Text>
        </TouchableOpacity>
        <View style={{ flex: 1 }} />
        <TextInput
          style={{
            color: 'black',
            height: 56,
            backgroundColor: 'yellow',
            width: '100%',
          }}
          placeholderTextColor={'black'}
          placeholder={'tap'}
        />
        <KeyboardSpacer handleAndroid />

        {/*<View*/}
        {/*  style={{*/}
        {/*    height: 10,*/}
        {/*    backgroundColor: 'green',*/}
        {/*    width: 100,*/}
        {/*    position: 'absolute',*/}
        {/*    top: t,*/}
        {/*  }}*/}
        {/*/>*/}
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
