package com.example.simplecleanarchitecture.ui.screen.userpasswordchange

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.simplecleanarchitecture.R
import com.example.simplecleanarchitecture.core.lib.const.Patterns
import com.example.simplecleanarchitecture.core.lib.resources.AppResources
import com.example.simplecleanarchitecture.core.lib.viewmodel.BaseUiStateViewModel
import com.example.simplecleanarchitecture.domain.usecase.user.UserPasswordUpdateUseCase
import kotlinx.coroutines.launch
import com.example.simplecleanarchitecture.ui.screen.userpasswordchange.UserPasswordChangeUiModel as Ui

class UserPasswordChangeViewModel(
    private val userId: String,
    private val savedState: SavedStateHandle,
    private val passwordUpdateUseCase: UserPasswordUpdateUseCase,
    private val appResources: AppResources
) : BaseUiStateViewModel<Ui.State, Ui.Effect>(Ui.State()), UserPasswordChangeUiListener {

    init {
        setPassword(savedState[STATE_PASSWORD] ?: "")
        setPasswordConfirmed(savedState[STATE_PASSWORD_CONFIRMED] ?: "")
    }

    override fun setPassword(password: String) {
        updateState {
            it.copy(
                password = password,
                passwordValidation = validatePassword(password),
                passwordConfirmedValidation = validatePasswordConfirmed(
                    password,
                    uiState.value.passwordConfirmed
                ),
                isSubmitEnabled = isSubmitEnabled(password, uiState.value.passwordConfirmed)
            )
        }
        savedState[STATE_PASSWORD] = uiState.value.password
    }

    override fun setPasswordConfirmed(passwordConfirmed: String) {
        updateState {
            it.copy(
                passwordConfirmed = passwordConfirmed,
                passwordValidation = validatePassword(uiState.value.password),
                passwordConfirmedValidation = validatePasswordConfirmed(
                    uiState.value.password,
                    passwordConfirmed
                ),
                isSubmitEnabled = isSubmitEnabled(uiState.value.password, passwordConfirmed)
            )
        }
        savedState[STATE_PASSWORD_CONFIRMED] = uiState.value.passwordConfirmed
    }

    override fun submit() {
        userId.takeIf { it.isNotEmpty() }?.let { userId ->
            viewModelScope.launch {
                updateState { uiState.value.copy(preloader = true) }
                runCatching {
                    passwordUpdateUseCase(userId, uiState.value.password)
                }.onSuccess {
                    sendEffect(Ui.Effect.CloseScreen())
                }.onFailure {
                    updateState { uiState.value.copy(preloader = false) }
                    sendEffect(Ui.Effect.ShowMessage(appResources.getStringResource(R.string.common_communication_error)))
                }
            }
        } ?: run {
            viewModelScope.launch {
                sendEffect(Ui.Effect.ShowMessage(appResources.getStringResource(R.string.common_communication_error)))
            }
        }
    }

    private fun validatePassword(password: String) =
        if (password.isNotEmpty() && !Patterns.PASSWORD.matcher(password).matches()) {
            appResources.getStringResource(R.string.password_validation_message)
        } else {
            ""
        }

    private fun validatePasswordConfirmed(password: String, passwordConfirmed: String) =
        if (password.isNotEmpty() && passwordConfirmed.isNotEmpty() && passwordConfirmed != password
        ) {
            appResources.getStringResource(R.string.password_confirmation_validation_message)
        } else {
            ""
        }

    private fun isSubmitEnabled(password: String, passwordConfirmed: String) =
        validatePassword(password).isEmpty()
                && validatePasswordConfirmed(password, passwordConfirmed).isEmpty()
                && password.isNotEmpty()
                && passwordConfirmed.isNotEmpty()

    companion object {
        const val STATE_PASSWORD = "STATE_PASSWORD"
        const val STATE_PASSWORD_CONFIRMED = "STATE_PASSWORD_CONFIRMED"
    }

}