package com.kanha.ide.build.compiler

import com.kanha.ide.build.core.BuildLogger
import com.kanha.ide.build.core.BuildProcess
import com.kanha.ide.build.model.BuildConfig
import com.kanha.ide.build.tool.BuildToolsManager
import java.io.File

class ZipAligner(
    private val config: BuildConfig,
    private val buildToolsManager: BuildToolsManager,
    private val logger: BuildLogger
) {
    suspend fun compile(): Boolean {
        val zipalign = buildToolsManager.getZipalign()
        if (zipalign == null || !zipalign.exists()) {
            logger.error("Required Android build tool is not installed: zipalign")
            return false
        }
        
        val intermediatesDir = File(config.projectRoot, "build/intermediates")
        val unsignedApk = File(intermediatesDir, "app-unsigned.apk")
        val alignedApk = File(intermediatesDir, "app-aligned.apk")

        if (!unsignedApk.exists()) {
            logger.error("Unsigned APK not found.")
            return false
        }

        logger.log("Aligning APK...")
        
        val args = listOf(
            zipalign.absolutePath,
            "-f", "4",
            unsignedApk.absolutePath,
            alignedApk.absolutePath
        )
        
        return BuildProcess(args, config.projectRoot, logger).execute()
    }
}
