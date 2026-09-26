package com.kanha.ide.ui.explorer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kanha.ide.R
import com.kanha.ide.data.model.FileNode
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

class FileExplorerFragment : Fragment(), FileExplorerListener {

    private lateinit var viewModel: FileExplorerViewModel
    private lateinit var adapter: FileTreeAdapter
    private var projectPath: String? = null

    companion object {
        fun newInstance(projectPath: String): FileExplorerFragment {
            val args = Bundle()
            args.putString("PROJECT_PATH", projectPath)
            val fragment = FileExplorerFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        projectPath = arguments?.getString("PROJECT_PATH")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_file_explorer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[FileExplorerViewModel::class.java]

        val tvProjectRoot: TextView = view.findViewById(R.id.tvProjectRoot)
        val rvFileTree: RecyclerView = view.findViewById(R.id.rvFileTree)

        adapter = FileTreeAdapter(this)
        rvFileTree.layoutManager = LinearLayoutManager(requireContext())
        rvFileTree.adapter = adapter
        
        // Remove item animator for smoother tree expand/collapse
        rvFileTree.itemAnimator = null

        projectPath?.let { path ->
            tvProjectRoot.text = File(path).name
            viewModel.initProjectRoot(path)
        } ?: run {
            tvProjectRoot.text = "No Project Opened"
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.fileTree.collectLatest { fileNodes ->
                adapter.submitList(fileNodes)
            }
        }
    }

    override fun onFileClick(node: FileNode) {
        (activity as? com.kanha.ide.ui.editor.EditorActivity)?.openFile(node.path)
    }

    override fun onFolderClick(node: FileNode, position: Int) {
        viewModel.toggleFolder(node, position)
    }

    override fun onFileLongClick(node: FileNode, view: View) {
        val bottomSheet = FileActionBottomSheet(node, viewModel)
        bottomSheet.show(parentFragmentManager, "FileActionBottomSheet")
    }
}
