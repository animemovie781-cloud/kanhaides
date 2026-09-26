package com.kanha.ide.build.tool

import java.io.File

class SdkManager(private val customSdkPath: String? = null) {

    fun getSdkRoot(): File? {
        // In a real Android IDE, the SDK (android.jar, aapt2, d8, etc.) is usually downloaded 
        // to the app's internal storage or extracted from assets.
        val pathsToTry = listOfNotNull(
            customSdkPath,
            "/storage/emulated/0/KanhaIDE/sdk" // A common public folder for users to place SDK files
        )

        for (path in pathsToTry) {
            val dir = File(path)
            if (dir.exists() && dir.isDirectory) {
                return dir
            }
        }
        return null
    }

    fun getAndroidJar(compileSdk: Int = 34): File? {
        val root = getSdkRoot() ?: return null
        val platformPath = File(root, "platforms/android-$compileSdk/android.jar")
        return if (platformPath.exists()) platformPath else null
    }

    fun getBuildTool(toolName: String, buildToolsVersion: String = "34.0.0"): File? {
        val root = getSdkRoot() ?: return null
        // On Windows tools often have .exe or .bat extensions, but we'll return base logic.
        // It's up to the execution environment if it needs exact extension.
        val isWindows = System.getProperty("os.name").lowercase().contains("win")
        val exactName = if (isWindows && toolName != "d8") "$toolName.exe" else toolName
        
        val toolPath = File(root, "build-tools/$buildToolsVersion/$exactName")
        val batPath = File(root, "build-tools/$buildToolsVersion/$toolName.bat") // e.g. d8.bat

        if (toolPath.exists()) return toolPath
        if (batPath.exists()) return batPath
        
        return null
    }

    fun getAapt2() = getBuildTool("aapt2")
    fun getD8() = getBuildTool("d8")
    fun getZipAlign() = getBuildTool("zipalign")
    fun getApkSigner() = getBuildTool("apksigner")
}
