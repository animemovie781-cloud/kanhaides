package com.kanha.ide.ui.explorer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kanha.ide.R
import com.kanha.ide.data.model.FileNode

class FileTreeAdapter(
    private val listener: FileExplorerListener
) : RecyclerView.Adapter<FileTreeAdapter.FileViewHolder>() {

    private val items = mutableListOf<FileNode>()

    fun submitList(newItems: List<FileNode>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_file_tree, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val node = items[position]
        
        holder.tvFileName.text = node.name
        
        // Indentation based on depth (e.g. 24dp per depth level)
        val paddingLeft = (node.depth * 24 * holder.itemView.context.resources.displayMetrics.density).toInt()
        holder.itemView.setPadding(
            paddingLeft + (8 * holder.itemView.context.resources.displayMetrics.density).toInt(),
            holder.itemView.paddingTop,
            holder.itemView.paddingRight,
            holder.itemView.paddingBottom
        )

        if (node.isDirectory) {
            if (node.isExpanded) {
                holder.ivIcon.setImageResource(R.drawable.ic_folder_open)
            } else {
                holder.ivIcon.setImageResource(R.drawable.ic_folder)
            }
            holder.itemView.setOnClickListener {
                listener.onFolderClick(node, position)
            }
        } else {
            // Very simple file type icon logic (can be expanded later)
            holder.ivIcon.setImageResource(R.drawable.ic_file)
            holder.itemView.setOnClickListener {
                listener.onFileClick(node)
            }
        }

        holder.itemView.setOnLongClickListener { v ->
            listener.onFileLongClick(node, v)
            true
        }
    }

    override fun getItemCount(): Int = items.size

    inner class FileViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvFileName: TextView = view.findViewById(R.id.tvFileName)
        val ivIcon: ImageView = view.findViewById(R.id.ivIcon)
    }
}
