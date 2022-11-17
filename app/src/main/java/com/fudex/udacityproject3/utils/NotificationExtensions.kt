package com.fudex.udacityproject3.utils

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.fudex.udacityproject3.R
import com.fudex.udacityproject3.ui.DetailsActivity
import com.fudex.udacityproject3.utils.Constants.DOWNLOAD_COMPLETED_ID
import com.fudex.udacityproject3.utils.Constants.NOTIFICATION_REQUEST_CODE
import com.fudex.udacityproject3.utils.Constants.notification_channel_description
import com.fudex.udacityproject3.utils.Constants.notification_channel_id
import com.fudex.udacityproject3.utils.Constants.notification_channel_name

fun NotificationManager.sendDownloadCompletedNotification(
    fileName: String,
    downloadStatus: DownloadStatus,
    context: Context
) {
    val contentIntent = Intent(context, DetailsActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        putExtras(DetailsActivity.getBundle(fileName, downloadStatus))
    }
    val contentPendingIntent = PendingIntent.getActivity(
        context,
        NOTIFICATION_REQUEST_CODE,
        contentIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    val checkStatusAction = NotificationCompat.Action.Builder(
        null,
        context.getString(R.string.notification_action_check_status),
        contentPendingIntent
    ).build()

    NotificationCompat.Builder(context, notification_channel_id) // Set the notification content
        .setSmallIcon(R.drawable.ic_assistant_black_24dp)
        .setContentTitle(context.getString(R.string.notification_title))
        .setContentText(context.getString(R.string.notification_description))
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setContentIntent(contentPendingIntent)
        .setAutoCancel(true)
        .addAction(checkStatusAction)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .apply {

        }.run {
            notify(DOWNLOAD_COMPLETED_ID, this.build())
        }
}

@SuppressLint("NewApi")
fun NotificationManager.createDownloadStatusChannel(context: Context) {
    Build.VERSION.SDK_INT.takeIf { it >= Build.VERSION_CODES.O }?.run {
        NotificationChannel(
            notification_channel_id,
            notification_channel_name,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = notification_channel_description
            setShowBadge(true)
        }.run {
            createNotificationChannel(this)
        }
    }
}