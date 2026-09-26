package com.kanha.ide.template

data class Template(
    val id: String,
    val name: String,
    val description: String,
    val category: String,
    val icon: String,
    val languages: List<String>
)
