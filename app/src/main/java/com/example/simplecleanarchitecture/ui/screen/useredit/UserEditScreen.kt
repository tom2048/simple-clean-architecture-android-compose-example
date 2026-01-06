package com.example.simplecleanarchitecture.ui.screen.useredit

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.simplecleanarchitecture.R
import com.example.simplecleanarchitecture.ui.common.LocalNavigationStack
import com.example.simplecleanarchitecture.ui.common.LocalSnackbarHostState
import com.example.simplecleanarchitecture.ui.screen.useredit.UserEditUiModel.Effect.ShowMessage
import com.example.simplecleanarchitecture.ui.screen.useredit.UserEditUiModel.Effect.Undefined
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import com.example.simplecleanarchitecture.ui.screen.useredit.UserEditUiModel as Ui

@Composable
fun UserEditScreen(id: String?) {
    val viewModel: UserEditViewModel = koinViewModel {
        parametersOf(id)
    }
    val uiState: Ui.State by viewModel.uiState.collectAsStateWithLifecycle()
    val uiEffect: Ui.Effect by viewModel.uiEffect.collectAsStateWithLifecycle(Undefined)

    UserEditLayout(uiState, viewModel)
    UiEffectHandler(uiEffect)
}

@Composable
fun UserEditLayout(uiState: Ui.State, uiListener: UserEditUiListener) {
    val avatarLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.toString()?.let { uiListener.addAvatar(it) }
    }
    val idScanLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.toString()?.let { uiListener.addIdScan(it) }
    }
    if (!uiState.preloader) {
        Column {
            Text(
                modifier = Modifier
                    .padding(8.dp)
                    .align(alignment = Alignment.CenterHorizontally),
                text = uiState.header,
                fontSize = 24.sp
            )
            TextField(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                value = uiState.nickname,
                onValueChange = { uiListener.setNickname(it) },
                label = { Text(stringResource(R.string.user_nickname_hint)) },
                isError = uiState.nicknameValidationError.isNotEmpty(),
                supportingText = { Text(uiState.nicknameValidationError) },
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
                value = uiState.email,
                onValueChange = { uiListener.setEmail(it) },
                label = { Text(stringResource(R.string.user_email_hint)) },
                isError = uiState.emailValidationError.isNotEmpty(),
                supportingText = { Text(uiState.emailValidationError) },
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
                value = uiState.description,
                onValueChange = { uiListener.setDescription(it) },
                label = { Text(stringResource(R.string.user_description_hint)) },
                isError = uiState.descriptionValidationError.isNotEmpty(),
                supportingText = { Text(uiState.descriptionValidationError) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    errorContainerColor = Color.Transparent
                )
            )
            Row {
                ImageBox(
                    modifier = Modifier.weight(1f),
                    imageUrl = uiState.newAvatarUri ?: uiState.avatarUri,
                    emptyResource = R.drawable.user_avatar_ico,
                    text = "Avatar",
                    onClick = { avatarLauncher.launch("image/*") }
                )
                ImageBox(
                    modifier = Modifier.weight(1f),
                    imageUrl = uiState.newIdScanUri ?: uiState.idScanUri,
                    emptyResource = R.drawable.user_id_scan_ico,
                    text = "Id scan",
                    onClick = { idScanLauncher.launch("image/*") }
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    modifier = Modifier.padding(8.dp),
                    onClick = { uiListener.cancel() }
                ) {
                    Text(text = stringResource(R.string.user_edit_cancel_button))
                }
                Button(
                    modifier = Modifier.padding(8.dp),
                    onClick = { uiListener.submit() },
                    enabled = uiState.isSubmitEnabled
                ) {
                    Text(text = stringResource(R.string.user_edit_save_button))
                }
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(modifier = Modifier.align(alignment = Alignment.Center))
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
            is ShowMessage -> coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = effect.text,
                    duration = SnackbarDuration.Short
                )
            }

            is Ui.Effect.CloseScreen -> backStack.removeLastOrNull()
            Undefined -> {}
        }
    }

}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun ImageBox(
    modifier: Modifier = Modifier,
    imageUrl: String?,
    @DrawableRes emptyResource: Int,
    text: String,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        imageUrl?.takeIf { it.isNotEmpty() }?.let {
            GlideImage(
                modifier = Modifier.clickable { onClick() },
                model = it,
                contentDescription = text
            )
        } ?: run {
            Image(
                modifier = Modifier.clickable { onClick() },
                painter = painterResource(emptyResource),
                contentDescription = text
            )
        }
        Text(modifier = Modifier.padding(8.dp), text = text)
    }
}
