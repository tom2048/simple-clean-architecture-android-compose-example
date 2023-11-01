package com.example.simplecleanarchitecture.users.ui.useredit

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStarted
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.simplecleanarchitecture.MainRouter
import com.example.simplecleanarchitecture.R
import com.example.simplecleanarchitecture.users.ui.useredit.UserEditViewModel.UiEffect
import com.example.simplecleanarchitecture.users.ui.useredit.UserEditViewModel.UiEffect.Message
import com.example.simplecleanarchitecture.users.ui.useredit.UserEditViewModel.UiEffect.Routing
import com.example.simplecleanarchitecture.users.ui.useredit.UserEditViewModel.UiEffect.Undefined
import com.example.simplecleanarchitecture.users.ui.useredit.UserEditViewModel.UiState
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.stateViewModel
import org.koin.core.parameter.parametersOf


class UserEditFragment : Fragment() {

    private val viewModel: UserEditViewModel by stateViewModel(state = { Bundle.EMPTY }) {
        parametersOf(arguments?.getString(USER_ID_KEY) ?: "")
    }

    private val router: MainRouter by inject()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply { setContent { Layout() } }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadDetails()
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun Layout() {
        val uiState: UiState by viewModel.uiState.collectAsStateWithLifecycle()
        val uiEffect: UiEffect by viewModel.uiEffect.collectAsStateWithLifecycle(Undefined)
        val snackbarHostState = remember { SnackbarHostState() }
        val coroutineScope = rememberCoroutineScope()
        Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { scaffoldPadding ->
            if (!uiState.preloader) {
                Column(modifier = Modifier.padding(scaffoldPadding)) {
                    Text(
                        modifier = Modifier
                            .padding(8.dp)
                            .align(alignment = Alignment.CenterHorizontally),
                        text = stringResource(id = R.string.user_edit_header),
                        fontSize = 24.sp
                    )
                    TextField(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth(),
                        value = uiState.nickname,
                        onValueChange = { viewModel.setNickname(it) },
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
                        onValueChange = { viewModel.setEmail(it) },
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
                        onValueChange = { viewModel.setDescription(it) },
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
                            onClick = { loadAvatar() }
                        )
                        ImageBox(
                            modifier = Modifier.weight(1f),
                            imageUrl = uiState.newIdScanUri ?: uiState.idScanUri,
                            emptyResource = R.drawable.user_id_scan_ico,
                            text = "Id scan",
                            onClick = { loadIdScan() }
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            modifier = Modifier.padding(8.dp),
                            onClick = { viewModel.cancel() }
                        ) {
                            Text(text = stringResource(R.string.user_edit_cancel_button))
                        }
                        Button(
                            modifier = Modifier.padding(8.dp),
                            onClick = { viewModel.submit() },
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
        LaunchedEffect(uiEffect) {
            when (val effect: UiEffect = uiEffect) {
                is Message -> coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = effect.text,
                        duration = SnackbarDuration.Short
                    )
                }

                is Routing -> router.execute(effect.command)
                else -> {}
            }
        }
    }

    @OptIn(ExperimentalGlideComposeApi::class)
    @Composable
    private fun ImageBox(
        modifier: Modifier = Modifier,
        imageUrl: String?,
        //imageData: ByteData?,
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

    private fun loadAvatar() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*//*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/png", "image/jpg", "image/jpeg"))
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(
            Intent.createChooser(
                intent,
                getString(R.string.image_chooser_title)
            ), AVATAR_REQUEST_ID
        )
    }

    private fun loadIdScan() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*//*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/png", "image/jpg", "image/jpeg"))
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(
            Intent.createChooser(
                intent,
                getString(R.string.image_chooser_title)
            ), ID_SCAN_REQUEST_ID
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        lifecycleScope.launch {
            viewLifecycleOwner.whenStarted {
                if (requestCode == AVATAR_REQUEST_ID && resultCode == Activity.RESULT_OK && data != null) {
                    data.data?.let { url ->
                        viewModel.addAvatar(url.toString())
                    }
                } else if (requestCode == ID_SCAN_REQUEST_ID && resultCode == Activity.RESULT_OK && data != null) {
                    data.data?.let { url ->
                        viewModel.addIdScan(url.toString())
                    }
                }
            }
        }
    }

    companion object {

        private const val AVATAR_REQUEST_ID = 1001
        private const val ID_SCAN_REQUEST_ID = 1002

        private const val USER_ID_KEY = "USER_ID_KEY"

        fun newInstance(id: String? = null) = UserEditFragment().apply {
            arguments = Bundle().apply {
                putString(USER_ID_KEY, id)
            }
        }
    }
}