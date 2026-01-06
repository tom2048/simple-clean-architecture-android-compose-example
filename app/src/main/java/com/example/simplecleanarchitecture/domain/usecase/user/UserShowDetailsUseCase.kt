package com.example.simplecleanarchitecture.domain.usecase.user

import com.example.simplecleanarchitecture.core.lib.AppDispatchers
import com.example.simplecleanarchitecture.domain.model.User
import com.example.simplecleanarchitecture.data.repository.AssetsRepository
import com.example.simplecleanarchitecture.data.repository.UsersRepository
import com.example.simplecleanarchitecture.data.repository.model.Asset
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext

fun interface UserShowDetailsUseCase {
    suspend operator fun invoke(id: String): User
}

@OptIn(ExperimentalCoroutinesApi::class)
class UserShowDetailsUseCaseDefault(
    private val usersRepository: UsersRepository,
    private val assetsRepository: AssetsRepository,
    private val dispatchers: AppDispatchers
) : UserShowDetailsUseCase {

    override suspend fun invoke(id: String): User = withContext(dispatchers.io) {
        usersRepository.get(id)
            .let { user -> User(user.id, user.nickname, user.email, user.description) }
            .let { user ->
                runCatching {
                    assetsRepository.getAsset(user.id!!, Asset.Type.Avatar)
                        .let { asset -> user.copy(avatarUri = asset.uri) }
                }.getOrDefault(user)
            }
            .let { user ->
                runCatching {
                    assetsRepository.getAsset(user.id!!, Asset.Type.IdScan)
                        .let { asset -> user.copy(idScanUri = asset.uri) }
                }.getOrDefault(user)
            }
    }

}