//
//  BluetoothPermissionRequester.swift
//  ExpoBeacon
//
//  Created by Dao Sang on 20/08/2022.
//

import Foundation
import CoreBluetooth
import ExpoModulesCore

public class BluetoothPermissionRequester : NSObject, EXPermissionsRequester {
    public static func permissionType() -> String! {
        return "bluetooth"
    }
    
    public func requestPermissions(resolver resolve: EXPromiseResolveBlock!, rejecter reject: EXPromiseRejectBlock!) {
        resolve(self.getPermissions());
    }
    
    public func getPermissions() -> [AnyHashable : Any]! {
        let status = BeaconScanner.shared.getBluetoothPermission();
        return ["status": status.rawValue];
    }
}
