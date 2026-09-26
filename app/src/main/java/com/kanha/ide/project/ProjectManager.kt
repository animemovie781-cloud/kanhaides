package com.kanha.ide.project

import android.content.Context
import com.kanha.ide.model.Project
import java.io.File

object ProjectManager {

    fun getLocalProjects(context: Context): List<Project> {
        val projects = mutableListOf<Project>()
        val defaultPath = File(context.getExternalFilesDir(null), "AndroidProjects")
        
        if (!defaultPath.exists()) {
            defaultPath.mkdirs()
            return projects
        }

        val dirs = defaultPath.listFiles { file -> file.isDirectory } ?: return projects

        var idCounter = 1
        for (dir in dirs) {
            // Very basic heuristic to check if it's an Android project
            // A real implementation would parse build.gradle or app/src/main/AndroidManifest.xml
            val isAndroidProject = File(dir, "app").exists() || File(dir, "build.gradle").exists() || File(dir, "build.gradle.kts").exists()
            
            if (isAndroidProject) {
                projects.add(
                    Project(
                        id = idCounter++,
                        name = dir.name,
                        projectName = dir.name,
                        version = "1.0",
                        versionCode = 1,
                        packageName = "com.example.${dir.name.lowercase()}", // Mocked, ideally read from Manifest/build.gradle
                        badgeNumber = 0,
                        path = dir.absolutePath
                    )
                )
            }
        }
        
        // Sort by last modified (newest first)
        projects.sortByDescending { File(it.path).lastModified() }
        
        return projects
    }
}
