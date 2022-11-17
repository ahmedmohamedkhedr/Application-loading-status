package com.fudex.udacityproject3.utils

import android.app.DownloadManager
import android.app.NotificationManager
import android.content.Context
import android.content.res.ColorStateList
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat

fun AppCompatActivity.setupToolbar(title: String, toolbar: Toolbar, textView: TextView) {
    this.apply {
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""
        textView.text = title
    }
}

fun Context.getDownloadManager(): DownloadManager =
    ContextCompat.getSystemService(this, DownloadManager::class.java) as DownloadManager

fun Context.getNotificationManager(): NotificationManager =
    ContextCompat.getSystemService(this, NotificationManager::class.java) as NotificationManager

fun ImageView.setDownloadStatusImage(@DrawableRes imageRes: Int) = setImageResource(imageRes)

fun ImageView.setDownloadStatusColor(@ColorRes colorRes: Int) {
    ContextCompat.getColor(context, colorRes)
        .also { color ->
            imageTintList = ColorStateList.valueOf(color)
        }
}

fun TextView.setDownloadStatusColor(@ColorRes colorRes: Int) {
    ContextCompat.getColor(context, colorRes)
        .also { color ->
            setTextColor(color)
        }
}