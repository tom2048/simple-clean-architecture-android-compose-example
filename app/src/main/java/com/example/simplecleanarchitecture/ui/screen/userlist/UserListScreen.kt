package com.example.simplecleanarchitecture.ui.screen.userlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.simplecleanarchitecture.R
import com.example.simplecleanarchitecture.ui.common.LocalNavigationStack
import com.example.simplecleanarchitecture.ui.common.LocalSnackbarHostState
import com.example.simplecleanarchitecture.ui.common.NavigationRoute
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import com.example.simplecleanarchitecture.ui.screen.userlist.UserListUiModel as Ui

@Composable
fun UserListScreen() {
    val viewModel: UserListViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val uiEffect by viewModel.uiEffect.collectAsStateWithLifecycle(Ui.Effect.Undefined)

    UserEditLayout(uiState, viewModel)
    UiEffectHandler(uiEffect)
}

@Composable
fun UserEditLayout(uiState: Ui.State, uiListener: UserListUiListener) {
    if (!uiState.preloader) {
        Column {
            UserList(
                modifier = Modifier.padding(8.dp),
                list = uiState.userList,
                onItemEdit = { userId -> uiListener.editUser(userId) },
                onItemPasswordChange = { userId -> uiListener.changeUserPassword(userId) },
                onItemDelete = { userId -> uiListener.deleteUser(userId) }
            )
            IconButton(
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(12.dp), onClick = { uiListener.addNewUser() }) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = stringResource(R.string.user_add_button))
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
    uiState.userActionConfirmation?.let { id ->
        AlertDialog(
            text = {
                Text(
                    text = stringResource(R.string.user_delete_confirmation_message),
                    Modifier.padding(8.dp)
                )
            },
            onDismissRequest = { uiListener.cancelUserAction() },
            confirmButton = {
                TextButton(onClick = { uiListener.deleteUserConfirmed(id) }) {
                    Text(text = stringResource(R.string.dialog_button_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { uiListener.cancelUserAction() }) {
                    Text(text = stringResource(R.string.dialog_button_cancel))
                }
            }
        )
    }
}


@Composable
fun UserList(
    modifier: Modifier = Modifier,
    list: List<Ui.UserListItem> = emptyList(),
    onItemEdit: (String) -> Unit,
    onItemPasswordChange: (String) -> Unit,
    onItemDelete: (String) -> Unit
) {
    LazyColumn(modifier = modifier) {
        items(list) { user ->
            UserListItemLayout(
                id = user.user.id ?: "",
                name = user.user.nickname,
                onEdit = { onItemEdit(user.user.id ?: "") },
                onPasswordChange = { onItemPasswordChange(user.user.id ?: "") },
                onDelete = { onItemDelete(user.user.id ?: "") }
            )
        }
    }
}

@Composable
fun UserListItemLayout(
    id: String,
    name: String,
    onEdit: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    Row(modifier = Modifier.padding(4.dp)) {
        Text(
            text = name, modifier = Modifier
                .weight(1f)
                .padding(2.dp)
                .align(Alignment.CenterVertically)
        )
        IconButton(onClick = { onEdit.invoke(id) }) {
            Icon(imageVector = Icons.Filled.Edit, contentDescription = stringResource(R.string.user_edit_button))
        }
        IconButton(onClick = { onPasswordChange(id) }) {
            Icon(imageVector = Icons.Filled.Settings, contentDescription = stringResource(R.string.user_set_password_button))
        }
        IconButton(onClick = { onDelete(id) }) {
            Icon(imageVector = Icons.Filled.Delete, contentDescription = stringResource(R.string.user_delete_button))
        }
    }
}

@Composable
fun UiEffectHandler(uiEffect: Ui.Effect) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = LocalSnackbarHostState.current
    val backStack = LocalNavigationStack.current
    LaunchedEffect(uiEffect) {
        when (val effect: Ui.Effect = uiEffect) {
            is Ui.Effect.ShowMessage -> coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = effect.text,
                    duration = SnackbarDuration.Short
                )
            }

            is Ui.Effect.OpenUserEdit -> backStack.add(NavigationRoute.UserProfile(effect.id))
            is Ui.Effect.OpenUserPasswordChange -> backStack.add(NavigationRoute.UserPasswordChange(effect.id))
            Ui.Effect.Undefined -> {}
        }
    }
}

