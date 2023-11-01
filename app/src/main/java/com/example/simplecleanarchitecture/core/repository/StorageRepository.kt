package com.example.simplecleanarchitecture.core.repository

import android.app.Application
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

interface StorageRepository {
    fun load(uri: String): Flow<ByteArray>
}

class FileStorageRepository(private val application: Application) : StorageRepository {

    override fun load(uri: String): Flow<ByteArray> = flow {
        application.contentResolver.openInputStream(Uri.parse(uri))?.use {
            emit(it.readBytes())
        } ?: throw RuntimeException("File not found")
    }.flowOn(Dispatchers.IO)

}

