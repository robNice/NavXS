package de.robnice.navxs.ui

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object AppForegroundState {
    private val _isInForeground = MutableStateFlow(false)
    val isInForeground: StateFlow<Boolean> = _isInForeground

    fun setInForeground(inForeground: Boolean) {
        _isInForeground.value = inForeground
    }
}
