package com.kanha.ide.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.Toolbar
import com.kanha.ide.R
import com.kanha.ide.template.Template
import com.kanha.ide.template.TemplateManager

class NewProjectActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_project)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        val rvTemplates: RecyclerView = findViewById(R.id.rvTemplates)
        rvTemplates.layoutManager = GridLayoutManager(this, 2) // Grid of 2 columns

        val templates = TemplateManager.getTemplates(this)
        
        if (templates.isEmpty()) {
            Toast.makeText(this, "No templates found in assets", Toast.LENGTH_LONG).show()
        }

        val adapter = TemplateAdapter(templates) { selectedTemplate ->
            // Launch ProjectDetailsActivity
            val intent = Intent(this, ProjectDetailsActivity::class.java).apply {
                putExtra("TEMPLATE_ID", selectedTemplate.id)
            }
            startActivity(intent)
        }
        rvTemplates.adapter = adapter
    }
}
