package com.kanha.ide.build.model

import java.io.File

data class BuildConfig(
    val projectRoot: File,
    val isDebug: Boolean = true,
    val compileSdk: Int = 34,
    val buildToolsVersion: String = "34.0.0",
    val applicationId: String,
    val versionCode: Int = 1,
    val versionName: String = "1.0",
    val minSdk: Int = 24,
    val targetSdk: Int = 34
)
