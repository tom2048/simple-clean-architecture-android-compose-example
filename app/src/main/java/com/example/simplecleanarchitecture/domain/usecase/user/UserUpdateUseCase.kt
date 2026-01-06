package com.example.simplecleanarchitecture.domain.usecase.user

import com.example.simplecleanarchitecture.R
import com.example.simplecleanarchitecture.core.lib.AppDispatchers
import com.example.simplecleanarchitecture.core.lib.const.Patterns
import com.example.simplecleanarchitecture.core.lib.exception.ValidationException
import com.example.simplecleanarchitecture.core.lib.resources.AppResources
import com.example.simplecleanarchitecture.data.repository.AssetsRepository
import com.example.simplecleanarchitecture.data.repository.StorageRepository
import com.example.simplecleanarchitecture.data.repository.UsersRepository
import com.example.simplecleanarchitecture.data.repository.model.Asset
import com.example.simplecleanarchitecture.data.repository.model.UserDetails
import com.example.simplecleanarchitecture.domain.const.Validation
import com.example.simplecleanarchitecture.domain.usecase.user.UserUpdateUseCase.Input
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext

fun interface UserUpdateUseCase {

    suspend operator fun invoke(input: Input)

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
    private val dispatchers: AppDispatchers,
    private val appResources: AppResources
) : UserUpdateUseCase {

    override suspend fun invoke(input: Input) = withContext(dispatchers.io) {
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
        if (isValid) {
            val userDetails = UserDetails(input.id, input.nickname, input.email, input.description)
            if (input.id.isNullOrEmpty()) {
                usersRepository.insert(userDetails)
                    .let { userId -> input.copy(id = userId) }
            } else {
                usersRepository.update(userDetails)
                    .let { input }
            }.let { currentInput ->
                if (!currentInput.avatarUri.isNullOrEmpty()) {
                    storageRepository.load(currentInput.avatarUri)
                        .let { fileContents ->
                            assetsRepository.saveAsset(
                                fileName = currentInput.avatarUri.split("/").last(),
                                contents = fileContents,
                                userId = currentInput.id!!,
                                type = Asset.Type.Avatar
                            )
                        }.let {
                            currentInput
                        }
                } else {
                    currentInput
                }
            }.let { currentInput ->
                if (!currentInput.idScanUri.isNullOrEmpty()) {
                    storageRepository.load(currentInput.idScanUri)
                        .let { fileContents ->
                            assetsRepository.saveAsset(
                                fileName = currentInput.idScanUri.split("/").last(),
                                contents = fileContents,
                                userId = currentInput.id!!,
                                type = Asset.Type.IdScan
                            )
                        }.let {
                            Unit
                        }
                } else {
                    Unit
                }
            }.let { Unit }
        } else {
            // TODO: Possible validation
            throw ValidationException(listOf())
        }
    }

}