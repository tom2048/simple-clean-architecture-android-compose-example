@file:Suppress("SpellCheckingInspection")

package com.example.simplecleanarchitecture.ui.screen.useredit

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

object UserEditUiModel {

    @Parcelize
    data class State(
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

    sealed class Effect {
        class CloseScreen : Effect()
        class ShowMessage(val text: String) : Effect()
        object Undefined : Effect()
    }

}
