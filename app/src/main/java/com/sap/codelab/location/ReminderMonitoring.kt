package com.sap.codelab.location

import android.content.Context
import com.sap.codelab.repository.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Coordinates persistence and the platform boundary. All DB operations are off the UI thread. */
internal class ReminderMonitoring(private val context: Context, private val registrar: GeofenceRegistrar =
    PlayServicesGeofenceRegistrar(context.applicationContext)) {

    suspend fun syncPending() = withContext(Dispatchers.IO) {
        if (!LocationPermissionHelper.canMonitor(context) || !LocationPermissionHelper.notificationAllowed(context)) return@withContext
        registrar.register(Repository.pendingLocationMemos())
    }

    suspend fun remove(memoId: Long) = withContext(Dispatchers.IO) {
        registrar.unregister(memoId)
    }
}
