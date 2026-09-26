package com.kanha.ide.build.compiler

import com.kanha.ide.build.core.BuildLogger
import com.kanha.ide.build.core.BuildProcess
import com.kanha.ide.build.model.BuildConfig
import com.kanha.ide.build.tool.SdkManager
import java.io.File

class Aapt2Compiler(
    private val config: BuildConfig,
    private val sdkManager: SdkManager,
    private val logger: BuildLogger
) {
    suspend fun compile(): Boolean {
        val aapt2 = sdkManager.getAapt2()
        if (aapt2 == null || !aapt2.exists()) {
            logger.error("Required Android build tool is missing: aapt2")
            logger.error("To build on a phone, you must place ARM64 binaries (aapt2, d8, etc.) and android.jar in: /storage/emulated/0/KanhaIDE/sdk/build-tools/34.0.0/")
            return false
        }
        
        val androidJar = sdkManager.getAndroidJar(config.compileSdk)
        if (androidJar == null || !androidJar.exists()) {
            logger.error("Required Android SDK component missing: android.jar for API ${config.compileSdk}")
            return false
        }

        val resDir = File(config.projectRoot, "app/src/main/res")
        val manifestFile = File(config.projectRoot, "app/src/main/AndroidManifest.xml")
        
        val buildDir = File(config.projectRoot, "build")
        val intermediatesDir = File(buildDir, "intermediates")
        val compiledResDir = File(intermediatesDir, "res/compiled")
        val genDir = File(buildDir, "generated/source/r")
        val apkOutputDir = File(buildDir, "outputs/apk")
        
        compiledResDir.mkdirs()
        genDir.mkdirs()
        apkOutputDir.mkdirs()

        // 1. AAPT2 Compile
        val compileArgs = mutableListOf(
            aapt2.absolutePath,
            "compile",
            "--dir", resDir.absolutePath,
            "-o", compiledResDir.absolutePath
        )
        
        var success = BuildProcess(compileArgs, config.projectRoot, logger).execute()
        if (!success) return false

        // 2. AAPT2 Link
        val linkArgs = mutableListOf(
            aapt2.absolutePath,
            "link",
            "-I", androidJar.absolutePath,
            "--manifest", manifestFile.absolutePath,
            "-o", File(intermediatesDir, "resources.apk").absolutePath,
            "--java", genDir.absolutePath,
            "--auto-add-overlay"
        )
        
        // Add compiled resources (this is simplified; in reality, we list the compiled flat files or archive)
        val compiledFiles = compiledResDir.listFiles()?.filter { it.extension == "flat" }
        if (compiledFiles != null) {
            for (f in compiledFiles) {
                linkArgs.add(f.absolutePath)
            }
        }
        
        success = BuildProcess(linkArgs, config.projectRoot, logger).execute()
        return success
    }
}
