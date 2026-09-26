package com.kanha.ide.ui.build

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kanha.ide.R
import com.kanha.ide.build.model.BuildState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

class BuildOutputBottomSheet : BottomSheetDialogFragment() {

    private lateinit var tvBuildStatus: TextView
    private lateinit var tvBuildLogs: TextView
    private lateinit var scrollViewLogs: ScrollView
    private lateinit var btnCancel: Button
    private lateinit var btnInstall: Button
    
    private lateinit var viewModel: BuildViewModel
    private var projectPath: String? = null

    companion object {
        fun newInstance(projectPath: String): BuildOutputBottomSheet {
            val args = Bundle().apply { putString("PROJECT_PATH", projectPath) }
            val fragment = BuildOutputBottomSheet()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_build_output, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        projectPath = arguments?.getString("PROJECT_PATH")
        
        // Share view model with activity to survive bottom sheet dismissals
        viewModel = ViewModelProvider(requireActivity())[BuildViewModel::class.java]
        
        tvBuildStatus = view.findViewById(R.id.tvBuildStatus)
        tvBuildLogs = view.findViewById(R.id.tvBuildLogs)
        scrollViewLogs = view.findViewById(R.id.scrollViewLogs)
        btnCancel = view.findViewById(R.id.btnCancel)
        btnInstall = view.findViewById(R.id.btnInstall)
        
        btnCancel.setOnClickListener {
            viewModel.cancelBuild()
        }
        
        btnInstall.setOnClickListener {
            val apkPath = File(projectPath, "build/outputs/apk/app-debug.apk")
            if (apkPath.exists()) {
                if (apkPath.length() < 100) {
                    Toast.makeText(context, "Testing install flow with Fake APK", Toast.LENGTH_SHORT).show()
                }
                installApk(apkPath)
            } else {
                Toast.makeText(context, "APK not found", Toast.LENGTH_SHORT).show()
            }
        }
        
        observeViewModel()
        
        // Automatically start build when opened if it's idle
        if (viewModel.buildState.value == BuildState.IDLE && projectPath != null) {
            viewModel.startBuild(projectPath!!)
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.buildState.collectLatest { state ->
                tvBuildStatus.text = "Status: $state"
                
                when (state) {
                    BuildState.SUCCESS -> {
                        btnInstall.visibility = View.VISIBLE
                        btnCancel.visibility = View.GONE
                    }
                    BuildState.FAILED, BuildState.CANCELLED -> {
                        btnInstall.visibility = View.GONE
                        btnCancel.visibility = View.GONE
                    }
                    else -> {
                        btnInstall.visibility = View.GONE
                        btnCancel.visibility = View.VISIBLE
                    }
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.logs.collectLatest { logText ->
                tvBuildLogs.text = logText
                scrollViewLogs.post {
                    scrollViewLogs.fullScroll(View.FOCUS_DOWN)
                }
            }
        }
    }

    private fun installApk(apkFile: File) {
        try {
            val uri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", apkFile)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot install APK: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
