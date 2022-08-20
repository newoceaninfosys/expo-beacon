import * as React from 'react';

import { ExpoBeaconViewProps } from './ExpoBeacon.types';

function ExpoBeaconWebView(props: ExpoBeaconViewProps) {
  return (
    <div>
      <span>{props.name}</span>
    </div>
  );
}

export default ExpoBeaconWebView;
