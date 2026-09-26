package com.kanha.ide.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kanha.ide.R
import com.kanha.ide.template.Template

class TemplateAdapter(
    private val templates: List<Template>,
    private val onClick: (Template) -> Unit
) : RecyclerView.Adapter<TemplateAdapter.TemplateViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION

    inner class TemplateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rootLayout: LinearLayout = itemView.findViewById(R.id.rootLayout)
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val tvLanguages: TextView = itemView.findViewById(R.id.tvLanguages)

        init {
            itemView.setOnClickListener {
                val previousSelected = selectedPosition
                selectedPosition = bindingAdapterPosition
                notifyItemChanged(previousSelected)
                notifyItemChanged(selectedPosition)
                onClick(templates[selectedPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TemplateViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_template, parent, false)
        return TemplateViewHolder(view)
    }

    override fun onBindViewHolder(holder: TemplateViewHolder, position: Int) {
        val template = templates[position]
        holder.tvName.text = template.name
        holder.tvDescription.text = template.description
        
        val langString = template.languages.joinToString(" • ") { it.replaceFirstChar { char -> char.uppercase() } }
        holder.tvLanguages.text = langString

        if (position == selectedPosition) {
            holder.rootLayout.setBackgroundColor(Color.parseColor("#2C2C2C")) // Slightly lighter for selected state
        } else {
            holder.rootLayout.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    override fun getItemCount() = templates.size
}
