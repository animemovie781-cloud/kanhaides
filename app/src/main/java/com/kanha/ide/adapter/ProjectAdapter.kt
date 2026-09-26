package com.kanha.ide.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kanha.ide.R
import com.kanha.ide.model.Project

class ProjectAdapter(
    private var projects: MutableList<Project>,
    private val onItemClick: (Project) -> Unit,
    private val onDropdownClick: (Project, View) -> Unit
) : RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder>() {

    inner class ProjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvProjectName: TextView = itemView.findViewById(R.id.tvProjectName)
        val tvProjectDetails: TextView = itemView.findViewById(R.id.tvProjectDetails)
        val tvPackageName: TextView = itemView.findViewById(R.id.tvPackageName)
        val tvBadge: TextView = itemView.findViewById(R.id.tvBadge)
        val ivDropdown: ImageView = itemView.findViewById(R.id.ivDropdown)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_project, parent, false)
        return ProjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        val project = projects[position]

        holder.tvProjectName.text = project.name
        holder.tvProjectDetails.text = project.detailsText
        holder.tvPackageName.text = project.packageName
        holder.tvBadge.text = project.badgeNumber.toString()

        holder.itemView.setOnClickListener {
            onItemClick(project)
        }

        holder.ivDropdown.setOnClickListener { view ->
            onDropdownClick(project, view)
        }
    }

    override fun getItemCount(): Int = projects.size

    fun updateProjects(newProjects: List<Project>) {
        projects.clear()
        projects.addAll(newProjects)
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        // This method can be extended for search functionality
    }
}
