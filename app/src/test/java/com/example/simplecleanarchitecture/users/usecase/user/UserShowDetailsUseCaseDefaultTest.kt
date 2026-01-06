package com.example.simplecleanarchitecture.users.usecase.user

import com.example.simplecleanarchitecture.core.lib.TestException
import com.example.simplecleanarchitecture.core.lib.schedulers.MainCoroutineRule
import com.example.simplecleanarchitecture.core.lib.utils.DefaultTestHelper
import com.example.simplecleanarchitecture.core.lib.utils.TestHelper
import com.example.simplecleanarchitecture.core.mock.Mocks
import com.example.simplecleanarchitecture.core.mock.repository.AssetsRepositoryMockTestHelper
import com.example.simplecleanarchitecture.core.mock.repository.UsersRepositoryMockTestHelper
import com.example.simplecleanarchitecture.data.repository.AssetsRepository
import com.example.simplecleanarchitecture.data.repository.UsersRepository
import com.example.simplecleanarchitecture.data.repository.model.Asset
import com.example.simplecleanarchitecture.data.repository.model.UserDetails
import com.example.simplecleanarchitecture.domain.usecase.user.UserShowDetailsUseCaseDefault
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.get
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.reset
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify

@OptIn(ExperimentalCoroutinesApi::class)
class UserShowDetailsUseCaseDefaultTest : TestHelper by DefaultTestHelper(), KoinTest, UsersRepositoryMockTestHelper, AssetsRepositoryMockTestHelper {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        modules(
            module {
                single { createTestDispatchers() }
                single { createUsersRepositoryMock() }
                single { createAssetsRepositoryMock() }
                single { UserShowDetailsUseCaseDefault(get(), get(), get()) }
            }
        )
        setMocks()
    }

    private fun setMocks(
        getUserDetails: (String) -> UserDetails = { Mocks.UserDetails.detailsDefault },
        getAssetDetails: (String, Asset.Type) -> Asset = { _, _ -> Mocks.Asset.default }
    ) {
        get<UsersRepository>().stub {
            reset(mock)
            setUsersRepositoryMock(getResult = getUserDetails)
        }
        get<AssetsRepository>().stub {
            reset(mock)
            setAssetsRepositoryMock(getAssetResult = getAssetDetails)
        }
    }

    @Test
    fun `should merge user details and assets when user exists`() = runTest(StandardTestDispatcher()) {
        setMocks()
        val useCase = get<UserShowDetailsUseCaseDefault>()
        val expected = Mocks.User.detailsDefault

        val result = useCase(Mocks.UserDetails.detailsDefault.id!!)
        advanceUntilIdle()

        assert(result == expected)
        verify(get<UsersRepository>()).get(any())
    }

    @Test
    fun `should not load assets when user don't exists`() = runTest(StandardTestDispatcher()) {
        setMocks(getUserDetails = { throw TestException() })
        val useCase = get<UserShowDetailsUseCaseDefault>()

        runCatching { useCase.invoke(Mocks.UserDetails.detailsDefault.id!!) }

        verify(get<UsersRepository>()).get(any())
        verify(get<AssetsRepository>(), never()).getAsset(any(), any())
    }

    @Test
    fun `should not result with error when user exists and photo doesn't`() = runTest(StandardTestDispatcher()) {
        setMocks()
        val useCase = get<UserShowDetailsUseCaseDefault>()
        val expected = Mocks.User.detailsDefault

        val result = useCase.invoke(Mocks.UserDetails.detailsDefault.id!!)

        assert(result == expected)
    }

}