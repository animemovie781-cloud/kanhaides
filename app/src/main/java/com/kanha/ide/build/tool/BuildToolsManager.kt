package com.kanha.ide.build.tool

import android.content.Context
import java.io.File

interface BuildToolsManager {
    fun isInstalled(): Boolean
    fun getSdkDirectory(): File
    fun getAndroidJar(): File
    fun getAapt2(): File
    fun getD8Jar(): File
    fun getZipalign(): File
    fun getApkSignerJar(): File
    
    suspend fun downloadAndInstall(
        url: String,
        expectedSha256: String,
        onProgress: (bytesDownloaded: Long, totalBytes: Long, speedBps: Long) -> Unit
    )
}

class BuildToolsManagerImpl(private val context: Context) : BuildToolsManager {

    private val validator = BuildToolsValidator(context)
    private val downloader = BuildToolsDownloader(context)
    private val installer = BuildToolsInstaller(context)

    override fun isInstalled(): Boolean {
        return validator.validate().valid
    }

    override fun getSdkDirectory(): File = File(context.filesDir, "sdk")
    override fun getAndroidJar(): File = File(getSdkDirectory(), "android.jar")
    override fun getAapt2(): File = File(getSdkDirectory(), "aapt2")
    override fun getD8Jar(): File = File(getSdkDirectory(), "d8.jar")
    override fun getZipalign(): File = File(getSdkDirectory(), "zipalign")
    override fun getApkSignerJar(): File = File(getSdkDirectory(), "apksigner.jar")

    override suspend fun downloadAndInstall(
        url: String, 
        expectedSha256: String,
        onProgress: (bytesDownloaded: Long, totalBytes: Long, speedBps: Long) -> Unit
    ) {
        val tempZip = File(context.cacheDir, "build-tools.zip")
        
        // 1. Download
        downloader.download(url, tempZip, onProgress)
        
        // 2. Validate Checksum
        if (expectedSha256 != "IGNORE") {
            val calculatedHash = validator.calculateSha256(tempZip)
            if (!calculatedHash.equals(expectedSha256, ignoreCase = true)) {
                tempZip.delete()
                throw SecurityException("Build Tools checksum verification failed. Expected: $expectedSha256, Got: $calculatedHash")
            }
        }
        
        // 3. Install (Extract, set permissions, atomic rename)
        installer.install(tempZip)
        
        // 4. Verify Final Installation
        val result = validator.validate()
        if (!result.valid) {
            throw IllegalStateException("Installation verified as invalid: ${result.errors.joinToString(", ")}")
        }
    }
}
