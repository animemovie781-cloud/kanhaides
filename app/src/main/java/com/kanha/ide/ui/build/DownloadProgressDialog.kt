package com.kanha.ide.ui.build

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.kanha.ide.R

class DownloadProgressDialog : DialogFragment() {

    private lateinit var tvStatus: TextView
    private lateinit var tvProgressBytes: TextView
    private lateinit var tvProgressPercent: TextView
    private lateinit var tvSpeed: TextView
    private lateinit var progressBar: ProgressBar
    
    var onCancelClicked: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_download_progress, container, false)
        
        tvStatus = view.findViewById(R.id.tvStatus)
        tvProgressBytes = view.findViewById(R.id.tvProgressBytes)
        tvProgressPercent = view.findViewById(R.id.tvProgressPercent)
        tvSpeed = view.findViewById(R.id.tvSpeed)
        progressBar = view.findViewById(R.id.progressBar)

        isCancelable = false // Prevent closing by tapping outside

        view.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            onCancelClicked?.invoke()
            dismiss()
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    fun updateProgress(bytesDownloaded: Long, totalBytes: Long, speedBps: Long) {
        if (!isAdded) return
        
        val mbDownloaded = bytesDownloaded / (1024f * 1024f)
        val mbTotal = totalBytes / (1024f * 1024f)
        val mbSpeed = speedBps / (1024f * 1024f)
        
        val percent = if (totalBytes > 0) ((bytesDownloaded.toFloat() / totalBytes) * 100).toInt() else 0

        tvProgressBytes.text = String.format("%.1f MB / %.1f MB", mbDownloaded, mbTotal)
        tvProgressPercent.text = "$percent%"
        tvSpeed.text = String.format("Download speed: %.1f MB/s", mbSpeed)
        
        progressBar.max = 100
        progressBar.progress = percent
    }

    fun updateStatus(status: String) {
        if (!isAdded) return
        tvStatus.text = status
    }
}
