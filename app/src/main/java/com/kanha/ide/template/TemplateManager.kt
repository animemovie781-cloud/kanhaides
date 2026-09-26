package com.kanha.ide.template

import android.content.Context
import org.json.JSONObject
import java.io.InputStreamReader

object TemplateManager {

    private const val TEMPLATES_DIR = "templates"

    fun getTemplates(context: Context): List<Template> {
        val templates = mutableListOf<Template>()
        try {
            val templateDirs = context.assets.list(TEMPLATES_DIR) ?: return emptyList()
            for (dir in templateDirs) {
                val jsonPath = "$TEMPLATES_DIR/$dir/template.json"
                try {
                    val inputStream = context.assets.open(jsonPath)
                    val jsonString = InputStreamReader(inputStream).readText()
                    val jsonObject = JSONObject(jsonString)
                    
                    val languagesArray = jsonObject.getJSONArray("languages")
                    val languages = mutableListOf<String>()
                    for (i in 0 until languagesArray.length()) {
                        languages.add(languagesArray.getString(i))
                    }

                    val template = Template(
                        id = jsonObject.getString("id"),
                        name = jsonObject.getString("name"),
                        description = jsonObject.getString("description"),
                        category = jsonObject.getString("category"),
                        icon = jsonObject.getString("icon"),
                        languages = languages
                    )
                    templates.add(template)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return templates
    }

    fun getTemplate(context: Context, id: String): Template? {
        return getTemplates(context).find { it.id == id }
    }

    fun supportsLanguage(template: Template, language: String): Boolean {
        return template.languages.contains(language.lowercase())
    }
}
