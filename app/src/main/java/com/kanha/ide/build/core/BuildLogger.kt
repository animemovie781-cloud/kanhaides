package com.kanha.ide.build.core

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class BuildLogger {
    private val _logs = MutableSharedFlow<String>(replay = 100)
    val logs: SharedFlow<String> = _logs.asSharedFlow()

    suspend fun log(message: String) {
        _logs.emit(message)
    }

    suspend fun error(message: String) {
        _logs.emit("[ERROR] $message")
    }
}
