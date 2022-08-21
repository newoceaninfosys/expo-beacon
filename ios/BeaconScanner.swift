//
//  BeaconScanner.swift
//  ExpoBeacon
//
//  Created by Dao Sang on 20/08/2022.
//

import Foundation
import CoreLocation;
import ExpoModulesCore
import CoreBluetooth

public class BeaconScanner: NSObject, CLLocationManagerDelegate {
    
    static let shared = BeaconScanner()
    
    private var locationManager: CLLocationManager = CLLocationManager();
    private var centralManager: CBCentralManager!
    private var options: Dictionary<String, Any>? = nil;
    private var locationPermissionRequester: EXPromiseResolveBlock? = nil;
    private var locationAlwaysPermissionRequester: EXPromiseResolveBlock? = nil;
    private var rangeCallback: (() -> ())? = nil;
    
    override init() {
        locationManager.desiredAccuracy = kCLLocationAccuracyBest;
        locationManager.distanceFilter = kCLDistanceFilterNone;
        
        super.init();
        locationManager.delegate = self;
        
//        var queue = DispatchQueue(label: "beacon_operation_queue", attributes: [])
//        centralManager = CBCentralManager(delegate: self, queue: queue);
//        centralManager.delegate = self;
    
    }

    
    // Location Manager Override
    public func locationManager(_ manager: CLLocationManager, didRangeBeacons beacons: [CLBeacon], in region: CLBeaconRegion) {
        log("BeaconTaskConsumer", "didRangeBeacons", beacons)
    }
    
    public func locationManager(_ manager: CLLocationManager, didEnterRegion region: CLRegion) {
        log("BeaconTaskConsumer", "didEnterRegion", region)
    }
    
    public func locationManager(_ manager: CLLocationManager, didExitRegion region: CLRegion) {
        log("BeaconTaskConsumer", "didExitRegion", region)
    }
    
    public func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        if(locationPermissionRequester != nil) {
            let status = getLocationPermission();
            
            locationPermissionRequester!(["status": status.rawValue]);
            locationPermissionRequester = nil;
        }
        
        if(locationAlwaysPermissionRequester != nil) {
            let status = getLocationAlwaysPermission();
            
            locationAlwaysPermissionRequester!(["status": status.rawValue]);
            locationAlwaysPermissionRequester = nil;
        }
    }
    
    // Public Methods
    
    public func create() -> Void {
        // Placeholder method
    }
    
    public func initWithOptions(opts: Dictionary<String, Any>) {
        options = opts;
    }
    
    public func requestLocationPermission(resolver: EXPromiseResolveBlock!) {
        let status = getLocationPermission();
        if(status == EXPermissionStatusGranted) {
            resolver(["status": status]);
        } else {
            locationPermissionRequester = resolver;
            locationManager.requestWhenInUseAuthorization();
        }
    }
    
    public func requestLocationAlwaysPermission(resolver: EXPromiseResolveBlock!) {
        let status = getLocationAlwaysPermission();
        if(status == EXPermissionStatusGranted) {
            resolver(["status": status]);
        } else {
            locationAlwaysPermissionRequester = resolver;
            locationManager.requestAlwaysAuthorization();
        }
    }
    
    public func getLocationPermission() -> EXPermissionStatus {
        var status = EXPermissionStatusDenied;
        var result: CLAuthorizationStatus?;
        if #available(iOS 14.0, *) {
            result = locationManager.authorizationStatus
        } else {
            result = CLLocationManager.authorizationStatus();
        };
 
        if(result == .authorizedWhenInUse || result == .authorizedAlways) {
            status = EXPermissionStatusGranted
        } else if(result == .notDetermined) {
            status = EXPermissionStatusUndetermined
        }
        
        return status;
    }
    
    public func getLocationAlwaysPermission() -> EXPermissionStatus {
        var status = EXPermissionStatusDenied;
        var result: CLAuthorizationStatus?;
        if #available(iOS 14.0, *) {
            result = locationManager.authorizationStatus
        } else {
            result = CLLocationManager.authorizationStatus();
        };
 
        if(result == .authorizedAlways) {
            status = EXPermissionStatusGranted
        } else if(result == .notDetermined) {
            status = EXPermissionStatusUndetermined
        }
        
        return status;
    }
    
    public func getBluetoothPermission() -> EXPermissionStatus {
        var status = EXPermissionStatusDenied;
        if #available(iOS 13.1, *) {
            if(CBCentralManager().authorization == .allowedAlways) {
                status = EXPermissionStatusGranted
            }
        } else {
            if(CBPeripheralManager.authorizationStatus() == .authorized) {
                status = EXPermissionStatusGranted
            }
        }
        
        return status;
    }
    
    public func startRanging() {
        let uuid: UUID = UUID(uuidString: options!["uuid"] as! String)!;
        let identifier = options!["identifier"] as! String;
        var region = self.createRegion(identifier: identifier, uuid: uuid);
        if(
            options!["major"] != nil &&
            options!["major"] as! String != "" &&
            options!["minor"] != nil &&
            options!["minor"] as! String != ""
        ) {
            let major = UInt16(truncating: options!["major"] as! NSNumber);
            let minor = UInt16(truncating: options!["minor"] as! NSNumber);
            region = self.createRegion(identifier: identifier, uuid: uuid, major: major, minor: minor);
        }
 
        
        locationManager.startMonitoring(for: region)
        locationManager.startRangingBeacons(in: region)
        locationManager.startUpdatingLocation()
    }
    
    public func stopRanging() {
        let uuid: UUID = UUID(uuidString: options!["uuid"] as! String)!;
        let identifier = options!["identifier"] as! String;
        let region = self.createRegion(identifier: identifier, uuid: uuid)
 
        locationManager.stopMonitoring(for: region)
        locationManager.stopRangingBeacons(in: region)
        locationManager.stopUpdatingLocation()
        rangeCallback = nil;
    }
    
    private func createRegion(identifier: String, uuid: UUID) -> CLBeaconRegion {
        if #available(iOS 13.0, *) {
            return CLBeaconRegion.init(uuid: uuid, identifier: identifier)
        } else {
            return CLBeaconRegion.init(proximityUUID: uuid, identifier: identifier)
        };
    }
    
    private func createRegion(identifier: String, uuid: UUID, major: UInt16, minor: UInt16) -> CLBeaconRegion {
        if #available(iOS 13.0, *) {
            return CLBeaconRegion.init(uuid: uuid, major: major, minor: minor, identifier: identifier)
        } else {
            return CLBeaconRegion.init(proximityUUID: uuid, major: major, minor: minor, identifier: identifier)
        };
    }
}
