//
//  LocationPermissionRequester.swift
//  ExpoBeacon
//
//  Created by Dao Sang on 20/08/2022.
//

import Foundation
import CoreLocation
import ExpoModulesCore

public class LocationPermissionRequester : NSObject, EXPermissionsRequester {
    public func getPermissions() -> [AnyHashable : Any]! {
        let status = BeaconScanner.shared.getLocationPermission();
        return ["status": status.rawValue];
    }
    
    public static func permissionType() -> String! {
        return "location";
    }
    
    public func requestPermissions(resolver resolve: EXPromiseResolveBlock!, rejecter reject: EXPromiseRejectBlock!) {
        BeaconScanner.shared.requestLocationPermission(resolver: resolve);
    }
}
