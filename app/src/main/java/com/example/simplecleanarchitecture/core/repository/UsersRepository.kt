package com.example.simplecleanarchitecture.core.repository

import com.example.simplecleanarchitecture.core.repository.model.UserDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.util.UUID

interface UsersRepository {
    fun getList(): Flow<List<UserDetails>>
    fun get(id: String): Flow<UserDetails>
    fun insert(user: UserDetails): Flow<String>
    fun update(user: UserDetails): Flow<Unit>
    fun updatePassword(userId: String, password: String): Flow<Unit>
    fun delete(id: String): Flow<Unit>
}

/**
 * Implementation of UsersRepository
 * It's only a simple memory implementation for this example project, in real project it would be a REST or database repository.
 * Timers were added for real data source delay simulation.
 */
class UsersRepositoryMemory : UsersRepository {

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

    override fun getList(): Flow<List<UserDetails>> = flow {
        emit(getListInternal())
    }.flowOn(Dispatchers.IO)

    private suspend fun getListInternal() = withContext(Dispatchers.IO) {
        delay(TEST_DELAY_MILLIS)
        synchronized(users) {
            users.values.toList().map { it.user }
        }
    }

    override fun get(id: String): Flow<UserDetails> {
        return synchronized(users) {
            users[id]
        }?.let {
            flow {
                delay(TEST_DELAY_MILLIS)
                emit(it.user)
            }.flowOn(Dispatchers.IO)
        } ?: run {
            flow<UserDetails> {
                delay(TEST_DELAY_MILLIS)
                throw Exception("User not found")
            }.flowOn(Dispatchers.IO)
        }
    }

    override fun insert(user: UserDetails): Flow<String> {
        return if (user.id.isNullOrEmpty()) {
            flow {
                delay(TEST_DELAY_MILLIS)
                emit(synchronized(users) {
                    val id = UUID.randomUUID().toString()
                    users[id] = UserWithCredentials(user.copy(id = id))
                    id
                })
            }.flowOn(Dispatchers.IO)
        } else {
            flow<String> {
                delay(TEST_DELAY_MILLIS)
                throw Exception("Invalid user object")
            }.flowOn(Dispatchers.IO)
        }
    }

    override fun update(user: UserDetails): Flow<Unit> {
        return user.id?.let { id ->
            synchronized(users) {
                users[id] = UserWithCredentials(user)
            }
            flow {
                delay(TEST_DELAY_MILLIS)
                emit(Unit)
            }.flowOn(Dispatchers.IO)
        } ?: run {
            flow<Unit> {
                delay(TEST_DELAY_MILLIS)
                throw Exception("Invalid user object")
            }.flowOn(Dispatchers.IO)
        }
    }

    override fun updatePassword(userId: String, password: String) = flow {
        delay(TEST_DELAY_MILLIS)
        synchronized(users) {
            users[userId]?.let { user ->
                users[userId] = user.copy(password = password)
            } ?: run {
                throw Exception("Invalid user id")
            }
        }
        emit(Unit)
    }.flowOn(Dispatchers.IO)

    override fun delete(id: String) = flow {
        delay(TEST_DELAY_MILLIS)
        synchronized(users) {
            if (users.remove(id) == null) {
                throw Exception("Invalid user id")
            }
        }
        emit(Unit)
    }.flowOn(Dispatchers.IO)

    companion object {
        private const val TEST_DELAY_MILLIS = 1000L
    }

    private data class UserWithCredentials(
        val user: UserDetails,
        val password: String? = null
    )
}

