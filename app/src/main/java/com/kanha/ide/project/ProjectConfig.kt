package com.kanha.ide.project

data class ProjectConfig(
    val projectName: String,
    val packageName: String,
    val language: String,
    val templateId: String,
    val projectPath: String,
    val minSdk: Int,
    val targetSdk: Int,
    val compileSdk: Int,
    val versionName: String,
    val versionCode: Int
)
