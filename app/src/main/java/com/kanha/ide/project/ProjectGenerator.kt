package com.kanha.ide.project

import android.content.Context
import com.kanha.ide.template.Template
import com.kanha.ide.template.VariableResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStreamReader

class ProjectGenerator(private val context: Context) {

    suspend fun generate(template: Template, config: ProjectConfig): Result<File> = withContext(Dispatchers.IO) {
        try {
            val projectDir = File(config.projectPath)
            if (projectDir.exists() && (projectDir.listFiles()?.isNotEmpty() == true)) {
                return@withContext Result.failure(Exception("Project directory already exists and is not empty"))
            }

            if (!projectDir.exists()) {
                projectDir.mkdirs()
            }

            val templateAssetPath = "templates/${template.id}/${config.language.lowercase()}/files"
            copyTemplateFiles(templateAssetPath, projectDir, config)
            
            // Note: Extra root files (like build.gradle.kts, gradle.properties etc) can be copied here if they were part of the template.
            // For now, the user requested only the files inside `files/` which contains MainActivity, etc.

            Result.success(projectDir)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun copyTemplateFiles(assetPath: String, destDir: File, config: ProjectConfig) {
        val assets = context.assets
        val items = assets.list(assetPath) ?: return

        for (item in items) {
            val currentAssetPath = "$assetPath/$item"
            val isDirectory = try {
                assets.list(currentAssetPath)?.isNotEmpty() == true
            } catch (e: Exception) {
                false
            }

            if (isDirectory) {
                val newDestDir = File(destDir, item)
                newDestDir.mkdirs()
                copyTemplateFiles(currentAssetPath, newDestDir, config)
            } else {
                // If the file is MainActivity, it needs to go into the package directory
                if (item == "MainActivity.kt" || item == "MainActivity.java") {
                    val packageDir = File(destDir, "app/src/main/java/${config.packageName.replace('.', '/')}")
                    packageDir.mkdirs()
                    val packageFile = File(packageDir, item)
                    processAndWriteFile(currentAssetPath, packageFile, config)
                } else if (item == "activity_main.xml") {
                    val layoutDir = File(destDir, "app/src/main/res/layout")
                    layoutDir.mkdirs()
                    val layoutFile = File(layoutDir, item)
                    processAndWriteFile(currentAssetPath, layoutFile, config)
                } else if (item == "AndroidManifest.xml") {
                    val manifestDir = File(destDir, "app/src/main")
                    manifestDir.mkdirs()
                    val manifestFile = File(manifestDir, item)
                    processAndWriteFile(currentAssetPath, manifestFile, config)
                } else if (item == "strings.xml") {
                    val valuesDir = File(destDir, "app/src/main/res/values")
                    valuesDir.mkdirs()
                    val valuesFile = File(valuesDir, item)
                    processAndWriteFile(currentAssetPath, valuesFile, config)
                } else {
                    // For generic files, just keep the relative path if any
                    val genericFile = File(destDir, item)
                    genericFile.parentFile?.mkdirs()
                    processAndWriteFile(currentAssetPath, genericFile, config)
                }
            }
        }
    }

    private fun processAndWriteFile(assetPath: String, destFile: File, config: ProjectConfig) {
        val inputStream = context.assets.open(assetPath)
        val content = InputStreamReader(inputStream).readText()
        val resolvedContent = VariableResolver.resolve(content, config)
        destFile.writeText(resolvedContent)
    }
}
