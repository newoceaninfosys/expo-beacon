package expo.modules.beacon

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import expo.modules.beacon.BeaconHelpers.createRangingResponse
import expo.modules.beacon.BeaconHelpers.createRegion
import expo.modules.beacon.BeaconHelpers.prepareBeaconManager
import expo.modules.core.interfaces.LifecycleEventListener
import expo.modules.interfaces.taskManager.TaskConsumer
import expo.modules.interfaces.taskManager.TaskConsumerInterface
import expo.modules.interfaces.taskManager.TaskInterface
import expo.modules.interfaces.taskManager.TaskManagerUtilsInterface
import org.altbeacon.beacon.*

class BeaconTaskConsumer(context: Context?, taskManagerUtils: TaskManagerUtilsInterface?) : TaskConsumer(context, taskManagerUtils), TaskConsumerInterface, LifecycleEventListener {
    private val TAG = "BeaconTaskConsumer"
    private var task: TaskInterface? = null
    private var isHostPaused = true
    private var beaconManager: BeaconManager? = null

    override fun taskType(): String {
        return "beacon";
    }

    override fun didRegister(internalTask: TaskInterface?) {
        Log.i(TAG, "didRegister()")

        task = internalTask;

        beaconManager = prepareBeaconManager(context)

        beaconManager!!.addMonitorNotifier(object : MonitorNotifier {
            override fun didEnterRegion(region: Region) {
                Log.i(TAG, "didEnterRegion()")
//                beaconManager!!.startRangingBeacons(region);
            }

            override fun didExitRegion(region: Region) {
                Log.i(TAG, "didExitRegion()")
//                beaconManager!!.stopRangingBeacons(region);
            }

            override fun didDetermineStateForRegion(state: Int, region: Region) {
                Log.i(TAG, "didDetermineStateForRegion(): $state")
                if(state == MonitorNotifier.OUTSIDE) {
                    beaconManager!!.stopRangingBeacons(region);
                } else {
                    beaconManager!!.startRangingBeacons(region);
                }
            }
        })

        beaconManager!!.addRangeNotifier { beacons, region ->
            task!!.execute(createRangingResponse(beacons, region), null)
        }

        startRanging();
    }

    override fun didUnregister() {
        Log.w(TAG, "didUnregister()");
        stopRanging();
        
        beaconManager!!.removeAllMonitorNotifiers();
        beaconManager!!.removeAllRangeNotifiers();
        beaconManager = null;
        task = null;
    }

    override fun didReceiveBroadcast(intent: Intent?) {
        Log.w(TAG, "didReceiveBroadcast()");
    }

    override fun didExecuteJob(jobService: JobService?, params: JobParameters?): Boolean {
        Log.i(TAG, "didExecuteJob()")

        jobService!!.jobFinished(params, false)
        return true;
    }

    override fun onHostResume() {
        Log.i(TAG, "onHostResume()")
        isHostPaused = false
    }

    override fun onHostPause() {
        Log.i(TAG, "onHostPause()")
        isHostPaused = true
    }

    override fun onHostDestroy() {
        Log.i(TAG, "onHostDestroy()")
        isHostPaused = true
    }

    private fun startRanging() {
        Log.i(TAG, "startRanging()")
        val region = getRegion();

        beaconManager!!.startMonitoring(region)
//        beaconManager!!.startRangingBeacons(region);
//        beaconManager!!.requestStateForRegion(region);
    }

    private fun stopRanging() {
        Log.i(TAG, "stopRanging()")
        val region = getRegion();


        beaconManager!!.stopMonitoring(region)
        beaconManager!!.stopRangingBeacons(region)
    }

    private fun getRegion(): Region {
        val identifier = task!!.options["identifier"] as String;
        val uuid = task!!.options["uuid"] as String;
        val major = task!!.options["major"] as Int;
        val minor = task!!.options["minor"] as Int;

        var region = createRegion(identifier, uuid)
        if(major != -1 && minor != -1) {
            region = if(minor != -1) {
                createRegion(identifier, uuid, major.toString(), minor.toString())
            } else {
                createRegion(identifier, uuid, major.toString())
            }
        }
        return region;
    }

    private val monitoringObserver = Observer<Int> { state ->
        if (state == MonitorNotifier.INSIDE) {
            Log.d(TAG, "Inside region")
        }
        else {
            Log.d(TAG, "Outside region")
        }
    }

    private val rangingObserver = Observer<Collection<Beacon>> { beacons ->
        Log.d(TAG, "Beacons detected >> " + beacons.count())
//        task!!.execute(createRangingResponse(beacons, region), null)
    }
}