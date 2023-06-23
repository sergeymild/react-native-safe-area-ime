import React, { memo, useEffect, useState } from 'react';
import {
  Dimensions,
  EmitterSubscription,
  Keyboard,
  KeyboardEvent,
  LayoutAnimation,
  LayoutAnimationConfig,
  Platform,
  StyleProp,
  StyleSheet,
  View,
  ViewStyle,
} from 'react-native';

// From: https://medium.com/man-moon/writing-modern-react-native-ui-e317ff956f02
const defaultAnimation: LayoutAnimationConfig = {
  duration: 500,
  create: {
    duration: 300,
    type: LayoutAnimation.Types.easeInEaseOut,
    property: LayoutAnimation.Properties.opacity,
  },
  update: {
    type: LayoutAnimation.Types.spring,
    springDamping: 200,
  },
};

interface Props {
  readonly topSpacing?: number;
  readonly onToggle?: (height: number) => void;
  readonly toggleVisible?: (visible: boolean) => void;
  readonly handleAndroid?: boolean;
  readonly style?: StyleProp<ViewStyle>;
  readonly defaultBottomSpacing?: number;
  readonly isBottomTabBarPresent?: boolean;
  readonly openedSpacing?: number;
  readonly isEnabledView?: boolean;
}

export const keyboardState = {
  value: {
    isPresent: false,
    height: 0,
  },
};

export const KeyboardSpacer: React.FC<Props> = memo((props) => {
  const [state, setState] = useState<{
    keyboardSpace: number;
    isKeyboardOpened: boolean;
  }>({ keyboardSpace: 0, isKeyboardOpened: false });

  const handleAndroid = props.handleAndroid ?? true;

  useEffect(() => {
    const updateKeyboardSpace = (event: KeyboardEvent) => {
      if (!event.endCoordinates) return;

      let animationConfig = defaultAnimation;
      if (Platform.OS === 'ios') {
        animationConfig = LayoutAnimation.create(
          event.duration,
          LayoutAnimation.Types[event.easing],
          LayoutAnimation.Properties.opacity
        );
      }
      LayoutAnimation.configureNext(animationConfig);

      // get updated on rotation
      const screenHeight = Dimensions.get('screen').height;
      // when external physical keyboard is connected
      // event.endCoordinates.height still equals virtual keyboard height
      // however only the keyboard toolbar is showing if there should be one
      let screenY =
        Platform.OS === 'ios' ? screenHeight - event.endCoordinates.screenY : 0;
      if (handleAndroid && Platform.OS === 'android')
        screenY = screenHeight - event.endCoordinates.screenY; // + 56
      console.log('🍏[KeyboardSpacer.updateKeyboardSpace]', {
        windowHeight: screenHeight,
        screenHeight: Dimensions.get('screen').height,
        screenY: event.endCoordinates.screenY,
      });

      let keyboardSpace = screenY + (props.topSpacing ?? 0);
      keyboardState.value = { height: keyboardSpace, isPresent: true };
      setState({ keyboardSpace, isKeyboardOpened: true });
      props.onToggle?.(keyboardSpace);
      props.toggleVisible?.(true);
    };

    const resetKeyboardSpace = (event: KeyboardEvent) => {
      let animationConfig = defaultAnimation;
      if (Platform.OS === 'ios') {
        animationConfig = LayoutAnimation.create(
          event.duration,
          LayoutAnimation.Types[event.easing],
          LayoutAnimation.Properties.opacity
        );
      }
      LayoutAnimation.configureNext(animationConfig);
      keyboardState.value = { height: 0, isPresent: false };
      setState({ keyboardSpace: 0, isKeyboardOpened: false });
      props.onToggle?.(0);
      props.toggleVisible?.(false);
    };

    const updateListener =
      Platform.OS === 'android' ? 'keyboardDidShow' : 'keyboardWillShow';
    const resetListener =
      Platform.OS === 'android' ? 'keyboardDidHide' : 'keyboardWillHide';

    let update: EmitterSubscription | undefined;
    let reset: EmitterSubscription | undefined;

    update?.remove();
    reset?.remove();
    update = Keyboard.addListener(updateListener, updateKeyboardSpace);
    reset = Keyboard.addListener(resetListener, resetKeyboardSpace);

    return () => {
      update?.remove();
      reset?.remove();
    };
  }, [handleAndroid, props]);

  if (props.isEnabledView === false) return null;

  const style = (): StyleProp<ViewStyle> => {
    let spacing = props.defaultBottomSpacing ?? 0;
    if (state.keyboardSpace > 0) spacing = props.openedSpacing ?? 0;
    return [
      styles.container,
      { height: state.keyboardSpace + spacing },
      props.style,
    ];
  };

  return <View style={style()} />;
});

const styles = StyleSheet.create({
  container: {
    left: 0,
    right: 0,
    bottom: 0,
  },
});
