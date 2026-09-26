package com.kanha.ide.ui.explorer

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kanha.ide.R
import com.kanha.ide.data.model.FileNode

class FileActionBottomSheet(
    private val node: FileNode,
    private val viewModel: FileExplorerViewModel
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_file_actions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.tvTitle).text = node.name
        
        val btnNewFile: TextView = view.findViewById(R.id.btnNewFile)
        val btnNewFolder: TextView = view.findViewById(R.id.btnNewFolder)
        
        if (!node.isDirectory) {
            btnNewFile.visibility = View.GONE
            btnNewFolder.visibility = View.GONE
        }

        btnNewFile.setOnClickListener {
            dismiss()
            showInputDialog("New File", "Enter file name") { name ->
                viewModel.createFile(node.file, name,
                    onSuccess = { 
                        Toast.makeText(context, "Created", Toast.LENGTH_SHORT).show()
                        viewModel.refreshAll()
                    },
                    onError = { Toast.makeText(context, "Error creating file", Toast.LENGTH_SHORT).show() }
                )
            }
        }

        btnNewFolder.setOnClickListener {
            dismiss()
            showInputDialog("New Folder", "Enter folder name") { name ->
                viewModel.createDirectory(node.file, name,
                    onSuccess = { 
                        Toast.makeText(context, "Created", Toast.LENGTH_SHORT).show()
                        viewModel.refreshAll()
                    },
                    onError = { Toast.makeText(context, "Error creating folder", Toast.LENGTH_SHORT).show() }
                )
            }
        }

        view.findViewById<TextView>(R.id.btnRename).setOnClickListener {
            dismiss()
            showInputDialog("Rename", "Enter new name", node.name) { newName ->
                viewModel.renameFile(node.file, newName,
                    onSuccess = { 
                        Toast.makeText(context, "Renamed", Toast.LENGTH_SHORT).show()
                        viewModel.refreshAll()
                    },
                    onError = { Toast.makeText(context, "Error renaming", Toast.LENGTH_SHORT).show() }
                )
            }
        }

        view.findViewById<TextView>(R.id.btnDelete).setOnClickListener {
            dismiss()
            AlertDialog.Builder(requireContext())
                .setTitle("Delete")
                .setMessage("Are you sure you want to delete ${node.name}?")
                .setPositiveButton("Delete") { _, _ ->
                    viewModel.deleteFile(node.file,
                        onSuccess = { 
                            Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
                            viewModel.refreshAll()
                        },
                        onError = { Toast.makeText(context, "Error deleting", Toast.LENGTH_SHORT).show() }
                    )
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun showInputDialog(title: String, hint: String, defaultText: String = "", onResult: (String) -> Unit) {
        val input = EditText(requireContext())
        input.hint = hint
        input.setText(defaultText)
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        input.layoutParams = lp

        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setView(input)
            .setPositiveButton("OK") { _, _ ->
                val text = input.text.toString().trim()
                if (text.isNotEmpty()) {
                    onResult(text)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
