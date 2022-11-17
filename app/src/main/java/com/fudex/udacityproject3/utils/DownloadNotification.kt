package com.fudex.udacityproject3.utils

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.fudex.udacityproject3.R

class DownloadNotification(private val context: Context, private val lifecycle: Lifecycle) :
    LifecycleObserver {

    init {
        lifecycle.addObserver(this)
    }

    fun notify(fileName: String, downloadStatus: DownloadStatus) {
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            Toast.makeText(
                context,
                context.getString(R.string.download_completed),
                Toast.LENGTH_SHORT
            ).show()
        }
        with(context.applicationContext) {
            getNotificationManager().run {
                createDownloadStatusChannel(applicationContext)
                sendDownloadCompletedNotification(
                    fileName,
                    downloadStatus,
                    applicationContext
                )
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun unregisterObserver() = lifecycle.removeObserver(this)

}