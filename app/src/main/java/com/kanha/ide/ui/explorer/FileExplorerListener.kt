package com.kanha.ide.ui.explorer

import com.kanha.ide.data.model.FileNode
import android.view.View

interface FileExplorerListener {
    fun onFileClick(node: FileNode)
    fun onFolderClick(node: FileNode, position: Int)
    fun onFileLongClick(node: FileNode, view: View)
}
