package com.kanha.ide.ui.editor

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.kanha.ide.R
import com.kanha.ide.ui.explorer.FileExplorerFragment

class EditorActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor)

        val projectPath = intent.getStringExtra("PROJECT_PATH") ?: return finish()

        drawerLayout = findViewById(R.id.drawerLayout)
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

        if (savedInstanceState == null) {
            val explorerFragment = FileExplorerFragment.newInstance(projectPath)
            supportFragmentManager.beginTransaction()
                .replace(R.id.explorerContainer, explorerFragment)
                .commit()
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
        val editorFragment = EditorFragment.newInstance(filePath)
        supportFragmentManager.beginTransaction()
            .replace(R.id.editorContainer, editorFragment)
            .commit()
            
        // Optionally close the drawer when a file is opened on smaller screens
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        }
    }
}
