package com.fudex.udacityproject3.ui

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil.setContentView
import com.fudex.udacityproject3.R
import com.fudex.udacityproject3.databinding.ActivityMainBinding
import com.fudex.udacityproject3.ui.progress_button.ProgressButtonState
import com.fudex.udacityproject3.utils.DownloadNotification
import com.fudex.udacityproject3.utils.DownloadStatus
import com.fudex.udacityproject3.utils.getDownloadManager
import com.fudex.udacityproject3.utils.setupToolbar

class MainActivity : AppCompatActivity() {
    private var _binder: ActivityMainBinding? = null
    private val binder: ActivityMainBinding
        get() = _binder!!

    private var downloadedFileName = ""
    private var downloadedID: Long = NO_DOWNLOAD_ID
    private var downloadedContentObserver: ContentObserver? = null
    private var downloadedNotification: DownloadNotification? = null


    companion object {
        var URL = ""
        private const val NO_DOWNLOAD_ID = 0L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binder = setContentView(
            this,
            R.layout.activity_main
        )
        setup()
    }

    private fun setup() {
        binder.apply {
            lifecycleOwner = this@MainActivity
            activity = this@MainActivity
            toolbar.apply {
                setupToolbar(
                    getString(R.string.app_name),
                    toolbar,
                    tvNameToolbar
                )
            }
            setupControllers()
            registerReceiver(
                downloadCompletedReceiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            )
        }
    }

    private fun setupControllers() {
        binder.mainContent.apply {
            progressBtn.setOnClickListener {
                when (downloadOptionRadioGroup.checkedRadioButtonId) {
                    View.NO_ID -> Toast.makeText(
                        this@MainActivity,
                        getString(R.string.select_option_to_download),
                        Toast.LENGTH_SHORT
                    ).show()
                    else -> {
                        downloadedFileName =
                            findViewById<RadioButton>(downloadOptionRadioGroup.checkedRadioButtonId).text.toString()
                        setURL(downloadOptionRadioGroup.checkedRadioButtonId)
                        onRequestDownloadFile()
                    }
                }
            }
        }
    }

    private fun onRequestDownloadFile() {
        with(getDownloadManager()) {

            val request = DownloadManager.Request(Uri.parse(URL))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .also {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        it.setRequiresCharging(false)
                    }
                }

            downloadedID = enqueue(request)
            downloadContentObserver()
        }
    }

    private fun DownloadManager.downloadContentObserver() {
        object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                downloadedContentObserver?.run { queryProgress() }
            }
        }.also {
            downloadedContentObserver = it
            contentResolver.registerContentObserver(
                "content://downloads/my_downloads".toUri(),
                true,
                downloadedContentObserver!!
            )
        }
    }

    @SuppressLint("Range")
    private fun DownloadManager.queryProgress() {
        query(DownloadManager.Query().setFilterById(downloadedID)).use {
            with(it) {
                if (this != null && moveToFirst()) {
                    when (getInt(getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                        DownloadManager.STATUS_FAILED -> {
                            binder.mainContent.progressBtn.changeButtonState(ProgressButtonState.Completed)
                        }

                        DownloadManager.STATUS_RUNNING -> {
                            binder.mainContent.progressBtn.changeButtonState(ProgressButtonState.Loading)
                        }

                        DownloadManager.STATUS_SUCCESSFUL -> {
                            binder.mainContent.progressBtn.changeButtonState(ProgressButtonState.Completed)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(downloadCompletedReceiver)
        unregisterDownloadObserver()
        downloadedNotification = null
    }

    private fun unregisterDownloadObserver() {
        downloadedContentObserver?.let {
            contentResolver.unregisterContentObserver(it)
            downloadedContentObserver = null
        }
    }

    private val downloadCompletedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            id?.let {
                val downloadStatus = getDownloadManager().queryStatus(it)
                unregisterDownloadObserver()
                downloadStatus.takeIf { status -> status != DownloadStatus.UNKNOWN }
                    ?.run {
                        getDownloadNotification().notify(downloadedFileName, downloadStatus)
                    }
            }
        }
    }

    @SuppressLint("Range")
    private fun DownloadManager.queryStatus(id: Long): DownloadStatus {
        query(DownloadManager.Query().setFilterById(id)).use {
            with(it) {
                if (this != null && moveToFirst()) {
                    return when (getInt(getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                        DownloadManager.STATUS_SUCCESSFUL -> DownloadStatus.SUCCESSFUL
                        DownloadManager.STATUS_FAILED -> DownloadStatus.FAILED
                        else -> DownloadStatus.UNKNOWN
                    }
                }
                return DownloadStatus.UNKNOWN
            }
        }
    }

    private fun getDownloadNotification(): DownloadNotification = when (downloadedNotification) {
        null -> DownloadNotification(this, lifecycle).also { downloadedNotification = it }
        else -> downloadedNotification!!
    }

    private fun setURL(selectedId: Int) = when (selectedId) {
        R.id.glideRB -> {
            URL = "https://github.com/bumptech/glide/archive/master.zip"
        }
        R.id.loadAppRB -> {
            URL =
                "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        }
        else -> {
            URL = "https://github.com/square/retrofit/archive/master.zip"
        }
    }

}