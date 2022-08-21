//
//  BeaconTaskConsumer.swift
//  ExpoBeacon
//
//  Created by Dao Sang on 20/08/2022.
//

import Foundation
import ExpoModulesCore
import CoreLocation

class BeaconTaskConsumer : NSObject, EXTaskConsumerInterface {
    var task: EXTaskInterface?;
    
    override init() {
        super.init();
    }
    
    func taskType() -> String {
        return "beacon"
    }
    
    func didRegisterTask(_ task: EXTaskInterface) {
        log("BeaconTaskConsumer", "didRegisterTask()", task.options)
        self.task = task;
        var opts: Dictionary<String, Any> = [
            "identifier": task.options!["identifier"] as! String,
            "uuid": task.options!["uuid"] as! String
        ]
        if(task.options!["major"] != nil) {
            opts["major"] = task.options!["major"] as! NSNumber;
        }
        
        if(task.options!["minor"] != nil) {
            opts["minor"] = task.options!["minor"] as! NSNumber;
        }
        BeaconScanner.shared.initWithOptions(opts: opts)
        BeaconScanner.shared.startRanging()
    }
    
    func didBecomeReadyToExecuteWithData(_ data: NSDictionary) {
        log("BeaconTaskConsumer didBecomeReadyToExecuteWithData()", data);
    }
    
    func didUnregister() {
        log("BeaconTaskConsumer didUnregister()")
        
        BeaconScanner.shared.stopRanging();
        
        self.task = nil;
    }
    
    func didFinish() {
        log("BeaconTaskConsumer didFinish()")
    }
}
