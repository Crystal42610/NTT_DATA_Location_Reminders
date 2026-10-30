package com.sap.codelab.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.sap.codelab.R
import com.sap.codelab.model.Memo
import com.sap.codelab.view.detail.BUNDLE_MEMO_ID
import com.sap.codelab.view.detail.ViewMemo

internal class ReminderNotifier(private val context: Context) {
    private val channel = "location_reminders"

    fun notify(memo: Memo) {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        val manager = context.getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= 26) {
            manager.createNotificationChannel(NotificationChannel(
                channel, context.getString(R.string.channel_name), NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = context.getString(R.string.channel_description) })
        }
        val detail = Intent(context, ViewMemo::class.java).apply {
            putExtra(BUNDLE_MEMO_ID, memo.id)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val intent = PendingIntent.getActivity(context, memo.id.toInt(), detail,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val excerpt = NotePreview.firstCharacters(memo.description)
        val notification = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_reminder_notification)
            .setContentTitle(memo.title)
            .setContentText(excerpt)
            .setStyle(NotificationCompat.BigTextStyle().bigText(excerpt))
            .setContentIntent(intent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(context).notify(memo.id.toInt(), notification)
    }
}
