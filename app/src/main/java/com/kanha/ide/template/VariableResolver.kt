package com.kanha.ide.template

import com.kanha.ide.project.ProjectConfig

object VariableResolver {

    fun resolve(content: String, config: ProjectConfig): String {
        return content
            .replace("{{APP_NAME}}", config.projectName)
            .replace("{{PACKAGE_NAME}}", config.packageName)
            .replace("{{MIN_SDK}}", config.minSdk.toString())
            .replace("{{TARGET_SDK}}", config.targetSdk.toString())
            .replace("{{COMPILE_SDK}}", config.compileSdk.toString())
            .replace("{{VERSION_NAME}}", config.versionName)
            .replace("{{VERSION_CODE}}", config.versionCode.toString())
            .replace("{{ACTIVITY_NAME}}", "MainActivity")
            .replace("{{PROJECT_NAME}}", config.projectName.replace(" ", ""))
    }
}
