package com.kanha.ide.ui.editor

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kanha.ide.R
import com.kanha.ide.ui.explorer.FileExplorerFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class EditorActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var rvEditorTabs: RecyclerView
    private lateinit var tabAdapter: EditorTabAdapter
    private lateinit var viewModel: EditorViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor)

        viewModel = androidx.lifecycle.ViewModelProvider(this)[EditorViewModel::class.java]

        val projectPath = intent.getStringExtra("PROJECT_PATH") ?: return finish()

        drawerLayout = findViewById(R.id.drawerLayout)
        rvEditorTabs = findViewById(R.id.rvEditorTabs)
        
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.editorToolbar)
        
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }
        
        // Open the drawer by default to show the project structure
        drawerLayout.openDrawer(GravityCompat.START)

        setupTabs()

        if (savedInstanceState == null) {
            val explorerFragment = FileExplorerFragment.newInstance(projectPath)
            supportFragmentManager.beginTransaction()
                .replace(R.id.explorerContainer, explorerFragment)
                .commit()
        }
    }
    
    private fun setupTabs() {
        tabAdapter = EditorTabAdapter(
            onTabClick = { tab -> viewModel.openFile(tab.filePath) },
            onCloseClick = { tab -> viewModel.closeFile(tab.filePath) }
        )
        rvEditorTabs.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvEditorTabs.adapter = tabAdapter

        lifecycleScope.launch {
            viewModel.tabs.collectLatest { tabs ->
                tabAdapter.submitTabs(tabs)
            }
        }

        lifecycleScope.launch {
            viewModel.activeTabPath.collectLatest { path ->
                if (path != null) {
                    val editorFragment = EditorFragment.newInstance(path)
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.editorContainer, editorFragment)
                        .commit()
                } else {
                    // Show empty placeholder or clear container
                    val fragment = supportFragmentManager.findFragmentById(R.id.editorContainer)
                    if (fragment != null) {
                        supportFragmentManager.beginTransaction()
                            .remove(fragment)
                            .commit()
                    }
                }
            }
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_editor, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val fragment = supportFragmentManager.findFragmentById(R.id.editorContainer) as? EditorFragment
        return when (item.itemId) {
            R.id.action_undo -> { fragment?.undo(); true }
            R.id.action_redo -> { fragment?.redo(); true }
            R.id.action_save -> { fragment?.saveFile(); true }
            R.id.action_search -> { fragment?.search(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
    
    fun openFile(filePath: String) {
        viewModel.openFile(filePath)
            
        // Optionally close the drawer when a file is opened on smaller screens
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        }
    }
}
