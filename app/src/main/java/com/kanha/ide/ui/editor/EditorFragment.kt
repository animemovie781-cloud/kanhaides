package com.kanha.ide.ui.editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.kanha.ide.R
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.charset.Charset

class EditorFragment : Fragment() {

    private lateinit var codeEditor: CodeEditor
    private var currentFilePath: String? = null

    companion object {
        fun newInstance(filePath: String): EditorFragment {
            val args = Bundle()
            args.putString("FILE_PATH", filePath)
            val fragment = EditorFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_editor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        codeEditor = view.findViewById(R.id.codeEditor)
        setupEditor()

        currentFilePath = arguments?.getString("FILE_PATH")
        currentFilePath?.let {
            loadFile(File(it))
        }
    }

    private fun setupEditor() {
        // Set a basic dark theme
        val colorScheme = codeEditor.colorScheme
        colorScheme.setColor(EditorColorScheme.WHOLE_BACKGROUND, android.graphics.Color.parseColor("#121212"))
        colorScheme.setColor(EditorColorScheme.TEXT_NORMAL, android.graphics.Color.parseColor("#E0E0E0"))
        colorScheme.setColor(EditorColorScheme.LINE_NUMBER_BACKGROUND, android.graphics.Color.parseColor("#1E1E1E"))
        colorScheme.setColor(EditorColorScheme.LINE_NUMBER_PANEL_TEXT, android.graphics.Color.parseColor("#808080"))
        colorScheme.setColor(EditorColorScheme.SELECTION_INSERT, android.graphics.Color.parseColor("#1F6FEB"))
        colorScheme.setColor(EditorColorScheme.SELECTION_HANDLE, android.graphics.Color.parseColor("#1F6FEB"))
        
        codeEditor.isWordwrap = false
    }

    private fun loadFile(file: File) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (!file.exists() || !file.isFile) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Invalid file", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }
                
                // Note: Sora Editor provides its own asynchronous load logic for large files in newer versions,
                // but a simple text assignment is fine for basic files.
                val content = file.readText(Charset.defaultCharset())
                
                withContext(Dispatchers.Main) {
                    codeEditor.setText(content)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to load file", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    // Future Phase: save file
    fun saveFile() {
        currentFilePath?.let { path ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    File(path).writeText(codeEditor.text.toString())
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Save failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
