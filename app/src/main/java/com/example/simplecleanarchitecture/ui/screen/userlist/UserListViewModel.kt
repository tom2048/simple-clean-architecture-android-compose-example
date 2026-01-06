package com.example.simplecleanarchitecture.ui.screen.userlist

import androidx.lifecycle.viewModelScope
import com.example.simplecleanarchitecture.R
import com.example.simplecleanarchitecture.core.lib.AppDispatchers
import com.example.simplecleanarchitecture.core.lib.resources.AppResources
import com.example.simplecleanarchitecture.core.lib.viewmodel.BaseUiStateViewModel
import com.example.simplecleanarchitecture.data.repository.model.UserDetails
import com.example.simplecleanarchitecture.domain.usecase.user.UserDeleteUseCase
import com.example.simplecleanarchitecture.domain.usecase.user.UserShowListUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import com.example.simplecleanarchitecture.ui.screen.userlist.UserListUiModel as Ui

@OptIn(ExperimentalCoroutinesApi::class)
class UserListViewModel(
    private val showListUseCase: UserShowListUseCase,
    private val userDeleteUseCase: UserDeleteUseCase,
    private val dispatchers: AppDispatchers,
    private val appResources: AppResources
) : BaseUiStateViewModel<Ui.State, Ui.Effect>(Ui.State()), UserListUiListener {

    init {
        loadUsers()
    }

    private fun loadUsers() {
        viewModelScope.launch(dispatchers.io) {
            updateState { it.copy(preloader = true) }
            runCatching {
                showListUseCase.invoke()
            }.onSuccess { result ->
                updateState { it.copy(preloader = false) }
                updateState { it.copy(userList = prepareUserItems(result)) }
            }.onFailure {
                updateState { it.copy(preloader = false) }
                sendEffect(Ui.Effect.ShowMessage(appResources.getStringResource(R.string.common_communication_error)))
            }
        }
    }

    override fun editUser(id: String) {
        viewModelScope.launch {
            sendEffect(Ui.Effect.OpenUserEdit(id))
        }
    }


    override fun addNewUser() {
        viewModelScope.launch {
            sendEffect(Ui.Effect.OpenUserEdit(null))
        }
    }

    override fun deleteUser(id: String) {
        updateState { uiState -> uiState.copy(userActionConfirmation = id) }
    }

    override fun cancelUserAction() {
        updateState { it.copy(userActionConfirmation = null) }
    }

    override fun changeUserPassword(id: String) {
        viewModelScope.launch {
            sendEffect(Ui.Effect.OpenUserPasswordChange(id))
        }
    }

    override fun deleteUserConfirmed(id: String) {
        viewModelScope.launch(dispatchers.io) {
            updateState { it.copy(preloader = true, userActionConfirmation = null) }
            runCatching {
                userDeleteUseCase.invoke(id)
                showListUseCase.invoke()
            }.onSuccess { result ->
                updateState {
                    it.copy(
                        preloader = false,
                        userList = prepareUserItems(result)
                    )
                }
                sendEffect(Ui.Effect.ShowMessage(appResources.getStringResource(R.string.user_delete_success_message)))
            }.onFailure {
                updateState { it.copy(preloader = false) }
                sendEffect(Ui.Effect.ShowMessage(appResources.getStringResource(R.string.common_communication_error)))
            }
        }
    }

    private fun prepareUserItems(users: List<UserDetails>): List<Ui.UserListItem> = users.map {
        Ui.UserListItem(it)
    }

}