import { requireNativeViewManager } from 'expo-modules-core';
import * as React from 'react';

import { ExpoBeaconViewProps } from './ExpoBeacon.types';

const NativeView: React.ComponentType<ExpoBeaconViewProps> =
  requireNativeViewManager('ExpoBeacon');

export default function ExpoBeaconView(props: ExpoBeaconViewProps) {
  return <NativeView name={props.name} />;
}
