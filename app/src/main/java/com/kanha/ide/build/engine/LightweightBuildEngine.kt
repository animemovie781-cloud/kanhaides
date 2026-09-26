package com.kanha.ide.build.engine

import com.kanha.ide.build.compiler.*
import com.kanha.ide.build.core.BuildLogger
import com.kanha.ide.build.model.BuildArtifact
import com.kanha.ide.build.model.BuildConfig
import com.kanha.ide.build.model.BuildResult
import com.kanha.ide.build.model.BuildState
import com.kanha.ide.build.tool.BuildToolsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File

class LightweightBuildEngine(
    private val config: BuildConfig,
    private val buildToolsManager: BuildToolsManager,
    private val logger: BuildLogger
) {
    private val _state = MutableStateFlow(BuildState.IDLE)
    val state: StateFlow<BuildState> = _state.asStateFlow()
    
    private var isCancelled = false

    suspend fun startBuild(): BuildResult = withContext(Dispatchers.IO) {
        isCancelled = false
        val startTime = System.currentTimeMillis()
        
        try {
            _state.value = BuildState.PREPARING
            logger.log("Preparing project...")
            
            // Clean/setup dirs
            val buildDir = File(config.projectRoot, "build")
            buildDir.mkdirs()

            if (isCancelled) return@withContext result(false, "Cancelled", startTime)

            _state.value = BuildState.COMPILING_RESOURCES
            logger.log("[1/6] Compiling resources...")
            
            if (!Aapt2Compiler(config, buildToolsManager, logger).compile()) {
                return@withContext result(false, "Failed to compile resources", startTime)
            }

            if (isCancelled) return@withContext result(false, "Cancelled", startTime)

            _state.value = BuildState.COMPILING_JAVA
            logger.log("[2/6] Compiling Java/Kotlin...")
            if (!JavaCompiler(config, buildToolsManager, logger).compile()) {
                return@withContext result(false, "Failed to compile Java", startTime)
            }
            if (!KotlinCompiler(config, buildToolsManager, logger).compile()) {
                return@withContext result(false, "Failed to compile Kotlin", startTime)
            }

            if (isCancelled) return@withContext result(false, "Cancelled", startTime)

            _state.value = BuildState.DEXING
            logger.log("[3/6] Converting classes to DEX...")
            if (!DexCompiler(config, buildToolsManager, logger).compile()) {
                return@withContext result(false, "Failed to run D8", startTime)
            }

            if (isCancelled) return@withContext result(false, "Cancelled", startTime)

            _state.value = BuildState.PACKAGING
            logger.log("[4/6] Packaging APK...")
            if (!ApkPackager(config, logger).compile()) {
                return@withContext result(false, "Failed to package APK", startTime)
            }

            if (isCancelled) return@withContext result(false, "Cancelled", startTime)

            _state.value = BuildState.ALIGNING
            logger.log("[5/6] Aligning APK...")
            if (!ZipAligner(config, buildToolsManager, logger).compile()) {
                return@withContext result(false, "Failed to align APK", startTime)
            }

            if (isCancelled) return@withContext result(false, "Cancelled", startTime)

            _state.value = BuildState.SIGNING
            logger.log("[6/6] Signing APK...")
            if (!ApkSigner(config, buildToolsManager, logger).compile()) {
                return@withContext result(false, "Failed to sign APK", startTime)
            }

            _state.value = BuildState.SUCCESS
            logger.log("BUILD SUCCESSFUL")
            
            val finalApk = File(config.projectRoot, "build/outputs/apk/app-debug.apk")
            if (!finalApk.exists()) {
                return@withContext result(false, "Build completed but APK file not found at ${finalApk.absolutePath}", startTime)
            }
            
            return@withContext result(true, "Build successful", startTime, listOf(
                BuildArtifact(com.kanha.ide.build.model.ArtifactType.APK_DEBUG, finalApk)
            ))
            
        } catch (e: Exception) {
            _state.value = BuildState.FAILED
            logger.error("Build crashed: ${e.message}")
            return@withContext result(false, e.message ?: "Unknown error", startTime)
        }
    }

    fun cancelBuild() {
        isCancelled = true
        _state.value = BuildState.CANCELLED
    }

    private fun result(success: Boolean, msg: String, startTime: Long, artifacts: List<BuildArtifact> = emptyList()): BuildResult {
        if (!success && _state.value != BuildState.CANCELLED) {
            _state.value = BuildState.FAILED
        }
        return BuildResult(
            success = success,
            message = msg,
            artifacts = artifacts,
            durationMs = System.currentTimeMillis() - startTime
        )
    }
}
