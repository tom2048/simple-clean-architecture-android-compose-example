package com.example.simplecleanarchitecture.domain.usecase.user

import com.example.simplecleanarchitecture.data.repository.UsersRepository

fun interface UserDeleteUseCase {
    suspend operator fun invoke(id: String)
}

class UserDeleteUseCaseDefault(private val usersRepository: UsersRepository) : UserDeleteUseCase {

    // No unit tests for simple getters
    override suspend fun invoke(id: String): Unit = usersRepository.delete(id)

}