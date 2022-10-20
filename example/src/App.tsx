import * as React from 'react';

import {
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  View,
} from 'react-native';
import { safeArea } from 'react-native-safe-area-ime';

export default function App() {
  const [result, setResult] = React.useState<number | undefined>();

  React.useEffect(() => {
    console.log('[App.]', safeArea.safeArea);
  }, []);

  return (
    <ScrollView
      keyboardDismissMode={'interactive'}
      style={{ flex: 1 }}
      contentContainerStyle={{ flex: 1 }}
    >
      <View style={styles.container}>
        <TouchableOpacity
          onPress={() => console.log('[🥸App.]', safeArea.safeArea)}
        >
          <Text>Get</Text>
        </TouchableOpacity>

        <TouchableOpacity
          onPress={() => {
            safeArea.listenKeyboard((params) => {
              console.log('[🥸App.keyboardWillShow]', params);
            });
          }}
        >
          <Text>Listen</Text>
        </TouchableOpacity>

        <TouchableOpacity
          onPress={() => {
            safeArea.stopListenKeyboard();
          }}
        >
          <Text>Stop</Text>
        </TouchableOpacity>
        <TextInput placeholder={'tap'} />
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
