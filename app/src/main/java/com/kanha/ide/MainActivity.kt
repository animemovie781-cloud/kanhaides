package com.kanha.ide

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.kanha.ide.adapter.ProjectAdapter
import com.kanha.ide.model.Project

class MainActivity : AppCompatActivity() {

    private lateinit var rvProjects: RecyclerView
    private lateinit var projectAdapter: ProjectAdapter
    private lateinit var etSearch: EditText
    private lateinit var fabNewProject: ExtendedFloatingActionButton
    private lateinit var bottomNavigation: BottomNavigationView

    private val allProjects = mutableListOf<Project>()
    private val filteredProjects = mutableListOf<Project>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupRecyclerView()
        loadSampleProjects()
        setupSearch()
        setupClickListeners()
        setupBottomNavigation()
    }

    override fun onResume() {
        super.onResume()
        // Refresh project list when returning to this screen
        loadSampleProjects()
    }

    private fun initViews() {
        rvProjects = findViewById(R.id.rvProjects)
        etSearch = findViewById(R.id.etSearch)
        fabNewProject = findViewById(R.id.fabNewProject)
        bottomNavigation = findViewById(R.id.bottomNavigation)
    }

    private fun setupRecyclerView() {
        projectAdapter = ProjectAdapter(
            filteredProjects,
            onItemClick = { project ->
                val intent = android.content.Intent(this, com.kanha.ide.ui.editor.EditorActivity::class.java)
                intent.putExtra("PROJECT_PATH", project.path)
                startActivity(intent)
            },
            onDropdownClick = { project, view ->
                showProjectOptionsMenu(project, view)
            }
        )

        rvProjects.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = projectAdapter
            setHasFixedSize(false)
        }
    }

    private fun loadSampleProjects() {
        val localProjects = com.kanha.ide.project.ProjectManager.getLocalProjects(this)
        
        if (localProjects.isEmpty()) {
            // Optional: load some dummy data if empty for demo purposes, 
            // but since it's an IDE, it's better to show an empty state.
            // For now, let's just clear and show empty list.
            allProjects.clear()
        } else {
            allProjects.clear()
            allProjects.addAll(localProjects)
        }

        filteredProjects.clear()
        filteredProjects.addAll(allProjects)
        projectAdapter.notifyDataSetChanged()
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().lowercase().trim()
                filteredProjects.clear()
                if (query.isEmpty()) {
                    filteredProjects.addAll(allProjects)
                } else {
                    filteredProjects.addAll(
                        allProjects.filter {
                            it.name.lowercase().contains(query) ||
                            it.packageName.lowercase().contains(query) ||
                            it.projectName.lowercase().contains(query)
                        }
                    )
                }
                projectAdapter.notifyDataSetChanged()
            }
        })
    }

    private fun setupClickListeners() {
        // Hamburger menu
        findViewById<ImageButton>(R.id.btnMenu).setOnClickListener {
            Toast.makeText(this, "Menu clicked", Toast.LENGTH_SHORT).show()
            // TODO: Open drawer navigation
        }

        // Search icon
        findViewById<ImageButton>(R.id.btnSearch).setOnClickListener {
            etSearch.requestFocus()
        }

        // Sort button
        findViewById<ImageButton>(R.id.btnSort).setOnClickListener {
            sortProjectsAlphabetically()
        }

        // Restore projects card
        findViewById<View>(R.id.restoreProjectsCard).setOnClickListener {
            Toast.makeText(this, "Restore Projects", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to restore screen
        }

        // FAB - New project
        fabNewProject.setOnClickListener {
            startActivity(android.content.Intent(this, com.kanha.ide.ui.NewProjectActivity::class.java))
        }
    }

    private fun setupBottomNavigation() {
        bottomNavigation.selectedItemId = R.id.nav_projects

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_projects -> {
                    // Already on projects tab
                    true
                }
                R.id.nav_store -> {
                    Toast.makeText(this, "Store coming soon!", Toast.LENGTH_SHORT).show()
                    // TODO: Navigate to store
                    true
                }
                else -> false
            }
        }
    }

    private var isSortedAZ = false

    private fun sortProjectsAlphabetically() {
        if (isSortedAZ) {
            filteredProjects.sortByDescending { it.name.lowercase() }
        } else {
            filteredProjects.sortBy { it.name.lowercase() }
        }
        isSortedAZ = !isSortedAZ
        projectAdapter.notifyDataSetChanged()
    }

    private fun showProjectOptionsMenu(project: Project, anchor: View) {
        val popup = PopupMenu(this, anchor)
        popup.menu.add("Open")
        popup.menu.add("Rename")
        popup.menu.add("Delete")
        popup.menu.add("Export")
        popup.menu.add("Properties")

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.title) {
                "Open" -> Toast.makeText(this, "Opening ${project.name}", Toast.LENGTH_SHORT).show()
                "Rename" -> Toast.makeText(this, "Rename ${project.name}", Toast.LENGTH_SHORT).show()
                "Delete" -> Toast.makeText(this, "Delete ${project.name}", Toast.LENGTH_SHORT).show()
                "Export" -> Toast.makeText(this, "Export ${project.name}", Toast.LENGTH_SHORT).show()
                "Properties" -> Toast.makeText(this, "Properties of ${project.name}", Toast.LENGTH_SHORT).show()
            }
            true
        }
        popup.show()
    }
}
