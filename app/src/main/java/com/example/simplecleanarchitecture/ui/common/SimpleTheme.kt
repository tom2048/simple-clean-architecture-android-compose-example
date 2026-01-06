@file:Suppress("unused")

package com.example.simplecleanarchitecture.ui.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun SimpleTheme(content: @Composable () -> Unit) {
    // TODO: to be extended
    MaterialTheme(content = content)
    LocalContext
}

object SimpleTheme {
    // TODO: to be extended
}