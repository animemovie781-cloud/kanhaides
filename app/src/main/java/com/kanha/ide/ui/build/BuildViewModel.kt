package com.kanha.ide.ui.build

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kanha.ide.build.core.BuildLogger
import com.kanha.ide.build.engine.BuildManager
import com.kanha.ide.build.model.BuildConfig
import com.kanha.ide.build.model.BuildResult
import com.kanha.ide.build.model.BuildState
import com.kanha.ide.build.tool.SdkManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class BuildViewModel(application: Application) : AndroidViewModel(application) {
    private val sdkManager = SdkManager(application.filesDir.absolutePath + "/sdk")
    private val buildLogger = BuildLogger()
    private val buildManager = BuildManager(sdkManager, buildLogger)

    private val _buildState = MutableStateFlow(BuildState.IDLE)
    val buildState: StateFlow<BuildState> = _buildState.asStateFlow()

    private val _logs = MutableStateFlow("")
    val logs: StateFlow<String> = _logs.asStateFlow()
    
    private val _buildResult = MutableStateFlow<BuildResult?>(null)
    val buildResult: StateFlow<BuildResult?> = _buildResult.asStateFlow()

    init {
        viewModelScope.launch {
            buildLogger.logs.collect { newLog ->
                _logs.value = _logs.value + "\n" + newLog
            }
        }
    }

    fun startBuild(projectPath: String) {
        _logs.value = ""
        _buildResult.value = null
        _buildState.value = BuildState.PREPARING

        val config = BuildConfig(
            projectRoot = File(projectPath),
            applicationId = "com.kanha.generated.app"
        )

        viewModelScope.launch {
            // Forward states from internal engine
            launch {
                buildManager.getActiveState()?.collect { state ->
                    _buildState.value = state
                }
            }
            
            val result = buildManager.buildProject(config)
            _buildResult.value = result
            if (_buildState.value != BuildState.CANCELLED) {
                _buildState.value = if (result.success) BuildState.SUCCESS else BuildState.FAILED
            }
        }
    }

    fun cancelBuild() {
        buildManager.cancelActiveBuild()
        _buildState.value = BuildState.CANCELLED
    }
}
