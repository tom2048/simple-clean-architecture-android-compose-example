@file:Suppress("unused")

package com.example.simplecleanarchitecture.ui.common

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf

class TopBarState {

    val topBarData by mutableStateOf<TopBarData?>(null)
    // TODO: extend the implementation
}

sealed interface TopBarData {
    object None : TopBarData
    data class Simple(val title: String) : TopBarData
    // TODO: extend with new top bars
}