package com.example.simplecleanarchitecture.core.mock.repository

import com.example.simplecleanarchitecture.core.mock.Mocks
import com.example.simplecleanarchitecture.data.repository.StorageRepository
import kotlinx.coroutines.yield
import org.mockito.kotlin.KStubbing
import org.mockito.kotlin.any
import org.mockito.kotlin.doSuspendableAnswer
import org.mockito.kotlin.mock

interface StorageRepositoryMockTestHelper {

    fun createStorageRepositoryMock() = mock<StorageRepository>()

    fun KStubbing<StorageRepository>.setStorageRepositoryMock(
        loadResult: (String) -> ByteArray = { Mocks.Storage.defaultAssetContents }
    ) {
        onBlocking { load(any()) }.doSuspendableAnswer {
            yield()
            loadResult(it.arguments.first().toString())
        }
    }

}