@file:Suppress("SpellCheckingInspection")

package com.example.simplecleanarchitecture.users.ui.useredit

import androidx.lifecycle.SavedStateHandle
import com.example.simplecleanarchitecture.core.lib.TestException
import com.example.simplecleanarchitecture.core.lib.exception.ValidationException
import com.example.simplecleanarchitecture.core.lib.extensions.last
import com.example.simplecleanarchitecture.core.lib.schedulers.MainCoroutineRule
import com.example.simplecleanarchitecture.core.lib.utils.DefaultTestHelper
import com.example.simplecleanarchitecture.core.lib.utils.TestHelper
import com.example.simplecleanarchitecture.core.lib.utils.mockObserver
import com.example.simplecleanarchitecture.core.lib.utils.observe
import com.example.simplecleanarchitecture.core.mock.Mocks
import com.example.simplecleanarchitecture.domain.model.User
import com.example.simplecleanarchitecture.ui.screen.useredit.UserEditUiModel.Effect.CloseScreen
import com.example.simplecleanarchitecture.ui.screen.useredit.UserEditUiModel.Effect.ShowMessage
import com.example.simplecleanarchitecture.ui.screen.useredit.UserEditViewModel
import com.example.simplecleanarchitecture.domain.usecase.user.UserShowDetailsUseCase
import com.example.simplecleanarchitecture.domain.usecase.user.UserUpdateUseCase
import com.example.simplecleanarchitecture.domain.usecase.user.UserUpdateUseCase.Input
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.module.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.get
import org.mockito.kotlin.any
import org.mockito.kotlin.argWhere
import org.mockito.kotlin.atLeast
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.doSuspendableAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.isA
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.not
import org.mockito.kotlin.only
import org.mockito.kotlin.reset
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(JUnitParamsRunner::class)
class UserEditViewModelTest : TestHelper by DefaultTestHelper(), KoinTest {

    @get:Rule
    var coroutineRule = MainCoroutineRule()

    @get:Rule
    var koinTestRule = KoinTestRule.create {
        modules(
            module {
                single { createTestDispatchers() }
                single { createTestAppResources() }
                single { mock<UserShowDetailsUseCase>() }
                single { mock<UserUpdateUseCase>() }
                viewModel { (userId: String, state: SavedStateHandle) -> UserEditViewModel(userId, state, get(), get(), get(), get()) }
            }
        )
        setMocks()
    }

    private fun setMocks(
        getUserDetails: (String) -> User = { Mocks.User.detailsDefault },
        updateUserDetails: (Input) -> Unit = {}
    ) {
        get<UserShowDetailsUseCase>().stub {
            reset(mock)
            onBlocking { invoke(any()) }.doSuspendableAnswer {
                yield()
                getUserDetails(it.arguments.first().toString())
            }
        }
        get<UserUpdateUseCase>().stub {
            reset(mock)
            onBlocking { invoke(any()) }.doSuspendableAnswer {
                yield()
                updateUserDetails(it.arguments.first() as Input)
            }
        }
    }

    private fun getViewModel(
        id: String = Mocks.User.detailsDefault.id.orEmpty(),
        state: SavedStateHandle = SavedStateHandle()
    ) =
        get<UserEditViewModel> {
            parametersOf(id, state)
        }


    @Test
    fun `should close the form when successfully submitted`() = runTest(StandardTestDispatcher()) {
        getViewModel().observe(this) { viewModel, _, effectObserver ->
            advanceUntilIdle()

            viewModel.submit()
            advanceUntilIdle()

            verify(effectObserver, last()).onEach(isA<CloseScreen>())
        }
    }

    @Test
    fun `should never close the form when there is an error during save`() = runTest(StandardTestDispatcher()) {
        setMocks(updateUserDetails = { throw TEST_VALIDATION_EXCEPTION })
        getViewModel().observe(this) { viewModel, _, effectObserver ->
            advanceUntilIdle()

            viewModel.submit()
            advanceUntilIdle()

            verify(effectObserver, never()).onEach(isA<CloseScreen>())
        }
    }

    @Test
    fun `should display an error message when there is an error during save`() = runTest(StandardTestDispatcher()) {
        setMocks(updateUserDetails = { throw TEST_VALIDATION_EXCEPTION })
        getViewModel().observe(this) { viewModel, _, effectObserver ->
            clearInvocations(effectObserver)
            advanceUntilIdle()

            viewModel.submit()
            advanceUntilIdle()

            verify(effectObserver, only()).onEach(isA<ShowMessage>())
            verify(effectObserver, only()).onEach(argWhere { (it as ShowMessage).text.isNotEmpty() })
        }
    }

    @Test
    fun `should invoke update use case when form is saved with correct user details set`() = runTest(StandardTestDispatcher()) {
        val viewModel = getViewModel()
        val updateUseCase = get<UserUpdateUseCase>()
        advanceUntilIdle()
        viewModel.setEmail(DEFAULT_USER_INPUT.email)
        viewModel.setNickname(DEFAULT_USER_INPUT.nickname)

        viewModel.submit()
        advanceUntilIdle()

        verify(updateUseCase, only()).invoke(any())
    }

    @Test
    fun `should show validation message when user sets an invalid nickname`() = runTest(StandardTestDispatcher()) {
        val viewModel = getViewModel()
        val observer = viewModel.uiState.map { it.nicknameValidationError }.mockObserver(this)
        advanceUntilIdle()

        viewModel.setNickname(INVALID_NICKNAME)
        advanceUntilIdle()

        verify(observer, last()).onEach(not(eq("")))
        observer.cancel()
    }

    @Test
    fun `should show validation message when user sets an invalid email`() = runTest(StandardTestDispatcher()) {
        val viewModel = getViewModel()
        val observer = viewModel.uiState.map { it.emailValidationError }.mockObserver(this)
        advanceUntilIdle()

        viewModel.setEmail(INVALID_EMAIL)
        advanceUntilIdle()

        verify(observer, last()).onEach(not(eq("")))
        observer.cancel()
    }

    @Test
    fun `should show validation message when user sets and invalid description`() = runTest(StandardTestDispatcher()) {
        val viewModel = getViewModel()
        val observer = viewModel.uiState.map { it.descriptionValidationError }.mockObserver(this)
        advanceUntilIdle()

        viewModel.setDescription(INVALID_DESCRIPTION)
        advanceUntilIdle()

        verify(observer, last()).onEach(not(eq("")))
        observer.cancel()
    }

    @Test
    fun `should enable submit button when user sets all fields to correct values`() = runTest(StandardTestDispatcher()) {
        val viewModel = getViewModel()
        val observer = viewModel.uiState.map { it.isSubmitEnabled }.mockObserver(this, true)
        advanceUntilIdle()

        viewModel.setNickname(VALID_NICKNAME)
        viewModel.setEmail(VALID_EMAIL)
        viewModel.setDescription(VALID_DESCRIPTION)
        advanceUntilIdle()

        verify(observer, atLeast(1)).onEach(eq(false))
        verify(observer, last()).onEach(eq(true))
        observer.cancel()
    }

    @Test
    fun `should set proper data when user data is properly loaded from the datasource`() = runTest(StandardTestDispatcher()) {
        getViewModel().observe(this) { stateObserver, _ ->
            advanceUntilIdle()
            verify(stateObserver, last()).onEach(argWhere {
                it.nickname == DEFAULT_USER.nickname && it.email == DEFAULT_USER.email && it.description == DEFAULT_USER.description
            })
        }
    }

    @Test
    @Parameters("true", "false")
    fun `should show and then hide the preloader when the data is loaded`(isDataCorrect: Boolean) = runTest(StandardTestDispatcher()) {
        setMocks(getUserDetails = { if (isDataCorrect) DEFAULT_USER else throw TestException() })
        val viewModel = getViewModel()
        val observer = viewModel.uiState.map { it.preloader }.mockObserver(this)
        advanceUntilIdle()

        inOrder(observer) {
            observer.onEach(eq(true))
            observer.onEach(eq(false))
        }
        observer.cancel()
    }

    @Test
    fun `should add an avatar image to the assets when user selects an avatar image`() = runTest(StandardTestDispatcher()) {
        val viewModel = getViewModel()
        val observer = viewModel.uiState.map { it.newAvatarUri }.mockObserver(this)
        val newAvatarUri = "file://avatar/uri.png"
        advanceUntilIdle()

        viewModel.addAvatar(newAvatarUri)
        advanceUntilIdle()

        verify(observer, last()).onEach(eq(newAvatarUri))
        observer.cancel()
    }

    @Test
    fun `should add an id scan image to the assets when user selects an id scan`() = runTest(StandardTestDispatcher()) {
        val viewModel = getViewModel()
        val observer = viewModel.uiState.map { it.newIdScanUri }.mockObserver(this)
        val newIdScanUri = "file://idScan/uri.png"
        advanceUntilIdle()

        viewModel.addIdScan(newIdScanUri)
        advanceUntilIdle()

        verify(observer, last()).onEach(eq(newIdScanUri))
        observer.cancel()
    }

    @Test
    fun `should show preloader and not hide it when the form is correctly saved`() = runTest(StandardTestDispatcher()) {
        val viewModel = getViewModel()
        val observer = viewModel.uiState.map { it.preloader }.mockObserver(this)
        advanceUntilIdle()

        viewModel.submit()
        advanceUntilIdle()

        verify(observer, last()).onEach(eq(true))
        observer.cancel()
    }

    @Test
    fun `should show and then hide preloader when there is an error while saving the form`() = runTest(StandardTestDispatcher()) {
        setMocks(updateUserDetails = { throw TestException() })
        val viewModel = getViewModel()
        val observer = viewModel.uiState.map { it.preloader }.mockObserver(this)
        advanceUntilIdle()

        viewModel.submit()
        advanceUntilIdle()

        inOrder(observer) {
            observer.onEach(eq(true))
            observer.onEach(eq(false))
        }
        observer.cancel()
    }

    @Test
    fun `should close the screen when user canceled the form`() = runTest(StandardTestDispatcher()) {
        getViewModel().observe(this) { viewModel, _, effectObserver ->
            advanceUntilIdle()

            viewModel.cancel()
            advanceUntilIdle()

            verify(effectObserver, last()).onEach(isA<CloseScreen>())
        }
    }


    companion object {
        private val TEST_VALIDATION_EXCEPTION = ValidationException(listOf(Pair("test", "test")))

        private val DEFAULT_USER = Mocks.User.detailsDefault
        private val DEFAULT_USER_INPUT = Input(
            DEFAULT_USER.id!!, DEFAULT_USER.nickname, DEFAULT_USER.email, DEFAULT_USER.description, null, null
        )
        private const val VALID_NICKNAME = "Nick1"
        private const val INVALID_NICKNAME = "TooLongNicknameOfTheUser"
        private const val VALID_EMAIL = "test@test.com"
        private const val INVALID_EMAIL = "test@test"
        private const val VALID_DESCRIPTION = "Test description"
        private const val INVALID_DESCRIPTION = "@test@"
    }
}