package com.example.simplecleanarchitecture.core.mock.repository

import com.example.simplecleanarchitecture.core.mock.Mocks
import com.example.simplecleanarchitecture.data.repository.AssetsRepository
import com.example.simplecleanarchitecture.data.repository.model.Asset
import kotlinx.coroutines.yield
import org.mockito.kotlin.KStubbing
import org.mockito.kotlin.any
import org.mockito.kotlin.doSuspendableAnswer
import org.mockito.kotlin.mock

interface AssetsRepositoryMockTestHelper {

    fun createAssetsRepositoryMock() = mock<AssetsRepository>()

    fun KStubbing<AssetsRepository>.setAssetsRepositoryMock(
        getAssetResult: (String, Asset.Type) -> Asset = { _, _ -> Mocks.Asset.default },
        saveAssetResult: (String, ByteArray, String, Asset.Type) -> String = { _, _, _, _ -> Mocks.Asset.default.uri },
    ) {
        onBlocking { getAsset(any(), any()) }.doSuspendableAnswer {
            yield()
            getAssetResult(it.arguments[0].toString(), it.arguments[1] as Asset.Type)
        }
        onBlocking { saveAsset(any(), any(), any(), any()) }.doSuspendableAnswer {
            yield()
            saveAssetResult(
                it.arguments[0].toString(),
                it.arguments[1] as ByteArray,
                it.arguments[2].toString(),
                it.arguments[3] as Asset.Type
            )
        }
    }

}