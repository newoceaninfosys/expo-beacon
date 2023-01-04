package expo.modules.beacon

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import expo.modules.kotlin.Promise
import expo.modules.interfaces.permissions.PermissionsResponse
import expo.modules.interfaces.permissions.PermissionsStatus
import expo.modules.kotlin.functions.AsyncFunctionWithPromiseComponent
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition

class ExpoBeaconModule : Module() {
  // Each module class must implement the definition function. The definition consists of components
  // that describes the module's functionality and behavior.
  // See https://docs.expo.dev/modules/module-api for more details about available components.
  override fun definition() = ModuleDefinition {
    // Sets the name of the module that JavaScript code will use to refer to the module. Takes a string as an argument.
    // Can be inferred from module's class name, but it's recommended to set it explicitly for clarity.
    // The module will be accessible from `requireNativeModule('ExpoBeacon')` in JavaScript.
    Name("ExpoBeacon")

    // Sets constant properties on the module. Can take a dictionary or a closure that returns a dictionary.
    Constants(
      "PI" to Math.PI
    )

    // Defines event names that the module can send to JavaScript.
    Events("onChange")

    // Defines a JavaScript synchronous function that runs the native code on the JavaScript thread.
    Function("hello") {
      "Hello world! 👋"
    }

    // Defines a JavaScript function that always returns a Promise and whose native code
    // is by default dispatched on the different thread than the JavaScript runtime runs on.
    AsyncFunction("setValueAsync") { value: String ->
      // Send an event to JavaScript.
      sendEvent("onChange", mapOf(
        "value" to value
      ))
    }

    // Enables the module to be used as a view manager. The view manager definition is built from
    // the definition components used in the closure passed to viewManager.
    // Definition components that are accepted as part of the view manager definition: `View`, `Prop`.
    ViewManager {
      // Defines the factory creating a native view when the module is used as a view.
      View { context -> 
        ExpoBeaconView(context) 
      }

      // Defines a setter for the `name` prop.
      Prop("name") { view: ExpoBeaconView, prop: String ->
        println(prop)
      }
    }

    AsyncFunction("startRangingAsync") { taskName: String, identifier: String, uuid: String, major: Int, minor: Int, promise: Promise ->
      val options: Map<String, Any> = mapOf("identifier" to identifier, "uuid" to uuid, "major" to major, "minor" to minor);
      appContext.taskManager!!.registerTask(taskName, BeaconTaskConsumer::class.java, options)
      promise.resolve(true);
    }

    AsyncFunction("stopRangingAsync") { taskName: String, promise: Promise ->
      val isConsumed = appContext.taskManager!!.taskHasConsumerOfClass(taskName, BeaconTaskConsumer::class.java)
      if(isConsumed) {
        appContext.taskManager!!.unregisterTask(taskName, BeaconTaskConsumer::class.java);
      }
      promise.resolve(true);
    }

    AsyncFunction("hasStartedRangingAsync") { taskName: String, promise: Promise ->
      promise.resolve(appContext.taskManager!!.taskHasConsumerOfClass(taskName, BeaconTaskConsumer::class.java));
    }

    AsyncFunction("requestLocationPermissionAsync") { promise: Promise ->
      Log.i("ExpoBeaconModule", "requestLocationPermissionAsync()")
      requestLocationPermission(promise);
    }

    AsyncFunction("requestLocationAlwaysPermissionAsync") { promise: Promise ->
      Log.i("ExpoBeaconModule", "requestLocationAlwaysPermissionAsync()")
      requestLocationPermission(promise);
    }

    AsyncFunction("getLocationServiceEnabledAsync") { promise: Promise ->
      val locationManager = appContext.reactContext!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
      val enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
      promise.resolve(enabled);
    }

    AsyncFunction("getBluetoothServiceEnabledAsync") { promise: Promise ->
      val bluetoothManager = appContext.reactContext!!.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
      val enabled = bluetoothManager.adapter.isEnabled
      promise.resolve(enabled);
    }

    AsyncFunction("requestForegroundPermissionAsync") { promise: Promise ->
      Log.i("ExpoBeaconModule", "requestForegroundPermissionAsync()")
      appContext.permissions!!.askForPermissions(
              { result: Map<String, PermissionsResponse> ->
                promise.resolve(handleForegroundLocationPermissions(result))
              },
              Manifest.permission.ACCESS_FINE_LOCATION,
              Manifest.permission.ACCESS_COARSE_LOCATION
      )
    }

    AsyncFunction("requestBackgroundPermissionAsync") { promise: Promise ->
      Log.i("ExpoBeaconModule", "requestBackgroundPermissionAsync()")

      appContext.permissions!!.askForPermissions(
              { result: Map<String, PermissionsResponse> ->
                promise.resolve(handleBackgroundLocationPermissions(result))
              },
              Manifest.permission.ACCESS_BACKGROUND_LOCATION
      )
    }
  }

  private fun requestLocationPermission(promise: Promise) {
    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
      appContext.permissions!!.askForPermissions(
              { result: Map<String, PermissionsResponse> ->
                promise.resolve(handleLegacyPermissions(result))
              },
              Manifest.permission.ACCESS_FINE_LOCATION,
              Manifest.permission.ACCESS_COARSE_LOCATION,
              Manifest.permission.ACCESS_BACKGROUND_LOCATION
      )
    } else {
      appContext.permissions!!.askForPermissions(
              { result: Map<String, PermissionsResponse> ->
                promise.resolve(handleForegroundLocationPermissions(result))
              },
              Manifest.permission.ACCESS_FINE_LOCATION,
              Manifest.permission.ACCESS_COARSE_LOCATION
      )
    }
  }

  @RequiresApi(Build.VERSION_CODES.Q)
  private fun handleLegacyPermissions(result: Map<String, PermissionsResponse>): Bundle {
    val accessFineLocation = requireNotNull(result[Manifest.permission.ACCESS_FINE_LOCATION])
    val accessCoarseLocation = requireNotNull(result[Manifest.permission.ACCESS_COARSE_LOCATION])
    requireNotNull(result[Manifest.permission.ACCESS_BACKGROUND_LOCATION])
    var status = PermissionsStatus.UNDETERMINED
    var accuracy = "none"
    val canAskAgain = accessCoarseLocation.canAskAgain && accessFineLocation.canAskAgain
    if (accessFineLocation.status == PermissionsStatus.GRANTED) {
      accuracy = "fine"
      status = PermissionsStatus.GRANTED
    } else if (accessCoarseLocation.status == PermissionsStatus.GRANTED) {
      accuracy = "coarse"
      status = PermissionsStatus.GRANTED
    } else if (
            accessFineLocation.status == PermissionsStatus.DENIED &&
            accessCoarseLocation.status == PermissionsStatus.DENIED
    ) {
      status = PermissionsStatus.DENIED
    }
    val resultBundle = Bundle()
    resultBundle.putString(PermissionsResponse.STATUS_KEY, status.status)
    resultBundle.putString(PermissionsResponse.EXPIRES_KEY, PermissionsResponse.PERMISSION_EXPIRES_NEVER)
    resultBundle.putBoolean(PermissionsResponse.CAN_ASK_AGAIN_KEY, canAskAgain)
    resultBundle.putBoolean(PermissionsResponse.GRANTED_KEY, status == PermissionsStatus.GRANTED)
    val androidBundle = Bundle()
    androidBundle.putString("accuracy", accuracy)
    resultBundle.putBundle("android", androidBundle)
    return resultBundle
  }

  private fun handleForegroundLocationPermissions(result: Map<String, PermissionsResponse>): Bundle {
    val accessFineLocation = requireNotNull(result[Manifest.permission.ACCESS_FINE_LOCATION])
    val accessCoarseLocation = requireNotNull(result[Manifest.permission.ACCESS_COARSE_LOCATION])
    var status = PermissionsStatus.UNDETERMINED
    var accuracy = "none"
    val canAskAgain = accessCoarseLocation.canAskAgain && accessFineLocation.canAskAgain
    if (accessFineLocation.status == PermissionsStatus.GRANTED) {
      accuracy = "fine"
      status = PermissionsStatus.GRANTED
    } else if (accessCoarseLocation.status == PermissionsStatus.GRANTED) {
      accuracy = "coarse"
      status = PermissionsStatus.GRANTED
    } else if (
            accessFineLocation.status == PermissionsStatus.DENIED &&
            accessCoarseLocation.status == PermissionsStatus.DENIED
    ) {
      status = PermissionsStatus.DENIED
    }
    return Bundle().apply {
      putString(PermissionsResponse.STATUS_KEY, status.status)
      putString(PermissionsResponse.EXPIRES_KEY, PermissionsResponse.PERMISSION_EXPIRES_NEVER)
      putBoolean(PermissionsResponse.CAN_ASK_AGAIN_KEY, canAskAgain)
      putBoolean(PermissionsResponse.GRANTED_KEY, status == PermissionsStatus.GRANTED)
      val androidBundle = Bundle().apply {
        putString("scoped", accuracy) // deprecated
        putString("accuracy", accuracy)
      }
      putBundle("android", androidBundle)
    }
  }

  private fun handleBackgroundLocationPermissions(result: Map<String, PermissionsResponse>): Bundle {
    val accessBackgroundLocation = requireNotNull(result[Manifest.permission.ACCESS_BACKGROUND_LOCATION])
    var status = PermissionsStatus.UNDETERMINED
    val canAskAgain = accessBackgroundLocation.canAskAgain
    if (accessBackgroundLocation.status == PermissionsStatus.GRANTED) {
      status = PermissionsStatus.GRANTED
    }

    return Bundle().apply {
      putString(PermissionsResponse.STATUS_KEY, status.status)
      putString(PermissionsResponse.EXPIRES_KEY, PermissionsResponse.PERMISSION_EXPIRES_NEVER)
      putBoolean(PermissionsResponse.CAN_ASK_AGAIN_KEY, canAskAgain)
      putBoolean(PermissionsResponse.GRANTED_KEY, status == PermissionsStatus.GRANTED)
    }
  }
}
