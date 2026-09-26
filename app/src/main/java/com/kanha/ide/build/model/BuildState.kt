package com.kanha.ide.build.model

enum class BuildState {
    IDLE,
    PREPARING,
    COMPILING_RESOURCES,
    COMPILING_JAVA,
    COMPILING_KOTLIN,
    DEXING,
    PACKAGING,
    ALIGNING,
    SIGNING,
    SUCCESS,
    FAILED,
    CANCELLED
}
