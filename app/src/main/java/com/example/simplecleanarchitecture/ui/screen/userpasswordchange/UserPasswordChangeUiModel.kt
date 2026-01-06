@file:Suppress("SpellCheckingInspection")

package com.example.simplecleanarchitecture.ui.screen.userpasswordchange

object UserPasswordChangeUiModel {

    data class State(
        val password: String = "",
        val passwordConfirmed: String = "",
        val passwordValidation: String = "",
        val passwordConfirmedValidation: String = "",
        val preloader: Boolean = false,
        val isSubmitEnabled: Boolean = false
    )

    sealed class Effect {
        class CloseScreen : Effect()
        class ShowMessage(val text: String) : Effect()
        object Undefined : Effect()
    }
}
