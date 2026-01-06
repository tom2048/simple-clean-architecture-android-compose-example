package com.example.simplecleanarchitecture.ui.common

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface NavigationRoute : NavKey {

    @Serializable
    data object UserList : NavigationRoute, NavKey

    @Serializable
    data class UserProfile(val id: String?) : NavigationRoute, NavKey

    @Serializable
    data class UserPasswordChange(val id: String) : NavigationRoute, NavKey

}