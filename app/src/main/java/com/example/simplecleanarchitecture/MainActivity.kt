package com.example.simplecleanarchitecture

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity
import androidx.navigation3.runtime.NavEntry
import com.example.simplecleanarchitecture.ui.common.NavigationRoute
import com.example.simplecleanarchitecture.ui.common.SimpleNavigation
import com.example.simplecleanarchitecture.ui.common.SimpleScaffold
import com.example.simplecleanarchitecture.ui.common.SimpleTheme
import com.example.simplecleanarchitecture.ui.screen.useredit.UserEditScreen
import com.example.simplecleanarchitecture.ui.screen.userlist.UserListScreen
import com.example.simplecleanarchitecture.ui.screen.userpasswordchange.UserPasswordChangeScreen
import org.koin.androidx.viewmodel.ext.android.viewModel

@Suppress("unused")
class MainActivity : FragmentActivity() {

    private val viewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SimpleTheme {
                SimpleScaffold {
                    SimpleNavigation(listOf(NavigationRoute.UserList)) { key ->
                        when (key) {
                            NavigationRoute.UserList -> NavEntry(key) { UserListScreen() }
                            is NavigationRoute.UserPasswordChange -> NavEntry(key) { UserPasswordChangeScreen(key.id) }
                            is NavigationRoute.UserProfile -> NavEntry(key) { UserEditScreen(key.id) }
                            else -> NavEntry(key) { UserListScreen() }
                        }
                    }
                }
            }
        }
    }

}