package com.kanha.ide.ui.build

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.kanha.ide.R
import com.kanha.ide.build.tool.BuildToolsConfig

class BuildToolsSetupDialog : DialogFragment() {

    var onDownloadClicked: (() -> Unit)? = null
    var onLaterClicked: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_build_tools_setup, container, false)
        
        val tvDetails = view.findViewById<TextView>(R.id.tvDetails)
        tvDetails.text = """
            Package: KanhaIDE Android Build Tools
            Android API: ${BuildToolsConfig.API_LEVEL}
            Architecture: ${BuildToolsConfig.ARCHITECTURE}
            Download Size: ~60–70 MB
            Storage Required: ~70–100 MB
        """.trimIndent()

        view.findViewById<Button>(R.id.btnDownload).setOnClickListener {
            dismiss()
            onDownloadClicked?.invoke()
        }

        view.findViewById<Button>(R.id.btnLater).setOnClickListener {
            dismiss()
            onLaterClicked?.invoke()
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
}
