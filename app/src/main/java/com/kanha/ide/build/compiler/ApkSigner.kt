package com.kanha.ide.build.compiler

import com.kanha.ide.build.core.BuildLogger
import com.kanha.ide.build.core.BuildProcess
import com.kanha.ide.build.model.BuildConfig
import com.kanha.ide.build.tool.BuildToolsManager
import java.io.File

class ApkSigner(
    private val config: BuildConfig,
    private val buildToolsManager: BuildToolsManager,
    private val logger: BuildLogger
) {
    suspend fun compile(): Boolean {
        val apksigner = buildToolsManager.getApkSignerJar()
        if (apksigner == null || !apksigner.exists()) {
            logger.error("Required Android build tool is not installed: apksigner")
            return false
        }
        
        val intermediatesDir = File(config.projectRoot, "build/intermediates")
        val apkOutputDir = File(config.projectRoot, "build/outputs/apk")
        val alignedApk = File(intermediatesDir, "app-aligned.apk")
        val finalApk = File(apkOutputDir, "app-debug.apk")

        if (!alignedApk.exists()) {
            logger.error("Aligned APK not found.")
            return false
        }
        
        // For debug builds we need a keystore. Real IDE would generate/use default debug.keystore
        // We assume it's provided or we use a dummy path
        val keystore = File(config.projectRoot, "debug.keystore")
        if (!keystore.exists()) {
            logger.log("Warning: debug.keystore not found. You need a keystore to sign!")
            // Optionally auto-generate one with keytool
        }

        logger.log("Signing APK...")
        
        // Copy aligned to final before signing since apksigner signs in-place (or specify out)
        alignedApk.copyTo(finalApk, overwrite = true)
        
        val args = listOf(
            "java",
            "-jar", apksigner.absolutePath,
            "sign",
            "--ks", keystore.absolutePath,
            "--ks-pass", "pass:android",
            "--key-pass", "pass:android",
            finalApk.absolutePath
        )
        
        return BuildProcess(args, config.projectRoot, logger).execute()
    }
}
