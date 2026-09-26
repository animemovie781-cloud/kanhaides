package com.kanha.ide.build.compiler

import com.kanha.ide.build.core.BuildLogger
import com.kanha.ide.build.core.BuildProcess
import com.kanha.ide.build.model.BuildConfig
import com.kanha.ide.build.tool.BuildToolsManager
import java.io.File

class JavaCompiler(
    private val config: BuildConfig,
    private val buildToolsManager: BuildToolsManager,
    private val logger: BuildLogger
) {
    suspend fun compile(): Boolean {
        // Find javac on system PATH or internal tools
        val javac = "javac" // Assuming javac is in PATH for now, real IDE would bundle or find it
        
        val androidJar = buildToolsManager.getAndroidJar()
        if (androidJar == null || !androidJar.exists()) {
            logger.error("Required Android SDK component missing: android.jar")
            return false
        }

        val javaSrcDir = File(config.projectRoot, "app/src/main/java")
        val rSrcDir = File(config.projectRoot, "build/generated/source/r")
        val outDir = File(config.projectRoot, "build/intermediates/classes")
        
        outDir.mkdirs()

        // Find all java files
        val javaFiles = mutableListOf<String>()
        javaSrcDir.walkTopDown().filter { it.extension == "java" }.forEach { javaFiles.add(it.absolutePath) }
        rSrcDir.walkTopDown().filter { it.extension == "java" }.forEach { javaFiles.add(it.absolutePath) }
        
        if (javaFiles.isEmpty()) {
            logger.log("No Java sources to compile.")
            return true
        }

        val args = mutableListOf(
            javac,
            "-source", "1.8",
            "-target", "1.8",
            "-bootclasspath", androidJar.absolutePath,
            "-d", outDir.absolutePath
        )
        args.addAll(javaFiles)
        
        return BuildProcess(args, config.projectRoot, logger).execute()
    }
}
