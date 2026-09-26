package com.kanha.ide.build.model

import java.io.File

data class BuildArtifact(
    val type: ArtifactType,
    val file: File
)

enum class ArtifactType {
    APK_DEBUG,
    APK_RELEASE,
    AAR,
    JAR,
    DEX
}
