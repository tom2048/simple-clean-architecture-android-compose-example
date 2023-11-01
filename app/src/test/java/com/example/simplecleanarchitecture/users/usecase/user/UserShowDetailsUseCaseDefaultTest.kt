package com.example.simplecleanarchitecture.users.usecase.user

import com.example.simplecleanarchitecture.core.lib.TestException
import com.example.simplecleanarchitecture.core.lib.schedulers.MainCoroutineRule
import com.example.simplecleanarchitecture.core.lib.utils.CoroutineDefaultTestHelper
import com.example.simplecleanarchitecture.core.lib.utils.CoroutineTestHelper
import com.example.simplecleanarchitecture.core.lib.utils.mockObserver
import com.example.simplecleanarchitecture.core.model.User
import com.example.simplecleanarchitecture.core.repository.AssetsRepository
import com.example.simplecleanarchitecture.core.repository.UsersRepository
import com.example.simplecleanarchitecture.core.repository.model.UserDetails
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argWhere
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

@OptIn(ExperimentalCoroutinesApi::class)
class UserShowDetailsUseCaseDefaultTest : CoroutineTestHelper by CoroutineDefaultTestHelper() {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    private lateinit var userDetailsUseCase: UserShowDetailsUseCaseDefault

    private lateinit var usersRepository: UsersRepository
    private lateinit var assetsRepository: AssetsRepository


    @BeforeTest
    fun setUp() {
        usersRepository = mock()
        assetsRepository = mock()
        userDetailsUseCase = UserShowDetailsUseCaseDefault(usersRepository, assetsRepository)
    }

    @AfterTest
    fun cleanupDown() {
        // Cleanup if needed
    }


    @Test
    fun `invoke() merges user details and assets when user exists`() = runTest(UnconfinedTestDispatcher()) {
        val expected = User(
            DEFAULT_USER.id,
            DEFAULT_USER.nickname,
            DEFAULT_USER.email,
            DEFAULT_USER.description
        )
        whenever(usersRepository.get(any())).thenReturn(testFlowOf(DEFAULT_USER))

        val result = userDetailsUseCase(DEFAULT_USER.id!!).last()

        assert(result == expected)
        verify(usersRepository).get(any())
    }

    @Test
    fun `invoke() don't load assets when user don't exists`() = runTest(UnconfinedTestDispatcher()) {
        whenever(usersRepository.get(any())).thenReturn(testFlowOf(TestException()))

        userDetailsUseCase.invoke(DEFAULT_USER.id!!).mockObserver(this)
        verify(usersRepository).get(any())
    }

    @Test
    fun `invoke() doesn't result with error when user exists and photo doesn't`() = runTest(UnconfinedTestDispatcher()) {
        val expected = User(
            DEFAULT_USER.id,
            DEFAULT_USER.nickname,
            DEFAULT_USER.email,
            DEFAULT_USER.description,
            null
        )
        whenever(usersRepository.get(any())).thenReturn(testFlowOf(DEFAULT_USER))

        val observer = userDetailsUseCase.invoke(DEFAULT_USER.id!!).mockObserver(this)

        verify(observer).onEach(argWhere {
            it == expected
        })

        observer.cancel()
    }

    companion object {
        private val DEFAULT_USER = UserDetails(
            "a312b3ee-84c2-11eb-8dcd-0242ac130003",
            "Testnick",
            "test@test.com",
            "Test description"
        )
    }
}