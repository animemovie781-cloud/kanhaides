package com.kanha.ide.ui.editor

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class EditorViewModel : ViewModel() {

    private val _tabs = MutableStateFlow<List<EditorTab>>(emptyList())
    val tabs: StateFlow<List<EditorTab>> = _tabs.asStateFlow()

    private val _activeTabPath = MutableStateFlow<String?>(null)
    val activeTabPath: StateFlow<String?> = _activeTabPath.asStateFlow()

    fun openFile(filePath: String) {
        val currentTabs = _tabs.value.toMutableList()
        val existingTab = currentTabs.find { it.filePath == filePath }

        if (existingTab == null) {
            val file = File(filePath)
            val newTab = EditorTab(filePath = filePath, fileName = file.name, isActive = true)
            // Set all others to inactive
            currentTabs.forEach { it.isActive = false }
            currentTabs.add(newTab)
            _tabs.value = currentTabs
            _activeTabPath.value = filePath
        } else {
            // Tab already exists, make it active
            currentTabs.forEach { it.isActive = (it.filePath == filePath) }
            _tabs.value = currentTabs.toList() // Force flow emission
            _activeTabPath.value = filePath
        }
    }

    fun closeFile(filePath: String) {
        val currentTabs = _tabs.value.toMutableList()
        val index = currentTabs.indexOfFirst { it.filePath == filePath }
        if (index == -1) return

        val wasActive = currentTabs[index].isActive
        currentTabs.removeAt(index)

        if (wasActive) {
            // Select another tab if available
            if (currentTabs.isNotEmpty()) {
                val newActiveIndex = if (index > 0) index - 1 else 0
                currentTabs[newActiveIndex].isActive = true
                _activeTabPath.value = currentTabs[newActiveIndex].filePath
            } else {
                _activeTabPath.value = null
            }
        }

        _tabs.value = currentTabs
    }

    fun markModified(filePath: String, isModified: Boolean) {
        val currentTabs = _tabs.value.toMutableList()
        val tab = currentTabs.find { it.filePath == filePath }
        if (tab != null && tab.isModified != isModified) {
            tab.isModified = isModified
            _tabs.value = currentTabs.toList()
        }
    }
}
