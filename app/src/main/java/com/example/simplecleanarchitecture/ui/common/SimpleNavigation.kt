package com.example.simplecleanarchitecture.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Composable
fun SimpleNavigation(
    initialBackStack: List<NavKey> = listOf(),
    entryProvider: (key: NavKey) -> NavEntry<NavKey>
) {
    val navBackStack = rememberNavBackStack(
        configuration = SavedStateConfiguration {
            serializersModule = SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(NavigationRoute.UserList::class, NavigationRoute.UserList.serializer())
                    subclass(NavigationRoute.UserProfile::class, NavigationRoute.UserProfile.serializer())
                    subclass(NavigationRoute.UserPasswordChange::class, NavigationRoute.UserPasswordChange.serializer())
                }
            }
        },
        *initialBackStack.toTypedArray()
    )
    CompositionLocalProvider(
        LocalNavigationStack provides navBackStack
    ) {
        NavDisplay(
            navBackStack,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            onBack = { navBackStack.removeLastOrNull() },
            entryProvider = entryProvider
        )
    }
}