package com.example.simplecleanarchitecture.core.lib.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

abstract class BaseUiStateViewModel<UiState : Any, UiEffect : Any>(uiState: UiState) : ViewModel() {

    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(uiState)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _uiEffect: MutableSharedFlow<UiEffect> = MutableSharedFlow(replay = 0)
    val uiEffect: SharedFlow<UiEffect> = _uiEffect

    protected fun updateState(update: (UiState) -> UiState) {
        _uiState.update(update)
    }

    protected suspend fun sendEffect(effect: UiEffect) {
        _uiEffect.emit(effect)
    }

}