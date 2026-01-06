package com.example.simplecleanarchitecture.data.repository

import android.annotation.SuppressLint
import android.app.Application
import android.net.Uri
import com.example.simplecleanarchitecture.core.lib.AppDispatchers
import kotlinx.coroutines.withContext

fun interface StorageRepository {
    suspend fun load(uri: String): ByteArray
}

class FileStorageRepository(private val application: Application, private val dispatchers: AppDispatchers) : StorageRepository {

    @SuppressLint("UseKtx")
    override suspend fun load(uri: String): ByteArray = withContext(dispatchers.io) {
        application.contentResolver.openInputStream(Uri.parse(uri))?.use {
            it.readBytes()
        } ?: throw RuntimeException("File not found")
    }

}

