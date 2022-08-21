//
//  BeaconExceptions.swift
//  ExpoBeacon
//
//  Created by Dao Sang on 20/08/2022.
//

import Foundation
import ExpoModulesCore


internal class PermissionModuleNotFound : Exception {
    override var reason: String {
        "Permissions module not found. Are you sure that Expo modules are properly linked?"
    }
}
