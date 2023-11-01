package com.example.simplecleanarchitecture.users.ui.passwordchange

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.simplecleanarchitecture.MainRouter
import com.example.simplecleanarchitecture.users.ui.passwordchange.UserPasswordChangeViewModel.UiEffect.Message
import com.example.simplecleanarchitecture.users.ui.passwordchange.UserPasswordChangeViewModel.UiEffect.Routing
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.stateViewModel
import org.koin.core.parameter.parametersOf

class UserPasswordChangeFragment : Fragment() {

    private val viewModel: UserPasswordChangeViewModel by stateViewModel(state = { Bundle.EMPTY }) {
        parametersOf(arguments?.getString(USER_ID_KEY) ?: "")
    }
    private val routing: MainRouter by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply { setContent { Layout() } }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun Layout() {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val uiEffect: UserPasswordChangeViewModel.UiEffect by viewModel.uiEffect.collectAsStateWithLifecycle(Message(""))
        val snackbarHostState = remember { SnackbarHostState() }
        val coroutineScope = rememberCoroutineScope()
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
                        onValueChange = { viewModel.setPassword(it) },
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
                        onValueChange = { viewModel.setPasswordConfirmed(it) },
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
                        onClick = { viewModel.submit() },
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

        LaunchedEffect(uiEffect) {
            when (val effect: UserPasswordChangeViewModel.UiEffect = uiEffect) {
                is Message -> coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = effect.text,
                        duration = SnackbarDuration.Short
                    )
                }

                is Routing -> routing.execute(effect.command)
                else -> {}
            }
        }
    }

    companion object {

        private const val USER_ID_KEY = "USER_ID_KEY"

        fun newInstance(id: String? = null) = UserPasswordChangeFragment().apply {
            arguments = Bundle().apply {
                putString(USER_ID_KEY, id)
            }
        }

    }
}