package com.example.simplecleanarchitecture.ui.screen.userpasswordchange

interface UserPasswordChangeUiListener {
    fun setPassword(password: String)
    fun setPasswordConfirmed(passwordConfirmed: String)
    fun submit()
}