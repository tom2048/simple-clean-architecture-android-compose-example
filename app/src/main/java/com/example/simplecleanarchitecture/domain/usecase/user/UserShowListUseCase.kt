package com.example.simplecleanarchitecture.domain.usecase.user

import com.example.simplecleanarchitecture.data.repository.UsersRepository
import com.example.simplecleanarchitecture.data.repository.model.UserDetails

fun interface UserShowListUseCase {
    suspend operator fun invoke(): List<UserDetails>
}

class UserShowListUseCaseDefault(private val usersRepository: UsersRepository) :
    UserShowListUseCase {

    // No unit tests for simple getters
    override suspend fun invoke(): List<UserDetails> = usersRepository
        .getList()

}