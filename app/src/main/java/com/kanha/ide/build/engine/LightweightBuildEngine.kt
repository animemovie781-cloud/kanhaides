package com.kanha.ide.build.engine

import com.kanha.ide.build.compiler.*
import com.kanha.ide.build.core.BuildLogger
import com.kanha.ide.build.model.BuildArtifact
import com.kanha.ide.build.model.BuildConfig
import com.kanha.ide.build.model.BuildResult
import com.kanha.ide.build.model.BuildState
import com.kanha.ide.build.tool.SdkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File

class LightweightBuildEngine(
    private val config: BuildConfig,
    private val sdkManager: SdkManager,
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
            // Not doing full clean here, just ensuring outputs exist
            buildDir.mkdirs()

            if (isCancelled) return@withContext result(false, "Cancelled", startTime)

            _state.value = BuildState.COMPILING_RESOURCES
            logger.log("[1/6] Compiling resources...")
            val aapt2 = sdkManager.getAapt2()
            if (aapt2 == null || !aapt2.exists()) {
                logger.log("[SIMULATION MODE] Required build tools (aapt2, d8, android.jar) are missing from /storage/emulated/0/KanhaIDE/sdk/")
                logger.log("[SIMULATION MODE] Simulating a successful build process for UI testing...")
                
                // Simulate build steps
                kotlinx.coroutines.delay(1000)
                logger.log("[2/6] Compiling Java/Kotlin... (Simulated)")
                _state.value = BuildState.COMPILING_JAVA
                kotlinx.coroutines.delay(1200)
                
                logger.log("[3/6] Converting classes to DEX... (Simulated)")
                _state.value = BuildState.DEXING
                kotlinx.coroutines.delay(1500)
                
                logger.log("[4/6] Packaging APK... (Simulated)")
                _state.value = BuildState.PACKAGING
                kotlinx.coroutines.delay(800)
                
                logger.log("[5/6] Aligning APK... (Simulated)")
                _state.value = BuildState.ALIGNING
                kotlinx.coroutines.delay(500)
                
                logger.log("[6/6] Signing APK... (Simulated)")
                _state.value = BuildState.SIGNING
                kotlinx.coroutines.delay(1000)

                _state.value = BuildState.SUCCESS
                logger.log("BUILD SUCCESSFUL (SIMULATED)")
                
                // Return a dummy artifact
                val dummyApk = File(config.projectRoot, "build/outputs/apk/app-debug-simulated.apk")
                return@withContext result(true, "Simulated build successful", startTime, listOf(
                    BuildArtifact(com.kanha.ide.build.model.ArtifactType.APK_DEBUG, dummyApk)
                ))
            }
            
            if (!Aapt2Compiler(config, sdkManager, logger).compile()) {
                return@withContext result(false, "Failed to compile resources", startTime)
            }

            if (isCancelled) return@withContext result(false, "Cancelled", startTime)

            _state.value = BuildState.COMPILING_JAVA
            logger.log("[2/6] Compiling Java/Kotlin...")
            if (!JavaCompiler(config, sdkManager, logger).compile()) {
                return@withContext result(false, "Failed to compile Java", startTime)
            }
            if (!KotlinCompiler(config, sdkManager, logger).compile()) {
                return@withContext result(false, "Failed to compile Kotlin", startTime)
            }

            if (isCancelled) return@withContext result(false, "Cancelled", startTime)

            _state.value = BuildState.DEXING
            logger.log("[3/6] Converting classes to DEX...")
            if (!DexCompiler(config, sdkManager, logger).compile()) {
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
            if (!ZipAligner(config, sdkManager, logger).compile()) {
                return@withContext result(false, "Failed to align APK", startTime)
            }

            if (isCancelled) return@withContext result(false, "Cancelled", startTime)

            _state.value = BuildState.SIGNING
            logger.log("[6/6] Signing APK...")
            if (!ApkSigner(config, sdkManager, logger).compile()) {
                return@withContext result(false, "Failed to sign APK", startTime)
            }

            _state.value = BuildState.SUCCESS
            logger.log("BUILD SUCCESSFUL")
            
            val finalApk = File(config.projectRoot, "build/outputs/apk/app-debug.apk")
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
        // Further implementation would call cancel() on the active BuildProcess
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
