package com.kanha.ide.build.engine

import com.kanha.ide.build.core.BuildLogger
import com.kanha.ide.build.model.BuildConfig
import com.kanha.ide.build.model.BuildResult
import com.kanha.ide.build.model.BuildState
import com.kanha.ide.build.tool.SdkManager
import kotlinx.coroutines.flow.StateFlow

class BuildManager(
    private val sdkManager: SdkManager,
    val logger: BuildLogger
) {
    private var activeEngine: LightweightBuildEngine? = null

    suspend fun buildProject(config: BuildConfig): BuildResult {
        val engine = LightweightBuildEngine(config, sdkManager, logger)
        activeEngine = engine
        return engine.startBuild()
    }

    fun cancelActiveBuild() {
        activeEngine?.cancelBuild()
    }
    
    fun getActiveState(): StateFlow<BuildState>? {
        return activeEngine?.state
    }
}
