//
//  LocationAlwaysPermissionRequester.swift
//  ExpoBeacon
//
//  Created by Dao Sang on 21/08/2022.
//

import Foundation
import CoreLocation
import ExpoModulesCore

public class LocationAlwaysPermissionRequester : NSObject, EXPermissionsRequester {
    public func getPermissions() -> [AnyHashable : Any]! {
        let status = BeaconScanner.shared.getLocationAlwaysPermission();
        return ["status": status.rawValue];
    }
    
    public static func permissionType() -> String! {
        return "location";
    }
    
    public func requestPermissions(resolver resolve: EXPromiseResolveBlock!, rejecter reject: EXPromiseRejectBlock!) {
        BeaconScanner.shared.requestLocationAlwaysPermission(resolver: resolve);
    }
}
