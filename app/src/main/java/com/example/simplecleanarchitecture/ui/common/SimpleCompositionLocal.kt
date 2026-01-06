package com.example.simplecleanarchitecture.ui.common

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.compositionLocalOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

val LocalSnackbarHostState = compositionLocalOf<SnackbarHostState> { error("No SnackbarHostState provided") }

val LocalTopBarState = compositionLocalOf<TopBarState> { error("No TopBarState provided") }

val LocalNavigationStack = compositionLocalOf<NavBackStack<NavKey>> { error("No navigation back stack provided") }