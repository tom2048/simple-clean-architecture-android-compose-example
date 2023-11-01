package com.example.simplecleanarchitecture.users.usecase.user

import com.example.simplecleanarchitecture.core.model.User
import com.example.simplecleanarchitecture.core.repository.AssetsRepository
import com.example.simplecleanarchitecture.core.repository.UsersRepository
import com.example.simplecleanarchitecture.core.repository.model.Asset
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

interface UserShowDetailsUseCase : (String) -> Flow<User>

@OptIn(ExperimentalCoroutinesApi::class)
class UserShowDetailsUseCaseDefault(
    private val usersRepository: UsersRepository,
    private val assetsRepository: AssetsRepository
) : UserShowDetailsUseCase {

    override fun invoke(id: String): Flow<User> = usersRepository
        .get(id)
        .map { user ->
            User(user.id, user.nickname, user.email, user.description)
        }
        .flatMapLatest { user ->
            assetsRepository.getAsset(user.id!!, Asset.Type.Avatar)
                .map { asset ->
                    user.copy(avatarUri = asset.uri)
                }
                .catch {
                    emit(user)
                }
        }
        .flatMapLatest { user ->
            assetsRepository.getAsset(user.id!!, Asset.Type.IdScan)
                .map { asset ->
                    user.copy(idScanUri = asset.uri)
                }
                .catch {
                    emit(user)
                }
        }

}