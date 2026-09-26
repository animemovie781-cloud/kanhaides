package com.kanha.ide.build.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class BuildProcess(
    private val command: List<String>,
    private val workingDir: File,
    private val logger: BuildLogger
) {
    private var process: Process? = null

    suspend fun execute(): Boolean = withContext(Dispatchers.IO) {
        try {
            logger.log("Executing: ${command.joinToString(" ")}")
            
            val pb = ProcessBuilder(command)
            pb.directory(workingDir)
            pb.redirectErrorStream(true)
            
            process = pb.start()
            
            val reader = BufferedReader(InputStreamReader(process!!.inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                logger.log(line ?: "")
            }
            
            val exitCode = process!!.waitFor()
            if (exitCode != 0) {
                logger.error("Process exited with code $exitCode")
                return@withContext false
            }
            return@withContext true
        } catch (e: Exception) {
            logger.error("Failed to execute process: ${e.message}")
            return@withContext false
        }
    }
    
    fun cancel() {
        process?.destroy()
    }
}
