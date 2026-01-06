package com.example.simplecleanarchitecture.ui.screen.userlist

interface UserListUiListener {
    fun editUser(id: String)
    fun addNewUser()
    fun deleteUser(id: String)
    fun cancelUserAction()
    fun changeUserPassword(id: String)
    fun deleteUserConfirmed(id: String)
}