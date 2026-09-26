package com.kanha.ide.build.compiler

import com.kanha.ide.build.core.BuildLogger
import com.kanha.ide.build.model.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class ApkPackager(
    private val config: BuildConfig,
    private val logger: BuildLogger
) {
    suspend fun compile(): Boolean = withContext(Dispatchers.IO) {
        val intermediatesDir = File(config.projectRoot, "build/intermediates")
        val resourcesApk = File(intermediatesDir, "resources.apk")
        val dexDir = File(intermediatesDir, "dex")
        val unsignedApk = File(intermediatesDir, "app-unsigned.apk")
        
        if (!resourcesApk.exists()) {
            logger.error("resources.apk not found. AAPT2 link may have failed.")
            return@withContext false
        }

        try {
            logger.log("Packaging APK...")
            ZipOutputStream(FileOutputStream(unsignedApk)).use { zos ->
                // Copy all entries from resources.apk
                ZipInputStream(FileInputStream(resourcesApk)).use { zis ->
                    var entry = zis.nextEntry
                    while (entry != null) {
                        zos.putNextEntry(ZipEntry(entry.name))
                        zis.copyTo(zos)
                        zos.closeEntry()
                        entry = zis.nextEntry
                    }
                }
                
                // Add dex files
                val dexFiles = dexDir.listFiles()?.filter { it.extension == "dex" } ?: emptyList()
                for (dexFile in dexFiles) {
                    zos.putNextEntry(ZipEntry(dexFile.name))
                    FileInputStream(dexFile).use { it.copyTo(zos) }
                    zos.closeEntry()
                }
            }
            return@withContext true
        } catch (e: Exception) {
            logger.error("Packaging failed: ${e.message}")
            return@withContext false
        }
    }
}
