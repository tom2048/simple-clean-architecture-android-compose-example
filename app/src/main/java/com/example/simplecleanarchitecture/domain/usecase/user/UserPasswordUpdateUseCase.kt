package com.example.simplecleanarchitecture.domain.usecase.user

import com.example.simplecleanarchitecture.data.repository.UsersRepository

fun interface UserPasswordUpdateUseCase {
    suspend operator fun invoke(userId: String, password: String)
}

class UserPasswordUpdateUseCaseDefault(private val usersRepository: UsersRepository) : UserPasswordUpdateUseCase {

    // No unit tests for simple getters
    override suspend fun invoke(userId: String, password: String): Unit =
        usersRepository.updatePassword(userId, password)

}