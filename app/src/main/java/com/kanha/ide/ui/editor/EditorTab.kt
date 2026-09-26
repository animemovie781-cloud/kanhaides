package com.kanha.ide.ui.editor

data class EditorTab(
    val filePath: String,
    val fileName: String,
    var isModified: Boolean = false,
    var isActive: Boolean = false
)
