package com.example.simplecleanarchitecture.ui.screen.useredit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.simplecleanarchitecture.R
import com.example.simplecleanarchitecture.core.lib.AppDispatchers
import com.example.simplecleanarchitecture.core.lib.const.Patterns
import com.example.simplecleanarchitecture.core.lib.exception.ValidationException
import com.example.simplecleanarchitecture.core.lib.resources.AppResources
import com.example.simplecleanarchitecture.core.lib.viewmodel.BaseUiStateViewModel
import com.example.simplecleanarchitecture.domain.usecase.user.UserShowDetailsUseCase
import com.example.simplecleanarchitecture.domain.usecase.user.UserUpdateUseCase
import kotlinx.coroutines.launch
import com.example.simplecleanarchitecture.ui.screen.useredit.UserEditUiModel as Ui

class UserEditViewModel(
    private val userId: String?,
    private val savedState: SavedStateHandle,
    private val userShowDetailsUseCase: UserShowDetailsUseCase,
    private val userUpdateUseCase: UserUpdateUseCase,
    private val dispatchers: AppDispatchers,
    private val resources: AppResources
) : BaseUiStateViewModel<Ui.State, Ui.Effect>(Ui.State()), UserEditUiListener {

    init {
        updateState {
            savedState.get<Ui.State>(UI_STATE) ?: Ui.State(
                header = if (userId.isNullOrBlank()) {
                    resources.getStringResource(R.string.user_add_header)
                } else {
                    resources.getStringResource(R.string.user_edit_header)
                }
            )
        }
        loadDetails()
    }

    private fun loadDetails() {
        userId.takeIf { it.orEmpty().isNotEmpty() }?.let { userId ->
            viewModelScope.launch(dispatchers.io) {
                updateState { it.copy(preloader = true) }
                runCatching {
                    userShowDetailsUseCase.invoke(userId)
                }.onSuccess { loadedUser ->
                    val uiSavedState = savedState.get<Ui.State>(UI_STATE)
                    val user = uiSavedState?.let { state ->
                        loadedUser.copy(
                            nickname = state.nickname,
                            email = state.email,
                            description = state.description,
                            avatarUri = state.avatarUri,
                            idScanUri = state.idScanUri
                        )
                    } ?: loadedUser
                    updateState {
                        it.copy(
                            nickname = user.nickname,
                            email = user.email,
                            description = user.description,
                            nicknameValidationError = getNicknameValidationError(user.nickname),
                            emailValidationError = getEmailValidationError(user.email),
                            descriptionValidationError = getDescriptionValidationError(user.description),
                            avatarUri = user.avatarUri,
                            idScanUri = user.idScanUri,
                            newAvatarUri = uiSavedState?.newAvatarUri,
                            newIdScanUri = uiSavedState?.newIdScanUri,
                            isSubmitEnabled = getIsSubmitEnabled(
                                user.nickname,
                                user.email,
                                user.description
                            ),
                            preloader = false
                        )
                    }
                    savedState[UI_STATE] = uiState.value
                }.onFailure {
                    updateState { it.copy(preloader = false) }
                    sendEffect(Ui.Effect.CloseScreen())
                }
            }
        }
    }

    override fun setNickname(name: String) {
        updateState {
            it.copy(
                nickname = name,
                nicknameValidationError = getNicknameValidationError(name),
                isSubmitEnabled = getIsSubmitEnabled(
                    name,
                    uiState.value.email,
                    uiState.value.description
                )
            )
        }
        savedState[UI_STATE] = uiState.value
    }

    private fun getNicknameValidationError(nickname: String): String = when {
        nickname.length > 10 -> resources.getStringResource(R.string.nickname_validation_message)
        else -> ""
    }

    override fun setEmail(email: String) {
        updateState {
            it.copy(
                email = email,
                emailValidationError = getEmailValidationError(email),
                isSubmitEnabled = getIsSubmitEnabled(
                    uiState.value.nickname,
                    email,
                    uiState.value.description
                )
            )
        }
        savedState[UI_STATE] = uiState.value
    }

    private fun getEmailValidationError(email: String): String = when {
        !Patterns.EMAIL_ADDRESS.matcher(email)
            .matches() -> resources.getStringResource(R.string.email_validation_message)

        else -> ""
    }

    override fun setDescription(description: String) {
        updateState {
            it.copy(
                description = description,
                descriptionValidationError = getDescriptionValidationError(description),
                isSubmitEnabled = getIsSubmitEnabled(
                    uiState.value.nickname,
                    uiState.value.email,
                    description
                )
            )
        }
        savedState[UI_STATE] = uiState.value
    }

    private fun getDescriptionValidationError(description: String): String = when {
        !Patterns.ALPHANUMERIC.matcher(description)
            .matches() -> resources.getStringResource(R.string.description_validation_message)

        else -> ""
    }

    private fun getIsSubmitEnabled(nickname: String, email: String, description: String): Boolean =
        nickname.isNotEmpty() && getNicknameValidationError(nickname).isEmpty()
                && email.isNotEmpty() && getEmailValidationError(email).isEmpty()
                && description.isNotEmpty() && getDescriptionValidationError(description).isEmpty()

    override fun addAvatar(url: String) {
        updateState { it.copy(newAvatarUri = url) }
        savedState[UI_STATE] = uiState.value
    }

    override fun addIdScan(url: String) {
        updateState { it.copy(newIdScanUri = url) }
        savedState[UI_STATE] = uiState.value
    }

    override fun submit() {
        if (uiState.value.isSubmitEnabled) {
            viewModelScope.launch(dispatchers.io) {
                updateState { it.copy(preloader = true) }
                runCatching {
                    userUpdateUseCase
                        .invoke(
                            UserUpdateUseCase.Input(
                                id = userId,
                                nickname = uiState.value.nickname,
                                email = uiState.value.email,
                                description = uiState.value.description,
                                avatarUri = uiState.value.newAvatarUri,
                                idScanUri = uiState.value.newIdScanUri,
                            )
                        )
                }.onSuccess {
                    sendEffect(Ui.Effect.CloseScreen())
                }.onFailure { throwable ->
                    updateState { it.copy(preloader = false) }
                    if (throwable is ValidationException) {
                        sendEffect(Ui.Effect.ShowMessage(throwable.validationMessages.joinToString(separator = "\n") { item -> item.second }))
                    } else {
                        sendEffect(Ui.Effect.ShowMessage(resources.getStringResource(R.string.common_communication_error)))
                    }
                }
            }
        }
    }

    override fun cancel() {
        viewModelScope.launch {
            sendEffect(Ui.Effect.CloseScreen())
        }
    }


    companion object {
        private const val UI_STATE = "UI_STATE"
    }

}