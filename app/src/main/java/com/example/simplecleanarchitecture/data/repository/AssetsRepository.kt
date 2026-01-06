package com.example.simplecleanarchitecture.data.repository

import android.content.Context
import com.example.simplecleanarchitecture.core.lib.AppDispatchers
import com.example.simplecleanarchitecture.data.repository.model.Asset
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/**
 * This is repository responsible for providing user assets like images, logos, documents, avatars, etc.
 * In real application the asset would be stored on server and accessed by url address.
 */
interface AssetsRepository {

    suspend fun getAsset(userId: String, type: Asset.Type): Asset
    suspend fun saveAsset(fileName: String, contents: ByteArray, userId: String, type: Asset.Type): String

}

/**
 * Implementation of asset repository. This is just an example, so for simplicity we have simple file based implementation, but in real life most likely
 * we will need some more advanced solution here (e.q. server based).
 */
class AssetsRepositoryStorage(private val context: Context, private val dispatchers: AppDispatchers) : AssetsRepository {

    private val assets = mutableListOf<Asset>()

    @Deprecated("Switched to URI")
    override suspend fun getAsset(userId: String, type: Asset.Type): Asset = withContext(dispatchers.io) {
        delay(TEST_DELAY_MILLIS)
        val asset = synchronized(assets) {
            assets.find { it.userId == userId && it.type == type }
        }
        asset ?: run {
            throw Exception("User not found")
        }
    }

    override suspend fun saveAsset(fileName: String, contents: ByteArray, userId: String, type: Asset.Type): String = withContext(dispatchers.io) {
        delay(TEST_DELAY_MILLIS)
        val assetId = UUID.randomUUID().toString()
        val outputFile = File(context.filesDir, assetId + "." + File(fileName).extension)
            .apply {
                outputStream().use { it.write(contents) }
            }
        val asset = Asset(assetId, type, userId, outputFile.toURI().toString())
        synchronized(assets) {
            assets.apply {
                filter { it.userId == userId && it.type == type }
                    .forEach { asset ->
                        runCatching {
                            val file = File(asset.uri)
                            if (asset.uri.contains(context.filesDir.toString()) && file.exists()) {
                                file.delete()
                            }
                        }
                    }
                removeAll { it.userId == userId && it.type == type }
                add(asset)
            }
        }
        asset.uri
    }

    companion object {
        private const val TEST_DELAY_MILLIS = 1L
    }

}