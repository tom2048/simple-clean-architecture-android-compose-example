package com.example.simplecleanarchitecture.users.usecase.user

import com.example.simplecleanarchitecture.R
import com.example.simplecleanarchitecture.core.lib.const.Patterns
import com.example.simplecleanarchitecture.core.lib.exception.ValidationException
import com.example.simplecleanarchitecture.core.lib.resources.AppResources
import com.example.simplecleanarchitecture.core.repository.AssetsRepository
import com.example.simplecleanarchitecture.core.repository.StorageRepository
import com.example.simplecleanarchitecture.core.repository.UsersRepository
import com.example.simplecleanarchitecture.core.repository.model.Asset
import com.example.simplecleanarchitecture.core.repository.model.UserDetails
import com.example.simplecleanarchitecture.users.const.Validation
import com.example.simplecleanarchitecture.users.usecase.user.UserUpdateUseCase.Input
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

interface UserUpdateUseCase : (Input) -> Flow<Unit> {
    data class Input(
        val id: String?,
        val nickname: String,
        val email: String,
        val description: String,
        val avatarUri: String?,
        val idScanUri: String?
    )
}

@OptIn(ExperimentalCoroutinesApi::class)
class UserUpdateUseCaseDefault(
    private val storageRepository: StorageRepository,
    private val usersRepository: UsersRepository,
    private val assetsRepository: AssetsRepository,
    private val appResources: AppResources
) : UserUpdateUseCase {

    override fun invoke(input: Input): Flow<Unit> {
        // TODO: It would be possible to create validation in this place instead of in the viewModel, which could be more convenient when reusing use case or
        //  simply when performing client - server consistent validation. TBD
        // Regarding validation - there is some useful discussion about:
        // https://stackoverflow.com/questions/57603422/clean-architecture-where-to-put-input-validation-logic
        // https://groups.google.com/g/clean-code-discussion/c/latn4x6Zo7w/m/bFwtDI1XSA8J
        // val validationErrors = mutableListOf<Pair<String, String>>()
        // if (!Patterns.EMAIL_ADDRESS.matcher(user.email).matches()) {
        //     validationErrors.add(Pair(Validation.EMAIL_VALIDATION_KEY, appResources.getStringResource(R.string.email_validation_message)))
        // }
        // val isValid = validationErrors.isEmpty()
        val validationErrors = mutableListOf<Pair<String, String>>()
        if (!Patterns.EMAIL_ADDRESS.matcher(input.email).matches()) {
            validationErrors.add(Pair(Validation.EMAIL_VALIDATION_KEY, appResources.getStringResource(R.string.email_validation_message)))
        }
        val isValid = validationErrors.isEmpty()
        return if (isValid) {
            val userDetails = UserDetails(input.id, input.nickname, input.email, input.description)
            if (input.id.isNullOrEmpty()) {
                usersRepository.insert(userDetails)
                    .flatMapLatest { userId ->
                        flowOf(input.copy(id = userId))
                    }
            } else {
                usersRepository.update(userDetails)
                    .flatMapLatest {
                        flowOf(input)
                    }
            }.flatMapLatest { currentInput ->
                if (!currentInput.avatarUri.isNullOrEmpty()) {
                    storageRepository.load(currentInput.avatarUri)
                        .flatMapLatest { fileContents ->
                            assetsRepository.saveAsset(
                                fileName = currentInput.avatarUri.split("/").last(),
                                contents = fileContents,
                                userId = currentInput.id!!,
                                type = Asset.Type.Avatar
                            )
                        }.flatMapLatest {
                            flowOf(currentInput)
                        }
                } else {
                    flowOf(currentInput)
                }
            }.flatMapLatest { currentInput ->
                if (!currentInput.idScanUri.isNullOrEmpty()) {
                    storageRepository.load(currentInput.idScanUri)
                        .flatMapLatest { fileContents ->
                            assetsRepository.saveAsset(
                                fileName = currentInput.idScanUri.split("/").last(),
                                contents = fileContents,
                                userId = currentInput.id!!,
                                type = Asset.Type.IdScan
                            )
                        }.flatMapLatest {
                            flowOf(Unit)
                        }
                } else {
                    flowOf(Unit)
                }
            }.flatMapLatest { flowOf(Unit) }
        } else {
            // TODO: Possible validation
            flow { throw ValidationException(listOf()) }
        }
    }

}