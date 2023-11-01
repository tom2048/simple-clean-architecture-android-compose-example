package com.example.simplecleanarchitecture.core.lib.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.yield

interface CoroutineTestHelper {

    fun testFlowOf(): Flow<Unit>

    fun <T> testFlowOf(vararg elements: T): Flow<T>

    fun <T> testFlowOf(throwable: Throwable): Flow<T>
}

class CoroutineDefaultTestHelper : CoroutineTestHelper {

    override fun testFlowOf(): Flow<Unit> = flow {
        yield()
        emit(Unit)
    }

    override fun <T> testFlowOf(vararg elements: T) = flow {
        yield()
        elements.forEach { emit(it) }
    }

    override fun <T> testFlowOf(throwable: Throwable) = flow<T> {
        yield()
        throw throwable
    }


}