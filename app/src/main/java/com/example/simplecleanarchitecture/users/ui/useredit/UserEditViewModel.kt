package com.example.simplecleanarchitecture.users.ui.useredit

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.simplecleanarchitecture.R
import com.example.simplecleanarchitecture.core.lib.const.Patterns
import com.example.simplecleanarchitecture.core.lib.exception.ValidationException
import com.example.simplecleanarchitecture.core.lib.resources.AppResources
import com.example.simplecleanarchitecture.core.lib.viewmodel.BaseUiStateViewModel
import com.example.simplecleanarchitecture.users.ui.useredit.UserEditViewModel.UiEffect
import com.example.simplecleanarchitecture.users.ui.useredit.UserEditViewModel.UiEffect.Routing
import com.example.simplecleanarchitecture.users.ui.useredit.UserEditViewModel.UiState
import com.example.simplecleanarchitecture.users.usecase.user.UserShowDetailsUseCase
import com.example.simplecleanarchitecture.users.usecase.user.UserUpdateUseCase
import com.github.terrakok.cicerone.Back
import com.github.terrakok.cicerone.Command
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

class UserEditViewModel(
    private val userId: String,
    private val savedState: SavedStateHandle,
    private val userShowDetailsUseCase: UserShowDetailsUseCase,
    private val userUpdateUseCase: UserUpdateUseCase,
    private val appResources: AppResources
) : BaseUiStateViewModel<UiState, UiEffect>(UiState()) {

    init {
        _uiState.value = savedState.get<UiState>(UI_STATE) ?: UiState(
            header = if (userId.isNotEmpty()) {
                appResources.getStringResource(R.string.user_edit_header)
            } else {
                appResources.getStringResource(R.string.user_add_header)
            }
        )
    }

    fun loadDetails() {
        userId.takeIf { it.isNotEmpty() }?.let { userId ->
            viewModelScope.launch {
                userShowDetailsUseCase.invoke(userId)
                    .onStart {
                        _uiState.value = uiState.value.copy(preloader = true)
                    }
                    .catch {
                        _uiState.value = uiState.value.copy(preloader = false)
                        _uiEffect.tryEmit(Routing(Back()))
                    }
                    .collectLatest { loadedUser ->
                        val uiSavedState = savedState.get<UiState>(UI_STATE)
                        val user = uiSavedState?.let { state ->
                            loadedUser.copy(
                                nickname = state.nickname,
                                email = state.email,
                                description = state.description,
                                avatarUri = state.avatarUri,
                                idScanUri = state.idScanUri
                            )
                        } ?: loadedUser
                        _uiState.value = uiState.value.copy(
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
                        savedState[UI_STATE] = uiState.value
                    }
            }
        }
    }

    fun setNickname(nickname: String) {
        _uiState.value = uiState.value.copy(
            nickname = nickname,
            nicknameValidationError = getNicknameValidationError(nickname),
            isSubmitEnabled = getIsSubmitEnabled(
                nickname,
                uiState.value.email,
                uiState.value.description
            )
        )
        savedState[UI_STATE] = uiState.value
    }

    private fun getNicknameValidationError(nickname: String): String = when {
        nickname.length > 10 -> appResources.getStringResource(R.string.nickname_validation_message)
        else -> ""
    }

    fun setEmail(email: String) {
        _uiState.value = uiState.value.copy(
            email = email,
            emailValidationError = getEmailValidationError(email),
            isSubmitEnabled = getIsSubmitEnabled(
                uiState.value.nickname,
                email,
                uiState.value.description
            )
        )
        savedState[UI_STATE] = uiState.value
    }

    private fun getEmailValidationError(email: String): String = when {
        !Patterns.EMAIL_ADDRESS.matcher(email)
            .matches() -> appResources.getStringResource(R.string.email_validation_message)

        else -> ""
    }

    fun setDescription(description: String) {
        _uiState.value = uiState.value.copy(
            description = description,
            descriptionValidationError = getDescriptionValidationError(description),
            isSubmitEnabled = getIsSubmitEnabled(
                uiState.value.nickname,
                uiState.value.email,
                description
            )
        )
        savedState[UI_STATE] = uiState.value
    }

    private fun getDescriptionValidationError(description: String): String = when {
        !Patterns.ALPHANUMERIC.matcher(description)
            .matches() -> appResources.getStringResource(R.string.description_validation_message)

        else -> ""
    }

    private fun getIsSubmitEnabled(nickname: String, email: String, description: String): Boolean =
        nickname.isNotEmpty() && getNicknameValidationError(nickname).isEmpty()
                && email.isNotEmpty() && getEmailValidationError(email).isEmpty()
                && description.isNotEmpty() && getDescriptionValidationError(description).isEmpty()

    fun addAvatar(url: String) {
        _uiState.value = uiState.value.copy(newAvatarUri = url)
        savedState[UI_STATE] = uiState.value
    }

    fun addIdScan(url: String) {
        _uiState.value = uiState.value.copy(newIdScanUri = url)
        savedState[UI_STATE] = uiState.value
    }

    fun submit() {
        if (uiState.value.isSubmitEnabled) {
            viewModelScope.launch {
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
                    .onStart {
                        _uiState.value = uiState.value.copy(preloader = true)
                    }
                    .catch {
                        _uiState.value = uiState.value.copy(preloader = false)
                        if (it is ValidationException) {
                            _uiEffect.emit(UiEffect.Message(it.validationMessages.joinToString(separator = "\n") { item -> item.second }))
                        } else {
                            _uiEffect.emit(UiEffect.Message(appResources.getStringResource(R.string.common_communication_error)))
                        }
                    }
                    .collectLatest {
                        _uiEffect.emit(Routing(Back()))
                    }
            }
        }
    }

    fun cancel() {
        viewModelScope.launch {
            _uiEffect.emit(Routing(Back()))
        }
    }


    companion object {
        private const val UI_STATE = "UI_STATE"
    }

    @Parcelize
    data class UiState(
        val header: String = "",
        val nickname: String = "",
        val email: String = "",
        val description: String = "",
        val nicknameValidationError: String = "",
        val emailValidationError: String = "",
        val descriptionValidationError: String = "",
        val isSubmitEnabled: Boolean = false,
        val avatarUri: String? = "",
        val idScanUri: String? = "",
        val newAvatarUri: String? = "",
        val newIdScanUri: String? = "",
        val preloader: Boolean = false
    ) : Parcelable

    sealed class UiEffect {
        class Routing(val command: Command) : UiEffect()
        class Message(val text: String) : UiEffect()
        object Undefined : UiEffect()
    }

}