package com.example.simplecleanarchitecture.core.lib.utils

import androidx.annotation.IntegerRes
import androidx.annotation.StringRes
import com.example.simplecleanarchitecture.core.lib.AppDispatchers
import com.example.simplecleanarchitecture.core.lib.resources.AppResources
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.yield

interface TestHelper {

    fun testFlowOf(): Flow<Unit>

    fun <T> testFlowOf(vararg elements: T): Flow<T>

    fun <T> testFlowOf(throwable: Throwable): Flow<T>

    fun getResource(@StringRes id: Int, vararg params: Any): String

    fun createTestAppResources(): AppResources

    fun createTestDispatchers(): AppDispatchers

}

class DefaultTestHelper : TestHelper {

    override fun testFlowOf(): Flow<Unit> = flow {
        yield()
        emit(Unit)
    }.flowOn(Dispatchers.Main)

    override fun <T> testFlowOf(vararg elements: T) = flow {
        yield()
        elements.forEach { emit(it) }
    }.flowOn(Dispatchers.Main)

    override fun <T> testFlowOf(throwable: Throwable) = flow<T> {
        yield()
        throw throwable
    }.flowOn(Dispatchers.Main)

    override fun getResource(@StringRes id: Int, vararg params: Any): String =
        "text:$id" + params.map { it.toString() }.toList().joinToString("-").takeIf { it.isNotBlank() }?.let { ":$it" }.orEmpty()

    override fun createTestAppResources(): AppResources =
        object : AppResources {
            override fun getStringResource(@StringRes id: Int, vararg params: Any): String = getResource(id, *params)

            override fun getIntResource(@IntegerRes id: Int): Int = id
        }

    override fun createTestDispatchers(): AppDispatchers =
        AppDispatchers(
            main = Dispatchers.Main,
            io = Dispatchers.Main,
            default = Dispatchers.Default,
            unconfined = Dispatchers.Unconfined
        )
}