import ExpoModulesCore
import CoreFoundation
import CoreBluetooth
import CoreLocation

public class ExpoBeaconModule: Module {
    private var taskManager: EXTaskManagerInterface? = nil;
    
  // Each module class must implement the definition function. The definition consists of components
  // that describes the module's functionality and behavior.
  // See https://docs.expo.dev/modules/module-api for more details about available components.
  public func definition() -> ModuleDefinition {
    // Sets the name of the module that JavaScript code will use to refer to the module. Takes a string as an argument.
    // Can be inferred from module's class name, but it's recommended to set it explicitly for clarity.
    // The module will be accessible from `requireNativeModule('ExpoBeacon')` in JavaScript.
    Name("ExpoBeacon")

    // Sets constant properties on the module. Can take a dictionary or a closure that returns a dictionary.
    Constants([
      "PI": Double.pi
    ])

    // Defines event names that the module can send to JavaScript.
    Events("onChange")

    // Defines a JavaScript synchronous function that runs the native code on the JavaScript thread.
    Function("hello") {
      return "Hello world! 👋"
    }

    // Defines a JavaScript function that always returns a Promise and whose native code
    // is by default dispatched on the different thread than the JavaScript runtime runs on.
    AsyncFunction("setValueAsync") { (value: String) in
      // Send an event to JavaScript.
      self.sendEvent("onChange", [
        "value": value
      ])
    }

    // Enables the module to be used as a view manager. The view manager definition is built from
    // the definition components used in the closure passed to viewManager.
    // Definition components that are accepted as part of the view manager definition: `View`, `Prop`.
    ViewManager {
      // Defines the factory creating a native view when the module is used as a view.
      View {
        ExpoBeaconView()
      }

      // Defines a setter for the `name` prop.
      Prop("name") { (view: ExpoBeaconView, prop: String) in
        print(prop)
      }
    }
      
      OnCreate {
          log("ExpoBeaconModule", "OnCreate()");
          
          BeaconScanner.shared.create(); // Make a call to create the instance in main thread
          
          self.taskManager = self.appContext!.legacyModule(implementing: EXTaskManagerInterface.self);
          self.appContext!.permissions?.register([
            LocationPermissionRequester(),
            LocationAlwaysPermissionRequester(),
            BluetoothPermissionRequester()
          ])
      }
      
      OnDestroy {
          log("ExpoBeaconModule", "OnDestroy()");
      }
      
      AsyncFunction("startRangingAsync", startRanging)
      AsyncFunction("stopRangingAsync", stopRanging)
      AsyncFunction("hasStartedRangingAsync", hasStartedRanging)
      AsyncFunction("getLocationServiceEnabledAsync", getLocationServiceEnabled)
      AsyncFunction("getBluetoothServiceEnabledAsync", getBluetoothServiceEnabled)
      AsyncFunction("requestLocationPermissionAsync", requestLocationPermission)
      AsyncFunction("requestLocationAlwaysPermissionAsync", requestLocationAlwaysPermission)
      AsyncFunction("requestBluetoothPermissionAsync", requestBluetoothPermission)
  }
    
    private func startRanging(
        taskName: String,
        identifier: String,
        uuid: String,
        major: Int,
        minor: Int,
        promise: Promise) -> Void {
        log("ExpoBeaconModule startRanging()", taskName, identifier, uuid)
        
        var opts: Dictionary<String, Any> = [
            "identifier":identifier,
            "uuid": uuid
        ]
        
        if(major != -1) {
            opts["major"] = major;
        }
        
        if(minor != -1) {
            opts["minor"] = minor;
        }
        
        self.taskManager?.registerTask(withName: taskName,
                                       consumer: BeaconTaskConsumer.self,
                                       options: opts);
        
        promise.resolve();
    }
    
    private func stopRanging(taskName: String, promise: Promise) -> Void {
        var hasRegistered = self.taskManager?.hasRegisteredTask(withName: taskName);
        if(hasRegistered!) {
            self.taskManager?.unregisterTask(withName: taskName, consumerClass: BeaconTaskConsumer.self);
        }
        
        promise.resolve();
    }
    
    
    private func hasStartedRanging(taskName: String, promise: Promise) -> Void {
        let result = self.taskManager?.hasRegisteredTask(withName: taskName);
        promise.resolve(result);
    }
    
    private func requestLocationPermission(promise: Promise) -> Void {
        guard let permissions = self.appContext?.permissions else {
            return promise.reject(PermissionModuleNotFound())
        }
        
        permissions.askForPermission(usingRequesterClass: LocationPermissionRequester.self, resolve: promise.resolver, reject: promise.legacyRejecter)
    }
    
    private func requestLocationAlwaysPermission(promise: Promise) -> Void {
        guard let permissions = self.appContext?.permissions else {
            return promise.reject(PermissionModuleNotFound())
        }
        
        permissions.askForPermission(usingRequesterClass: LocationAlwaysPermissionRequester.self, resolve: promise.resolver, reject: promise.legacyRejecter)
    }
    
    private func requestBluetoothPermission(promise: Promise) -> Void {
        guard let permissions = self.appContext?.permissions else {
            return promise.reject(PermissionModuleNotFound())
        }
        
        permissions.askForPermission(usingRequesterClass: BluetoothPermissionRequester.self, resolve: promise.resolver, reject: promise.legacyRejecter)
    }
    
    private func getLocationServiceEnabled(promise: Promise) -> Void {
        promise.resolve();
    }
    
    private func getBluetoothServiceEnabled(promise: Promise) -> Void {
        promise.resolve();
    }
}
