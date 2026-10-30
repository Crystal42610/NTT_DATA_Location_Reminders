package com.sap.codelab.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.sap.codelab.notifications.ReminderNotifier
import com.sap.codelab.repository.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class GeofenceReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val event = GeofencingEvent.fromIntent(intent) ?: return
        if (event.hasError() || event.geofenceTransition != Geofence.GEOFENCE_TRANSITION_ENTER) return
        val ids = event.triggeringGeofences.orEmpty().mapNotNull { GeoRules.memoId(it.requestId) }.distinct()
        if (ids.isEmpty()) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val notifier = ReminderNotifier(context.applicationContext)
                for (id in ids) {
                    // Don't consume the one-shot alert when Android notification permission is disabled.
                    if (!LocationPermissionHelper.notificationAllowed(context)) continue
                    val memo = Repository.claimNotification(id) ?: continue
                    notifier.notify(memo)
                    try { PlayServicesGeofenceRegistrar(context).unregister(id) }
                    catch (e: Exception) { Log.w("GeofenceReceiver", "Remove failed for $id", e) }
                }
            } catch (e: Exception) { Log.e("GeofenceReceiver", "Processing location reminder failed", e) }
            finally { pending.finish() }
        }
    }
}
