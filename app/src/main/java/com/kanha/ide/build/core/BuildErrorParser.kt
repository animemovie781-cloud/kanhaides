package com.kanha.ide.build.core

import java.io.File

data class ParsedError(
    val file: File,
    val line: Int,
    val column: Int,
    val message: String,
    val severity: ErrorSeverity
)

enum class ErrorSeverity {
    ERROR, WARNING, INFO
}

class BuildErrorParser {
    
    fun parseJavacOutput(output: String): List<ParsedError> {
        val errors = mutableListOf<ParsedError>()
        // Simplified regex for standard javac error e.g. "MainActivity.java:15: error: cannot find symbol"
        val regex = Regex("^(.*?):(\\d+): (error|warning): (.*)$")
        
        output.lines().forEach { line ->
            val match = regex.find(line)
            if (match != null) {
                val (file, lineNum, severityStr, msg) = match.destructured
                errors.add(
                    ParsedError(
                        file = File(file),
                        line = lineNum.toIntOrNull() ?: 1,
                        column = 1,
                        message = msg,
                        severity = if (severityStr == "error") ErrorSeverity.ERROR else ErrorSeverity.WARNING
                    )
                )
            }
        }
        return errors
    }
    
    // Future: add parseKotlinOutput, parseAapt2Output
}
