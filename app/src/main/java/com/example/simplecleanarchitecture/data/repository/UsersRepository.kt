package com.example.simplecleanarchitecture.data.repository

import com.example.simplecleanarchitecture.core.lib.AppDispatchers
import com.example.simplecleanarchitecture.data.repository.model.UserDetails
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.UUID

interface UsersRepository {
    suspend fun getList(): List<UserDetails>
    suspend fun get(id: String): UserDetails
    suspend fun insert(user: UserDetails): String
    suspend fun update(user: UserDetails)
    suspend fun updatePassword(userId: String, password: String)
    suspend fun delete(id: String)
}

/**
 * Implementation of UsersRepository
 * It's only a simple memory implementation for this example project, in real project it would be a REST or database repository.
 * Timers were added for real data source delay simulation.
 */
class UsersRepositoryMemory(val dispatchers: AppDispatchers) : UsersRepository {

    private val users = mutableMapOf<String, UserWithCredentials>().apply {
        // Test users
        for (i in 1..10) {
            put(
                "a312b3ee-84c2-11eb-8dcd-0242ac13000${i}",
                UserWithCredentials(
                    UserDetails(
                        "a312b3ee-84c2-11eb-8dcd-0242ac13000${i}",
                        "Nickname${i}",
                        "nickname${i}@test.com",
                        "Test description $i"
                    )
                )
            )
        }
    }

    override suspend fun getList(): List<UserDetails> = withContext(dispatchers.io) {
        getListInternal()
    }

    private suspend fun getListInternal() = withContext(dispatchers.io) {
        delay(TEST_DELAY_MILLIS)
        synchronized(users) {
            users.values.toList().map { it.user }
        }
    }

    override suspend fun get(id: String): UserDetails = withContext(dispatchers.io) {
        synchronized(users) {
            users[id]
        }?.let {
            delay(TEST_DELAY_MILLIS)
            it.user
        } ?: run {
            delay(TEST_DELAY_MILLIS)
            throw Exception("User not found")
        }
    }

    override suspend fun insert(user: UserDetails): String = withContext(dispatchers.io) {
        if (user.id.isNullOrEmpty()) {
            delay(TEST_DELAY_MILLIS)
            synchronized(users) {
                val id = UUID.randomUUID().toString()
                users[id] = UserWithCredentials(user.copy(id = id))
                id
            }
        } else {
            delay(TEST_DELAY_MILLIS)
            throw Exception("Invalid user object")
        }
    }

    override suspend fun update(user: UserDetails) = withContext(dispatchers.io) {
        user.id?.let { id ->
            synchronized(users) {
                users[id] = UserWithCredentials(user)
            }
            delay(TEST_DELAY_MILLIS)
        } ?: run {
            delay(TEST_DELAY_MILLIS)
            throw Exception("Invalid user object")
        }
    }

    override suspend fun updatePassword(userId: String, password: String) = withContext(dispatchers.io) {
        delay(TEST_DELAY_MILLIS)
        synchronized(users) {
            users[userId]?.let { user ->
                users[userId] = user.copy(password = password)
            } ?: run {
                throw Exception("Invalid user id")
            }
        }
    }

    override suspend fun delete(id: String) = withContext(dispatchers.io) {
        delay(TEST_DELAY_MILLIS)
        synchronized(users) {
            if (users.remove(id) == null) {
                throw Exception("Invalid user id")
            }
        }
    }

    companion object {
        private const val TEST_DELAY_MILLIS = 1000L
    }

    private data class UserWithCredentials(
        val user: UserDetails,
        val password: String? = null
    )
}

