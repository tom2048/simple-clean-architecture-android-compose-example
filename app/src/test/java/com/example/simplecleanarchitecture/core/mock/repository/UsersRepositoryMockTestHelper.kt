package com.example.simplecleanarchitecture.core.mock.repository

import com.example.simplecleanarchitecture.core.mock.Mocks
import com.example.simplecleanarchitecture.data.repository.UsersRepository
import com.example.simplecleanarchitecture.data.repository.model.UserDetails
import kotlinx.coroutines.yield
import org.mockito.kotlin.KStubbing
import org.mockito.kotlin.any
import org.mockito.kotlin.doSuspendableAnswer
import org.mockito.kotlin.mock

interface UsersRepositoryMockTestHelper {

    fun createUsersRepositoryMock() = mock<UsersRepository>()

    fun KStubbing<UsersRepository>.setUsersRepositoryMock(
        getListResult: () -> List<UserDetails> = { Mocks.UserDetails.listDefault },
        getResult: (String) -> UserDetails = { Mocks.UserDetails.detailsDefault },
        insertResult: (UserDetails) -> String = { Mocks.UserDetails.detailsDefault.id!! },
        updateResult: (UserDetails) -> Unit = {},
        updatePasswordResult: (String, String) -> Unit = { _, _ -> },
        deleteResult: (UserDetails) -> Unit = {},

        ) {
        onBlocking { getList() }.doSuspendableAnswer {
            yield()
            getListResult()
        }
        onBlocking { get(any()) }.doSuspendableAnswer {
            yield()
            getResult(it.arguments.first().toString())
        }
        onBlocking { insert(any()) }.doSuspendableAnswer {
            yield()
            insertResult(it.arguments.first() as UserDetails)
        }
        onBlocking { update(any()) }.doSuspendableAnswer {
            yield()
            updateResult(it.arguments.first() as UserDetails)
        }
        onBlocking { updatePassword(any(), any()) }.doSuspendableAnswer {
            yield()
            updatePasswordResult(it.arguments[0].toString(), it.arguments[1].toString())
        }
        onBlocking { delete(any()) }.doSuspendableAnswer {
            yield()
            deleteResult(it.arguments.first() as UserDetails)
        }
    }

}