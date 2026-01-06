package com.example.simplecleanarchitecture.users.usecase.user

import com.example.simplecleanarchitecture.core.lib.schedulers.MainCoroutineRule
import com.example.simplecleanarchitecture.core.lib.utils.DefaultTestHelper
import com.example.simplecleanarchitecture.core.lib.utils.TestHelper
import com.example.simplecleanarchitecture.core.mock.Mocks
import com.example.simplecleanarchitecture.core.mock.repository.AssetsRepositoryMockTestHelper
import com.example.simplecleanarchitecture.core.mock.repository.StorageRepositoryMockTestHelper
import com.example.simplecleanarchitecture.core.mock.repository.UsersRepositoryMockTestHelper
import com.example.simplecleanarchitecture.data.repository.AssetsRepository
import com.example.simplecleanarchitecture.data.repository.StorageRepository
import com.example.simplecleanarchitecture.data.repository.UsersRepository
import com.example.simplecleanarchitecture.data.repository.model.Asset
import com.example.simplecleanarchitecture.data.repository.model.UserDetails
import com.example.simplecleanarchitecture.domain.usecase.user.UserUpdateUseCase
import com.example.simplecleanarchitecture.domain.usecase.user.UserUpdateUseCaseDefault
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.get
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.reset
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

@OptIn(ExperimentalCoroutinesApi::class)
class UserUpdateUseCaseDefaultTest : TestHelper by DefaultTestHelper(),
    KoinTest,
    UsersRepositoryMockTestHelper,
    AssetsRepositoryMockTestHelper,
    StorageRepositoryMockTestHelper {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        modules(
            module {
                single { createTestDispatchers() }
                single { createTestAppResources() }
                single { createUsersRepositoryMock() }
                single { createAssetsRepositoryMock() }
                single { createStorageRepositoryMock() }
                single { UserUpdateUseCaseDefault(get(), get(), get(), get(), get()) }
            }
        )
        setMocks()
    }

    private fun setMocks(
        loadAssetContents: (String) -> ByteArray = { Mocks.Storage.defaultAssetContents },
        updateUserDetails: (UserDetails) -> Unit = {},
        saveAssetDetails: (String, ByteArray, String, Asset.Type) -> String = { _, _, _, _ -> Mocks.Asset.default.uri }
    ) {
        get<StorageRepository>().stub {
            reset(mock)
            setStorageRepositoryMock(loadResult = loadAssetContents)
        }
        get<UsersRepository>().stub {
            reset(mock)
            setUsersRepositoryMock(updateResult = updateUserDetails)
        }
        get<AssetsRepository>().stub {
            reset(mock)
            setAssetsRepositoryMock(saveAssetResult = saveAssetDetails)
        }
    }

    private fun getUseCase() = get<UserUpdateUseCaseDefault>()


    @Test
    fun `should execute insert when empty user id`() = runTest(StandardTestDispatcher()) {
        val useCase = getUseCase()

        useCase.invoke(NEW_USER_INPUT)

        verify(get<UsersRepository>(), times(1)).insert(any())
        verify(get<UsersRepository>(), never()).update(any())
    }

    @Test
    fun `should execute update when not empty user id`() = runTest(StandardTestDispatcher()) {
        val useCase = getUseCase()

        useCase.invoke(EXISTING_USER_INPUT)

        verify(get<UsersRepository>(), never()).insert(any())
        verify(get<UsersRepository>(), times(1)).update(any())
    }

    @Test
    fun `should save avatar photo when avatar uri not null and new user added`() = runTest(StandardTestDispatcher()) {
        val useCase = getUseCase()

        useCase.invoke(ADD_ATTACHMENTS_NEW_USER_INPUT)

        verify(get<AssetsRepository>(), times(1)).saveAsset(
            eq(ADD_ATTACHMENTS_NEW_USER_INPUT.avatarUri!!.split("/").last()),
            any(),
            any(),
            any()
        )
    }

    @Test
    fun `should save avatar photo when avatar uri not null and existing user edited`() = runTest(StandardTestDispatcher()) {
        val useCase = getUseCase()

        useCase.invoke(ADD_ATTACHMENTS_EXISTING_USER_INPUT)

        verify(get<AssetsRepository>(), times(1)).saveAsset(
            eq(ADD_ATTACHMENTS_EXISTING_USER_INPUT.avatarUri!!.split("/").last()),
            any(),
            any(),
            any()
        )
    }

    @Test
    fun `should save id scan photo when id scan uri not null and new user added`() = runTest(StandardTestDispatcher()) {
        val useCase = getUseCase()

        useCase.invoke(ADD_ATTACHMENTS_NEW_USER_INPUT)

        verify(get<AssetsRepository>(), times(1)).saveAsset(
            eq(ADD_ATTACHMENTS_NEW_USER_INPUT.idScanUri!!.split("/").last()),
            any(),
            any(),
            any()
        )
    }

    @Test
    fun `should save id scan photo when id scan uri not null and existing user edited`() = runTest(StandardTestDispatcher()) {
        val useCase = getUseCase()

        useCase.invoke(ADD_ATTACHMENTS_EXISTING_USER_INPUT)

        verify(get<AssetsRepository>()).saveAsset(
            eq(ADD_ATTACHMENTS_EXISTING_USER_INPUT.idScanUri!!.split("/").last()),
            any(),
            any(),
            any()
        )
    }


    companion object {
        private val DEFAULT_USER = Mocks.User.detailsDefault

        private val EXISTING_USER_INPUT = UserUpdateUseCase.Input(
            id = DEFAULT_USER.id,
            nickname = DEFAULT_USER.nickname,
            email = DEFAULT_USER.email,
            description = DEFAULT_USER.description,
            avatarUri = DEFAULT_USER.avatarUri,
            idScanUri = DEFAULT_USER.idScanUri
        )

        private val NEW_USER_INPUT = EXISTING_USER_INPUT.copy(id = null)

        private val ADD_ATTACHMENTS_EXISTING_USER_INPUT = EXISTING_USER_INPUT.copy(
            avatarUri = "file://uri/avatar.png",
            idScanUri = "file://uri/idScan.png"
        )

        private val ADD_ATTACHMENTS_NEW_USER_INPUT = NEW_USER_INPUT.copy(
            avatarUri = "file://uri/avatar.png",
            idScanUri = "file://uri/idScan.png"
        )

    }
}