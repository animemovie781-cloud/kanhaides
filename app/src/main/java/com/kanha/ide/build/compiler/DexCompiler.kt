package com.kanha.ide.build.compiler

import com.kanha.ide.build.core.BuildLogger
import com.kanha.ide.build.core.BuildProcess
import com.kanha.ide.build.model.BuildConfig
import com.kanha.ide.build.tool.SdkManager
import java.io.File

class DexCompiler(
    private val config: BuildConfig,
    private val sdkManager: SdkManager,
    private val logger: BuildLogger
) {
    suspend fun compile(): Boolean {
        val d8 = sdkManager.getD8()
        if (d8 == null || !d8.exists()) {
            logger.error("Required Android build tool is not installed: d8")
            return false
        }
        
        val classesDir = File(config.projectRoot, "build/intermediates/classes")
        val dexOutDir = File(config.projectRoot, "build/intermediates/dex")
        
        dexOutDir.mkdirs()

        val classFiles = mutableListOf<String>()
        classesDir.walkTopDown().filter { it.extension == "class" }.forEach { classFiles.add(it.absolutePath) }
        
        if (classFiles.isEmpty()) {
            logger.log("No classes to dex.")
            return true
        }

        val args = mutableListOf(
            d8.absolutePath,
            "--output", dexOutDir.absolutePath,
            "--lib", sdkManager.getAndroidJar(config.compileSdk)!!.absolutePath
        )
        args.addAll(classFiles)
        
        return BuildProcess(args, config.projectRoot, logger).execute()
    }
}
