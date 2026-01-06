@file:Suppress("SpellCheckingInspection")

package com.example.simplecleanarchitecture.users.ui.passwordchange

import androidx.lifecycle.SavedStateHandle
import com.example.simplecleanarchitecture.core.lib.TestException
import com.example.simplecleanarchitecture.core.lib.extensions.last
import com.example.simplecleanarchitecture.core.lib.schedulers.MainCoroutineRule
import com.example.simplecleanarchitecture.core.lib.utils.DefaultTestHelper
import com.example.simplecleanarchitecture.core.lib.utils.TestHelper
import com.example.simplecleanarchitecture.core.lib.utils.mockObserver
import com.example.simplecleanarchitecture.core.lib.utils.observe
import com.example.simplecleanarchitecture.core.mock.Mocks
import com.example.simplecleanarchitecture.ui.screen.userpasswordchange.UserPasswordChangeUiModel.Effect.CloseScreen
import com.example.simplecleanarchitecture.ui.screen.userpasswordchange.UserPasswordChangeUiModel.Effect.ShowMessage
import com.example.simplecleanarchitecture.ui.screen.userpasswordchange.UserPasswordChangeViewModel
import com.example.simplecleanarchitecture.domain.usecase.user.UserPasswordUpdateUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.junit.Rule
import org.junit.Test
import org.koin.core.module.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.get
import org.mockito.kotlin.any
import org.mockito.kotlin.argWhere
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
class UserPasswordChangeViewModelTest : TestHelper by DefaultTestHelper(), KoinTest {

    @get:Rule
    var coroutineRule = MainCoroutineRule()

    @get:Rule
    var koinTestRule = KoinTestRule.create {
        modules(
            module {
                single { createTestDispatchers() }
                single { createTestAppResources() }
                single { mock<UserPasswordUpdateUseCase>() }
                viewModel { (userId: String, state: SavedStateHandle) -> UserPasswordChangeViewModel(userId, state, get(), get()) }
            }
        )
        setMocks()
    }

    private fun setMocks(
        updatePassword: (String, String) -> Unit = { _, _ -> }
    ) {
        get<UserPasswordUpdateUseCase>().stub {
            reset(mock)
            onBlocking { invoke(any(), any()) }.doSuspendableAnswer {
                yield()
                updatePassword(it.arguments[0].toString(), it.arguments[1].toString())
            }
        }
    }

    private fun getViewModel(
        id: String = DEFAULT_USER_ID,
        state: SavedStateHandle = SavedStateHandle()
    ) =
        get<UserPasswordChangeViewModel> {
            parametersOf(id, state)
        }


    @Test
    fun `should properly set password when initialized with saved state`() = runTest(StandardTestDispatcher()) {
        val viewModel = getViewModel(state = SAVED_STATE)
        val observer = viewModel.uiState.mockObserver(this)
        advanceUntilIdle()

        verify(observer, last()).onEach(argWhere {
            it.password == SAVED_STATE.get<String>(UserPasswordChangeViewModel.STATE_PASSWORD) &&
                    it.passwordConfirmed == SAVED_STATE.get<String>(UserPasswordChangeViewModel.STATE_PASSWORD_CONFIRMED)
        })
        observer.cancel()
    }

    @Test
    fun `should hide validation message when a valid password is set`() = runTest(StandardTestDispatcher()) {
        getViewModel().observe(this) { viewModel, stateObserver, _ ->
            clearInvocations(stateObserver)

            viewModel.setPassword(VALID_PASSWORD)
            advanceUntilIdle()

            verify(stateObserver, only()).onEach(argWhere { it.password == VALID_PASSWORD && it.passwordValidation == "" })
        }
    }

    @Test
    fun `should show validation message when an invalid password is set`() = runTest(StandardTestDispatcher()) {
        getViewModel().observe(this) { viewModel, stateObserver, _ ->
            clearInvocations(stateObserver)

            viewModel.setPassword(INVALID_PASSWORD)
            advanceUntilIdle()

            verify(stateObserver, only()).onEach(argWhere { it.password == INVALID_PASSWORD && it.passwordValidation.isNotEmpty() })
        }
    }

    @Test
    fun `should show validation message when password is set to different value than confirm password`() = runTest(StandardTestDispatcher()) {
        val viewModel = getViewModel().apply {
            setPasswordConfirmed(VALID_PASSWORD)
        }
        advanceUntilIdle()
        val observer = viewModel.uiState.map { it.passwordConfirmedValidation }.mockObserver(this, true)

        viewModel.setPassword(VALID_PASSWORD2)
        advanceUntilIdle()

        verify(observer, only()).onEach(not(eq("")))
        observer.cancel()
    }

    @Test
    fun `should hide validation message when password is set to the same value as confirm password`() = runTest(StandardTestDispatcher()) {
        val viewModel = getViewModel().apply {
            setPasswordConfirmed(VALID_PASSWORD)
        }
        advanceUntilIdle()
        val observer = viewModel.uiState.map { it.passwordConfirmedValidation }.mockObserver(this, true)

        viewModel.setPassword(VALID_PASSWORD)
        advanceUntilIdle()

        verify(observer, only()).onEach(eq(""))
        observer.cancel()
    }

    @Test
    fun `should show validation message when confirm password is set to different value than password`() = runTest(StandardTestDispatcher()) {
        val viewModel = getViewModel().apply {
            setPassword(VALID_PASSWORD)
        }
        advanceUntilIdle()
        val observer = viewModel.uiState.map { it.passwordConfirmedValidation }.mockObserver(this, true)

        viewModel.setPasswordConfirmed(VALID_PASSWORD2)
        advanceUntilIdle()

        verify(observer, only()).onEach(not(eq("")))
        observer.cancel()
    }

    @Test
    fun `should hide validation message when confirm password is set to the same value as password`() = runTest(StandardTestDispatcher()) {
        val viewModel = getViewModel().apply {
            setPassword(VALID_PASSWORD)
        }
        advanceUntilIdle()
        val observer = viewModel.uiState.map { it.passwordConfirmedValidation }.mockObserver(this, true)

        viewModel.setPasswordConfirmed(VALID_PASSWORD)
        advanceUntilIdle()

        verify(observer, only()).onEach(eq(""))
        observer.cancel()
    }

    @Test
    fun `should enable submit button when password is set to the same value as confirm password`() = runTest(StandardTestDispatcher()) {
        val viewModel = getViewModel().apply {
            setPasswordConfirmed(VALID_PASSWORD)
        }
        advanceUntilIdle()
        val observer = viewModel.uiState.map { it.isSubmitEnabled }.mockObserver(this, true)

        viewModel.setPassword(VALID_PASSWORD)
        advanceUntilIdle()

        verify(observer, only()).onEach(eq(true))
        observer.cancel()
    }

    @Test
    fun `should disable submit button when password is set to an invalid value`() = runTest(StandardTestDispatcher()) {
        val viewModel = getViewModel().apply {
            setPassword(VALID_PASSWORD)
            setPasswordConfirmed(VALID_PASSWORD)
        }
        advanceUntilIdle()
        val observer = viewModel.uiState.map { it.isSubmitEnabled }.mockObserver(this)

        viewModel.setPassword(INVALID_PASSWORD)
        advanceUntilIdle()

        verify(observer).onEach(eq(false))
        observer.cancel()
    }

    @Test
    fun `should enable submit button when confirm password is set to the same value as password`() = runTest(StandardTestDispatcher()) {
        val viewModel = getViewModel().apply {
            setPassword(VALID_PASSWORD)
        }
        advanceUntilIdle()
        val observer = viewModel.uiState.map { it.isSubmitEnabled }.mockObserver(this)

        viewModel.setPasswordConfirmed(VALID_PASSWORD)
        advanceUntilIdle()

        verify(observer).onEach(eq(true))
        observer.cancel()
    }

    @Test
    fun `should disable submit button when confirm password is set to an invalid value`() = runTest(StandardTestDispatcher()) {
        val viewModel = getViewModel().apply {
            setPassword(VALID_PASSWORD)
            setPasswordConfirmed(VALID_PASSWORD)
        }
        advanceUntilIdle()
        val observer = viewModel.uiState.mockObserver(this)

        viewModel.setPasswordConfirmed(INVALID_PASSWORD)
        advanceUntilIdle()

        verify(observer).onEach(argWhere { !it.isSubmitEnabled })
        observer.cancel()
    }

    @Test
    fun `should show and then not hide the preloader when password is successfully changed on submit`() = runTest(StandardTestDispatcher()) {
        val viewModel = getViewModel()
        advanceUntilIdle()
        val observer = viewModel.uiState.map { it.preloader }.mockObserver(this, true)

        viewModel.submit()
        advanceUntilIdle()

        verify(observer).onEach(eq(true))
        observer.cancel()
    }

    @Test
    // Using runBlockingTest due to this issue: https://github.com/Kotlin/kotlinx.coroutines/issues/3367
    fun `should show and then hide the preloader when there were errors during password change on submit`() = runTest(StandardTestDispatcher()) {
        setMocks(updatePassword = { _, _ -> throw TestException() })
        val viewModel = getViewModel()
        advanceUntilIdle()
        val observer = viewModel.uiState.map { it.preloader }.mockObserver(this, true)

        viewModel.submit()
        advanceUntilIdle()

        inOrder(observer) {
            verify(observer).onEach(eq(true))
            verify(observer).onEach(eq(false))
        }
        observer.cancel()
    }

    @Test
    fun `should execute password update when password is valid on submit`() = runTest(StandardTestDispatcher()) {
        val viewModel = getViewModel()
        viewModel.setPassword(VALID_PASSWORD)
        viewModel.setPasswordConfirmed(VALID_PASSWORD)

        viewModel.submit()
        advanceUntilIdle()

        verify(get<UserPasswordUpdateUseCase>()).invoke(DEFAULT_USER_ID, VALID_PASSWORD)
    }

    @Test
    fun `should show error message when there were some errors on submit`() = runTest(StandardTestDispatcher()) {
        setMocks(updatePassword = { _, _ -> throw TestException() })
        val viewModel = getViewModel().apply {
            setPassword(INVALID_PASSWORD)
            setPasswordConfirmed(INVALID_PASSWORD)
        }
        advanceUntilIdle()
        val observer = viewModel.uiEffect.mockObserver(this)

        viewModel.submit()
        advanceUntilIdle()

        verify(observer).onEach(argWhere { it is ShowMessage && it.text.isNotEmpty() })
        observer.cancel()
    }

    @Test
    fun `should close the screen when there were no errors on submit`() = runTest(StandardTestDispatcher()) {
        val viewModel = getViewModel().apply {
            setPassword(VALID_PASSWORD)
            setPasswordConfirmed(VALID_PASSWORD)
        }
        advanceUntilIdle()
        val observer = viewModel.uiEffect.mockObserver(this, true)

        viewModel.submit()
        advanceUntilIdle()

        verify(observer, only()).onEach(isA<CloseScreen>())
        observer.cancel()
    }

    @Test
    fun `should show error when there there is an invalid user id passed on submit`() = runTest(StandardTestDispatcher()) {
        val viewModel = getViewModel(id = "").apply {
            setPassword(VALID_PASSWORD)
            setPasswordConfirmed(VALID_PASSWORD)
        }
        advanceUntilIdle()
        val observer = viewModel.uiEffect.mockObserver(this, true)

        viewModel.submit()
        advanceUntilIdle()

        verify(observer).onEach(argWhere { it is ShowMessage && it.text.isNotEmpty() })
        verify(observer, never()).onEach(isA<CloseScreen>())
        observer.cancel()
    }


    companion object {

        private const val VALID_PASSWORD = "V@lid001"
        private const val VALID_PASSWORD2 = "V@lid002"
        private const val INVALID_PASSWORD = "short"
        private val DEFAULT_USER_ID = Mocks.UserDetails.detailsDefault.id!!
        private val SAVED_STATE = SavedStateHandle(
            mapOf(
                UserPasswordChangeViewModel.STATE_PASSWORD to VALID_PASSWORD,
                UserPasswordChangeViewModel.STATE_PASSWORD_CONFIRMED to VALID_PASSWORD2
            )
        )

    }

}