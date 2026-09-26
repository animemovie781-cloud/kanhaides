package com.kanha.ide.ui.explorer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kanha.ide.data.model.FileNode
import com.kanha.ide.data.repository.FileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class FileExplorerViewModel : ViewModel() {

    private lateinit var repository: FileRepository
    
    private val _fileTree = MutableStateFlow<List<FileNode>>(emptyList())
    val fileTree: StateFlow<List<FileNode>> = _fileTree.asStateFlow()
    
    private val rootNodes = mutableListOf<FileNode>()

    fun initProjectRoot(path: String) {
        repository = FileRepository(path)
        viewModelScope.launch {
            val children = repository.listChildren(File(path), 0)
            rootNodes.clear()
            rootNodes.addAll(children)
            _fileTree.value = rootNodes.toList()
        }
    }

    fun toggleFolder(node: FileNode, position: Int) {
        if (!node.isDirectory) return

        viewModelScope.launch {
            val currentList = _fileTree.value.toMutableList()
            val actualIndex = currentList.indexOf(node)
            if (actualIndex == -1) return@launch

            if (node.isExpanded) {
                // Collapse: remove all children and sub-children from list
                node.isExpanded = false
                val countToRemove = countVisibleChildren(node)
                
                for (i in 0 until countToRemove) {
                    currentList.removeAt(actualIndex + 1)
                }
                
                _fileTree.value = currentList
            } else {
                // Expand: load children if not loaded, then insert
                node.isExpanded = true
                if (node.children == null) {
                    node.children = repository.listChildren(node.file, node.depth + 1)
                }
                
                val children = node.children ?: emptyList()
                currentList.addAll(actualIndex + 1, children)
                
                _fileTree.value = currentList
            }
        }
    }
    
    private fun countVisibleChildren(node: FileNode): Int {
        var count = 0
        node.children?.let { children ->
            count += children.size
            for (child in children) {
                if (child.isExpanded) {
                    count += countVisibleChildren(child)
                }
            }
        }
        return count
    }

    fun refreshNode(node: FileNode) {
        viewModelScope.launch {
            // Find parent to refresh, or root if it's top level
            // For simplicity, let's just refresh the entire tree or the specific node's children
            if (node.isDirectory && node.isExpanded) {
                val newChildren = repository.listChildren(node.file, node.depth + 1)
                node.children = newChildren
            }
            // Trigger flow update
            _fileTree.value = _fileTree.value.toList()
        }
    }

    fun refreshAll() {
        viewModelScope.launch {
            // Re-fetch root nodes
            val path = rootNodes.firstOrNull()?.file?.parent ?: return@launch
            initProjectRoot(path)
        }
    }

    fun createFile(parent: File, name: String, onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            if (repository.createFile(parent, name)) onSuccess() else onError()
        }
    }

    fun createDirectory(parent: File, name: String, onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            if (repository.createDirectory(parent, name)) onSuccess() else onError()
        }
    }

    fun renameFile(file: File, newName: String, onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            if (repository.rename(file, newName)) onSuccess() else onError()
        }
    }

    fun deleteFile(file: File, onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            if (repository.delete(file)) onSuccess() else onError()
        }
    }
}
