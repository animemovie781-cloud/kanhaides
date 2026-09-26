package com.kanha.ide.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.kanha.ide.MainActivity
import com.kanha.ide.R
import com.kanha.ide.project.ProjectConfig
import com.kanha.ide.project.ProjectGenerator
import com.kanha.ide.template.TemplateManager
import kotlinx.coroutines.launch
import java.io.File

class ProjectDetailsActivity : AppCompatActivity() {

    private lateinit var templateId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_details)

        templateId = intent.getStringExtra("TEMPLATE_ID") ?: return finish()

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        val etProjectName: EditText = findViewById(R.id.etProjectName)
        val etPackageName: EditText = findViewById(R.id.etPackageName)
        val etLocation: EditText = findViewById(R.id.etLocation)
        val rgLanguage: RadioGroup = findViewById(R.id.rgLanguage)
        val spMinSdk: Spinner = findViewById(R.id.spMinSdk)
        val spTargetSdk: Spinner = findViewById(R.id.spTargetSdk)
        val spCompileSdk: Spinner = findViewById(R.id.spCompileSdk)
        val btnGenerate: Button = findViewById(R.id.btnGenerate)
        
        val sdkOptions = arrayOf(21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35)
        val sdkStrings = sdkOptions.map { "API $it" }.toTypedArray()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sdkStrings)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        
        spMinSdk.adapter = adapter
        spTargetSdk.adapter = adapter
        spCompileSdk.adapter = adapter
        
        spMinSdk.setSelection(sdkOptions.indexOf(24))
        spTargetSdk.setSelection(sdkOptions.indexOf(34))
        spCompileSdk.setSelection(sdkOptions.indexOf(34))
        
        // Setup default path - replace with a real storage path logically
        val defaultPath = getExternalFilesDir(null)?.absolutePath + "/AndroidProjects"
        etLocation.setText(defaultPath)

        btnGenerate.setOnClickListener {
            val projectName = etProjectName.text.toString().trim()
            val packageName = etPackageName.text.toString().trim()
            val location = etLocation.text.toString().trim()
            
            val language = if (rgLanguage.checkedRadioButtonId == R.id.rbKotlin) "kotlin" else "java"

            if (projectName.isEmpty() || packageName.isEmpty() || location.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!packageName.matches(Regex("^[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)+$"))) {
                Toast.makeText(this, "Invalid package name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val template = TemplateManager.getTemplate(this, templateId)
            if (template == null) {
                Toast.makeText(this, "Template not found", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!TemplateManager.supportsLanguage(template, language)) {
                Toast.makeText(this, "Language not supported by this template", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val projectPath = "$location/${projectName.replace(" ", "")}"

            val minSdk = sdkOptions[spMinSdk.selectedItemPosition]
            val targetSdk = sdkOptions[spTargetSdk.selectedItemPosition]
            val compileSdk = sdkOptions[spCompileSdk.selectedItemPosition]

            val config = ProjectConfig(
                projectName = projectName,
                packageName = packageName,
                language = language,
                templateId = templateId,
                projectPath = projectPath,
                minSdk = minSdk,
                targetSdk = targetSdk,
                compileSdk = compileSdk,
                versionName = "1.0",
                versionCode = 1
            )

            btnGenerate.isEnabled = false
            btnGenerate.text = "Generating..."

            lifecycleScope.launch {
                val generator = ProjectGenerator(this@ProjectDetailsActivity)
                val result = generator.generate(template, config)

                if (result.isSuccess) {
                    Toast.makeText(this@ProjectDetailsActivity, "Project created successfully!", Toast.LENGTH_LONG).show()
                    // Open main activity and maybe pass the new project path to open it
                    val intent = Intent(this@ProjectDetailsActivity, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        putExtra("OPEN_PROJECT_PATH", projectPath)
                    }
                    startActivity(intent)
                    finish()
                } else {
                    btnGenerate.isEnabled = true
                    btnGenerate.text = "Create Project"
                    Toast.makeText(this@ProjectDetailsActivity, "Error: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
