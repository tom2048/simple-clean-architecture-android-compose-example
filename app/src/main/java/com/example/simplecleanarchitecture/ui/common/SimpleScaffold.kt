package com.example.simplecleanarchitecture.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
fun SimpleScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable ((TopBarState) -> Unit)? = null,
    bottomBar: @Composable (() -> Unit)? = null,
    snackbar: @Composable ((SnackbarData) -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val topBarState = remember { TopBarState() }

    CompositionLocalProvider(
        LocalSnackbarHostState provides snackbarHostState,
        LocalTopBarState provides topBarState
    ) {
        Scaffold(
            modifier = modifier,
            topBar = { topBar?.invoke(topBarState) },
            bottomBar = bottomBar ?: {},
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    snackbar = snackbar ?: { Snackbar(it) }
                )
            },
            content = { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    content.invoke()
                }
            }
        )
    }
}

