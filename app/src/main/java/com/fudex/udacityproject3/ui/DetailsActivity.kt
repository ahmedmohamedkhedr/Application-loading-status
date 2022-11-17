package com.fudex.udacityproject3.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import com.fudex.udacityproject3.BuildConfig
import com.fudex.udacityproject3.R
import com.fudex.udacityproject3.databinding.ActivityDetailsBinding
import com.fudex.udacityproject3.utils.DownloadStatus
import com.fudex.udacityproject3.utils.setDownloadStatusColor
import com.fudex.udacityproject3.utils.setDownloadStatusImage
import com.fudex.udacityproject3.utils.setupToolbar

class DetailsActivity : AppCompatActivity() {

    private var _dataBinder: ActivityDetailsBinding? = null
    private val dataBinder: ActivityDetailsBinding
        get() = _dataBinder!!

    private var fileName = ""
    private var downloadStatus = ""

    companion object {
        private const val EXTRA_FILE_NAME = "${BuildConfig.APPLICATION_ID}.FILE_NAME"
        private const val EXTRA_DOWNLOAD_STATUS = "${BuildConfig.APPLICATION_ID}.DOWNLOAD_STATUS"


        fun getBundle(fileName: String, downloadStatus: DownloadStatus) = bundleOf(
            EXTRA_FILE_NAME to fileName,
            EXTRA_DOWNLOAD_STATUS to downloadStatus.type
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _dataBinder =
            DataBindingUtil.setContentView(this@DetailsActivity, R.layout.activity_details)

        fileName = intent.getStringExtra(EXTRA_FILE_NAME) ?: ""
        downloadStatus = intent.getStringExtra(EXTRA_DOWNLOAD_STATUS) ?: ""

        onActivityCreated()
    }

    private fun onActivityCreated() {
        dataBinder.apply {
            lifecycleOwner = this@DetailsActivity
            activity = this@DetailsActivity
            detailsToolbar.apply {
                setupToolbar(
                    getString(R.string.app_name),
                    toolbar,
                    tvNameToolbar
                )
            }
            detailContent.fileNameTV.text = fileName
            detailContent.downloadStatusTV.text = downloadStatus

            setupControllers()
            setDownloadStatus()
        }
    }

    private fun setDownloadStatus() = dataBinder.detailContent.apply {
        when (downloadStatus) {
            DownloadStatus.SUCCESSFUL.type -> {
                downloadStatusImage.setDownloadStatusImage(R.drawable.ic_check_circle_outline_24)
                downloadStatusImage.setDownloadStatusColor(R.color.green)
                downloadStatusTV.setDownloadStatusColor(R.color.green)
            }
            DownloadStatus.FAILED.type -> {
                downloadStatusImage.setDownloadStatusImage(R.drawable.ic_error_24)
                downloadStatusImage.setDownloadStatusColor(R.color.red)
                downloadStatusTV.setDownloadStatusColor(R.color.red)
            }
        }
    }

    private fun setupControllers() =
        dataBinder.detailContent.okBtn.setOnClickListener { finish() }
}