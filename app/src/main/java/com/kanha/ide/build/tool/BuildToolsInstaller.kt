package com.kanha.ide.build.tool

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

class BuildToolsInstaller(private val context: Context) {

    suspend fun install(zipFile: File) = withContext(Dispatchers.IO) {
        val tmpSdkDir = File(context.filesDir, "sdk_tmp")
        if (tmpSdkDir.exists()) {
            tmpSdkDir.deleteRecursively()
        }
        tmpSdkDir.mkdirs()

        try {
            unzipSafe(zipFile, tmpSdkDir)

            // The ZIP might have extracted into sdk_tmp/sdk/ or just sdk_tmp/.
            // If it extracted into sdk_tmp/sdk/, we need to move the contents up.
            var extractedDir = tmpSdkDir
            val innerSdkDir = File(tmpSdkDir, "sdk")
            if (innerSdkDir.exists() && innerSdkDir.isDirectory) {
                extractedDir = innerSdkDir
            }

            // Set executable permissions for native binaries
            val aapt2 = File(extractedDir, "aapt2")
            if (aapt2.exists()) aapt2.setExecutable(true, false)

            val zipalign = File(extractedDir, "zipalign")
            if (zipalign.exists()) zipalign.setExecutable(true, false)

            // Atomic rename
            val finalSdkDir = File(context.filesDir, "sdk")
            if (finalSdkDir.exists()) {
                val backup = File(context.filesDir, "sdk_backup")
                if (backup.exists()) backup.deleteRecursively()
                finalSdkDir.renameTo(backup)
            }

            val success = extractedDir.renameTo(finalSdkDir)
            if (!success) {
                throw Exception("Failed to rename temporary SDK directory to final SDK directory.")
            }

            // Cleanup
            if (tmpSdkDir.exists() && tmpSdkDir != extractedDir) {
                tmpSdkDir.deleteRecursively()
            }
            zipFile.delete()

        } catch (e: Exception) {
            if (tmpSdkDir.exists()) {
                tmpSdkDir.deleteRecursively()
            }
            throw e
        }
    }

    private fun unzipSafe(zipFile: File, destDir: File) {
        ZipInputStream(FileInputStream(zipFile)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                val name = entry.name
                
                // Prevent Zip Slip
                if (name.contains("..") || name.startsWith("/")) {
                    throw SecurityException("Zip Slip attempt detected: $name")
                }

                val newFile = File(destDir, name)
                
                if (entry.isDirectory) {
                    newFile.mkdirs()
                } else {
                    newFile.parentFile?.mkdirs()
                    FileOutputStream(newFile).use { fos ->
                        val buffer = ByteArray(8192)
                        var len: Int
                        while (zis.read(buffer).also { len = it } > 0) {
                            fos.write(buffer, 0, len)
                        }
                    }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
    }
}
