package com.kanha.ide.build.tool

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class SdkInstaller(private val context: Context) {

    suspend fun installSdkIfNeeded() = withContext(Dispatchers.IO) {
        val sdkDir = File(context.filesDir, "sdk")
        if (!sdkDir.exists()) {
            sdkDir.mkdirs()
        }

        // We assume the user has placed the following structure in app/src/main/assets/sdk:
        // assets/sdk/
        // ├── android.jar
        // ├── aapt2
        // ├── d8.jar
        // ├── zipalign
        // └── apksigner.jar
        
        try {
            val assets = context.assets
            val sdkAssets = assets.list("sdk") ?: return@withContext
            
            for (assetName in sdkAssets) {
                val outFile = File(sdkDir, assetName)
                if (!outFile.exists() || outFile.length() == 0L) {
                    copyAsset("sdk/$assetName", outFile)
                    
                    // Make native binaries executable
                    if (assetName == "aapt2" || assetName == "zipalign") {
                        outFile.setExecutable(true, false)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun copyAsset(assetPath: String, outFile: File) {
        try {
            context.assets.open(assetPath).use { input ->
                FileOutputStream(outFile).use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
