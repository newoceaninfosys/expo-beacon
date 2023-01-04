package expo.modules.beacon

import android.content.Context
import android.os.Bundle
import org.altbeacon.beacon.*


object BeaconHelpers {
    private val TAG = BeaconHelpers::class.java.simpleName

    fun prepareBeaconManager(context: Context) : BeaconManager {
        val beaconManager = BeaconManager.getInstanceForApplication(context.applicationContext)

        beaconManager.beaconParsers.clear()
        beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"))
//        beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"))
//        beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"))
        beaconManager.setEnableScheduledScanJobs(false)
        beaconManager.isRegionStatePersistenceEnabled = false
        beaconManager.backgroundBetweenScanPeriod = 1000;
        beaconManager.foregroundBetweenScanPeriod = 1000;
        beaconManager.backgroundScanPeriod = 1000;
        beaconManager.foregroundScanPeriod = 1000;

        return beaconManager;
    }

    fun createRangingResponse(beacons: Collection<Beacon>, region: Region): Bundle {
        val beaconBundles = ArrayList<Bundle>();
        for (beacon in beacons) {
            val uuid: String = beacon.id1.toString();
            var major: Int? = null
            var minor: Int? = null
            var distance: Double;
            var proximity: String;

            if (beacon.identifiers.size > 2) {
                major = beacon.id2.toInt()
                minor= beacon.id3.toInt()
            }
            val rssi: Int = beacon.rssi;
            if (beacon.distance == Double.POSITIVE_INFINITY || java.lang.Double.isNaN(beacon.distance)
                    || beacon.distance == Double.NEGATIVE_INFINITY) {
                distance = 999.0
                proximity = "far"
            } else {
                distance = beacon.distance
                proximity = getProximity(beacon.distance)
            }
            beaconBundles.add(Bundle().apply {
                putString("uuid", uuid)
                if (major != null) {
                    putInt("major", major)
                }
                if (minor != null) {
                    putInt("minor", minor)
                }
                putDouble("distance", distance)
                putString("proximity", proximity)
                putInt("rssi", rssi)
            })
        }
        return Bundle().apply {
            putString("identifier", region.uniqueId)
            putString("uuid", if (region.id1 != null) region.id1.toString() else null)
            putParcelableArrayList("beacons", beaconBundles)
        };
    }

    private fun getProximity(distance: Double): String {
        return when {
            distance == -1.0 -> {
                "unknown"
            }
            distance < 1 -> {
                "immediate"
            }
            distance < 3 -> {
                "near"
            }
            else -> {
                "far"
            }
        }
    }

    fun createRegion(identifier: String, uuid: String): Region {
        return Region(identifier, Identifier.parse(uuid), null, null)
    }

    fun createRegion(identifier: String, uuid: String, major: String): Region {
        return Region(
                identifier,
                Identifier.parse(uuid),
                Identifier.parse(major),
                null
        )
    }

    fun createRegion(identifier: String, uuid: String, major: String, minor: String): Region {
        return Region(
                identifier,
                Identifier.parse(uuid),
                Identifier.parse(major),
                Identifier.parse(minor)
        )
    }
}