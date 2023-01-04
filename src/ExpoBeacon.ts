import { NativeModulesProxy, EventEmitter, Subscription } from 'expo-modules-core';

// Import the native module. On web, it will be resolved to ExpoBeacon.web.ts
// and on native platforms to ExpoBeacon.ts
import ExpoBeacon from './ExpoBeaconModule';
import ExpoBeaconView from './ExpoBeaconView';
import { ChangeEventPayload, ExpoBeaconViewProps } from './ExpoBeacon.types';

// Get the native constant value.
export const PI = ExpoBeacon.PI;

export function hello(): string {
  return ExpoBeacon.hello();
}

export async function setValueAsync(value: string) {
  return await ExpoBeacon.setValueAsync(value);
}

export async function startRangingAsync(taskName: string, identifier: string, uuid: string, major: number = -1, minor: number = -1) {
  const _major = isNaN(major) ? -1 : major;
  const _minor = isNaN(minor) ? -1 : minor;

  return await ExpoBeacon.startRangingAsync(taskName, identifier, uuid, _major, _minor);
}

export async function stopRangingAsync(taskName: string) {
  return await ExpoBeacon.stopRangingAsync(taskName);
}

export async function hasStartedRangingAsync(taskName: string) {
  return await ExpoBeacon.hasStartedRangingAsync(taskName);
}

export async function requestLocationPermissionAsync() {
  return await ExpoBeacon.requestLocationPermissionAsync();
}

export async function requestLocationAlwaysPermissionAsync() {
  return await ExpoBeacon.requestLocationAlwaysPermissionAsync();
}

export async function getLocationServiceEnabledAsync() {
  return await ExpoBeacon.getLocationServiceEnabledAsync();
}

export async function getBluetoothServiceEnabledAsync() {
  return await ExpoBeacon.getBluetoothServiceEnabledAsync();
}


export async function requestForegroundPermissionAsync () {

}

export async function requestBackgroundPermissionAsync () {
  
}

// For now the events are not going through the JSI, so we have to use its bridge equivalent.
// This will be fixed in the stable release and built into the module object.
// Note: On web, NativeModulesProxy.ExpoBeacon is undefined, so we fall back to the directly imported implementation
const emitter = new EventEmitter(NativeModulesProxy.ExpoBeacon ?? ExpoBeacon);

export function addChangeListener(listener: (event: ChangeEventPayload) => void): Subscription {
  return emitter.addListener<ChangeEventPayload>('onChange', listener);
}

export { ExpoBeaconView, ExpoBeaconViewProps, ChangeEventPayload };
