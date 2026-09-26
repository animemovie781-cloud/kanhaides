package com.kanha.ide.build.tool

import android.content.Context
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

data class BuildToolsValidationResult(
    val valid: Boolean,
    val missingFiles: List<String>,
    val errors: List<String>
)

class BuildToolsValidator(private val context: Context) {

    fun validate(): BuildToolsValidationResult {
        val sdkDir = File(context.filesDir, "sdk")
        val missingFiles = mutableListOf<String>()
        val errors = mutableListOf<String>()

        if (!sdkDir.exists() || !sdkDir.isDirectory) {
            return BuildToolsValidationResult(false, listOf("sdk/"), listOf("SDK directory does not exist."))
        }

        val requiredFiles = listOf(
            "android.jar",
            "aapt2",
            "d8.jar",
            "zipalign",
            "apksigner.jar",
            "build-tools.json"
        )

        for (file in requiredFiles) {
            val f = File(sdkDir, file)
            if (!f.exists()) {
                missingFiles.add(file)
            } else if (file == "aapt2" || file == "zipalign") {
                if (!f.canExecute()) {
                    errors.add("$file is not executable.")
                }
                // Optional: run aapt2 version to test it
                if (file == "aapt2" && f.canExecute()) {
                    try {
                        val process = ProcessBuilder(f.absolutePath, "version").start()
                        process.waitFor()
                        if (process.exitValue() != 0) {
                            errors.add("AAPT2 execution failed. Incompatible architecture?")
                        }
                    } catch (e: Exception) {
                        errors.add("AAPT2 execution error: ${e.message}")
                    }
                }
            }
        }

        return BuildToolsValidationResult(
            valid = missingFiles.isEmpty() && errors.isEmpty(),
            missingFiles = missingFiles,
            errors = errors
        )
    }

    fun calculateSha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { fis ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
