@file:Suppress("SpellCheckingInspection")

package com.example.simplecleanarchitecture.ui.screen.userlist

import com.example.simplecleanarchitecture.data.repository.model.UserDetails

object UserListUiModel {

    data class State(
        val preloader: Boolean = false,
        val userList: List<UserListItem> = listOf(),
        val userActionConfirmation: String? = null
    )

    sealed class Effect {
        class OpenUserEdit(val id: String?) : Effect()
        class OpenUserPasswordChange(val id: String) : Effect()
        class ShowMessage(val text: String) : Effect()
        object Undefined : Effect()
    }

    data class UserListItem(
        val user: UserDetails
    )

}

