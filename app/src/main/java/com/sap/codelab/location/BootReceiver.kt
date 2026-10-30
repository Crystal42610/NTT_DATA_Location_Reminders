package com.sap.codelab.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Google geofence registrations are lost after reboot; restore pending memos. */
internal class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try { ReminderMonitoring(context.applicationContext).syncPending() }
            catch (e: Exception) { Log.w("BootReceiver", "Could not restore reminders", e) }
            finally { pending.finish() }
        }
    }
}
