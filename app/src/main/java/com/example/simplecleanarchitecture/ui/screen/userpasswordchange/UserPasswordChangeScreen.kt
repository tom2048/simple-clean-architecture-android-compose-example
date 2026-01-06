package com.example.simplecleanarchitecture.ui.screen.userpasswordchange

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.simplecleanarchitecture.ui.common.LocalNavigationStack
import com.example.simplecleanarchitecture.ui.common.LocalSnackbarHostState
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import com.example.simplecleanarchitecture.ui.screen.userpasswordchange.UserPasswordChangeUiModel as Ui

@Composable
fun UserPasswordChangeScreen(userId: String) {
    val viewModel: UserPasswordChangeViewModel = koinViewModel {
        parametersOf(userId)
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val uiEffect: Ui.Effect by viewModel.uiEffect.collectAsStateWithLifecycle(Ui.Effect.Undefined)
    UserPasswordChangeLayout(uiState, viewModel)
    UiEffectHandler(uiEffect)
}

@Composable
private fun UserPasswordChangeLayout(
    uiState: Ui.State,
    listener: UserPasswordChangeUiListener
) {
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { scaffoldPadding ->
        if (!uiState.preloader) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(scaffoldPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Password setup",
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(PaddingValues(top = 4.dp)),
                    fontSize = 24.sp
                )
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(PaddingValues(top = 8.dp)),
                    text = "Please provide user credentials below:"
                )
                TextField(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    value = uiState.password,
                    onValueChange = listener::setPassword,
                    label = { Text("Password") },
                    isError = uiState.passwordValidation.isNotEmpty(),
                    supportingText = { Text(uiState.passwordValidation) },
                    visualTransformation = PasswordVisualTransformation(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        errorContainerColor = Color.Transparent
                    )
                )
                TextField(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    value = uiState.passwordConfirmed,
                    onValueChange = listener::setPasswordConfirmed,
                    label = { Text("Confirm password") },
                    isError = uiState.passwordConfirmedValidation.isNotEmpty(),
                    supportingText = { Text(uiState.passwordConfirmedValidation) },
                    visualTransformation = PasswordVisualTransformation(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        errorContainerColor = Color.Transparent
                    )
                )
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    modifier = Modifier
                        .align(alignment = Alignment.CenterHorizontally)
                        .padding(8.dp),
                    onClick = listener::submit,
                    enabled = uiState.isSubmitEnabled
                ) {
                    Text(text = "Set password")
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun UiEffectHandler(uiEffect: Ui.Effect) {
    val snackbarHostState = LocalSnackbarHostState.current
    val coroutineScope = rememberCoroutineScope()
    val backStack = LocalNavigationStack.current
    LaunchedEffect(uiEffect) {
        when (val effect: Ui.Effect = uiEffect) {
            is Ui.Effect.CloseScreen -> backStack.removeLastOrNull()
            is Ui.Effect.ShowMessage -> coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = effect.text,
                    duration = SnackbarDuration.Short
                )
            }

            Ui.Effect.Undefined -> {}
        }
    }
}