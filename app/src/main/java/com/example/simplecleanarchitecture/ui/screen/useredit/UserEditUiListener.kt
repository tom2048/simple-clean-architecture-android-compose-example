package com.example.simplecleanarchitecture.ui.screen.useredit

interface UserEditUiListener {
    fun setNickname(name: String)
    fun setEmail(email: String)
    fun setDescription(description: String)
    fun addAvatar(url: String)
    fun addIdScan(url: String)
    fun cancel()
    fun submit()
}