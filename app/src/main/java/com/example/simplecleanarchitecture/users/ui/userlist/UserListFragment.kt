package com.example.simplecleanarchitecture.users.ui.userlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.simplecleanarchitecture.MainRouter
import com.example.simplecleanarchitecture.R
import com.example.simplecleanarchitecture.users.ui.userlist.UserListViewModel.UiEffect.Message
import com.example.simplecleanarchitecture.users.ui.userlist.UserListViewModel.UiEffect.Routing
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class UserListFragment : Fragment() {

    private val viewModel: UserListViewModel by viewModel()
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
        viewModel.loadUsers()
    }

    @Composable
    fun Layout() {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val uiEffect by viewModel.uiEffect.collectAsStateWithLifecycle(UserListViewModel.UiEffect.Undefined)
        val snackbarHostState = remember { SnackbarHostState() }
        val coroutineScope = rememberCoroutineScope()
        Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { scaffoldPadding ->
            if (!uiState.preloader) {
                Column(modifier = Modifier.padding(scaffoldPadding)) {
                    UserList(
                        modifier = Modifier.padding(8.dp),
                        list = uiState.userList,
                        onItemEdit = { userId -> viewModel.editUser(userId) },
                        onItemPasswordChange = { userId -> viewModel.changeUserPassword(userId) },
                        onItemDelete = { userId -> viewModel.deleteUser(userId) }
                    )
                    IconButton(modifier = Modifier
                        .align(Alignment.End)
                        .padding(12.dp), onClick = { viewModel.addNewUser() }) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = "Add new user")
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
                            text = getString(R.string.user_delete_confirmation_message),
                            Modifier.padding(8.dp)
                        )
                    },
                    onDismissRequest = { viewModel.cancelUserAction() },
                    confirmButton = {
                        TextButton(onClick = { viewModel.deleteUserConfirmed(id) }) {
                            Text(text = getString(R.string.dialog_button_ok))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.cancelUserAction() }) {
                            Text(text = getString(R.string.dialog_button_cancel))
                        }
                    }
                )
            }
            LaunchedEffect(uiEffect) {
                when (val effect: UserListViewModel.UiEffect = uiEffect) {
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

    }

    @Composable
    fun UserList(
        modifier: Modifier = Modifier,
        list: List<UserListItem> = emptyList(),
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
                Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = { onPasswordChange(id) }) {
                Icon(imageVector = Icons.Filled.Settings, contentDescription = "Change password")
            }
            IconButton(onClick = { onDelete(id) }) {
                Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete")
            }
        }
    }

    companion object {
        fun newInstance() = UserListFragment()
    }

}