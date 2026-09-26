package com.kanha.ide.build.model

data class BuildResult(
    val success: Boolean,
    val message: String,
    val artifacts: List<BuildArtifact> = emptyList(),
    val durationMs: Long = 0
)
