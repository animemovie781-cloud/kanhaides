package com.kanha.ide.build.compiler

import com.kanha.ide.build.core.BuildLogger
import com.kanha.ide.build.core.BuildProcess
import com.kanha.ide.build.model.BuildConfig
import com.kanha.ide.build.tool.SdkManager
import java.io.File

class KotlinCompiler(
    private val config: BuildConfig,
    private val sdkManager: SdkManager,
    private val logger: BuildLogger
) {
    suspend fun compile(): Boolean {
        // Require kotlinc to be in PATH or provided
        val kotlinc = "kotlinc" 
        
        val androidJar = sdkManager.getAndroidJar(config.compileSdk)
        if (androidJar == null || !androidJar.exists()) {
            logger.error("Required Android SDK component missing: android.jar")
            return false
        }

        val ktSrcDir = File(config.projectRoot, "app/src/main/java")
        val outDir = File(config.projectRoot, "build/intermediates/classes")
        
        outDir.mkdirs()

        val ktFiles = mutableListOf<String>()
        ktSrcDir.walkTopDown().filter { it.extension == "kt" }.forEach { ktFiles.add(it.absolutePath) }
        
        if (ktFiles.isEmpty()) {
            logger.log("No Kotlin sources to compile.")
            return true
        }

        val args = mutableListOf(
            kotlinc,
            "-cp", androidJar.absolutePath,
            "-d", outDir.absolutePath
        )
        args.addAll(ktFiles)
        
        return BuildProcess(args, config.projectRoot, logger).execute()
    }
}
