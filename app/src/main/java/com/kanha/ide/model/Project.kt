package com.kanha.ide.model

data class Project(
    val id: Int,
    val name: String,
    val projectName: String,
    val version: String,
    val versionCode: Int,
    val packageName: String,
    val badgeNumber: Int
) {
    /** Returns formatted details string like "NewProject23 - 1.0 (1)" */
    val detailsText: String
        get() = "$projectName - $version ($versionCode)"
}
