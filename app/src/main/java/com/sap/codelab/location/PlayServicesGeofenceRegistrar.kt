package com.sap.codelab.location

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.sap.codelab.model.Memo
import kotlinx.coroutines.tasks.await

/** One-shot geofences delivered directly to a broadcast receiver, even with no Activity open. */
internal class PlayServicesGeofenceRegistrar(private val context: Context) : GeofenceRegistrar {
    private val client = LocationServices.getGeofencingClient(context)
    private val pendingIntent: PendingIntent by lazy {
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
            (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0)
        PendingIntent.getBroadcast(context, 0, Intent(context, GeofenceReceiver::class.java), flags)
    }

    @SuppressLint("MissingPermission") // Explicitly checked at call boundary.
    override suspend fun register(memos: List<Memo>) {
        if (!LocationPermissionHelper.canMonitor(context)) return
        if (memos.size > GeoRules.MAX_ACTIVE_GEOFENCES) {
            error("At most ${GeoRules.MAX_ACTIVE_GEOFENCES} active reminders can be monitored")
        }
        val geofences = memos.filter { GeoRules.validPoint(it.locationLatitude, it.locationLongitude) }
            .filter { !it.isDone && it.notifiedAt == null }
            .map { memo ->
                Geofence.Builder()
                    .setRequestId(GeoRules.requestId(memo.id))
                    .setCircularRegion(memo.locationLatitude!!, memo.locationLongitude!!, GeoRules.RADIUS_METERS)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                    .build()
            }
        if (geofences.isEmpty()) return
        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofences(geofences)
            .build()
        client.addGeofences(request, pendingIntent).await()
    }

    override suspend fun unregister(memoId: Long) {
        client.removeGeofences(listOf(GeoRules.requestId(memoId))).await()
    }
}
