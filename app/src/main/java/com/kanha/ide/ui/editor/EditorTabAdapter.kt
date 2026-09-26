package com.kanha.ide.ui.editor

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kanha.ide.R

class EditorTabAdapter(
    private val onTabClick: (EditorTab) -> Unit,
    private val onCloseClick: (EditorTab) -> Unit
) : RecyclerView.Adapter<EditorTabAdapter.TabViewHolder>() {

    private val tabs = mutableListOf<EditorTab>()

    fun submitTabs(newTabs: List<EditorTab>) {
        tabs.clear()
        tabs.addAll(newTabs)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_editor_tab, parent, false)
        return TabViewHolder(view)
    }

    override fun onBindViewHolder(holder: TabViewHolder, position: Int) {
        val tab = tabs[position]
        
        holder.tvTabName.text = tab.fileName
        holder.tvModifiedIndicator.visibility = if (tab.isModified) View.VISIBLE else View.GONE
        
        if (tab.isActive) {
            holder.itemView.setBackgroundColor(Color.parseColor("#1E1E1E")) // surface_dark
            holder.tvTabName.setTextColor(Color.parseColor("#3DDC84")) // android_green to highlight active
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT)
            holder.tvTabName.setTextColor(Color.parseColor("#E0E0E0")) // text_primary
        }

        holder.itemView.setOnClickListener { onTabClick(tab) }
        holder.ivClose.setOnClickListener { onCloseClick(tab) }
    }

    override fun getItemCount(): Int = tabs.size

    inner class TabViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTabName: TextView = view.findViewById(R.id.tvTabName)
        val tvModifiedIndicator: TextView = view.findViewById(R.id.tvModifiedIndicator)
        val ivClose: ImageView = view.findViewById(R.id.ivClose)
    }
}
